package com.cahue.car2go;

/**
 * Created by f.gatti.gomez on 31/05/16.
 */
public class Car2GoVehicle {

    private String vin;

    private double[] coordinates;

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public double getLatitude(){
        return coordinates[1];
    }

    public double getLongitude(){
        return coordinates[0];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Car2GoVehicle vehicle = (Car2GoVehicle) o;

        return vin != null ? vin.equals(vehicle.vin) : vehicle.vin == null;

    }

    @Override
    public int hashCode() {
        return vin != null ? vin.hashCode() : 0;
    }

    @Override
    public String toString() {
        return vin;
    }
}
