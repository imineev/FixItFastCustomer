package oracle.mobile.cloud.sample.fif.customer.mbeans.exceptions;

import java.util.HashMap;

import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;


/**
 *  *** Internal exception class - don't throw in custom MAF applications ***
 *
 * Exception thrown upon failure in the proxy services: Userinfo, Analytics, Notification, Storage. The object returns
 * an HTTP error code if available for the exception. If a REST API call to MCS returns a HTTP 400 status response then
 * this doesn't necessarily mean "Bad Request" but has a meaning in the context of Oracle MCS and the platform API that
 * was called. The MAF MCS Utility does its best to get to determine the Orace MCS error and to distinguish application
 * errors from excepions thrown by the REST transport layer or the remore server. This class exposes convenient methods
 * for MAF application developers to be able to tell wheher an exception is caused by an application error or not.
 * <p>
 * If an error is raised by Oracle MCE platform API then an error message is provided as a JSON object that, beside of
 * the http status code and a message, contains additional information like the MCS specific error code (mobile error)
 * and the error path (which is the request URI that lead to the failure).
 * <p>
 * An example error response coming from MCS is shown below
 * <p>
 * <pre>
 *{
 *  "type":"?",
 *  "status": <error_code>,
 *  "title": "<short description of the error>",
 *  "detail": "<long description of the error>",
 *  "o:ecid": "<?>",
 *  "o:errorCode": "MOBILE-<number here>",
 *  "o:errorPath": "<URI of the request>"
 * }
 * </pre>
 * <p>
 * MAF developers can use the combination of isApplicationError() == true and getMessage() to parse the error message into
 * an object.
 * <p>
 * <pre>
 *  com.oracle.maf.sample.mcs.shared.mbe.error.getMobileErrorObject meo = com.oracle.maf.sample.mcs.shared.mbe.error.OracleMobileErrorHelper.getMobileErrorObject(message);
 *  int statusCode = meo.getStatus();
 *  String errorTitle = meo.getTitle();
 *  ...
 * </pre>
 * <p>
 * The getMobileErrorObject(message) method throws a oracle.adfmf.json.JSONException if the message is not a JSOObject. If
 * this happens then either MCS did not return a JSON object or the error parsing of MAF MCS Utility failed. In this case
 * MAF developers would simply display the message string they obtain from this class.
 * <p>
 * Note that the httpResponseCode of an exception mirrors the value of the HTTP code causing the exception. If there is
 * no error code accessible, then -1 is returned.
 *
 * @author   Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public final class ServiceProxyException extends Exception{
    
    
    public static final String  INFO                       = "INFO";
    public static final String  WARNING                    = "WARNING";
    public static final String  ERROR                      = "ERROR";
    public static final String  FATAL                      = "FATAL";

    private boolean applicationError = false;
    private boolean exception = false;

    //set HTTP response code to -1 for all failed requests that don't provide a response
    private int httpResponseCode = -1;

    private HashMap<String,String> errorResponseHeaders = new HashMap<String,String>();

    private String message = null;
    String exceptionClass =  null;

    @SuppressWarnings({ "compatibility:3180833435509478707", "oracle.jdeveloper.java.serialversionuid-stale" })
    private static final long serialVersionUID = 1L;
    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    /**
     * ServiceProxyException that is used to return an MCS application error. Application errors have an error
     * message of some sort that conforms to the MCS Oracle error JSON format structure. Ideally we want this
     * error to be returned fro a AF MCS REST request as it provides the max. information about the cause of
     * the error
     *
     * @param httpResponseCode int value of the HTTP error code
     * @param message The detail message for this exception
     */
      public ServiceProxyException(int httpResponseCode, String message) {      
        this.httpResponseCode = httpResponseCode;
        this.message = message;
        this.applicationError = true;
    }



    /**
     * ServiceProxyException that is used to return an MCS application error. Application errors have an error
     * message of some sort that conforms to the MCS Oracle error JSON format structure. Ideally we want this
     * error to be returned fro a AF MCS REST request as it provides the max. information about the cause of
     * the error
     *
     * @param httpResponseCode int value of the HTTP error code
     * @param message The detail message for this exception
     * @param responseHeaders HashMap with the header information returned by a response
     */
    public ServiceProxyException(int httpResponseCode, String message, HashMap<String,String> responseHeaders) {
         this(httpResponseCode, message);
         this.setErrorResponseHeaders(responseHeaders);
    }

    /**
     * @param throwable
     * @param severity ServiceProxyException.SEVERITY
     */
    public ServiceProxyException(Throwable throwable, String severity) {
        exceptionClass = throwable != null ? throwable.getClass().getSimpleName() : "";
        this.exception = true;
        this.message = throwable != null ? throwable.getMessage() : "";
    }

    /**
     * @param message The detail message for this exception
     * @param severity ServiceProxyException.SEVERITY
     */
    public ServiceProxyException(String message, String severity) {
        this.message = message;
        this.exception = true;
    }

    /**
     * Whether the exception is a MCS application error response or a real exception. If it is an application
     * error then the error message is in the Oracle error format (JSON object string). The object string can
     * be parsed using the Oracle MobileErrorHelper class.
     * @return true/false
     */
    public boolean isApplicationError() {
        return applicationError;
    }

    /**
     * Whether the exception is a MCS application error response or a real exception. If it is a real exception
     * then the error message is a standard string
     * @return true/false
     */
    public boolean isException() {
        return exception;
    }

    /**
     * If an error is an application error, then this method returns the int value of the HTTP error returned from MCS. If
     * the exception is thrown on the transport layer or  other runtime or language exceptions then this method returns 0
     * @return HTTP error code or 0
     */
    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    /**
     * Exception message, which could be an Oracle mobile error or an exception string from an underlying
     * exception that is thrown. This method returns the flattened version of the message for logging or.
     * 
     * If the exception was thrown because of an application error, then the message format has the following 
     * structure:
     * 
     * {
     *  "type":"?",
     *  "status": <error_code>,
     *  "title": "<short description of the error>",
     *  "detail": "<long description of the error>",
     *  "o:ecid": "<?>",
     *  "o:errorCode": "MOBILE-<number here>",
     *  "o:errorPath": "<URI of the request>"
     * }
     *
     * @return the toString() representation of the error message
     */
    public String getMessage() {
        return this.message;
    }


    public void setErrorResponseHeaders(HashMap<String, String> errorResponseHeaders) {
        HashMap<String, String> oldErrorResponseHeaders = this.errorResponseHeaders;
        this.errorResponseHeaders = errorResponseHeaders;
        propertyChangeSupport.firePropertyChange("errorResponseHeaders", oldErrorResponseHeaders, errorResponseHeaders);
    }

    /**
     * HTTP headers returned with application errors. This header map is empty for transport layer exceptions and may be
     * empty for application errors if the header information is not accessible for the MAF MCS Utility. Applications can
     * not rely on this information to be available
     * @return
     */
    public HashMap<String, String> getErrorResponseHeaders() {
        return errorResponseHeaders;
    }

    /**
     * If this exception wraps another exception, calling getExceptionClassName will return the name of the
     * wrapped exception. E.g. JSONParsingException. If the exception does not wrap another Exception then
     * the name of this class is returned
     * @return
     */
    public String getExceptionClassName() {
        return this.exceptionClass == null ? this.getClass().getSimpleName() : this.exceptionClass;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
}
