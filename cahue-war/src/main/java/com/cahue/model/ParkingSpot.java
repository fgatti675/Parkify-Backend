package com.cahue.model;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;

import javax.xml.bind.annotation.XmlTransient;
import java.beans.Transient;
import java.util.Date;

@Cache
@Entity
@javax.persistence.Entity
public class ParkingSpot {

    private Date time = new Date();

    @Id
    private Long id;

    @Index
    @Load
    private Ref<Car> car;

    private Double longitude;
    private Double latitude;
    private Float accuracy;

    public ParkingSpot() {
    }

    @javax.persistence.Id
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

    @javax.persistence.Temporal(value = javax.persistence.TemporalType.TIMESTAMP)
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @XmlTransient
    @javax.persistence.Transient
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParkingSpot that = (ParkingSpot) o;

        if (accuracy != null ? !accuracy.equals(that.accuracy) : that.accuracy != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (latitude != null ? !latitude.equals(that.latitude) : that.latitude != null) return false;
        if (longitude != null ? !longitude.equals(that.longitude) : that.longitude != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (accuracy != null ? accuracy.hashCode() : 0);
        return result;
    }
}
