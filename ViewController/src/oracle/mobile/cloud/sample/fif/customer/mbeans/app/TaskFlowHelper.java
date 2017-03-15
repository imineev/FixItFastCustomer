package oracle.mobile.cloud.sample.fif.customer.mbeans.app;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import oracle.adfmf.bindings.dbf.AmxBindingContainer;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;

import oracle.mobile.cloud.sample.fif.customer.mbeans.security.AuthenticationHandler;
import oracle.mobile.cloud.sample.fif.customer.mbeans.util.ManagedBeansUtil;

/**
 *
 * Utility class that supports communication between pages and message on a page. As a rule of thumb: everything that
 * does not relate to a single page is added to this page. For page (view) related code, backing beans are created.
 *
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class TaskFlowHelper {
    
    //synchronization step used with push notification and customer selecting an incident. The incoming push event or 
    //the app sets the ID on this managed bean. the setter then accesses the data control to set the value as current 
    //on the data control
    private String _currentIncidentId = null;
       
   //message to display on page
    private String displayMessage = null;
    
    public TaskFlowHelper() {
        super();
    }


    /*
     * *** SETTER / GETTER METHODS
     */
    

    //the photo for an incident is loaded in the background. For this we need
    //to save the image link of the current selected incident report
    String currentIncidentImageLink = null;

    /**
     * If the drill-down case started from the SRList view, this method is called from a PropertyListener to 
     * prefecth the image before the detail page renders
     * @param currentIncidentImageLink
     */
    public void setCurrentIncidentImageLink(String currentIncidentImageLink) {
        String oldCurrentIncidentImageLink = this.currentIncidentImageLink;
        this.currentIncidentImageLink = currentIncidentImageLink;
        propertyChangeSupport.firePropertyChange("currentIncidentImageLink", oldCurrentIncidentImageLink,
                                                 currentIncidentImageLink);
    }
    
    /**
     * In the case of navigation that is triggered in response to a push notification, the image link needs to be 
     * queried for the current row. Called from "fetchImageLinkUrl" method activity
     */
    public void setCurrentIncidentImageLinkFromBinding() {
       String imageLink = (String) AdfmfJavaUtilities.getELValue("#{bindings.remoteImgLink.inputValue}");
       this.setCurrentIncidentId(imageLink);
    }

    public String getCurrentIncidentImageLink() {
        return currentIncidentImageLink;
    }


    //the x-week schema custom API does not proide information about the driving time to
    //the location for the single incident query. As a result, distance to location is 
    //always unknown for the SRDetail page. To solve this problem in the app, we copy 
    //the information to this managed bean bean upon incident selection
    String drivingTime = "Unknown";


    //save the distance to localtion for the cur1rent view
    public void setDrivingTime(String distanceToLocation) {
        String oldDistanceToLocation = this.drivingTime;
        this.drivingTime = distanceToLocation;
        propertyChangeSupport.firePropertyChange("drivingTime", oldDistanceToLocation, distanceToLocation);
    }

    public String getDrivingTime() {
        return drivingTime;
    }

    /**
     * Incident Id of report that should be shown on a detail page. This method is called from a method activity 
     * in response to a push message
     * 
     * @param incidentId String identifying the Id of a reported incident
     */
    public void setCurrentIncidentId(String incidentId) {
        String oldIncidentId = this._currentIncidentId;
        this._currentIncidentId = incidentId;
        propertyChangeSupport.firePropertyChange("IncidentId", oldIncidentId, this._currentIncidentId);
        
        //update the data control with the change
        ManagedBeansUtil.invokeDCSingleStringParameterMethod("setCurrentIncidentId", "currentIncidentId", incidentId);
    }

    public String getCurrentIncidentId() {
        return _currentIncidentId;
    }


    public void setDisplayMessage(String _errorMessageToDisplay) {
        String oldErrorMessageToDisplay = this.displayMessage;
        this.displayMessage = _errorMessageToDisplay;
        propertyChangeSupport.firePropertyChange("ErrorMessageToDisplay", oldErrorMessageToDisplay,
                                                 _errorMessageToDisplay);
    }

    /**
     * Method is referenced from error message elements on the views
     * @return Error message
     */
    public String getDisplayMessage() {
        return displayMessage;
    }
    
    
    

    /* 
     * **** PROPERTY CHANGE SUPPORT ****
     */
    
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    /* TASKFLOW ANALYTIC HELPER METHODS */
    
    //These methods are created to visualize adding analytic events by doing so in the task flow diagrammer. In a production 
    //application best practice is to create analytic events wthin the data control or managed bean code for centralized and 
    //consistent event-logging. This demo uses taskflow method activities to visually show analytic events in the task flow
    //diagrammer. Its a valid option but bears the risk, that analytic events are not consistently logged (as teh developer 
    //would need to understand where and when to set method activities in a diagram. 
    
    public void addPostAuthenticationValidationAnalyticEvent(){
        HashMap<String,String> customEventDef = new  HashMap<String,String>();
        
        AuthenticationHandler authenticationHandler = (AuthenticationHandler) AdfmfJavaUtilities.evaluateELExpression("#{pageFlowScope.authenticationHandler}");
        
        customEventDef.put("username", authenticationHandler.getUsername());
        customEventDef.put("action","Query Incidents"); 
        customEventDef.put("source","task flow navigation"); 
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date d = new Date();
        String timeStamp = sdf.format(d);
        
        customEventDef.put("timeStamp",timeStamp);
        ManagedBeansUtil.addCustomAnalyticEvent("post user validation",customEventDef);
    }
    
    
    /**
     * Analytic event for the selected incident a customer wants to see more details about
     * @param selectedIncident
     */
    public void addIncidentSelection(String selectedIncident){
        HashMap<String,String> customEventDef = new  HashMap<String,String>();
        
        AuthenticationHandler authenticationHandler = (AuthenticationHandler) AdfmfJavaUtilities.evaluateELExpression("#{pageFlowScope.authenticationHandler}");
        
        customEventDef.put("username", authenticationHandler.getUsername());
        customEventDef.put("action","incident selected"); 
        customEventDef.put("incidentId",selectedIncident); 
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date d = new Date();
        String timeStamp = sdf.format(d);
        
        customEventDef.put("timeStamp",timeStamp);
        ManagedBeansUtil.addCustomAnalyticEvent("incident",customEventDef);
    }
    
    /**
     * Adds an analytic event after login to share with the server who logged in
     * @param loginStatus
     */
    public void addLoginAnalyticEvent(String loginStatus){
        HashMap<String,String> customEventDef = new  HashMap<String,String>();
        
        AuthenticationHandler authenticationHandler = (AuthenticationHandler) AdfmfJavaUtilities.evaluateELExpression("#{pageFlowScope.authenticationHandler}");
        
        customEventDef.put("username", authenticationHandler.getUsername());
        customEventDef.put("status","loginStatus"); 
        customEventDef.put("action","application login"); 
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date d = new Date();
        String timeStamp = sdf.format(d);
        
        customEventDef.put("timeStamp",timeStamp);
        ManagedBeansUtil.addCustomAnalyticEvent("login",customEventDef);
    }
    
    /**
     *  Adds an analytic event about a log-out action. Analytic events is not the same as logging, so too fine grain events don't make sense. However, for this
     *  simple demo, we event-log as much as we can to produce enough activity data
     */
    public void addLogoutAnalyticEvent(){
        HashMap<String,String> customEventDef = new  HashMap<String,String>();
        
        AuthenticationHandler authenticationHandler = (AuthenticationHandler) AdfmfJavaUtilities.evaluateELExpression("#{pageFlowScope.authenticationHandler}");
        
        customEventDef.put("username", authenticationHandler.getUsername());
        customEventDef.put("action","application logout"); 
        customEventDef.put("source","task flow navigation"); 
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date d = new Date();
        String timeStamp = sdf.format(d);
        
        customEventDef.put("timeStamp",timeStamp);
        ManagedBeansUtil.addCustomAnalyticEvent("view-navigation",customEventDef);
    }
    
    /**
     * Analytic event to indicate a push notification being received by the mobile application. 
     * @param incidentId
     */
    public void addPushAnalyticEvent(String incidentId){
        HashMap<String,String> customEventDef = new  HashMap<String,String>();
        
        AuthenticationHandler authenticationHandler = (AuthenticationHandler) AdfmfJavaUtilities.evaluateELExpression("#{pageFlowScope.authenticationHandler}");
        
        customEventDef.put("username", authenticationHandler.getUsername());
        customEventDef.put("action","incident from push"); 
        customEventDef.put("incidentId",incidentId); 
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date d = new Date();
        String timeStamp = sdf.format(d);
        
        customEventDef.put("timeStamp",timeStamp);
        ManagedBeansUtil.addCustomAnalyticEvent("incident",customEventDef);
    }
    
    /**
     * End analytic session and report event queue to MCS server. This is where the server round trip
     * happens. Before, all events where queued in memory
     */ 
    public void flushAnalyticEventsToServer(){
        //issue data control method call with no arguments
        ManagedBeansUtil.invokeOnDataControl("flushAnalyticEventsToServer", new ArrayList<String>(),
                                                   new ArrayList<Object>(), new ArrayList<Class>());
        
        //RESET THIS BEAN'S INTERNAL STATE
        this.setCurrentIncidentImageLink(null);
        this.setCurrentIncidentId(null);
        this.setDisplayMessage(null);
        //this.resetCurrentBindings();
    }   
    
    /**
     * Reset bindings to leave a defined state
     * @return
     */
    public String resetCurrentBindings(){
        //reset notes
        AmxBindingContainer bindings = (AmxBindingContainer) AdfmfJavaUtilities.getELValue("#{bindings}");
        
        if(bindings != null){
            bindings.clear();
        }
        return null;
    }    
}
