package com.cahue.model;

import com.cahue.util.BooleanAdapter;
import com.google.appengine.api.datastore.GeoPt;
import com.google.apphosting.datastore.EntityV4;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;


@Cache
@Entity
public class FreeParkingSpot {

    private Date time = new Date();

    @Id
    private Long id;

    @Index
    private GeoPt location;
    private Float accuracy;

    @Index
    private Date expiryTime;

    private boolean future = false;

    public FreeParkingSpot() {
    }

    /**
     * Get the equivalent parking spot
     *
     * @return
     */
    public ParkingSpot createSpot() {
        ParkingSpot spot = new ParkingSpot();
        spot.setTime(time);
        spot.setId(id);
        spot.setLongitude((double) location.getLongitude());
        spot.setLatitude((double) location.getLatitude());
        spot.setAccuracy(accuracy);
        return spot;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @XmlTransient
    public Date getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Date expiryTime) {
        this.expiryTime = expiryTime;
    }

    @XmlJavaTypeAdapter(value = BooleanAdapter.class)
    public boolean isFuture() {
        return future;
    }

    public void setFuture(boolean future) {
        this.future = future;
    }

    public GeoPt getLocation() {
        return location;
    }

    public void setLocation(GeoPt location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "ParkingSpotIndexEntry{" +
                "id=" + id +
                ", location=" + location +
                ", accuracy=" + accuracy +
                ", time=" + time +
                ", future=" + future +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FreeParkingSpot that = (FreeParkingSpot) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
