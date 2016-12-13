package com.cahue.car2go;

import java.util.Set;

/**
 * Created by f.gatti.gomez on 31/05/16.
 */
public class Car2GoResult {

    private Set<Car2GoVehicle> placemarks;

    public Set<Car2GoVehicle> getPlacemarks() {
        return placemarks;
    }

    public void setPlacemarks(Set<Car2GoVehicle> placemarks) {
        this.placemarks = placemarks;
    }
}
