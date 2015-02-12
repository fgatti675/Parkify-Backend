package com.cahue.model;


import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;

import javax.xml.bind.annotation.XmlTransient;


/**
 * Android device, linked to a User.
 *
 * @author Francesco
 */
@Cache
@Entity
public class Device {

    public static Device createDevice(String regId, User user) {
        Device device = new Device();
        device.user = Ref.create(user);
        device.regId = regId;
        return device;
    }

    public Device(){}

    @Id
    private String regId;

    @Parent
    private Ref<User> user;

    public String getRegId() {
        return regId;
    }

    private void setRegId(String regId) {
        this.regId = regId;
    }

    @XmlTransient
    public User getUser() {
        return user.get();
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
