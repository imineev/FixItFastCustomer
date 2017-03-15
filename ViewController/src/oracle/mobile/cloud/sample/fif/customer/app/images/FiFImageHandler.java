package oracle.mobile.cloud.sample.fif.customer.app.images;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.io.Reader;
import java.io.StringWriter;

import java.util.Base64;
import java.util.HashMap;

import java.util.Map;
import java.util.Set;

import java.util.zip.GZIPInputStream;

import javax.el.ValueExpression;

import javax.microedition.io.HttpConnection;

import oracle.adfmf.dc.ws.rest.RestServiceAdapter;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.Model;

import oracle.mobile.cloud.sample.fif.customer.constants.FiFConstants;
import oracle.mobile.cloud.sample.fif.customer.mcs.log.FiFLogger;
import oracle.mobile.cloud.sample.fif.customer.mcs.mbe.FiFMobileBackend;
import oracle.mobile.cloud.sample.fif.customer.maf.RequestContext;
import oracle.mobile.cloud.sample.fif.customer.maf.ResponseContext;
import oracle.mobile.cloud.sample.fif.customer.maf.RestClient;
import oracle.mobile.cloud.sample.fif.customer.mcs.mbe.FiFMBEConfig;

/**
 * /**
 * Wrapper class to access the MBE FiFImageHandler API
 *
 * @author Frank Nimphius
 * @coyright Oracle Corporation, 2015
 */
public class FiFImageHandler {

    private static final String FIF_USER_DATA_COLLECTION_ID = "FIF_UserData";
    private static final String STORAGE_RELATIVE_URL = "/mobile/platform/storage";

    //base64 encoded image saying "NO IMAGE AVAILABLE"
    private static final String NO_IMAGE_AVAILABLE =
        "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 gUacS/qsEwAAAABJRU5ErkJggg==";


    public static String getImage(String incidentImageURL, FiFMobileBackend mbe) {
        FiFLogger.logFine("Getting image for remoteURL " + incidentImageURL, "Storage", "getImage");

        if (incidentImageURL != null && incidentImageURL.length() > 0) {
            //for MAF we need the URI of the string, not the full URL. To achieve this, we decompose the
            //remote link URL to then assemble our own URI
            int indxOfUserQueryString = incidentImageURL.indexOf("?user=");

            String objectId = incidentImageURL.substring(incidentImageURL.lastIndexOf('/') + 1, indxOfUserQueryString);

            //compose object URI
            String objectURI =
                FiFImageHandler.STORAGE_RELATIVE_URL + "/collections/" + FIF_USER_DATA_COLLECTION_ID + "/objects/" +
                objectId;

            //user isolated links need the userId to be added, so check the original link for a user ID
            if (indxOfUserQueryString > 0) {
                String userQueryString = incidentImageURL.substring(indxOfUserQueryString);
                objectURI += userQueryString;
            }

            FiFLogger.logFine("Image object URI " + objectURI, "Storage", "getImage");

            //get image from MCS
            RequestContext request = new RequestContext();

            request.setHttpMethod(oracle.mobile.cloud.sample.fif.customer.maf.RequestContext.HttpMethod.GET);
            request.setRequestURI(objectURI);
            request.setConnectionName(mbe.getMbeConfiguration().getMafRestConnectionName());

            HashMap<String, String> httpHeaders = new HashMap<String, String>();
            httpHeaders.put("Oracle-Mobile-Backend-Id", mbe.getMbeConfiguration().getMobileBackendId());
            httpHeaders.put("accept", "image/*");
            httpHeaders.put("Authorization", mbe.getMbeConfiguration().getOauthHttpHeaderToken());
            request.setHttpHeaders(httpHeaders);


            try {
                ResponseContext response = RestClient.sendForByteResponse(request);

                byte[] image = (byte[]) response.getResponsePayload();
                if (image != null && image.length > 0) {
                    String base64ImageString = Base64.getEncoder().encodeToString(image);
                    return base64ImageString;
                } else {
                    return FiFImageHandler.NO_IMAGE_AVAILABLE;
                }

            } catch (Exception e) {
                FiFLogger.logFine("Exception occured in call to REST client: " + e.getMessage(), "Storage", "getImage");
                return FiFImageHandler.NO_IMAGE_AVAILABLE;
            }

        } else {
            FiFLogger.logFine("Invalid remote image URL " + incidentImageURL, "Storage", "getImage");
            return FiFImageHandler.NO_IMAGE_AVAILABLE;

        }
    }

