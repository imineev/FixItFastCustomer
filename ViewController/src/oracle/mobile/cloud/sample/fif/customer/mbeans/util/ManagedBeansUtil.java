package oracle.mobile.cloud.sample.fif.customer.mbeans.util;

import java.io.OutputStream;

import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.io.HttpConnection;

import oracle.adfmf.dc.ws.rest.RestServiceAdapter;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.api.Model;
import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.framework.exception.AdfInvocationException;

import oracle.mobile.cloud.sample.fif.customer.app.log.AppLogger;

/**
 * Question 1: The methods in this class are marked static - final.  Why?
 * Answer   1: The utility class does nt save any state and its functions are shared across managed beans in this sample.
 *             Because the utility class is part of a demo and thus only used by this demo, there is no requirement for a
 *             class to subclass this. Thus the "final", giving the JIT compiler some advantages.
 *
 *
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class ManagedBeansUtil {
    

    public static final String NO_IMAGE_AVAILABLE = "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 gUacS/qsEwAAAABJRU5ErkJggg==";


    /**
     * Invokes the Data Control with name "FiFCustomerDC". Note that the entry "FiFCustomerDC" must be set in the
     * DataBindings.cpx file. This is automatically the case when components are dragged from the data control panel 
     * onto a view
     * 
     *
     * @param methodName Name of a method as exposed on the data control
     * @param paramNames ArrayList of String values representing the names of the arguments expected by the method
     * @param paramValues ArrayList of Object values representing the values of the arguments expected by the method
     * @param paramTypes ArrayList of class representing the class names (String.class) of the arguments expected by the method
     */
    public static final Object invokeOnDataControl( String methodName, ArrayList<String> paramNames,  ArrayList<Object> paramValues, ArrayList<Class> paramTypes){
        Object outcome  = null;                  
        try {
            AppLogger.logFine("Invoking method ("+methodName+") on data control", "MBeanUtils", "invokeOnDataControl");
            AppLogger.logFine("Arguments in call to "+methodName+", parameter names: "+paramNames+" --- parameter values: "+paramValues, "MBeanUtils", "invokeOnDataControl");
            //write token information to data control            
            outcome = AdfmfJavaUtilities.invokeDataControlMethod("FiFCustomerDC", null, methodName,paramNames, paramValues, paramTypes);
            return outcome;
            
        } catch (AdfInvocationException e) {

            AppLogger.logSevereError("Failed to invoke method on Data Control for  method ("+methodName+")","MBeanUtils", "invokeOnDataControl");
            e.printStackTrace();
            return outcome;
        }
    }
    
    /**
     * Helper method for single argument data control invocations
     * @param methodName
     * @param argumentName
     * @param argumentValue
     * @param argumentType
     */
    public static final Object invokeDCSingleStringParameterMethod(String methodName, String argumentName, String argumentValue){
        ArrayList<String>   paramNames = new ArrayList<String>();
        paramNames.add(argumentName);
        ArrayList<Object>   paramValues = new ArrayList<Object>();
        paramValues.add(argumentValue);
        ArrayList<Class>           paramTypes = new ArrayList<Class>();
        paramTypes.add(String.class);
        return invokeOnDataControl(methodName,paramNames,paramValues,paramTypes);
    }
    
    /**
     * Helper method to post analytic events 
     * @param eventName  custom event name
     * @param eventProperties key/value pairs to send as the custom event message
     * @return none
     */
    public static final Object addCustomAnalyticEvent(String eventName, HashMap<String,String>eventProperties){
        ArrayList<String>   paramNames = new ArrayList<String>();
        paramNames.add("eventName");
        paramNames.add("eventProperties");
        ArrayList<Object>   paramValues = new ArrayList<Object>();
        paramValues.add(eventName);
        paramValues.add(eventProperties);
        ArrayList<Class>           paramTypes = new ArrayList<Class>();
        paramTypes.add(String.class);
        paramTypes.add(HashMap.class);
        return invokeOnDataControl("addCustomAnalyticEvent",paramNames,paramValues,paramTypes);
    }
    
    /**
     * Helper method for single argument data control invocations
     * @param methodName
     * @param argumentName
     * @param argumentValue
     * @param argumentType
     */
    public static final Object invokeDCSingleBooleanParameterMethod(String methodName, String argumentName, Boolean argumentValue){
        ArrayList<String>   paramNames = new ArrayList<String>();
        paramNames.add(argumentName);
        ArrayList<Object>   paramValues = new ArrayList<Object>();
        paramValues.add(argumentValue);
        ArrayList<Class>           paramTypes = new ArrayList<Class>();
        paramTypes.add(Boolean.class);
        return invokeOnDataControl(methodName,paramNames,paramValues,paramTypes);
    }
    
}
