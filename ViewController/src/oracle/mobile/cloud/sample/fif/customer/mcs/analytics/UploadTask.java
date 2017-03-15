package oracle.mobile.cloud.sample.fif.customer.mcs.analytics;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import oracle.adf.model.datacontrols.device.DeviceManager;
import oracle.adf.model.datacontrols.device.DeviceManagerFactory;

import oracle.adfmf.framework.ApplicationInformation;
import oracle.adfmf.framework.api.AdfmfContainerUtilities;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.json.JSONArray;
import oracle.adfmf.json.JSONObject;

import oracle.mobile.cloud.sample.fif.customer.mcs.log.FiFLogger;
import oracle.mobile.cloud.sample.fif.customer.mcs.mbe.FiFMBEConfig;
import oracle.mobile.cloud.sample.fif.customer.mcs.mbe.FiFMobileBackend;
import oracle.mobile.cloud.sample.fif.customer.maf.RequestContext;
import oracle.mobile.cloud.sample.fif.customer.maf.ResponseContext;
import oracle.mobile.cloud.sample.fif.customer.maf.RestClient;
import oracle.mobile.cloud.sample.fif.customer.utils.DateUtil;
import oracle.mobile.cloud.sample.fif.customer.utils.MAFUtil;
import oracle.mobile.cloud.sample.fif.customer.utils.MapUtils;


//TODO add log information
//TODO add code to check network connection: If network is not available cache event in SQLite database

