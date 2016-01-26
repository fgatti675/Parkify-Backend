package com.cahue.model.transfer;

import com.cahue.model.Car;
import com.cahue.model.ParkingSpot;

import java.util.Date;

/**
 * Date: 20.03.15
 *
 * @author francesco
 */
public class CarTransfer {

    private String id;

    private String btAddress;

    private String name;

    private Integer color;

    private ParkingSpotTransfer spot;

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
            this.spot = new ParkingSpotTransfer();
            this.spot.setSpotId(spot.getId());
            this.spot.setLatitude(spot.getLatitude());
            this.spot.setLongitude(spot.getLongitude());
            this.spot.setAccuracy(spot.getAccuracy());
            this.spot.setTime(spot.getTime());
        }
    }

    public Car createOfyCar() {
        Car car = new Car();
        car.setId(id);
        car.setBtAddress(btAddress);
        car.setName(name);
        car.setColor(color);
        return car;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBtAddress() {
        return btAddress;
    }

    public void setBtAddress(String btAddress) {
        this.btAddress = btAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public ParkingSpotTransfer getSpot() {
        return spot;
    }

    public void setSpot(ParkingSpotTransfer spot) {
        this.spot = spot;
    }
}
