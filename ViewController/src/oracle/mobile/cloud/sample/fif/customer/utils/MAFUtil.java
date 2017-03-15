package oracle.mobile.cloud.sample.fif.customer.utils;

import oracle.adf.model.datacontrols.device.DeviceManager;
import oracle.adf.model.datacontrols.device.DeviceManagerFactory;


/**
 *
 * Abstraction layer to the MAF APIs
 *
 * @author   Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class MAFUtil {
    private static final String NETWORK_STATUS_NONE = "none";
    
    
    public static final String VENDOR_APPLE   = "Apple";
    public static final String VENDOR_GOOGLE  = "Google";
    public static final String VENDOR_OTHER   = "Other";
    
    public MAFUtil() {
        super();
    }
    
    /**
     * Check if network access is available. 
     * @return true if there is WIFI or 3,4 GM network
     */
    public static boolean isNetworkAccess(){
                               
        boolean isNetwork = false;
        isNetwork = DeviceManagerFactory.getDeviceManager().isDeviceOnline();
        return isNetwork;
    }
    
    /**
     * Checks if the device supports GEO location retireval, which could be disabled by the device not supporting 
     * GEO locations or the application missing the required permission
     * 
     * @return true if GEO location data can be accessed. False otherwise
     */
    public static boolean isGeoLocationAvailable(){
        return DeviceManagerFactory.getDeviceManager().hasGeolocation();
    }
    
    /**
     *
     * Determibes manufacturer based on OS
     * @return Apple for iOS, Google for Android, Other for the rest
     */
    public static String getOsVendor(){
        
        DeviceManager deviceManager = DeviceManagerFactory.getDeviceManager();
        
        String _toUppercaseManufacturerOS = deviceManager.getOs().toUpperCase();
        
        String manufacturer = _toUppercaseManufacturerOS.equals("IOS") ? VENDOR_APPLE :
                              _toUppercaseManufacturerOS.contains("ANDROID") ? VENDOR_GOOGLE : VENDOR_OTHER;      
        return manufacturer;
        
    }
    
    /**
     * Detect the device operation system
     * @return
     */
    public static String getDeviceOS(){
       
        DeviceManager deviceManager = DeviceManagerFactory.getDeviceManager();
        return deviceManager.getOs();
        
    }
    
    public static String getDeviceOSVersion(){

        DeviceManager deviceManager = DeviceManagerFactory.getDeviceManager();
        return deviceManager.getVersion();        
    }
    
    
}