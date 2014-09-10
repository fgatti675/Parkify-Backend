package com.cahue.api;

public class ParkingQuery {

    private Double longitude;
    private Double latitude;
    /**
     * Range in meters
     */
    private Long range;

    public ParkingQuery() {
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

    public Long getRange() {
        return range;
    }

    public void setRange(Long range) {
        this.range = range;
    }
}
