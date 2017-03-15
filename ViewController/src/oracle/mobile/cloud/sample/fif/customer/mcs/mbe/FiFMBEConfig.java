package oracle.mobile.cloud.sample.fif.customer.mcs.mbe;

import oracle.adfmf.framework.api.AdfmfContainerUtilities;

/**
 *
 * MBE Configuration Object
 *
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */

public class FiFMBEConfig {
    
    private final static String BASIC = "basic";

    private String mafApplicationName = AdfmfContainerUtilities.getApplicationInformation().getName();
    private String mafApplicationId = AdfmfContainerUtilities.getApplicationInformation().getId();
    private String mafApplicationVendorName = AdfmfContainerUtilities.getApplicationInformation().getVendor();
    private String mafApplicationVersion = AdfmfContainerUtilities.getApplicationInformation().getVersion();


    private String mobileBackendApplicationKey = null;
    //Apple or Google push notification token
    private String deviceToken = null;

    /* required */
    private String mafRestConnectionName = null;

    /* required
     *
     * MCS generates authentication keys for mobile backends in the form of a backend ID and an anonymous access key.
     * These values are unique for a mobile backend. All mobile client applications that are registered for a mobile
     * backend share the credentials. MAF applications need to make the keys available to the MBE at runtime, which is
     * why they are stored in the MBE configuration object.
     */
    private String mobileBackendId = null;



    //Default authentication is Basic
    private String authtype = BASIC;


    //OAUTH token that needs to be passed with each REST call
    private String oauthHttpHeaderToken = "";

    private String authenticatedUsername = null;


    private String googleRegistrationId = null;

    private FiFMBEConfig() {
        super();
    }

    /**
     * @param mafRestConnectionName the name of the MAF REST connection.
     * @param mobileBackendId - the unique string identifying the remote Mobile Backend.
     * @param mobileBackendBaseURL - The mobile backend base URL
     */
    public FiFMBEConfig(String mafRestConnectionName, String mobileBackendId, String mobileBackendApplicationKey) {

        this.mafRestConnectionName = mafRestConnectionName;
        this.mobileBackendId = mobileBackendId;
        this.mobileBackendApplicationKey = mobileBackendApplicationKey;
    }

    public void setMafApplicationName(String mApplicationName) {
        this.mafApplicationName = mApplicationName;
    }

    public String getMafApplicationName() {
        return mafApplicationName;
    }

    public String getMafApplicationId() {
        return mafApplicationId;
    }

    /**
     * Authtype in MCS is eiter "basic"" or "oauth"". The FiF Demo uses OAUTH authentication 
     * @see oracle.mobile.cloud.sample.fif.customer.mcs.mbe.FiFMBEConstants
     * @return basic or oauth
     */
    public String getAuthtype() {
        return authtype;
    }

    public void setMobileBackendId(String mobileBackendId) {
        this.mobileBackendId = mobileBackendId;
    }

    /**
     * Each MBE operates a mobile backend on the MCS server. The backend ID allows the MobileBackend instance of the
     * MAF MCS Utility to invoke methods on the MBE instance
     * @return
     */
    public String getMobileBackendId() {
        return mobileBackendId;
    }

    /**
     * A MAF Rest Connection must be created for the Mobile Backend Root URL. If this value is not explicitly set it
     * will automatically be defaulted to the name of the mobile backend (which makes sense anyway)
     * @param mafRestConnectionName
     */
    public void setMafRestConnectionName(String mafRestConnectionName) {
        this.mafRestConnectionName = mafRestConnectionName;
    }

    public String getMafRestConnectionName() {
        return mafRestConnectionName;
    }

    /**
     * Application vendor name is what the MAF application developer specifies in the map-application
     * configuration file
     * @return
     */
    public String getMafApplicationVendorName() {
        return mafApplicationVendorName;
    }


    public String getMafApplicationVersion() {
        return mafApplicationVersion;
    }

    /**
     * Application Key ? This is an unique identifier used only by a specific mobile application. The key is created
     * when registering the mobile application with Mobile Cloud Service
     * @param mobileBackendApplicationKey
     */
    public void setMobileBackendApplicationKey(String mobileBackendApplicationKey) {
        this.mobileBackendApplicationKey = mobileBackendApplicationKey;
    }

    /**
     * Application Key ? This is an unique identifier used only by a specific mobile application. The key is created
     * when registering the mobile application with Mobile Cloud Service
     * @return
     */
    public String getMobileBackendApplicationKey() {
        return mobileBackendApplicationKey;
    }


    public void setAuthenticatedUsername(String authenticatedUsername) {
        this.authenticatedUsername = authenticatedUsername;
    }

    /**
     * Returns the authenticated username or null if no user has been authenticated
     * @return
     */
    public String getAuthenticatedUsername() {
        return authenticatedUsername;
    }


    public void setOauthHttpHeaderToken(String oauthHttpHeaderToken) {
        this.oauthHttpHeaderToken = oauthHttpHeaderToken;
    }
    
    /**
     * OAUTH bearer token that needs to be send with the MCS RESt request
     * @return
     */
    public String getOauthHttpHeaderToken() {
        return oauthHttpHeaderToken;
    }


    public void setGoogleRegistrationId(String googleRegistrationId) {
        this.googleRegistrationId = googleRegistrationId;
    }

    //obtain Google registration
    public String getGoogleRegistrationId() {
        return googleRegistrationId;
    }


    /**
     * Apple or Google push notification token
     * @param deviceToken token as rceived from vendor
     */
    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    /**
     * Apple or Google push notification token
     * @return
     */
    public String getDeviceToken() {
        return deviceToken;
    }

}
