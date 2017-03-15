package oracle.mobile.cloud.sample.fif.customer.app.data.entities;

import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;

/**
 * Object that holds a note item of the incident note string, which is parsed in the Incident entity class
 *
 * @author Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class NoteItem {
    private int index = 0;
    private String message = "";
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public NoteItem() {
        super();
    }


    public void setIndex(int index) {
        int oldIndex = this.index;
        this.index = index;
        propertyChangeSupport.firePropertyChange("index", oldIndex, index);
    }

    public int getIndex() {
        return index;
    }

    public void setMessage(String message) {
        String oldMessage = this.message;
        this.message = message;
        propertyChangeSupport.firePropertyChange("message", oldMessage, message);
    }

    public String getMessage() {
        return message;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
}