/**
 * UploadTask handles the REST call to MCS to write analytic information to the server. Its
 * a wrapper - as a developer convenience - for invoking MCS REST URLs
 *
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class UploadTask implements Runnable {

    ArrayList<Event> mEventList = null;
    Analytics mAnalytics = null;
    Session mSession = null;
    FiFMBEConfig mbeConfig = null;
    FiFMobileBackend mobileBackend = null;
    private HashMap<String,String> mHeaderMap = null;
    
    //default events
    private JSONObject mContextEvent = null;
    private JSONObject mSessionStartEvent = null;
    private JSONObject mSessionEndEvent = null;

    private String LOG_TAG = "Upload Task - ";
    
    public UploadTask(Analytics analytics, ArrayList<Event> eventList, Session session) {

        super();
        
        this.mEventList = eventList;
        this.mAnalytics = analytics;
        this.mSession = session;
        this.mobileBackend = analytics.getMobileBackend();
        this.mbeConfig = this.mobileBackend.getMbeConfiguration();

        FiFLogger.logFine(LOG_TAG+"New Upload Task created for "+eventList.size()+"event(s)", this.getClass().getSimpleName(), "Constructor");
    }

    @Override
    public void run() {

        //if no events available, ignore request
        if (this.mEventList.size() < 1) {
            return;
        }

        this.mContextEvent = createSystemJson();
        this.mSessionStartEvent = createSessionStartJson();
        this.mSessionEndEvent = createSessionEndJson();

        try {
            FiFLogger.logFine(LOG_TAG + "Attempting to post " + this.mEventList.size() + " events", this.getClass().getSimpleName(),"run");
            postEvents();
        } catch (Exception e) {
            FiFLogger.logSevereError(LOG_TAG + "Could not post events because of Exception", this.getClass().getSimpleName(),"run");
            FiFLogger.logSevereError(LOG_TAG + "Exception cause: "+e.getCause().getMessage(), this.getClass().getSimpleName(),"run");
            e.printStackTrace();
        }
    }

    /**
     * creates JSON string to indicate start of analytic session. The JSON paload looks like
     * 
     * {
     *   "name":"sessionStart",
     *   "type":"system",
     *    "timestamp":"2013-04-12T23:20:55.052Z",
     *    "sessionID":"2d64d3ff-25c7-4b92-8e49-21884b3495ce"
     *    }
     * @return JSONObject
     */
    private JSONObject createSessionStartJson() {

        FiFLogger.logFine(LOG_TAG + "creating session start JSON", this.getClass().getSimpleName(),"createSessionStartJson");

        JSONObject json = new JSONObject();
        try {
            json.put("name", "sessionStart");
            json.put("timestamp", DateUtil.getISOTimeStamp(mSession.getStartTime()));
            json.put("sessionID", mSession.getSessionId());
            json.put("type", "system");
            json.put("component", this.mobileBackend.getApplicationFeatureName());
        } catch (Exception ex) {
            FiFLogger.logSevereError(LOG_TAG + "Could not create JSON Object because of Exception", this.getClass().getSimpleName(),"createSessionStartJson");
            FiFLogger.logSevereError(LOG_TAG + ex.getMessage(), this.getClass().getSimpleName(),"createSessionStartJson");
        }
        return json;
    }

    /**
     * Creates JSON Object signalling the end of the recorded session to the server. Payload looks like
     * 
     * {
     *   "name":"sessionEnd",
     *   "type":"system",
     *   "timestamp":"2013-04-12T23:25:55.052Z",
     *   "sessionID":"2d64d3ff-25c7-4b92-8e49-21884b3495ce"
     *  }
     * 
     * @return
     */
    private JSONObject createSessionEndJson() {

        FiFLogger.logFine(LOG_TAG + "creating session end JSON", this.getClass().getSimpleName(),"createSessionEndJson");

        JSONObject json = new JSONObject();
        try {
            json.put("name", "sessionEnd");
            json.put("timestamp", DateUtil.getISOTimeStamp(mSession.getEndTime()));
            json.put("sessionID", mSession.getSessionId());
            json.put("type", "system");
            json.put("component", this.mobileBackend.getApplicationFeatureName());

        } catch (Exception ex) {
            FiFLogger.logSevereError(LOG_TAG + "Could not create session end JSON Object because of Exception", this.getClass().getSimpleName(),"createSessionEndJson");
            FiFLogger.logSevereError(LOG_TAG + ex.getMessage(), this.getClass().getSimpleName(),"createSessionEndJson");            
        }
        return json;
    }

    /**
     * System events are logged with a JSON payload similar to 
     * {
     *   "name":"context",
     *   "type":"system",
     *   "timestamp":"2013-04-12T23:20:54.345Z",
     *   "properties":{
     *   "latitude":"37.35687",
     *   "longitude":"-122.11663",
     *   "timezone":"-14400",
     *   "carrier":"AT&T",
     *   "model":"iPhone5,1",
     *   "manufacturer":"Apple",
     *   "osName":"iPhone OS",
     *   "osVersion":"7.1",
     *   "osBuild":"13E28"
     *   }
     * @return JSON Object
     */
    private JSONObject createSystemJson() {
        FiFLogger.logFine(LOG_TAG + "Initializing System JSON", this.getClass().getSimpleName(),"createSystemJson");

        TimeZone timeZone = TimeZone.getDefault();
        JSONObject json = new JSONObject();
        try {
            json.put("name", "context");            
            json.put("sessionID", this.mSession.getSessionId());
            json.put("type", "system");

            JSONObject properties = new JSONObject();

            DeviceManager dm = DeviceManagerFactory.getDeviceManager();                        
            properties.put("model", dm.getModel());            
            properties.put("manufacturer", MAFUtil.getOsVendor());
            properties.put("timezone", Integer.toString(timeZone.getRawOffset() / 1000));
            properties.put("osName", MAFUtil.getDeviceOS());
            properties.put("osVersion", MAFUtil.getDeviceOSVersion());
            properties.put("longitude",""+this.mAnalytics.getLongitude());
            properties.put("latitude", ""+this.mAnalytics.getLatitude());            
            json.put("properties",properties);
            json.put("timestamp", getISOTimeStamp());

        } catch (Exception ex) {
            FiFLogger.logSevereError(LOG_TAG + "Could not create system JSON Object because of Exception", this.getClass().getSimpleName(),"createSystemJson");
            FiFLogger.logSevereError(LOG_TAG + ex.getMessage(), this.getClass().getSimpleName(),"createSystemJson");            
        }

        FiFLogger.logFine(LOG_TAG + "System JSON created", this.getClass().getSimpleName(),"createSystemJson");
        return json;

    }

    private String getISOTimeStamp() {
        return DateUtil.getISOTimeStamp(new Date());
    }

    /**
     * Send batch of collected events to the MBE instance
     *
     * @throws ServiceProxyException
     */
    private void postEvents() throws Exception {
        //get diagnostic headers
        
        Map<String, String> diagnosticsHeaders = this.mobileBackend.getDiagnostics().getHTTPHeaders();

        //Initialize Headers
        this.mHeaderMap = new HashMap<String, String>();

    
        mHeaderMap.put(AnalyticsConstants.CONTENT_TYPE_HEADER, "application/json");
        mHeaderMap.put(AnalyticsConstants.APPLICATION_KEY_HEADER, this.mbeConfig.getMobileBackendApplicationKey());
        mHeaderMap.put(AnalyticsConstants.ANALYTIC_MOBILE_BACKEND_ID_HEADER, this.mbeConfig.getMobileBackendId());
        mHeaderMap.put(AnalyticsConstants.ANALYTIC_MOBILE_DEVICE_ID_HEADER, this.mobileBackend.getClientUID());
        mHeaderMap.put(AnalyticsConstants.ANALYTIC_SESSION_ID_HEADER, this.mSession.getSessionId());
               
        //Populate Diagnostic Headers
        for (String header : diagnosticsHeaders.keySet()) {
            mHeaderMap.put(header, diagnosticsHeaders.get(header));
        }

        sendRequest();
    }

    /**
     * Send the REST request to the Mobile Backend Analytics API
     * @throws ServiceProxyException
     */
    private void sendRequest() throws Exception{

        FiFLogger.logFine(LOG_TAG + "sending server request", this.getClass().getSimpleName(),"sendRequest");
        JSONArray jsonArray = null;

        jsonArray = new JSONArray();

        jsonArray.put(this.mContextEvent);
        jsonArray.put(this.mSessionStartEvent);
        FiFLogger.logFine(LOG_TAG + "adding custom events ", this.getClass().getSimpleName(),"sendRequest");
        for (int indx = 0; indx < this.mEventList.size(); indx++) {
            jsonArray.put(createEventJson(mEventList.get(indx)));
        }
        jsonArray.put(mSessionEndEvent);

        //network access available
        if (MAFUtil.isNetworkAccess()) {
            FiFLogger.logFine(LOG_TAG + "Network access available: ready to send", this.getClass().getSimpleName(),"sendRequest");
            //Create a request context to hold request configuration before calling MAF
            //REST Service Adapter to post events to the MBE
            RequestContext request = new RequestContext();
            
            request.setConnectionName(this.mbeConfig.getMafRestConnectionName());  
            
            //add authorization
            mHeaderMap.put("Authorization", this.mobileBackend.getMbeConfiguration().getOauthHttpHeaderToken());            
            request.setHttpHeaders(this.mHeaderMap);
            request.setPayload(jsonArray.toString());
            FiFLogger.logFine(LOG_TAG +  "Header map: " + MapUtils.dumpProperties(mHeaderMap), this.getClass().getSimpleName(),"sendRequest");
            FiFLogger.logFine(LOG_TAG + "Payload : "+jsonArray.toString(), this.getClass().getSimpleName(),"sendRequest");
            request.setHttpMethod(oracle.mobile.cloud.sample.fif.customer.maf.RequestContext.HttpMethod.POST);
            //add MBE Analytics base Uri
            request.setRequestURI(AnalyticsConstants.ANALYTICS_RELATIVE_URL);
            FiFLogger.logFine(LOG_TAG + "Analytic URI : " + AnalyticsConstants.ANALYTICS_RELATIVE_URL, this.getClass().getSimpleName(),"sendRequest");
            //retry limit is set to 1 as there is no business case known yet
            //that demands more frequent retries (MAF issues a synchronous
            //call and the more often retry happens the longer users need to
            //wait).
            request.setRetryLimit(1);
            ResponseContext response = null;
            try {                
                //request REST Response in String
                response = RestClient.sendForStringResponse(request);
                FiFLogger.logFine(LOG_TAG + "REST API called : "+jsonArray.toString(), this.getClass().getSimpleName(),"sendRequest");
                
                //Analytics replies with HTTP-202 in case of request success. HTTP-202 means that the request is accepted but is not
                //yet precessed by the server. 
                if (response != null) {
                    int status = response.getResponseStatus();
                    if (status == AnalyticsConstants.HTTP_202) {
                        FiFLogger.logFine(LOG_TAG + "Rest call successful: " + response.getResponsePayload(),this.getClass().getSimpleName(),"sendRequest");
                        FiFLogger.logFine(LOG_TAG + "Clearing event list" + this.mEventList.size() + " events", this.getClass().getSimpleName(),"run");
                        mEventList.clear();
                    } else {
                        saveEventsForLater();
                        FiFLogger.logSevereError(LOG_TAG + "REST Invocation Failed in call to Analytics:  " + response.getResponsePayload(),this.getClass().getSimpleName(),"sendRequest");
                        FiFLogger.logSevereError(LOG_TAG + "Events are ocally saved for later post",this.getClass().getSimpleName(),"sendRequest");                                                                
                    }
                } else {
                    FiFLogger.logFine(LOG_TAG + "Rest call successful: NO RESPONSE MESSAGE",this.getClass().getSimpleName(),"sendRequest");
                    FiFLogger.logFine(LOG_TAG + "Clearing event list" + this.mEventList.size() + " events", this.getClass().getSimpleName(),"run");
                    mEventList.clear();
                }
                
            //MAF 2.1 throws an exception in the RestTransportLayer - readResponse method when the HTTP response code is 202. To handle this
            //wrong negative, we check the exception for the HTTP error code. If the error code is 202 then continue   
                   
            } catch (Exception e) {                
                                
                if(e.getMessage().toUpperCase().contains("202")) {
                    FiFLogger.logFine(LOG_TAG + "Rest call successful: " + response.getResponsePayload(),this.getClass().getSimpleName(),"sendRequest");
                    FiFLogger.logFine(LOG_TAG + "Clearing event list" + this.mEventList.size() + " events", this.getClass().getSimpleName(),"run");
                   mEventList.clear();                    
                }
                else{
                    FiFLogger.logSevereError(LOG_TAG + "REST Invocation Failed with Exception",this.getClass().getSimpleName(),"sendRequest");
                    FiFLogger.logSevereError(LOG_TAG + "Exception cause is: " +e.getMessage(),this.getClass().getSimpleName(),"sendRequest");                    
                                        
                    /*
                     * HTTP 400 	The request failed because the payload of JSON message was malformed, or because of 
                     *                  an exception that occurred during processing
                     * HTTP 405 	The request failed because it uses a method that is not supported by the resource
                     */
                    if(e.getMessage().toUpperCase().contains("400") || (e.getMessage().toUpperCase().contains("405"))){
                        FiFLogger.logFine(LOG_TAG + "REST Invocation Failed with MCS error 400 or 405. Check the validness of the JSON payload and the request URI",this.getClass().getSimpleName(),"sendRequest");                                        
                    }
                    else{
                        //failure in the REST Service Adapter call
                        FiFLogger.logSevereError(LOG_TAG + "Events are locally saved for later post to server",this.getClass().getSimpleName(),"sendRequest"); 
                        saveEventsForLater();
                    }
                }
            }
        }
        //offline use case
        else{
            saveEventsForLater();
        }
    }


    /**
     * If the attempted REST calls fails or if the network is not available, save the event locally for later.
     */
    private void saveEventsForLater(){
        
        //No offline support provided in the x-week sample
    }

    private JSONObject createEventJson(Event event) {

        JSONObject json = new JSONObject();
        try {
            json.put("name", event.getName());
            json.put("timestamp", DateUtil.getISOTimeStamp(event.getTimestamp()));
            json.put("sessionID", event.getSessionId());
            json.put("type", "custom");
            //unlike in Android, there is no "component" in MAF. The equivalent is
            //the application feature
            json.put("component", this.mobileBackend.getApplicationFeatureName());

            JSONObject params = new JSONObject();
            for (Map.Entry<String, String> entry : event.getProperties().entrySet()) {
                params.put(entry.getKey(), entry.getValue());
            }

            if (params.length() > 0)
                json.put("properties", params);

        } catch (Exception ex) {
            FiFLogger.logSevereError(LOG_TAG + ex.getMessage(),this.getClass().getSimpleName(),"createEventJson");
        }

        return json;
    }
    
}
