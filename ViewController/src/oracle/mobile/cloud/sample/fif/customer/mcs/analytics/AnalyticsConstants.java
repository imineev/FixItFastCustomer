package oracle.mobile.cloud.sample.fif.customer.mcs.analytics;


/**
 *
 * Constants for Mobile Cloud Service (MCS) Mobile Backend (MBE) Analytic Services
 *
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class AnalyticsConstants {
    
    public static final String ANALYTICS_RELATIVE_URL = "/mobile/platform/analytics/events";

    public static final String CONTENT_TYPE_HEADER                  = "Content-Type";
    public static final String APPLICATION_KEY_HEADER               = "Oracle-Mobile-APPLICATION-KEY";
    public static final String ANALYTIC_SESSION_ID_HEADER           = "Oracle-Mobile-ANALYTICS-SESSION-ID";
    public static final String ANALYTIC_MOBILE_DEVICE_ID_HEADER     = "Oracle-Mobile-DEVICE-ID";
    public static final String ANALYTIC_MOBILE_BACKEND_ID_HEADER    = "Oracle-Mobile-Backend-ID";

    public static final int HTTP_200 = 200;
    /**
     * HTTP 202 The events have successfully been logged
     */
    public static final int HTTP_202 = 202;
    /**
     * HTTP 400 The request failed because the payload of JSON message is not well-formed, or because an of exception that occurred during processing.
     */
    public static final int HTTP_400 = 400;
    
    /**
     * HTTP 405 The request failed because it uses a method that is not supported by the resource.
     */
    public static final int HTTP_405 = 405;
    
    private AnalyticsConstants() {}
}
