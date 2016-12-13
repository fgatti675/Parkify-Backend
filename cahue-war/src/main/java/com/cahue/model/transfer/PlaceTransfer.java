package com.cahue.model.transfer;

import com.cahue.model.Place;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by f.gatti.gomez on 31/05/16.
 */
@XmlRootElement
public class PlaceTransfer {

    public PlaceTransfer() {
    }

    public PlaceTransfer(Place place) {
        this.latitude = place.getLatitude();
        this.longitude = place.getLongitude();
        this.name = place.getName();
        this.address = place.getAddress();
        this.id = place.getId();
    }

    private String id;

    private Double latitude;

    private Double longitude;

    private String name;

    private String address;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Place{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
