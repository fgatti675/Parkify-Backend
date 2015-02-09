package com.cahue.model;


import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;


/**
 * Car linked to a User.
 *
 * @author Francesco
 */
@Cache
@Entity
public class Car {


    @Id
    private String id;

    private String bluetoothAddress;

    private String name;

    @Parent
    private Ref<User> user;

    private Date creationDate = new Date();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBluetoothAddress() {
        return bluetoothAddress;
    }

    public void setBluetoothAddress(String bluetoothAddress) {
        this.bluetoothAddress = bluetoothAddress;
    }

    @XmlTransient
    public Date getCreationDate() {
        return creationDate;
    }

    @XmlTransient
    public User getUser() {
        return user.get();
    }

    public void setUser(User user) {
        this.user = Ref.create(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Car car = (Car) o;

        if (id != null ? !id.equals(car.id) : car.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
