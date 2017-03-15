package oracle.mobile.cloud.sample.fif.customer.application.push;

import java.util.HashMap;
import java.util.logging.Level;

import javax.el.ValueExpression;

import oracle.adf.model.datacontrols.device.DeviceManagerFactory;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.JSONBeanSerializationHelper;
import oracle.adfmf.framework.event.Event;
import oracle.adfmf.framework.event.EventListener;
import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.json.JSONObject;
import oracle.adfmf.util.Utility;

import oracle.mobile.cloud.sample.fif.customer.application.constants.LifecycleConstants;

/**
 * The FIF customer application may receive push notification messages from MCS. The push notification is displayed in a note window
 * on top of the current page for the user to then decide whether or not to interrup his/her work to check the notification. Alternatively
 * the user can ignore the message and later query the list of incidents for the "new arrival".
 *
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class FiFPushHandler implements EventListener {
    
    public FiFPushHandler() {
        super();
        Utility.ApplicationLogger.logp(Level.FINE, this.getClass().getSimpleName(), "FiFPushHandler::constructor","Push activated");
    }



    /**
     * 
     * The Incident Message must be sent using the following JSON Object structure
     * 
     * {incidentId:"61",title:"New Service request",message: "This here seems to be urgent. 'Water is leaking from heater. Have water all over the place'"}
     * 
     * Message received by MAF for iOS :
     * 
     * {alert= {
     *           "incidentId":"61",
     *           "title":"New Service request",
     *           "message": "This here seems to be urgent: 'Water is leaking from heater. Have water all over the place"
     *         }
     *      }, 
     *      foreground=1, 
     *      deviceToken=c7645c692e143855054b40c3621d4c262ce1f97f0fd62a844bef34eab991758b
     *  }
     *  
     *         
     *  This can be parsed into a HashMap (see code in this class)
     * 
     * for Apple, payloads see: https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/ApplePushService.html
     * 
     * For Google the payload is a JSON Object :
     * 
     *  
     * {"alert":{"incidentId":"61","title":"New Service request","message": "This here seems to be urgent. 'Water is leaking from heater. Have water all over the place'"},
     *  "deviceToken": "...."}
     * 
     * https://docs.pushio.com/API_&_cURL_Information/Platform_Specific_Payloads/GCM_%28Google_Cloud_Messaging%29
     *  
     * Incident Id is written to         #{applicationScope.push_incidentId} 
     * Message is written to             #{applicationScope.push_message}
     * Message Title is                  #{applicationScope.push_messageTitle}
     * Message flag true/false is set to #{applicationScope.push_hasNewMessage}
     * 
     * see: LifecycleConstants and FiFConstants(view project) for Java handlers
     *   
     * @param event - event object passed in from PUSH receiving
     */
    public void onMessage(Event event) {

        String msg = event.getPayload();
        
        Utility.ApplicationLogger.logp(Level.FINE, this.getClass().getSimpleName(), "FiFPushHandler::onMessage","Raw push payload received from server = " + msg);
        
        //log payload once. This will give us debug information even if message parsing for iOS or Android fails
        //in the following
        AdfmfJavaUtilities.setELValue(LifecycleConstants.PUSH_DEBUG_PAYLOAD,msg);  
        // Parse the payload of the push notification

        try {
            
            //detect OS as payload for Android is different from payload on IOS            
            String os =         DeviceManagerFactory.getDeviceManager().getOs().toUpperCase();            
            //holds the MCS message
            JSONObject notificationAlertJson = null;
            
            if(os.equalsIgnoreCase("IOS")){
                
                HashMap payload = (HashMap) JSONBeanSerializationHelper.fromJSON(HashMap.class, msg);
                
                String notificationAlert = "No message received"; 
                
                //REMEMBER alert ?            
                notificationAlert = (String) payload.get("alert");
                
                Utility.ApplicationLogger.logp(Level.FINE, this.getClass().getSimpleName(), "FiFPushHandler::onMessage", "IOS Push message = " + notificationAlert);
                
                notificationAlertJson = new JSONObject(notificationAlert);            
                AdfmfJavaUtilities.setELValue(LifecycleConstants.PUSH_DEBUG_PAYLOAD,payload.toString());                         
                               
            }
            //or Android for this sample
            else{
                JSONObject jsonObject = new JSONObject(msg);
                
                Utility.ApplicationLogger.logp(Level.FINE, this.getClass().getSimpleName(), "FiFPushHandler::onMessage", "Android push message = " + msg);                
                notificationAlertJson = jsonObject.getJSONObject("alert");                                               
            }
            
            String mcsNoteIncidentId = optString(notificationAlertJson,"incidentId");
            
            if(mcsNoteIncidentId==null){
                //people should know how to demo a product. So we only log the problem here instead of
                //throwing an exception                 
                Utility.ApplicationLogger.logp(Level.FINE, this.getClass().getSimpleName(), "FiFPushHandler::onMessage", "IncidentId is required but found null"); 
                
                AdfmfJavaUtilities.setELValue(LifecycleConstants.PUSH_DEBUG_PAYLOAD,msg);               
            }
            
            String mcsNoteTitle = optString(notificationAlertJson,"title");
            String mcsNoteMessage = optString(notificationAlertJson,"message");
            
            
            //Save information in application memory scope
            AdfmfJavaUtilities.setELValue(LifecycleConstants.PUSH_INCIDENT_ID, mcsNoteIncidentId);
            AdfmfJavaUtilities.setELValue(LifecycleConstants.PUSH_MESSAGE_TITLE,mcsNoteTitle);
            AdfmfJavaUtilities.setELValue(LifecycleConstants.PUSH_MESSAGE, mcsNoteMessage);
            //set flag indicating new message
            AdfmfJavaUtilities.setELValue(LifecycleConstants.PUSH_HAS_NEW_MESSAGE,true);     
            
            
        }

        catch (Exception e) {
            e.printStackTrace();

        }
    }


    public void onError(AdfException adfException) {
        Utility.ApplicationLogger.logp(Level.WARNING, this.getClass().getSimpleName(), "FiFPushHandler::onError",
                                       "Message = " + adfException.getMessage() + "\nSeverity = " +
                                       adfException.getSeverity() + "\nType = " + adfException.getType());

        // Write the error into app scope
        ValueExpression ve =
            AdfmfJavaUtilities.getValueExpression("#{applicationScope.push_errorMessage}", String.class);
        ve.setValue(AdfmfJavaUtilities.getAdfELContext(), adfException.toString());

    }

    public void onOpen(String token) {
        Utility.ApplicationLogger.logp(Level.FINE, this.getClass().getSimpleName(), "FiFPushHandler::onOpen","Registration token = " + token);
        // Clear error in app scope
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{applicationScope.errorMessage}", String.class);
        ve.setValue(AdfmfJavaUtilities.getAdfELContext(), null);

        // Write the token into app scope
        AdfmfJavaUtilities.setELValue(LifecycleConstants.PUSH_TOKEN,token);
    }
    
    /**
     * Check for null occurences of one of the parameters
     * @param json the JSON Object
     * @param key the attribute name
     * @return String or null
     */
    private String optString(JSONObject json, String key)
    {
        if (json.isNull(key))
            return null;
        else
            return json.optString(key, null);
    }

}
