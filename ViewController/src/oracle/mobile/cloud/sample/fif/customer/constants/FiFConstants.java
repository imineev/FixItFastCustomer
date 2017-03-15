package oracle.mobile.cloud.sample.fif.customer.constants;


/**
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class FiFConstants {
    
    //the application memory scope is used to dispacth incoming push messages with the application features
    public final static String PUSH_INCIDENT_ID = "#{applicationScope.push_incidentId}";
    public final static String PUSH_MESSAGE_TITLE = "#{applicationScope.push_messageTitle}";
    public final static String PUSH_MESSAGE = "#{applicationScope.push_message}";
    public final static String PUSH_HAS_NEW_MESSAGE = "#{applicationScope.push_hasNewMessage}";  
    public final static String PUSH_DEBUG_PAYLOAD = "#{applicationScope.push_debug}";     
    public final static String PUSH_TOKEN = "#{applicationScope.deviceToken}";   
    
    /**
     * All MCS requests happen in the context of an Oracle Mobile Backend. For this all requests must have 
     * the ORACLE_MOBILE_BACKEND_ID set to a valid MBE Id
     */
    public static final String ORACLE_MOBILE_BACKEND_ID  = "Oracle-Mobile-Backend-Id";
    /**
     * HTTP accept header indicating the response format accepted by this client
     */
    public static final String ACCEPT_HEADER = "Accept";
    public final static String BASIC_AUTH = "basic";
    
}
