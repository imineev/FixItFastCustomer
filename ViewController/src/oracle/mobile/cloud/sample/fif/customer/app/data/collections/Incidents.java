package oracle.mobile.cloud.sample.fif.customer.app.data.collections;

import java.util.ArrayList;

import oracle.adfmf.json.JSONArray;
import oracle.adfmf.json.JSONException;
import oracle.adfmf.json.JSONObject;

import oracle.mobile.cloud.sample.fif.customer.app.data.entities.Incident;
import oracle.mobile.cloud.sample.fif.customer.app.log.AppLogger;

/**
 * Incidents class is used to parse the JSON objects into a list of incident (Lis&lt;Incident&gt;) An example payload for
 * allIncidents is shown below
 *
 * {
 * "items": [
 * {
 * "id": 61,
 * "title": "Leaking Water Heater",
 * "createdon": "2015-03-27 14:12:13 UTC",
 * "status": "New",
 * "priority": "Low",
 * "imageLink": "/mobile/platform/storage/collections/FIF_UserData/objects/db89fd14-7ab0-4fe1-8593-5fffd8fdf974?user=46c5692a-bbed-4417-80d2-f114e41dc32a",
 * "notes": "\n2015-03-27T14:12:13.472Z\nAOSmith water heater leaking from right valve. Please hurry- water all over basement floor.\n\n2015-03-27T14:13:01.707Z\nMore water now.\n\n",
 * "technician": "joe@fixit.com",
 * "contact": {
 * "name": "Lynn Smith",
 * "street": "45 O Connor Street",
 * "city": "Ottawa",
 * "postalcode": "12345",
 * "username": "lynn"
 * }
 * },
 * {
 * "id": 63,
 * "title": "Leaking Water Heater",
 * "createdon": "2015-04-01 12:59:09 UTC",
 * "status": "New",
 * "priority": "High",
 * "imageLink": "storage/collections/2e029813-d1a9-4957-a69a-fbd0d7431d77/objects/6cdaa3a8-097e-49f7-9bd2-88966c45668f?user=lynn1014",
 * "notes": "\n2015-04-01T12:59:09.444Z\nmy water heater is broken\n",
 * "technician": "joe@fixit.com",
 * "contact": {
 * "name": "undefined undefined"
 * }
 * }
 * ]
 * ...
 * }
 *
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class Incidents {
    
    private JSONArray items = null;
    
    public Incidents() {
        super();
    }


    public void setItems(JSONArray items) {
        this.items = items;
    }

    public JSONArray getItems() {
        return items;
    }


    /**
     * populate a list with the content queried from the REST call
     * @param emptyList a list to populate with the items found in the JSON response
     * @return List of Incident
     */
    public ArrayList<Incident> populateIncidentList() {
        
        ArrayList<Incident> incidents = incidents = new ArrayList<Incident>();
        
        if(items!=null){            
            //populate entities in list
            for (int indx = 0; indx < items.length(); indx++) {
                try {
                    JSONObject incidentJSON = items.getJSONObject(indx);
                    Incident incident = new Incident();
                    incident.populateInstanceFromJSON(incidentJSON);
                    incidents.add(incident);
                    
                } catch (JSONException e) {
                    AppLogger.logSevereError("Failed to parse JSON Array Object. \n"+"Error: "+e.getMessage(), this.getClass().getSimpleName(), "getIncidents");
                }
            }

        }
        return incidents;
    }
}
