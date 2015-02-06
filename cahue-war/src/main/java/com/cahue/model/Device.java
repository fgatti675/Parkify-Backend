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

    @Id
    private String regId;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    @XmlInverseReference(mappedBy = "devices")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (regId != null ? !regId.equals(device.regId) : device.regId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return regId != null ? regId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Device{" +
                "regId='" + regId + '\'' +
                ", user=" + user +
                '}';
    }
}
