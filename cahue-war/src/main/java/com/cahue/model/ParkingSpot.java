package com.cahue.model;

import com.google.appengine.api.datastore.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;

@Cache
@Entity
public class ParkingSpot {

    private Date time = new Date();

    @Id
    private long id;

    @Index
    @Load
    private Ref<Car> car;

    private Double longitude;
    private Double latitude;
    private Float accuracy;

    public ParkingSpot() {
    }

    @XmlTransient
    public long getId() {
		return id;
	}

	public void setId(long id) {
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
        return car.get();
    }

    public void setCar(Car car) {
        this.car = Ref.create(car);
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
