package oracle.mobile.cloud.sample.fif.customer.maf;

import java.io.OutputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.microedition.io.HttpConnection;

import oracle.adfmf.dc.ws.rest.RestServiceAdapter;
import oracle.adfmf.framework.api.Model;
import oracle.adfmf.util.Utility;

import oracle.mobile.cloud.sample.fif.customer.mcs.log.FiFLogger;
import oracle.mobile.cloud.sample.fif.customer.mcs.log.FiFLogger;

/**
 *
 * Invokes the REST service using the MAF REST Service Adapter.
 *
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class RestClient {

    private RestClient() {
    }

    /**
     *
     * Sends the REST request to the server for String and byte[] payloads. The response type is expected to be 
     * String. This method therefore attempts to convert the REST response into String
     * 
     * @param request RequestContext object with the REST call configuration
     * @return ResponseContext with header information and responsePayload body
     * @throws Exception Exceptions thrown upon invoking the REST call (could be anything)
     */
    public static ResponseContext sendForStringResponse(RequestContext request) throws Exception {
        ResponseContext responseContext = sendForByteResponse(request);
        if(responseContext != null && responseContext.getResponsePayload() != null){
            if(responseContext.getResponsePayload() instanceof byte[]){
                responseContext.setResponsePayload(Utility.bytesToString((byte[]) responseContext.getResponsePayload()));
            }
        }
        return responseContext;
    }

    /**
     * Sends the REST request to the server for String and byte array payloads. The response type is expected to be 
     * byte[]
     * 
     * @param request RequestContext object with the REST call configuration
     * @return ResponseContext with header information and responsePayload body
     * @throws Exception Exceptions thrown upon invoking the REST call (could be anything)
     */
    public static ResponseContext sendForByteResponse(RequestContext request) throws Exception {

        RestServiceAdapter restServiceAdapter = prepareRestServiceAdapter(request);
        ResponseContext responseContext = new ResponseContext();

        //response can be either String or byte[]
        if(request.getPayload() == null || request.getPayload() instanceof String){
            byte[] responseRaw = restServiceAdapter.sendReceive(request.getPayload() == null? "" : (String) request.getPayload());
            
            responseContext.setRequestUrl(restServiceAdapter.getConnectionEndPoint(request.getConnectionName()) +
                                          restServiceAdapter.getRequestURI());
            
            responseContext.setResponsePayload(responseRaw);            
            responseContext.setResponseContentType(restServiceAdapter.getResponseContentType());
            responseContext.setResponseStatus(restServiceAdapter.getResponseStatus());
            responseContext.setResponseHeaders(restServiceAdapter.getResponseHeaders());
        }
        
        //handle binary payload
        else if( request.getPayload() != null && request.getPayload() instanceof byte[]){
                                
            responseContext = handleBinaryRequest(restServiceAdapter,request);
            
            //add the full request URL to the response object for logging purpose            
            responseContext.setRequestUrl(restServiceAdapter.getConnectionEndPoint(request.getConnectionName()) +
                                          restServiceAdapter.getRequestURI());
        }
        return responseContext;        
    }
    
    /**
     * Method that handles the upload of binary data. The RestServiceAdapter by design handles String payloads but doesn'z do byte arrays. This helper method provides 
     * this functionality, still using the RestServiceAdapter in MAF to handle the request and the response. The input arguments are expected in bytes[]. 
     * 
     * @param  restServiceAdapter  The prepared RestServiceAdapter (means containing all request properties. The payload will be overwritten with and empty String)"
     * @param  request RequestContext object 
     * @param  responseContext The response object to return to the client
     * @return ResponseContext object containing the payload and theresponse header information
     * @throws Exception
     */
    private static final ResponseContext handleBinaryRequest(RestServiceAdapter restServiceAdapter, RequestContext request) throws Exception{
                
        String url = restServiceAdapter.getConnectionEndPoint(request.getConnectionName()) + request.getRequestURI();
        //prepare the response context object to return the outcome of the REST reqest
        ResponseContext responseContext = new ResponseContext();
        
        HashMap headerProperties = request.getHttpHeaders();
        HttpConnection httpConnection = restServiceAdapter.getHttpConnection(request.getHttpMethod().toString(), url, headerProperties);  
        
        OutputStream outputStream = restServiceAdapter.getOutputStream(httpConnection);   
    
        try{      
            if(outputStream != null){
                outputStream.write((byte[]) request.getPayload());    
                
                //this is an interesting bit. Apparently the RESTServiceAdapter is called again, which in fact it is not. When there is no payload 
                //to send then the RESTServiceAdapter does not send anything. As the response buffer of the previous call - the http connection ->
                //outputStream -> write is still around, the response is read from it directly, leveraging all of the internal RESTServiceAdapter
                //processing
                restServiceAdapter.send("");
                
                //set the response status, headers and the returned payload to the context object for
                //delivery to the requesting client
                responseContext.setResponseStatus(restServiceAdapter.getResponseStatus());
                responseContext.setResponseHeaders(restServiceAdapter.getResponseHeaders());
                responseContext.setResponsePayload(httpConnection.getResponseMessage());
            }
            else{

                FiFLogger.logSevereError("Request could not be send to server. No error received.", "RestClient.java", "handleBinaryRequest");
            
            }        
        }
        catch(Exception e){
            //rethrow as in this try/catch block we are only 
            //intersted in closing the output stream gracefully
            throw e;
        }
        finally{
            Utility.closeSilently(outputStream);
            httpConnection.close();
        }
        return responseContext;
    }
    

    /**
     * Creates and configures an instance of RestServiceAdapter with information from the request context
     * @param request
     * @return RestServiceAdapter
     */
    protected static RestServiceAdapter prepareRestServiceAdapter(RequestContext request) {
       
        RestServiceAdapter restServiceAdapter = Model.createRestServiceAdapter();
        
        restServiceAdapter.clearRequestProperties();

        restServiceAdapter.setConnectionName(request.getConnectionName());
        restServiceAdapter.setRequestType(request.getHttpMethod().toString());
        restServiceAdapter.setRequestURI(request.getRequestURI());
        restServiceAdapter.setRetryLimit(request.getRetryLimit());

        //set default accept header to application/json. The values are overwritten with the information in the header 
        //map of the request context object
        restServiceAdapter.addRequestProperty("accept", "application/json");      
        restServiceAdapter.addRequestProperty("content-type", "application/json");      

        //set headers
        Map requestHeaderMap = request.getHttpHeaders();
        if (requestHeaderMap != null) {
            Set keySet = requestHeaderMap.keySet();

            //read the headers passed in the request context
            for (Object headerParam : keySet) {
                String paramName = (String) headerParam;
                String paramValue = (String) requestHeaderMap.get(paramName);
                restServiceAdapter.addRequestProperty(paramName, paramValue);
            }
        }
        return restServiceAdapter;
    }
}
