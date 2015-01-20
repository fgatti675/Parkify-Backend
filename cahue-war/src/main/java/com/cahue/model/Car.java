package com.cahue.model;


import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.List;


/**
 * Android device, linked to a User.
 *
 * @author Francesco
 */
@Entity
public class Car {

//    public static Car createDevice(@Nullable String btID, User user) {
//        Car car = new Car();
//        car.owner = user;
//        car.bluetoothAddress = btID;
//        return car;
//    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    private String bluetoothAddress;

    private String name;

    @ManyToOne
    private User owner;

    private List<User> relatedUsers;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getBluetoothAddress() {
        return bluetoothAddress;
    }

    public void setBluetoothAddress(String bluetoothAddress) {
        this.bluetoothAddress = bluetoothAddress;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @ManyToMany (mappedBy = "cars")
    public List<User> getRelatedUsers() {
        return relatedUsers;
    }

    public void setRelatedUsers(List<User> relatedUsers) {
        this.relatedUsers = relatedUsers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Car car = (Car) o;

        if (key != null ? !key.equals(car.key) : car.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

}
