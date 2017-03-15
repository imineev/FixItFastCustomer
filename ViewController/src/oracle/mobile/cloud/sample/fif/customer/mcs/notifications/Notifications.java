package oracle.mobile.cloud.sample.fif.customer.mcs.notifications;

import java.util.HashMap;
import java.util.Iterator;

import oracle.adfmf.json.JSONException;
import oracle.adfmf.json.JSONObject;

import oracle.mobile.cloud.sample.fif.customer.maf.RequestContext;
import oracle.mobile.cloud.sample.fif.customer.maf.ResponseContext;
import oracle.mobile.cloud.sample.fif.customer.maf.RestClient;
import oracle.mobile.cloud.sample.fif.customer.mcs.log.FiFLogger;
import oracle.mobile.cloud.sample.fif.customer.mcs.mbe.FiFMBEConfig;
import oracle.mobile.cloud.sample.fif.customer.mcs.mbe.FiFMobileBackend;
import oracle.mobile.cloud.sample.fif.customer.utils.MAFUtil;

/**
 *
 * Proxy class to work with Mobile Cloud Service (MCS) notification service.
 *
 * Push notifications are notifications sent from an external source, such as MCS, to an application on a mobile device.
 * The Notifications.java class does not handle receiving push notifications but entitles MCS to send push messages to this
 * client. To receove push notifications in a MAG application you need to configure push notifications for MAF
 *
 * For details on how to enable MAF appications to receive push notifications, read "Enabling and Using Notifications"
 * in the Oracle? Mobile Application Framework Developing Mobile Applications with Oracle Mobile Application Framework
 * documentation.
 *
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class Notifications {

    /*
     * User is not allowed to perform the request. This can be due to a missing or false mobile backend Id or a 
     * malformed URI
     */
    private static final int HTTP_NOT_AUTHORIZED_STATUS = 401;
    
    /*
     * Usually in response to an invalid client Id. 
     */
    private static final int HTTP_BAD_REQUEST_STATUS = 400;
    private static final int HTTP_OK_STATUS = 200;

    private static final String REGISTER_DEVICE_URL = "/mobile/platform/devices/register";
    private static final String DEREGISTER_DEVICE_URL = "/mobile/platform/devices/deregister";

    private FiFMBEConfig mbeConfiguration = null;
    private boolean deviceRegisteredForPush = false;
    
    public Notifications(FiFMobileBackend mbe) {
        super();
        this.mbeConfiguration = mbe.getMbeConfiguration();
    }


    /**
     * register device to receive Push notifications from MCS
     * @return true/false based on registration success
     */
    public boolean registerDeviceToMCSForPush() {
        boolean success =  false;
        
        if (deviceRegisteredForPush == false) {
            success = deviceRegistrationHandler(REGISTER_DEVICE_URL);
            if (success == true) {
                deviceRegisteredForPush = true;
            }
        }
        else{
            
        }
        return success;
    }
    
    
    /**
     * deregister device from receiving Push notifications from MCS
     * @return true/false based on registration success
     */
    public boolean deregisterDeviceToMCSForPush() {
        boolean success = deviceRegistrationHandler(DEREGISTER_DEVICE_URL);
        if (success == true){
            deviceRegisteredForPush = false;
        }
        return success;
    }


    /**
     * register/deregister device to/from receive(ing) Push notifications from MCS
     * @return true/false based on (de)registration success
     */
    private boolean deviceRegistrationHandler(String mcsURI){

        boolean registrationSuccess = false;

        FiFLogger.logFine("Registering device for push with URI: "+mcsURI, this.getClass().getSimpleName(),"deviceRegistrationHandler");
        //check for supported Push platform
        
            try {

                //prepare to register client to MCS

                RequestContext requestObject = new RequestContext();
                //register of de-register Device from MCS
                requestObject.setRequestURI(mcsURI);
                requestObject.setConnectionName(mbeConfiguration.getMafRestConnectionName());
                requestObject.setHttpMethod(oracle.mobile.cloud.sample.fif.customer.maf.RequestContext.HttpMethod.POST);

                JSONObject payloadJSONObject = new JSONObject();
                JSONObject nestedDetailJSONObject = new JSONObject();
            
                //ID attribute is only added to payload upon registration
                if (MAFUtil.getOsVendor().equalsIgnoreCase(MAFUtil.VENDOR_GOOGLE)) {

                    //Google wants is package name here. The MAF application ID becomes the Google bundle ID unless changed
                    //In the latter case, the application developer should have set this value in the MBE configuration to
                    //whatever the custom name is
                    nestedDetailJSONObject.put("id","\""+"oracle.cloud.mobile.demo.fixitfastcustomer"+"\"");

                } else if (MAFUtil.getOsVendor().equals(MAFUtil.VENDOR_APPLE)) {
                    nestedDetailJSONObject.put("id","\""+"com.oraclecorp.internal.ent3.FixItFastCustomer"+"\"");
                }
                //not sure the version value is looked at. However, I see this hard coded in teh Android SDK, so doing
                //the same here
                if(mcsURI.equalsIgnoreCase(REGISTER_DEVICE_URL)){
                    nestedDetailJSONObject.put("version","\""+"1.0"+"\"");
                }
                nestedDetailJSONObject.put("platform","\"" + MAFUtil.getDeviceOS().toUpperCase()+"\"");

                //add version and platform
                payloadJSONObject.put("mobileClient", nestedDetailJSONObject);
                payloadJSONObject.put("notificationToken", "\""+this.mbeConfiguration.getDeviceToken()+"\"");
                
                String payloadString = this.stringifyJSONObject(payloadJSONObject);
                
                if(mcsURI.equalsIgnoreCase(REGISTER_DEVICE_URL)){
                FiFLogger.logFine("Payload for device registration: "+payloadString, this.getClass().getSimpleName(),"deviceRegistrationHandler");
                }
                else{
                FiFLogger.logFine("Payload for device de-registration: "+payloadString, this.getClass().getSimpleName(),"deviceRegistrationHandler");
                }

                HashMap<String, String> headers = new HashMap<String, String>();

                //Note: Authorization is automatically added to the requet header either by MAF or the RestClient

                headers.put("Content-Type", "application/json");
                headers.put("Oracle-Mobile-Backend-Id", this.mbeConfiguration.getMobileBackendId());                
                headers.put("Authorization", this.mbeConfiguration.getOauthHttpHeaderToken());
                
                requestObject.setHttpHeaders(headers);

                requestObject.setPayload(payloadString);

                ResponseContext responseObject = RestClient.sendForStringResponse(requestObject);

                //add loogin in case of application failure
                if (responseObject != null) {
                                                                        
                    //customize message for operation that happens
                    String operation = mcsURI.equalsIgnoreCase(REGISTER_DEVICE_URL)?"registration":"deregistration";
                    
                    if (responseObject.getResponseStatus() == HTTP_BAD_REQUEST_STATUS) {
                    FiFLogger.logWarning("BAD REQUEST: device "+operation+" failed with HTTP ERROR" +responseObject.getResponseStatus(), this.getClass().getSimpleName(),"deviceRegistrationHandler");
                    FiFLogger.logWarning("BAD REQUEST: Oracle MCS Error Message:" + responseObject.getResponsePayload(),this.getClass().getSimpleName(), "deviceRegistrationHandler");
                    } else if (responseObject.getResponseStatus() == HTTP_NOT_AUTHORIZED_STATUS) {
                    FiFLogger.logWarning("NOT AuTHORIZED: Push notification "+operation+" failed with HTTP ERROR" +responseObject.getResponseStatus()+". Possible problem could be an invalid or missing mobile client ID", this.getClass().getSimpleName(), "deviceRegistrationHandler");
                    FiFLogger.logWarning("NOT AuTHORIZED: Oracle MCS Error Message:" +responseObject.getResponsePayload() , this.getClass().getSimpleName(),
                                         "registerDeviceToMCSForPush");
                        } else if (responseObject.getResponseStatus() == HTTP_OK_STATUS) {
                    FiFLogger.logFine(MAFUtil.getOsVendor()+" device "+operation+" successful.", this.getClass().getSimpleName(), "deviceRegistrationHandler");
                    FiFLogger.logFine("Response is: "+responseObject.getResponsePayload(), this.getClass().getSimpleName(), "deviceRegistrationHandler");

                        registrationSuccess = true;
                    }
                }

            } catch (JSONException e) {
            FiFLogger.logWarning("Could not create JSONObject for payload: " + e.getMessage(),this.getClass().getSimpleName(), "deviceRegistrationHandler");            
            }                
            catch (Exception e) {
            FiFLogger.logWarning("Failure in sending REST request: " + e.getMessage(),this.getClass().getSimpleName(), "deviceRegistrationHandler  ");                 
            }
        
        return registrationSuccess;
    }



    /**
     * Simple JSONObject-String parser that parses JSONObject that may or may not contain nested
     * instances of JSONObject
     *
     * @param jsonObjectToStringify
     * @return JSONObject string representation
     */
    private String stringifyJSONObject(JSONObject jsonObjectToStringify) {

        try {
            Iterator keys = jsonObjectToStringify.keys();
            //open bracket
            StringBuffer sb = new StringBuffer("{");
            //iterate keys to create JSON string structure
            while (keys.hasNext()) {
                //determine whether to add a comma as a delimiter between attributes
                if (sb.length() > 1) {
                    sb.append(',');
                }
                String key = (String) keys.next();
                //ensure proper encoding of JSON strings e.g using backslash
                //encodings for quote charaters
                sb.append(JSONObject.quote(key.toString()));
                sb.append(':');
                Object valueObject = jsonObjectToStringify.get(key);

                //check if value is nested JSON object
                if (valueObject instanceof JSONObject) {
                    //recursive call to this method
                    sb.append(this.stringifyJSONObject((JSONObject) valueObject));
                }
                //simple value
                else {
                    sb.append(valueObject);
                }
            }
            sb.append('}');
            return sb.toString();
        } catch (Exception e) {
            FiFLogger.logWarning("Failed parsing JSONObject: " + jsonObjectToStringify + " to String",
                             this.getClass().getSimpleName(), "stringifyJSONObject");
            return null;
        }
    }


    /**
     * Returns information about whether the device is registered for receiving MCS notifications
     * @return true/false
     */
    public boolean isDeviceRegisteredForPush() {
        return deviceRegisteredForPush;
    }
}
