package com.cahue.car2go;

/**
 * Created by f.gatti.gomez on 31/05/16.
 */
public class Car2GoLocation {

    private String locationName;

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Car2GoLocation vehicle = (Car2GoLocation) o;

        return locationName != null ? locationName.equals(vehicle.locationName) : vehicle.locationName == null;

    }

    @Override
    public int hashCode() {
        return locationName != null ? locationName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return locationName;
    }
}