    public static String postImage(String incidentImageURL, FiFMobileBackend mbe) {
        FiFLogger.logFine("Getting image for remoteURL " + incidentImageURL, "Storage", "postImage");

        if (incidentImageURL != null && incidentImageURL.length() > 0) {

            //compose object URI
            String objectURI =
                FiFImageHandler.STORAGE_RELATIVE_URL + "/collections/" + FIF_USER_DATA_COLLECTION_ID + "/objects";

            FiFLogger.logFine("Image object URI " + objectURI, "Storage", "getImage");

            //post image to MCS
            RequestContext request = new RequestContext();

            request.setHttpMethod(oracle.mobile.cloud.sample.fif.customer.maf.RequestContext.HttpMethod.POST);
            request.setRequestURI(objectURI);
            request.setConnectionName(mbe.getMbeConfiguration().getMafRestConnectionName());

            HashMap<String, String> httpHeaders = new HashMap<String, String>();
            httpHeaders.put("Oracle-Mobile-Backend-Id", mbe.getMbeConfiguration().getMobileBackendId());
            httpHeaders.put("Content-Type", "image/jpeg");
            //httpHeaders.put("Content-Length", "12000");
            httpHeaders.put("Authorization", mbe.getMbeConfiguration().getOauthHttpHeaderToken());
            request.setHttpHeaders(httpHeaders);
            
            ResponseContext response = new ResponseContext();
            String responseMessage=null;
            
            byte[] imgToPost = getFileContent(incidentImageURL);
            request.setPayload(imgToPost);

            try {
                RestServiceAdapter restServiceAdapter = prepareRestServiceAdapter(request);

                String url = restServiceAdapter.getConnectionEndPoint(request.getConnectionName()) + request.getRequestURI();
                HashMap headerProperties = request.getHttpHeaders();
                HttpConnection httpConnection = restServiceAdapter.getHttpConnection(request.getHttpMethod().toString(), url, headerProperties);
                OutputStream outputStream = httpConnection.openDataOutputStream();
                if(outputStream != null){
                    //serialize Object to binary[]
                    copyStream(new ByteArrayInputStream((byte[])request.getPayload()) , outputStream);                
                                    
                    Integer statusCode = httpConnection.getResponseCode();
                    String contentEncoding = httpConnection.getHeaderField("Content-Encoding");
                    
                    if(contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")){
                       responseMessage = getResponse(httpConnection.openInputStream(), true);
                    }
                    else{
                       responseMessage = getResponse(httpConnection.openInputStream(), false);
                    }
                    
                    //set the response status, headers and the returned payload to the context object for
                    //delivery to the requesting client
                    response.setResponseStatus(statusCode);
                    response.setResponseHeaders(restServiceAdapter.getResponseHeaders());
                    response.setResponsePayload(responseMessage);
                    response.setRequestUrl(url);
                }
                else{
                     FiFLogger.logFine("Request could not be send to server. No error received.", "RestClient.java", "handleBinaryRequest");
                }        

                return responseMessage;

            } catch (Exception e) {
                FiFLogger.logFine("Exception occured in call to REST client: " + e.getMessage(), "Storage",
                                  "postImage");
                return FiFImageHandler.NO_IMAGE_AVAILABLE;
            }

        } else {
            FiFLogger.logFine("Invalid remote image URL " + incidentImageURL, "Storage", "postImage");
            return FiFImageHandler.NO_IMAGE_AVAILABLE;

        }
    }

