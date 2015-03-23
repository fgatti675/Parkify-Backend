package com.cahue.model;


import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;

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

    private String btAddress;

    @Index
    private String name;

    private Integer color;

    @Index
    private Ref<ParkingSpot> spot;

    @Index
    private Date lastModified;

    @Parent
    private Ref<User> user;

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

    public String getBtAddress() {
        return btAddress;
    }

    public void setBtAddress(String btAddress) {
        this.btAddress = btAddress;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @XmlTransient
    public User getUser() {
        return user.get();
    }

    public void setUser(User user) {
        this.user = Ref.create(user);
    }

    public ParkingSpot getSpot() {
        if (spot == null) return null;
        return spot.get();
    }

    public void setSpot(ParkingSpot spot) {
        if (spot == null)
            this.spot = null;
        else
            this.spot = Ref.create(spot);
    }

    public Key<Car> createKey() {
        return Key.create(user.getKey(), Car.class, id);
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

    @Override
    public String toString() {
        return "Car{" +
                "id='" + id + '\'' +
                ", btAddress='" + btAddress + '\'' +
                ", name='" + name + '\'' +
                ", color=" + color +
                ", spot=" + spot +
                '}';
    }
}
