package com.cahue.model.transfer;

import com.cahue.model.Car;
import com.cahue.model.ParkingSpot;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Date: 20.03.15
 *
 * @author francesco
 */
@XmlRootElement
public class CarTransfer {

    private String id;

    private String btAddress;

    private String name;

    private Integer color;

    private Long spotId;

    private Double latitude;

    private Double longitude;

    private Float accuracy;

    public CarTransfer() {
    }

    public CarTransfer(Car car) {
        this(car, car.getSpot());
    }

    public CarTransfer(Car car, ParkingSpot spot) {
        id = car.getId();
        btAddress = car.getBtAddress();
        name = car.getName();
        color = car.getColor();

        if (spot != null) {
            spotId = spot.getId();
            latitude = spot.getLatitude();
            longitude = spot.getLongitude();
            accuracy = spot.getAccuracy();
        }
    }
    public Car getCar() {
        Car car = new Car();
        car.setId(id);
        car.setBtAddress(btAddress);
        car.setName(name);
        car.setColor(color);
        return car;
    }

    public ParkingSpot getSpot() {
        if (latitude != null && longitude != null) {
            ParkingSpot spot = new ParkingSpot();
            spot.setId(spotId);
            spot.setLatitude(latitude);
            spot.setLongitude(longitude);
            spot.setAccuracy(accuracy);
            return spot;
        } else {
            return null;
        }
    }

    /**
     * Parking time
     */
    private Date time;

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

    public Long getSpotId() {
        return spotId;
    }

    public void setSpotId(Long spotId) {
        this.spotId = spotId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
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

}