    public static byte[] getFileContent(String filePath) {
        //   filePath =      "file:///storage/sdcard/Android/data/oracle.cloud.mobile.demo.fixitfastcustomer/cache/1435417578300.jpg";
        filePath = filePath.substring(7);
        File file = new File(filePath);


        byte contentArray[] = null;
        try {
            FileInputStream fis = new FileInputStream(file);

            System.out.println("Total file size to read (in bytes) : " + fis.available());
            int nunberOfBytesAvailable = fis.available();
            contentArray = new byte[nunberOfBytesAvailable];
            fis.read(contentArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //   System.out.println(new String(contentArray));
        return contentArray;
    }

    private static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024]; // Adjust if you want
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    private static String getResponse(InputStream is, boolean gzipEncoded) {
        Reader reader = null;
        StringWriter writer = null;
        String charset = "UTF-8"; //  
        InputStream response = null;
        try {
           
            if(gzipEncoded == true){ 
               response = new GZIPInputStream(is);
            }
            else{
                response = is; 
            }
            
           reader = new InputStreamReader(response, charset);
           writer = new StringWriter();

           char[] buffer = new char[10240];
           for (int length = 0; (length = reader.read(buffer)) > 0;) {
                  writer.write(buffer, 0, length);
            }           
            } catch (IOException e) {
              
            }               
            //housekeeping
            finally {
              try {
                 writer.close();
                 reader.close();
             } catch (IOException e) {
                 //nothing we can do here. So we leave the dirt and go. 
             }            
         }
         
         return writer.toString();
        }
    
    /**
     * Creates and configures an instance of RestServiceAdapter with information from the request context
     * @param request
     * @return RestServiceAdapter
     */
    private static RestServiceAdapter prepareRestServiceAdapter(RequestContext request) {
        RestServiceAdapter restServiceAdapter = Model.createRestServiceAdapter();
        restServiceAdapter.clearRequestProperties();

        restServiceAdapter.setConnectionName(request.getConnectionName());
        restServiceAdapter.setRequestType(request.getHttpMethod().toString());
        restServiceAdapter.setRequestURI(request.getRequestURI());
        restServiceAdapter.setRetryLimit(request.getRetryLimit());

        //default accept header and content-type header to application/jason. The values
        //are overwritten with the information in the header map of the request context
        //object. If not then they are set at least
        restServiceAdapter.addRequestProperty("Accept", "application/json");
        restServiceAdapter.addRequestProperty("Content-Type", "application/json");
        
        //requests in Oracle MCS allways happen in the context of a valie Oracle Mobile Backend object. For this 
        //reason it is set here. If the information is contained in the custom headers then this information here
        //gets overwritten with the information in the headers
        if(request.getMbeConfig() != null &&  request.getMbeConfig().getMobileBackendId() != null && !request.getMbeConfig().getMobileBackendId().isEmpty()){
           
            restServiceAdapter.addRequestProperty("Oracle-Mobile-Backend-Id", request.getMbeConfig().getMobileBackendId());    
        
        }
        else{
            //the mobile backend id can also be passed as part of the header map, in which case it would not be 
            //required in the RequestContext object. While this is not an error, at least we will log it here
            
            FiFLogger.logFine(" Mobile backend ID could not be added to header for request to: "+request.getRequestURI() , "FiFImageHandler", "postImage");
            
        }               
        
        HashMap<String,String> allRequestPropertyMap = new HashMap<String,String>();
        
        Map<String,String> requestHeaderMap = request.getHttpHeaders();
        if (requestHeaderMap != null && !requestHeaderMap.isEmpty()){
            allRequestPropertyMap.putAll(requestHeaderMap);
        }
        
        if (allRequestPropertyMap != null && !allRequestPropertyMap.isEmpty()) {
            
            Set keySet = allRequestPropertyMap.keySet();
            //read the headers passed in the request context
            for (Object headerParam : keySet) {
                String paramName = (String) headerParam;                
                String paramValue = allRequestPropertyMap.get(paramName).toString();
                restServiceAdapter.addRequestProperty(paramName, paramValue);                 
            }
        }
        return restServiceAdapter;
    }
    
    protected byte[] getDocumentContent(String fileURL) {

        
        //note that you may need to check if teh URL contains file:// as a prefix. It could be you then need to remove this. Debugger is your friend
        File documentFile = new File(fileURL);

        byte[] documentByteArray = null;
        
        if (documentFile.exists()) {            
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(documentFile);
                
                documentByteArray = convertInputStreamToByteArray(fileInputStream);
                
                //documentByteArray = new byte[fileInputStream.available()];            
                //fileInputStream.read(documentByteArray);

            } catch (FileNotFoundException e) {
                FiFLogger.logFine("File" + fileURL + "does not exist.", "FiFImageHandler", "getDocumentContent");
            } 
            try {
                fileInputStream.close();
            } catch (IOException e) {
                //could not close file handle. However, to this point we are unable to
                //do anything here. So lets just share this information with the user
            }
        }
        else{
            FiFLogger.logFine("File" + fileURL + "does not exist.", "FiFImageHandler", "getDocumentContent");
        }

        return documentByteArray;
    }
    
    private byte[] convertInputStreamToByteArray(InputStream inputStream) {
        
        if(inputStream == null)
            return new byte[0];

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buff = new byte[2048];
        int bytesRead;

        try {
            while ((bytesRead = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, bytesRead);
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] content = outputStream.toByteArray();

        return content;
    }
    
}
