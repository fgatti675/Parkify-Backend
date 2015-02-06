package com.cahue.model;

import com.google.appengine.api.datastore.Key;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
public class ParkingSpot {

    private Double longitude;
    private Double latitude;
    private Float accuracy;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date time = new Date();

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Key id;

    public ParkingSpot() {
    }

    @ManyToOne (fetch = FetchType.LAZY)
    private Car car;

    @XmlTransient
    public Key getId() {
		return id;
	}

	public void setId(Key id) {
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

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getTime() {
		return time;
	}

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    @Override
    public String toString() {
        return "ParkingSpot{" +
                "id=" + id +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", accuracy=" + accuracy +
                ", time=" + time +
                '}';
    }
}
