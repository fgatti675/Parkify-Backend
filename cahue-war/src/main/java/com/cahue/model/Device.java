package com.cahue.model;


import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import org.eclipse.persistence.oxm.annotations.XmlInverseReference;

import javax.persistence.*;


/**
 * Android device, linked to a User.
 *
 * @author Francesco
 */
@Entity
public class Device {

    public static Device createDevice(String regId, User user) {
        Device device = new Device();
        device.user = user;
        device.key = KeyFactory.createKey(user.getKey(), device.getClass().getSimpleName(), regId);
        device.regId = regId;
        return device;
    }

    private Device(){}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    private String regId;

    @ManyToOne
    private User user;

    public String getRegId() {
        return regId;
    }

    private void setRegId(String regId) {
        this.regId = regId;
    }

    @XmlInverseReference(mappedBy="devices")
    public User getUser() {
        return user;
    }

    private void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (key != null ? !key.equals(device.key) : device.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Device{" +
                "regId='" + regId + '\'' +
                ", user=" + user +
                '}';
    }
}
