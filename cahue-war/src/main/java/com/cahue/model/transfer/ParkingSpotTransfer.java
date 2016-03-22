package com.cahue.model.transfer;

import com.cahue.model.FreeParkingSpot;
import com.cahue.model.ParkingSpot;
import com.cahue.util.BooleanAdapter;
import com.google.appengine.api.datastore.GeoPt;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

@XmlRootElement
public class ParkingSpotTransfer {

    private Date time = new Date();

    private Long id;

    private Double longitude;
    private Double latitude;
    private Float accuracy;

    private Date expiryTime;

    private boolean future = false;

    public ParkingSpotTransfer() {
    }

    public ParkingSpotTransfer(FreeParkingSpot original) {
        time = original.getTime();
        id = original.getId();
        longitude = Double.valueOf(original.getLocation().getLongitude());
        latitude = Double.valueOf(original.getLocation().getLatitude());
        accuracy = original.getAccuracy();
    }

    /**
     * Get the equivalent parking spot
     *
     * @return
     */
    public FreeParkingSpot createSpot() {
        FreeParkingSpot spot = new FreeParkingSpot();
        spot.setTime(time);
        spot.setId(id);
        spot.setLocation(new GeoPt(latitude.floatValue(), longitude.floatValue()));
        spot.setAccuracy(accuracy);
        return spot;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
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

    @Override
    public String toString() {
        return "ParkingSpotIndexEntry{" +
                "id=" + id +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", accuracy=" + accuracy +
                ", time=" + time +
                ", future=" + future +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParkingSpotTransfer that = (ParkingSpotTransfer) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
