package oracle.mobile.cloud.sample.fif.customer.mcs.analytics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import oracle.adf.model.datacontrols.device.DeviceManagerFactory;
import oracle.adf.model.datacontrols.device.Location;

import oracle.mobile.cloud.sample.fif.customer.mcs.log.FiFLogger;
import oracle.mobile.cloud.sample.fif.customer.mcs.mbe.FiFMBEConfig;
import oracle.mobile.cloud.sample.fif.customer.mcs.mbe.FiFMobileBackend;
import oracle.mobile.cloud.sample.fif.customer.utils.MAFUtil;
import oracle.mobile.cloud.sample.fif.customer.utils.MapUtils;


/**
 *
 * Proxy class to access Mobile Cloud Service (MCS) analytic features and functionality. The Mobile Client SDK Analytics
 * captures Mobile Analytics Events and uploads them in batches to the Analytics Collector Service in MCS
 *
 * REST URI: /mobile/platform/analytics/events
 *
 * HTTP Codes returned from MCS
 * -----------------------------
 * HTTP 202 The events have successfully been logged
 * HTTP 400 The request failed because the payload of JSON message is not well-formed, or because an of exception that occurred during processing.
 * HTTP 405 The request failed because it uses a method that is not supported by the resource.
 *
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class Analytics {
    private String LOG_TAG = "Analytics - ";

    private Session mSession = null;
    private List<Event> mEvents = null;

    private double mLongitude = 0;
    private double mLatitude = 0;
    boolean locationServiceEnabledOnDevice = false;

    private ExecutorService mExecutorService = null;
    private FiFMBEConfig fifMBEConfig = null;
    private FiFMobileBackend mobileBackend = null;

    public Analytics(FiFMobileBackend mbe) {
        super();

        this.mobileBackend = mbe;
        this.fifMBEConfig = mbe.getMbeConfiguration();
        //create a list of events
        this.mEvents = new ArrayList<Event>();
        
        mExecutorService = Executors.newSingleThreadExecutor();

        //get GEO Location if device supports it
        FiFLogger.logFine(LOG_TAG + "Trying to read GEO Location data", this.getClass().getSimpleName(), "Constructor");
        if (MAFUtil.isGeoLocationAvailable()) {
            FiFLogger.logFine(LOG_TAG + "GEO Location available", this.getClass().getSimpleName(), "Constructor");
            //allow location information to be up to 10 minutes old
            int maxAllowedAgeofCachedLocationData = 600;
            //position doesn't need to be highly accurate for analytics
            boolean enableHighAccuracy = false;
            
            //applications that run on a simulator may not have GEO positioning available. In this case we catch 
            //the exception and log it
            try{
              Location location =
                 DeviceManagerFactory.getDeviceManager().getCurrentPosition(maxAllowedAgeofCachedLocationData,
                                                                           enableHighAccuracy);
              this.mLatitude = location.getLatitude();
              this.mLongitude = location.getLongitude();

                FiFLogger.logFine(LOG_TAG + "Longitude: " + mLongitude + " Latitude: " + mLatitude,
                                  this.getClass().getSimpleName(), "Constructor");
                //after successful setting the latitude, set location service enabled flag to true
                if (this.mLatitude != 0) {
                    locationServiceEnabledOnDevice = true;
                }
            }
            catch (Exception e){

                FiFLogger.logWarning(LOG_TAG + "GEO Location not available. The application runs on a device (Simulator?) that cannot access the GEO location.", this.getClass().getSimpleName(), "Constructor");
                FiFLogger.logFine(LOG_TAG + "Setting fixed lat/long value to 39.355589,-120.652492", this.getClass().getSimpleName(), "Constructor");
                
                this.mLatitude = 39.355589; 
                this.mLongitude = -120.652492;
                
                locationServiceEnabledOnDevice = false;
            }
            
        } else {
            FiFLogger.logFine(LOG_TAG + "GEO Location data not available. Please check permission.",
                              this.getClass().getSimpleName(), "Constructor");
        }

    }

    /**
     * Analytic events are colleted for a session (recording period) and then uploaded to MCS in a batch
     */
    public void startSession() {
        FiFLogger.logFine(LOG_TAG + "start session", this.getClass().getSimpleName(), "startSession");
        if (mSession == null) {
            FiFLogger.logFine(LOG_TAG + "creating new Session object", this.getClass().getSimpleName(), "startSession");
            mSession = new Session();
            FiFLogger.logFine(LOG_TAG + "new session created; Session ID:" + mSession.getSessionId(),
                              this.getClass().getSimpleName(), "startSession");
        } else {
            FiFLogger.logFine(LOG_TAG + "Existing session found with ID: " + mSession.getSessionId(),
                              this.getClass().getSimpleName(), "startSession");
        }
    }

    public void endSession() {
        FiFLogger.logFine(LOG_TAG + "endSession called. Flushin event list to server", this.getClass().getSimpleName(),
                          "enDession");
        flushEventQueueToServer();
    }

    /**
     * Creates a new default scoped event and adds it to the outgoing queue. Event is configured with event name, session ID and
     * a generated timestamp
     *
     * @param eventName
     * @return Event the new default event object
     */
    public Event addEmptyEventToOutgoingQueue(String eventName) {
        FiFLogger.logFine(LOG_TAG + "adding new event for name: " + eventName, this.getClass().getSimpleName(),
                          "addNewEventToOutgoingQueue");

        if (eventName == null) {
            FiFLogger.logFine(LOG_TAG + "event name is null. Throwing exception", this.getClass().getSimpleName(),
                              "addNewEventToOutgoingQueue");
            throw new IllegalArgumentException("'name' cannot be null");
        }

        if (mSession == null) {
            mSession = new Session();
            FiFLogger.logFine(LOG_TAG + "no current session found. Creating new session with ID: " + mSession,
                              this.getClass().getSimpleName(), "addNewEventToOutgoingQueue");
        }
        Event event = new Event(eventName, mSession.getSessionId());
        this.mEvents.add(event);
        FiFLogger.logFine(LOG_TAG + "new event created and added to list", this.getClass().getSimpleName(),
                          "addNewEventToOutgoingQueue");
        return event;
    }

    /**
     * Add event object to list of events to be published to the server
     * @param event
     */
    public Event addEventToOutgoingQueue(Event event) {
        if (event == null) {
            FiFLogger.logSevereError(LOG_TAG + "event object cannot be NULL", this.getClass().getSimpleName(),
                                     "addExistingEventToOutgoingQueue");
            throw new IllegalArgumentException("'event' cannot be null");
        }

        FiFLogger.logFine(LOG_TAG + "event object found. Session ID=" + event.getSessionId() + " TimeStamp=" +
                          event.getTimestamp() + " Properties=" + MapUtils.dumpProperties(event.getProperties()),
                          this.getClass().getSimpleName(), "addExistingEventToOutgoingQueue");

        if (mSession == null) {
            mSession = new Session();
            FiFLogger.logFine(LOG_TAG + "no current session found. Creating new session with ID: " + mSession,
                              this.getClass().getSimpleName(), "addExistingEventToOutgoingQueue");

        }

        event.setTimestamp(new Date());
        event.setSessionId(mSession.getSessionId());
        this.mEvents.add(event);

        return event;
    }

    /**
     *  Takes the events in the current queue and send them to the server. Ensure analytics is enabled for the MBE instance
     *  as otherwise no messages are sent
     */
    public void flushEventQueueToServer() {
        FiFLogger.logFine(LOG_TAG + "attempt to post events to server", this.getClass().getSimpleName(),
                          "flushEventQueueToServer");
        if (mEvents.size() < 1) {
            FiFLogger.logWarning(LOG_TAG + " - Events queue is empty. No server post necessary",
                                 this.getClass().getSimpleName(), "flushEventQueueToServer");
            return;
        }

        FiFLogger.logFine(LOG_TAG + "Check if analytics is enabled for MBE", this.getClass().getSimpleName(),
                          "flushEventQueueToServer");
        mSession.setEndTime(new Date());
        FiFLogger.logFine(LOG_TAG + "Preparing upload of events", this.getClass().getSimpleName(),
                          "flushEventQueueToServer");
        Runnable uploadTask = new UploadTask(this, new ArrayList<Event>(mEvents), mSession);
        FiFLogger.logFine(LOG_TAG + "Clearing event queue", this.getClass().getSimpleName(), "flushEventQueueToServer");
        mEvents = new ArrayList<Event>();
        FiFLogger.logFine(LOG_TAG + "End of Analytic session", this.getClass().getSimpleName(),
                          "flushEventQueueToServer");
        mSession = null;
        FiFLogger.logFine(LOG_TAG + "Uploading events", this.getClass().getSimpleName(), "flushEventQueueToServer");
        mExecutorService.execute(uploadTask);
    }

    public void setSession(Session session) {
        this.mSession = session;
    }

    public Session getSession() {
        return mSession;
    }

    public void setEvents(List<Event> events) {
        this.mEvents = events;
    }

    public List<Event> getEvents() {
        return mEvents;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }


    public FiFMobileBackend getMobileBackend() {
        return mobileBackend;
    }

    /**
     * If the mobile device allows this utility to determine the location (longitude(latitude) then
     * this method returns true; false otherwise
     * @return
     */
    public boolean isLocationServiceEnabledOnDevice() {
        return locationServiceEnabledOnDevice;
    }
}
