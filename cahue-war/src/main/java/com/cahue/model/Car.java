package com.cahue.model;


import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.datanucleus.annotations.Unowned;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static javax.persistence.GenerationType.IDENTITY;


/**
 * Car linked to a User.
 *
 * @author Francesco
 */
@Entity
public class Car {

    public static Car createCar(User user, String name, @Nullable String btID) {
        Car car = new Car();
        car.user = user;
        car.bluetoothAddress = btID;
        car.name = name;
        return car;
    }


    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    private String bluetoothAddress;

    private String name;

    @ManyToOne
    private User user;

    @Temporal(value = TemporalType.TIMESTAMP)
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

        Car car = (Car) o;

        if (id != null ? !id.equals(car.id) : car.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
