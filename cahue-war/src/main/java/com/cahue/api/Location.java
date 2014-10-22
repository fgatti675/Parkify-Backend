package com.cahue.api;

/**
 * Date: 11.09.14
 *
 * @author francesco
 */
public class Location {

    private Double longitude;
    private Double latitude;
    private Float accuracy;

    public Location() {
    }

    public Location(Double longitude, Double latitude, Float accuracy) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.accuracy = accuracy;
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
}
