package oracle.mobile.cloud.sample.fif.customer.mbeans.backing;

import oracle.adfmf.amx.event.ActionEvent;
import oracle.adfmf.dc.bean.ConcreteJavaBeanObject;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;

import oracle.mobile.cloud.sample.fif.customer.app.data.entities.Incident;
import oracle.mobile.cloud.sample.fif.customer.app.log.AppLogger;
import oracle.mobile.cloud.sample.fif.customer.constants.FiFConstants;
import oracle.mobile.cloud.sample.fif.customer.mbeans.util.ManagedBeansUtil;


/**
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class SRListBacking {


    private static String DEFAULT_FILTER_CRITERIA = "All";
    private String incidentFilterCriteria = DEFAULT_FILTER_CRITERIA;
    
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public SRListBacking() {
        super();
    }


    /**
     * Set filter value to query list items in incidet list. Allwed values are All, New, Open, Complete
     * @param incidentFilterList
     */
    public void setIncidentFilterCriteria(String incidentFilterList) {
        String oldIncidentFilterList = this.incidentFilterCriteria;
        this.incidentFilterCriteria = incidentFilterList;
        propertyChangeSupport.firePropertyChange("incidentFilterCriteria", oldIncidentFilterList, incidentFilterList);
    }

    public String getIncidentFilterCriteria() {
        return incidentFilterCriteria;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Filter the incidents list
     * @param action
     */
    public void invokeFilterList(ActionEvent action) {

        AppLogger.logFine("Invoke Data Control method \"filterIncidentListInMemory\" with argument value :" +
                          incidentFilterCriteria, this.getClass().getSimpleName(), "invokeFilterList");
        ManagedBeansUtil.invokeDCSingleStringParameterMethod("filterIncidentListInMemory", "filterValue", incidentFilterCriteria);
    }
    
    
    //called when reloading the incident list data
    public String resetFilterList(){
        //reset to default
        this.setIncidentFilterCriteria(DEFAULT_FILTER_CRITERIA);
        return null;
    }

    /**
     * Shows push message by navigating to the message in the SRDetail view
     * @return
     */
    public String showPushMessage(){
                       
        //the push message has been delivered and the system can be reset so that it again shows the alert when a new message comes in
        AdfmfJavaUtilities.setELValue(FiFConstants.PUSH_HAS_NEW_MESSAGE, false);
        
        //what needs to happen next? The new incident record may not be queried yet. Furthermore, we need to get the incident 
        //image link so that the image can be loaded while transitioning (navigating) to the details page. For this we accept 
        //an extra call to MCS to obtain the push notification detail information, which is the image link. Note that another
        //information - time to destination - is not available for single object queries (missing on the x-week custom API). 
        
        //get incident ID from push notification
        String incidentId = (String) AdfmfJavaUtilities.getELValue("#{applicationScope.push_incidentId}");
        
        //set current row Id in data control. A method is exposed on the task flow helper bean for programmatic
        //and declarative use
        AdfmfJavaUtilities.setELValue("#{pageFlowScope.taskFlowHelper.currentIncidentId}", incidentId);
        
        //next. Query MCS for a copy of the new Inident to obtain the imageUrl from. The object returned from the data 
        //control is of instance ConcreteJavaBeanObject. The instance of this object is "Incident", which is the class
        //we are looking for.
        ConcreteJavaBeanObject incidentConcreteObject = (ConcreteJavaBeanObject) ManagedBeansUtil.invokeDCSingleStringParameterMethod("querySingleIncidentById", "incidentId", incidentId);
        Incident incident =  (Incident)incidentConcreteObject.getDataProvider();
        String imageLink = incident.getRemoteImgLink();
        //set image link to task flow helper managed bean for querying the image upon navigation
        AdfmfJavaUtilities.setELValue("#{pageFlowScope.taskFlowHelper.currentIncidentImageLink}", imageLink);   
        //all set to navigate to the detail page. So return the task flow control flow case name for action
        return "handlePush";        
    }
    
    /**
     * Ignores push message stying on same view. 
     * @return
     */
    public String ignorePushMessage(){
        //the push message has been delivered and the system can be reset so that it again shows the alert when a new message comes in
        AdfmfJavaUtilities.setELValue(FiFConstants.PUSH_HAS_NEW_MESSAGE, false);
        //this navigation case doesn't exist and thus the view thus doesn't change. The alert should go away though
        return "ignorePush";    
    }
    
}
