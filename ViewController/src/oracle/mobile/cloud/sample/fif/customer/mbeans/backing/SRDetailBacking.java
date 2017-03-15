package oracle.mobile.cloud.sample.fif.customer.mbeans.backing;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;

import oracle.mobile.cloud.sample.fif.customer.app.log.AppLogger;
import oracle.mobile.cloud.sample.fif.customer.mbeans.security.AuthenticationHandler;
import oracle.mobile.cloud.sample.fif.customer.mbeans.util.ManagedBeansUtil;

/**
 * Backing bean supporting the SRDetails page with information that otherwis would be hard to get from pure declarative
 * EL expressions
 *
 * Question 1: You invoke method on the data control directly instead of working through the MAF binding layer. In ADF
 *             the binding layer recommendation is to always access the business services through the binding. Does this
 *             recommendation not aply to MAF?
 * Answer   1: The short answer is that the request lifecycle in Oracle ADF and ADF Faces is different from MAF, which
 *             behaves more like a desktop applications (UI is refreshed through change events). Thus there is no clear
 *             advantage of always working throughthe binding layer other than a consistent API. In addition, ADF does
 *             have a central error handling mechanism that application developers benefit from when working through the
 *             binding layer. In MAF the binding layer makes sense to use for all UI binding. For invoking methods, its up
 *             to the application developer to invoke the method on the DC directly or use a method expression to invoke
 *             a method binding
 *
 * Question 2: I don't understand the code in getBase64EncodedIncidentPhoto(), how does it work and how does it help
 *             improving performance?
 * Answer   2: Well, the code in getBase64EncodedIncidentPhoto() does three things. i) The image is saved as a base64
 *             encoded string in a member variable after being fetched from MCS. So first thing the code does, it checks
 *             if the image is available. If not, then a separate thread is used to fetch the image from MCS. This then
 *             does not block the rendering of the detail view, nor does it make the image flicker when loading. What is
 *             not visible in the code is that the method is called twice. First time the method is called in a method
 *             activity on transitioning from the list view to th edetail view. And then, when the user switches the
 *             detailView to show the image, the method is called again. If all works well, then the second invocation
 *             will find the image already available and not fetch it again. This way the method call activity performs
 *             a pre-loading of the image. And if pre-loading fails, then users will see the image when it is fetched
 *             after switching to the image view (with a flicker then).
 *
 * Question 3: How much time does the image pre-fetching save?
 * Answer   3: A "quick-n-dirty" test with a small image (3MB) showed an improvement of 1055 ms(!). This is the time
 *             spent on fetching the image from MCS and transforming it into a base64 encoded string
 *
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class SRDetailBacking {
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private static final String SR_DETAILS_VIEW = "details";
    private static final String SR_DETAIL_NOTE_UPDTATE_MSG = "Update was successfully submitted to server.";
    private static final String SR_DETAIL_NOTE_UPDTATE_EMPTY_MSG = " ";

    public SRDetailBacking() {
        super();
    }
    
    String serviceDetailView = SR_DETAILS_VIEW;
    
    
    String base64EncodedIncidentPhoto = null; 
    
    String serverUpdateMsg = SR_DETAIL_NOTE_UPDTATE_EMPTY_MSG;

    /**
     * Property that is referenced from the deck component on the detail page to switch between views
     *
     * @param detailViewId  "details", "photo", "update"
     */
    public void setServiceDetailView(String detailViewId) {
        String oldDeckComponentChildId = this.serviceDetailView;
        this.serviceDetailView = detailViewId;
        
        logDetailViewSelectionAnalyticEvent("user detail view selection", detailViewId);
        
        propertyChangeSupport.firePropertyChange("serviceDetailView", oldDeckComponentChildId, detailViewId);
    }
    
    
    private void logDetailViewSelectionAnalyticEvent(String action, String viewSelection){
        HashMap<String,String> customEventDef = new  HashMap<String,String>();
        
        AuthenticationHandler authenticationHandler = (AuthenticationHandler) AdfmfJavaUtilities.evaluateELExpression("#{pageFlowScope.authenticationHandler}");
        
        customEventDef.put("username", authenticationHandler.getUsername());
        customEventDef.put("action",action); 
        customEventDef.put("selectedDetailView",viewSelection); 
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date d = new Date();
        String timeStamp = sdf.format(d);
        
        customEventDef.put("timeStamp",timeStamp);
        ManagedBeansUtil.addCustomAnalyticEvent("incident",customEventDef);
    }
    
    public String getServiceDetailView() {
        return serviceDetailView;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    public void setBase64EncodedIncidentPhoto(String base64EncodedIncidentPhoto) {
        String oldBase64EncodedIncidentPhoto = this.base64EncodedIncidentPhoto;
        this.base64EncodedIncidentPhoto = base64EncodedIncidentPhoto;
        propertyChangeSupport.firePropertyChange("base64EncodedIncidentPhoto", oldBase64EncodedIncidentPhoto,
                                                 base64EncodedIncidentPhoto);
    }
        

    /**
     * Method reads the base64 String encoded image representation for an incident from the MCS Storage. The method is exposed as a
     * property (base64EncodedIncidentPhoto), thus the setter method. The get method is called from a method call activity with the
     * goal to load the image before the page renders. This should then avoid the image to flicker when the page swtches to show the
     * image
     * 
     * @return
     */
    public String getBase64EncodedIncidentPhoto() {
        
        if(base64EncodedIncidentPhoto == null || base64EncodedIncidentPhoto.isEmpty()){

            AppLogger.logFine("Image not pre-loaded: Querying image now.", this.getClass().getSimpleName(), "getBase64EncodedIncidentPhoto"); 
            String mafBase64ImageStringPrefix = "data:image/gif;base64,";
        
            //step 1: get access to the image link saved in tha TaskFlowHelper bean
            String restURI = (String) AdfmfJavaUtilities.evaluateELExpression("#{pageFlowScope.taskFlowHelper.currentIncidentImageLink}");
    
            //step 2: Incoke DC method to obtain image from MCS
            if(restURI != null && !restURI.isEmpty()){
/*              
                //for performance reason, have the image being fetched from MCS within a thread to not block
                //the rendering of the detail page
                Runnable ImageFetcher = new Runnable(){
                     public void run(){                         
                         //read image from MCS
                         Object imageStringObject =
                            ManagedBeansUtil.invokeDCSingleStringParameterMethod("getIncidentImageFromMCS", "incidentImageURL", restURI);
                         if(imageStringObject != null && !((String)imageStringObject).isEmpty() ){
                             base64EncodedIncidentPhoto = mafBase64ImageStringPrefix+(String)imageStringObject; 
                         }        
                     }
                   };                              
                   ExecutorService executor = Executors.newFixedThreadPool(2);
                   executor.execute(ImageFetcher);
                   executor.shutdown();
*/


                         //read image from MCS
                         Object imageStringObject =
                            ManagedBeansUtil.invokeDCSingleStringParameterMethod("getIncidentImageFromMCS", "incidentImageURL", restURI);
                         if(imageStringObject != null && !((String)imageStringObject).isEmpty() ){
                             base64EncodedIncidentPhoto = mafBase64ImageStringPrefix+(String)imageStringObject; 
                         }           


            }
            else{        
            //if you get here then the MAF binding for the SRDetails page does not have a image URI in its remoteImgLink attribute. Instead
            //we return a "No Image" image
            base64EncodedIncidentPhoto = mafBase64ImageStringPrefix + ManagedBeansUtil.NO_IMAGE_AVAILABLE;
            }
        }
        
        return base64EncodedIncidentPhoto;
    }

    public void setServerUpdateMsg(String serverUpdateMsg) {
        String oldServerUpdateMsg = this.serverUpdateMsg;
        this.serverUpdateMsg = serverUpdateMsg;
        propertyChangeSupport.firePropertyChange("serverUpdateMsg", oldServerUpdateMsg, serverUpdateMsg);
    }

    public String getServerUpdateMsg() {
        return serverUpdateMsg;
    }

    //switches the detail view to show Google Maps
    public String showMapView(){
        this.setServiceDetailView("map");
        return null;
    }
    
    /**
     * Method to display server update message from command link or button action property. This method calls the 
     * setServerUpdateMsg(String) method that performs a property change event notification. 
     * @return
     */
    public String setServerUpdateMsg(){
        this.setServerUpdateMsg(SR_DETAIL_NOTE_UPDTATE_MSG);
        return null;
    }
        
    /**
     *  Resets the SRDetail page to its default views. his method calls the setServerUpdateMsg(String) method 
     *  that performs a property change event notification. 
     */
    public void srDetailResetToDefault(){        
         this.setServiceDetailView(SR_DETAILS_VIEW);
         this.resetNotes();
         this.setBase64EncodedIncidentPhoto(null);
    }
    
    /**
     * Resets the SR serviceupdate input fields amd message box
     * @return
     */
    public String resetNotes(){
        //reset notes
        AdfmfJavaUtilities.setELValue("#{bindings.notes.inputValue}","");
        //set status back to current status
        AdfmfJavaUtilities.setELValue("#{bindings.status1.inputValue}","#{bindings.status.inputValue}");
        this.setServerUpdateMsg(SR_DETAIL_NOTE_UPDTATE_EMPTY_MSG);
        
        //in case of navigation off the page, chance sare that property change doesn't happen anymore. So 
        //back it up here and set the value to an empty string
        this.serverUpdateMsg = SR_DETAIL_NOTE_UPDTATE_EMPTY_MSG;
        return null;
    }
    
    public String backNavigation (){        
        
        this.srDetailResetToDefault();
     
        //navigate to flush analytic messages to MCS
        return "flushAnalytics";
    }
    
}
