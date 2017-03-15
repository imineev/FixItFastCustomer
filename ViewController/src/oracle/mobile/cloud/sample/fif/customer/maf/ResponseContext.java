package oracle.mobile.cloud.sample.fif.customer.maf;


import java.util.HashMap;


/**
 * The ResponseContext class wraps the REST Service Adapter responsePayload including the returned
 * payload, the responsePayload status code and the responsePayload header parameter
 *
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class ResponseContext {
    
    
    private String LOG_TAG = "Response Context: ";
    
    //short list of common response status codes    
    /*
     * Status 0 is returned for requests that are opened
     * or that cannot be sent. This could indicate that a
     * VPN connection (or other proxy) hinders the request
     */
    public static final int STATUS_0                = 0;
    
    
    public static final int STATUS_RESPONSE_OK         = 200;
    public static final int STATUS_RESPONSE_CREATED    = 201;
    public static final int STATUS_RESPONSE_ACCEPTED   = 202;
    public static final int STATUS_RESPONSE_NO_CONTENT = 204;
    
    public static final int STATUS_RESPONSE_PERMANENTLY_MOVED = 301;
    public static final int STATUS_RESPONSE_FOUND = 302;
    public static final int STATUS_RESPONSE_NOT_MODIFIED = 304;
    
    public static final int STATUS_RESPONSE_BAD_REQUEST  = 400;
    public static final int STATUS_RESPONSE_UNAUTHORIZED = 401;
    public static final int STATUS_RESPONSE_FORBIDDEN    = 403;
    public static final int STATUS_RESPONSE_NOT_FOUND    = 404;

    public static final int STATUS_RESPONSE_PROXY_AUTHENTICATION_REQUIRED   = 407;
    public static final int STATUS_RESPONSE_REQUEST_TIMEOUT    = 408;
    public static final int STATUS_RESPONSE_LENGTH_REQUIRED   = 411;
    public static final int STATUS_RESPONSE_UNSUPPORTED_MEDIA_TYPE   = 415;
    
    
    public static final int STATUS_RESPONSE_INTERNAL_SERVER_ERROR   = 500; 
    public static final int STATUS_RESPONSE_NOT_IMPLEMENTED   = 501; 
    public static final int STATUS_RESPONSE_BAD_GATEWAY   = 502; 
    public static final int STATUS_RESPONSE_SERVICE_UNAVAILABLE   = 503; 
    
    private HashMap     responseHeaders = new HashMap();
    private Object      responsePayload = null;
    private int         responseStatus = 0;
    private String      responseContentType = "";
    private String      requestUrl = null;
      
    public ResponseContext() {
        super();
    }


    public void setResponseHeaders(HashMap responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    /**
     * Headers of the MCS responsePayload added as key/value pairs in the Map
     * @return HashMap
     */
    public HashMap getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponsePayload(Object response) {
        this.responsePayload = response;
    }

    /**
     * The REST Service Adapter in MAF can query String or binary payloads (streamed).
     * @return Object
     */
    public Object getResponsePayload() {
        return responsePayload;
    }


    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public void setResponseContentType(String responseContent) {
        this.responseContentType = responseContent;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestUrl() {
        return requestUrl;
    }
}
