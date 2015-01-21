package com.cahue.model;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Date: 14.01.15
 *
 * @author francesco
 */
@NamedQuery(name = "User.findByGoogleId", query = "select u from User u where u.googleId = :googleId")
@Entity
public class User implements Serializable {

    public User() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    private String email;

    private String googleId;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate = new Date();

    @OneToMany(mappedBy = "user")
    private Set<Device> devices = new HashSet<>();

    @ManyToMany
    private Set<Car> cars = new HashSet<>();

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Set<Device> getDevices() {
        return devices;
    }

    public void setDevices(Set<Device> devices) {
        this.devices = devices;
    }

    public Set<Car> getCars() {
        return cars;
    }

    public void setCars(Set<Car> cars) {
        this.cars = cars;
    }

    @Override
    public String toString() {
        return "User{" +
                "key=" + key +
                ", email=" + email +
                ", googleId='" + googleId + '\'' +
                '}';
    }

    public static Key createGoogleUserKey(String googleId) {
        return KeyFactory.createKey(User.class.getSimpleName(), "G" + googleId);
    }

}