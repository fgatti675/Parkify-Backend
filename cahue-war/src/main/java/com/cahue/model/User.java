package com.cahue.model;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.datanucleus.annotations.Unowned;
import org.eclipse.persistence.annotations.JoinFetchType;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
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
    private String id;

    private String email;

    private String googleId;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate = new Date();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Device> devices = new HashSet<>();

    @Unowned
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Car> cars = new HashSet<>();

    @Transient
    private String authToken;

    @XmlTransient
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email=" + email +
                ", googleId='" + googleId + '\'' +
                '}';
    }

}
