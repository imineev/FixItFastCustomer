package oracle.mobile.cloud.sample.fif.customer.app.log;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.logging.Level;

import oracle.adfmf.util.Utility;

/**
 *
 * @author   Frank Nimphius
 * @coyright Oracle Corporation, 2015
 */
public class AppLogger {
    
    private static final String LOG_TAG = "FixItFast Customer: ";
    
    private AppLogger() {}    
    
    /**
     * CONFIG messages are intended to provide a variety of static
     * configuration information, to assist in debugging problems
     */
    public static void logConfig(String message, String className, String methodName){
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyyy-MM-dd hh:mm:ss");
        if(Utility.ApplicationLogger.isLoggable(Level.CONFIG) || Utility.ApplicationLogger.isLoggable(Level.FINE)){
            Utility.ApplicationLogger.logp(Level.CONFIG,className, methodName,dt1.format(new Date())+": "+message);
        }
    }
    
    /**
     * FINE is a message level providing tracing information.
     */
    public static void logFine(String message, String className, String methodName){
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyyy-MM-dd hh:mm:ss");
        if(Utility.ApplicationLogger.isLoggable(Level.FINE)){
          Utility.ApplicationLogger.logp(Level.FINE,LOG_TAG+className, methodName,dt1.format(new Date())+": "+message);
        }
    }
    
    /**
     * Indicating a serious failure. Describes events that are
     * of importance and will prevent normal program execution.
     */
    public static void logSevereError(String message, String className, String methodName){
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyyy-MM-dd hh:mm:ss");
            if(Utility.ApplicationLogger.isLoggable(Level.SEVERE) || Utility.ApplicationLogger.isLoggable(Level.FINE)){
           Utility.ApplicationLogger.logp(Level.SEVERE,LOG_TAG+className, methodName, dt1.format(new Date())+": "+message);
        }
    }
    /**
     * Indicating a potential problem. Describes event that will
     * be of interest to end users or system managers
     */
    public static void logWarning(String message, String className, String methodName){
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyyy-MM-dd hh:mm:ss");
            if(Utility.ApplicationLogger.isLoggable(Level.WARNING) || Utility.ApplicationLogger.isLoggable(Level.FINE)){
          Utility.ApplicationLogger.logp(Level.WARNING,LOG_TAG+className, methodName, dt1.format(new Date())+": "+message);
        }
    }
    
    /**
     * Informational messages. Messages will be written to the console
     * or its equivalent.  Use for reasonably important messages that 
     * make sense to end users and system administrators.
     */
    public static void logInfo(String message, String className, String methodName){
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyyy-MM-dd hh:mm:ss");
            if(Utility.ApplicationLogger.isLoggable(Level.INFO) || Utility.ApplicationLogger.isLoggable(Level.FINE)){
          Utility.ApplicationLogger.logp(Level.INFO,LOG_TAG+className, methodName, dt1.format(new Date())+": "+message);
        }
    }
}
