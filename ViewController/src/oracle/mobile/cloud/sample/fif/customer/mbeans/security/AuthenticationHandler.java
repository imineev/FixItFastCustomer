package oracle.mobile.cloud.sample.fif.customer.mbeans.security;

import java.util.ArrayList;
import java.util.Base64;
import java.util.GregorianCalendar;
import java.util.HashMap;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.json.JSONObject;

import oracle.mobile.cloud.sample.fif.customer.app.log.AppLogger;
import oracle.mobile.cloud.sample.fif.customer.constants.FiFConstants;
import oracle.mobile.cloud.sample.fif.customer.maf.RequestContext;
import oracle.mobile.cloud.sample.fif.customer.maf.ResponseContext;
import oracle.mobile.cloud.sample.fif.customer.maf.RestClient;
import oracle.mobile.cloud.sample.fif.customer.mbeans.exceptions.ServiceProxyException;
import oracle.mobile.cloud.sample.fif.customer.mbeans.util.ManagedBeansUtil;
import oracle.mobile.cloud.sample.fif.customer.utils.StringEncodeUtil;

/**
 * This class is exposed as a managed bean in the page flow scope (if the application had more than one feature it would be
 * saved in a application scope bean so that OAUTH authentication could be used across features). The managed bean is used
 * wihin the bounded task flow to determine the authenticated use state, to perform authentication and to pass the login token
 * to the data control.
 *
 * Question 1: Does this class have to be a managed bean or can OAUTH authentcation also be handled in a data control using the RestServiceAdapter?
 * Answer   1: Both approaches do work. The decision to use a managed bean was a) educational: to ensure the data control oly contains MCS access
 *             pattern and b) a matter of taste: the login process belongs to the client and not the business service model.
 *
 * Question 2: Is there another option to authenticate MAF applications with OAUTH and what is best practice?
 * Answer   2: MAF applications can use built-in, declarative authentication on a feature level. For this you mark the MAF feature to require security
 *             which you do in the maf-features.xml file. In the maf-application.xml file you then configure a security provider for basic or OAUTH
 *             authentication. Definitive, declarative authentication is recommended because of its ease of use. You would use programmatic authentication
 *             if you need to interact with the login-process or if you need to perform more than one authentication within the context of a single MAF
 *             feature
 *
 * Question 3: This class invokes a MAF data control to pass username and token information. What if this call executes before any UI component bound to the data control
 *             is invoked. Is there an exception thrown that need to be handled and if where?
 * Answer   3: Invoking a data control from a managed bean is as if this data control is invoked through the MAF binding layer. If the data control is not instantiated then
 *             it will be instantiated for the call. An exception would only occur in custom logic if the data control makes any assumption about e.g. being called first
 *             from a page or method actvity (e.g. a flag that is set in response to this initial call). This however is considered bad coding practices as the MAF data control
 *             should not have any dependency to the UI (vice versa is a valid option)
 *
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class AuthenticationHandler {
    
    //Map identifiers to control flow cases
    private static final String AUTHENTICATION_SUCCESS ="auth-success";
    private static final String AUTHENTICATION_FAILURE ="auth-failure";
    
    private static final String AUTH_HEADER = "Authorization";
    
    public static final String AUTH_LOGIN_URL = "/mobile/platform/users/login";

    private static int STATUS_RESPONSE_OK = 200;
    
    private String username;
    private String password;
    
    private String accessToken = null;
    private String tokenType = null;
    private boolean userAuthenticated = false;
    
    private long expiryTimeInMilliSeconds = 0;
    
    String authenticationResponseMessage = "";
    
    String backendId = null;
    String backendUrl = null;
    boolean authenticationSuccess = false;
        
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public AuthenticationHandler() {
        super();
    }

    /**
     * Athentication is performed during processing of the login. This value cannot be set directly. However, we 
     * use the property change event to ensure the router re-evaluates the authentication
     * @param userAuthenticated
     */
    private void setUserAuthenticated(boolean userAuthenticated) {
        boolean oldUserAuthenticated = this.userAuthenticated;
        this.userAuthenticated = userAuthenticated;
        propertyChangeSupport.firePropertyChange("userAuthenticated", oldUserAuthenticated,  this.userAuthenticated);
    }

    /**
     * Checks if the user is authenticated. For this it is required that the OAUTH access token exists and
     * that the token is not expired
     * @return
     */
    public boolean isUserAuthenticated() {
             
        boolean oauthTokenExists = this.accessToken!=null && !this.accessToken.isEmpty();        
        //authentcation token must exist and must NOT be expired
        userAuthenticated = oauthTokenExists && !this.isAuthenticationExpired();    
        
        AppLogger.logFine("Verify if user is authenticated. User authenticated = "+userAuthenticated, this.getClass().getSimpleName(), "isUserAuthenticated");
                
        return userAuthenticated;
    }


   
    /**
     * Method that can be called from the method call activity in MAF to determine navigation path
     * to follow next 
     */
    public String isUserSessionAuthenticated(){  
        boolean authenticated = isUserAuthenticated(); 
        if(authenticated){
            return AUTHENTICATION_SUCCESS;
        }
      return AUTHENTICATION_FAILURE;
    }

    public void setAuthenticationExpired(boolean authenticationExpired) {}

    //The OAUTh token expires after 8 hours by default
    private boolean isAuthenticationExpired(){
        boolean authenticationExpired = true;
                
        if(GregorianCalendar.getInstance().getTimeInMillis() > this.expiryTimeInMilliSeconds){
            authenticationExpired = true;
        }
        else{
            authenticationExpired = false;
        }
        
        AppLogger.logFine("Verify if authentication has expired. Authentication expired = "+authenticationExpired, this.getClass().getSimpleName(), "isAuthenticationExpired");
        
        return authenticationExpired;
    }


    public void setUsername(String username) {
        String oldUsername = this.username;
        this.username = username;
        propertyChangeSupport.firePropertyChange("username", oldUsername, username);
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        String oldPassword = this.password;
        this.password = password;
        propertyChangeSupport.firePropertyChange("password", oldPassword, password);
    }

    public String getPassword() {
        return password;
    }


    public void setAuthenticationResponseMessage(String authenticationResponseMessage) {
        String oldAuthenticationResponseMessage = this.authenticationResponseMessage;
        this.authenticationResponseMessage = authenticationResponseMessage;
        propertyChangeSupport.firePropertyChange("authenticationResponseMessage", oldAuthenticationResponseMessage,
                                                 authenticationResponseMessage);
    }

    public String getAuthenticationResponseMessage() {
        return authenticationResponseMessage;
    }

    /**
     * Method called from the login method activity. The returned String should be used to determine the
     * next navigation stop to be the login page again or a following page.
     * @return
     */
    public String login() throws ServiceProxyException {  
        
        //reset any authentication message
        setAuthenticationResponseMessage("");
        
        boolean authenticationSuccess = performBasicAuthentication(this.getUsername(), this.getPassword());
                        
        if(authenticationSuccess){
            this.setUserAuthenticated(true);
            AppLogger.logFine("Basic Login Success for user "+this.getUsername(), this.getClass().getSimpleName(), "login");
            
            //reset password
            this.setPassword(null);
            return AUTHENTICATION_SUCCESS;
        }
        
        else{
            this.setUserAuthenticated(false);
            AppLogger.logFine("OAUTH Login Failure for user "+this.getUsername(), this.getClass().getSimpleName(), "login");
            return AUTHENTICATION_FAILURE;
        }
    }
    
    /**
     * user is no longer logged in. Reste login flag and also ensure that 
     * the EL expressions (e.g. used in router) are re-evaluated 
     * 
     */
    public void logout(){
        //ensure EL expressions are re-evaluated : the router activity uses EL to determine 
        //wether the user session is authenticated or not. calling the setter method fires 
        //a property change event to enfoce re-evaluation
        setUserAuthenticated(false);
    }

    /**
     * Performs OAUTH credential owner authentication against the MCS OAUTH instance and token endpoint provided in the 
     * application preferences
     * @param _username MBE user
     * @param _password MBE user password
     * @return
     */
    private boolean performOauthAuthentication(String _username, String _password){
        boolean authenticationScuccess = false;
        
        
        //read configuration from application preferences        
        String oauthClientId =              (String) AdfmfJavaUtilities.getELValue("#{preferenceScope.application.oauth.oauthClientId}");
        String oauthClientSecret =          (String) AdfmfJavaUtilities.getELValue("#{preferenceScope.application.oauth.oauthClientSecret}");
        String oauthTokenEndpointURI =      (String) AdfmfJavaUtilities.getELValue("#{preferenceScope.application.oauth.oauthTokenEndpointUri}");
        
        AppLogger.logFine("Preferences: oauthClientId = "+oauthClientId+" oauthClientSecret="+oauthClientSecret+" oauthTokenEndpointURI="+oauthTokenEndpointURI, this.getClass().getSimpleName(), "performOauthAuthentication");
        
        //to authenticate the OAUTH user, the client ID and the client secret must be sent in a BASIC authorization header. The values are 
        //base64 encoded and concatenated by a ":"
        String base64EncodedBasicCredentials = "Basic " + Base64.getEncoder().encodeToString((oauthClientId+":"+oauthClientSecret).getBytes());
        
        //prepare the REST call headers
        HashMap<String,String> headers = new HashMap<String,String>();
        headers.put("Content-Type","application/x-www-form-urlencoded; charset=utf-8");
        headers.put("Authorization", base64EncodedBasicCredentials);       
        
        RequestContext request = new RequestContext();
        
        request.setHttpHeaders(headers);
        request.setConnectionName("OAUTH");
        request.setHttpMethod(RequestContext.HttpMethod.POST);
        
        //ensure URI uses a leading "/"
        if(!oauthTokenEndpointURI.startsWith("/")){
            oauthTokenEndpointURI = "/"+oauthTokenEndpointURI;
        }
        
        request.setRequestURI(oauthTokenEndpointURI);
        request.setPayload("grant_type=password&username="+_username+"&password="+_password);


        try {
            ResponseContext response = RestClient.sendForStringResponse(request);            
            
            if(response.getResponseStatus() == ResponseContext.STATUS_RESPONSE_OK){
                
                AppLogger.logFine("Rest Call succeeded with http-200. RAW Json response string is "+response.getResponsePayload(), this.getClass().getSimpleName(), "performOauthAuthentication");
                
                JSONObject jsonObject = new JSONObject((String)response.getResponsePayload());
                
                  accessToken = jsonObject.getString("access_token");
                  tokenType = jsonObject.getString("token_type");
                
                  long tokenExpriresInMilliseconds = new Long(jsonObject.getString("expires_in")).longValue() * 1000;
                  expiryTimeInMilliSeconds = GregorianCalendar.getInstance().getTimeInMillis() + tokenExpriresInMilliseconds;                
                  
                 //REST calls in the FIF Technician application are performed from managed beans and the Data Control. For the latter
                 //to issue authorized calls, we need to pass the OAUTH token to the data control, which then passes it on to the MBE
                  //ConfigObject
                 updateDataControlWithOauthToken(username, accessToken, tokenType);   
                 authenticationScuccess = true;
            }
            else{
                AppLogger.logFine("Rest Call failed with http-"+response.getResponseStatus()+". RAW Json response string is "+response.getResponsePayload(), this.getClass().getSimpleName(), "performAuthentication");                                
                setAuthenticationResponseMessage("Login failed. Please verify username and password.");
                authenticationScuccess = false;
            }
            
        } catch (Exception e) {
            AppLogger.logSevereError("REST Call fails with exception: "+e.getMessage(), this.getClass().getSimpleName(), "performAuthentication");
            authenticationScuccess = false;  
            
            //the MAF framwork shows exceptions as AdfExceptions so that we cannot use the exception type
            //to discover a failed network access. Thus looking at the error message to show a proper user
            //message
            if((e.getMessage()).toLowerCase().contains("connection refused")){
                setAuthenticationResponseMessage("Service not available. Please try again later.");
            }
            else{
                setAuthenticationResponseMessage("Login failed. Please verify username and password.");
            }
        }
        

        return authenticationScuccess;
    }
    
    /**
     * Performs OAUTH credential owner authentication against the MCS OAUTH instance and token endpoint provided in the 
     * application preferences
     * @param _username MBE user
     * @param _password MBE user password
     * @return
     */
    private boolean performBasicAuthentication(String username, String password) throws ServiceProxyException {
        authenticationSuccess = false;
        
        AppLogger.logFine("Authenticate: " + username + " PW == null?: " + (password == null ? true : false),
                        this.getClass().getSimpleName(), "authenticate");
        //username and password must be provided
        if ((username == null || username.length() == 0) || (password == null || password.length() == 0)) {
            throw new IllegalArgumentException("Username and password cannot be null in Basic Authentication");
        }

        //BasicAuthentication uses Base64 encoding
        String userCredentials = username + ":" + password;
        String base64EncodedBasicCredentials = "Basic " + StringEncodeUtil.base64Encode(userCredentials);

        backendId = (String) AdfmfJavaUtilities.getELValue("#{preferenceScope.application.mcs.fifMobileBackendId}");
        backendUrl = (String) AdfmfJavaUtilities.getELValue("#{preferenceScope.application.mcs.fifMobileBackendURL}");
        
        AppLogger.logFine("Authenticating user " + username + " (backendId: " + backendId + ")",
                        this.getClass().getSimpleName(), "authenticate");
        AppLogger.logFine("Authentication URI: " + AUTH_LOGIN_URL + " (full MBE URL: " +
                        backendUrl + AUTH_LOGIN_URL + ")", this.getClass().getSimpleName(),
                        "authenticate");

        handleBasicAuthentication(username, base64EncodedBasicCredentials);       

        return authenticationSuccess;
    }    
    
    /**
     * Invokes the Data Control with name "FiFTechnicianDC". Note that the entry "FiFTechnicianDC" must be set in the
     * DataBindings.cpx file. This is automatically the case when components are dragged from the data control panel 
     * onto a view
     * 
     * @param _username     e.g. joe@fixit.com
     * @param _oauthtoken   the OAUTH token obtained from authentication
     * @param _tokenType    "Bearer". However, its part of the authentication payload and should be taken from there
     */
    private void updateDataControlWithOauthToken(String _username, String _oauthtoken, String _tokenType){
        
             ArrayList<String> paramNames   = new ArrayList<String>();
             ArrayList<Object> paramValues  = new ArrayList<Object>();
             ArrayList<Class> paramTypes   =  new ArrayList<Class>();

             paramNames.add("username");
             paramValues.add(_username);
             paramTypes.add(String.class);
             
             paramNames.add("token");
             paramValues.add(_oauthtoken);
             paramTypes.add(String.class);

            AppLogger.logFine("Attempting to update data control - calling setOauthAuthenticatedUsernameAndToken: username = "+_username+" token != null? "+!_oauthtoken.isEmpty()+" tokenType = "+_tokenType, this.getClass().getSimpleName(), "updateDataControlWithOauthToken");
            //write token information to data control            
            ManagedBeansUtil.invokeOnDataControl("setOauthAuthenticatedUsernameAndToken", paramNames, paramValues, paramTypes);                            
    }
    
    private void handleBasicAuthentication(String username,
                                           String base64EncodedCredentials) throws IllegalArgumentException,
                                                                                   ServiceProxyException {


        if ((username == null || username.length() == 0) ||
            (base64EncodedCredentials == null || base64EncodedCredentials.length() == 0)) {
            throw new IllegalArgumentException("Username, credentials cannot be null in Basic Authentication");
        }

        RequestContext requestObject = new RequestContext();
        requestObject.setHttpMethod(RequestContext.HttpMethod.GET);
        requestObject.setRequestURI(AUTH_LOGIN_URL);
        requestObject.setConnectionName("FiFMBE");

        HashMap<String, String> httpHeaders = new HashMap<String, String>();
        httpHeaders.put(AUTH_HEADER, base64EncodedCredentials);
        httpHeaders.put(FiFConstants.ORACLE_MOBILE_BACKEND_ID, backendId);
        httpHeaders.put(FiFConstants.ACCEPT_HEADER, "application/json");

        requestObject.setHttpHeaders(httpHeaders);


        try {
            AppLogger.logFine("Atttempt to authenticate user: " + username, this.getClass().getSimpleName(),
                            "handleBasicAuthentication");
            ResponseContext mcsResponse = RestClient.sendForStringResponse(requestObject);

            //throw exception if the authnetication call succeeds but the authorization not
            if (mcsResponse != null && mcsResponse.getResponseStatus() == STATUS_RESPONSE_OK) {

                //authorization succeeded
                authenticationSuccess = true;
                accessToken = base64EncodedCredentials;
                updateDataControlWithOauthToken(username, accessToken, tokenType); 
            }
        } catch (Exception e) {
            AppLogger.logSevereError("REST Call fails with exception: "+e.getMessage(), this.getClass().getSimpleName(), "performAuthentication");
            authenticationSuccess = false;  
            
            //the MAF framwork shows exceptions as AdfExceptions so that we cannot use the exception type
            //to discover a failed network access. Thus looking at the error message to show a proper user
            //message
            if((e.getMessage()).toLowerCase().contains("connection refused")){
                setAuthenticationResponseMessage("Service not available. Please try again later.");
            }
            else{
                setAuthenticationResponseMessage("Login failed. Please verify username and password.");
            }
    }
}        
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
}
