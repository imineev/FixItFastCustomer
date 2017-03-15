package oracle.mobile.cloud.sample.fif.customer.application.constants;


/**
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class LifecycleConstants {
    
    //the application memory scope is used to dispacth incoming push messages with the application features
    public final static String PUSH_INCIDENT_ID = "#{applicationScope.push_incidentId}";
    public final static String PUSH_MESSAGE_TITLE = "#{applicationScope.push_messageTitle}";
    public final static String PUSH_MESSAGE = "#{applicationScope.push_message}";
    public final static String PUSH_HAS_NEW_MESSAGE = "#{applicationScope.push_hasNewMessage}";      
    public final static String PUSH_DEBUG_PAYLOAD = "#{applicationScope.push_debug}";     
    public final static String PUSH_TOKEN = "#{applicationScope.deviceToken}";     
    
}
