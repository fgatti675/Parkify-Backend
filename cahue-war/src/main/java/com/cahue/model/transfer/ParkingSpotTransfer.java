package com.cahue.model.transfer;

import com.cahue.model.ParkingSpot;
import java.util.Date;

public class ParkingSpotTransfer {

    private Long spotId;

    private Double latitude;

    private Double longitude;

    private Float accuracy;

    /**
     * Parking time
     */
    private Date time;

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

    public ParkingSpot createOfySpot() {
        if (latitude != null && longitude != null) {
            ParkingSpot spot = new ParkingSpot();
            spot.setId(spotId);
            spot.setLatitude(latitude);
            spot.setLongitude(longitude);
            spot.setAccuracy(accuracy);
            spot.setTime(time);
            return spot;
        } else {
            return null;
        }
    }
}
