package com.cahue.model;


import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;


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
        car.generateId();
        car.updateKey();
        return car;
    }

    public void generateId(){
        this.id = UUID.randomUUID().toString();
    }

    public void updateKey() {
        this.key = KeyFactory.createKey(this.user.getKey(), this.getClass().getSimpleName(), UUID.randomUUID().toString());
    }

    private Key key;

    @Id
    private String id;

    private String bluetoothAddress;

    private String name;

    @ManyToOne
    private User user;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate = new Date();

    private List<Key> relatedUsers;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

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

        if (key != null ? !key.equals(car.key) : car.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

}
