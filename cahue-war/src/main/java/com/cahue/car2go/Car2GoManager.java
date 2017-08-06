package com.cahue.car2go;

import javax.inject.Singleton;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Created by f.gatti.gomez on 13/12/2016.
 */
@Singleton
public class Car2GoManager {

    public Map<String, Set<Car2GoVehicle>> currentVehiclesMap = new HashMap<>();

    public Set<Car2GoLocation> getLocations() {

        Car2GoLocationResult response = ClientBuilder.newClient().target("https://www.car2go.com/api/v2.1/locations")
                .queryParam("oauth_consumer_key", "car2gowebsite")
                .queryParam("format", "json")
                .request(MediaType.APPLICATION_JSON)
                .get(Car2GoLocationResult.class);

        return response.getLocation();

    }

    public Set<Car2GoVehicle> getVehicles(String location) {

        Car2GoVehicleResult response = ClientBuilder.newClient().target("https://www.car2go.com/api/v2.1/vehicles")
                .queryParam("loc", location)
                .queryParam("oauth_consumer_key", "car2gowebsite")
                .queryParam("format", "json")
                .request(MediaType.APPLICATION_JSON)
                .get(Car2GoVehicleResult.class);

        return response.getPlacemarks();

    }

    public Set<Car2GoVehicle> getMovedVehicles(String location) {

        Date now = new Date();

        Set<Car2GoVehicle> movedCars = new HashSet<>();

        Set<Car2GoVehicle> currentVehicles = getVehicles(location);
        currentVehicles.addAll(getVehicles(location));
        currentVehicles.addAll(getVehicles(location));
        currentVehicles.addAll(getVehicles(location));
        currentVehicles.addAll(getVehicles(location));
//        System.out.println("curr "  + currentVehicles);
        Set<Car2GoVehicle> previousVehicles = currentVehiclesMap.get(location);
        if (previousVehicles != null) {
            for (Car2GoVehicle prevVehicle : previousVehicles) {
                if (!currentVehicles.contains(prevVehicle)) {
                    movedCars.add(prevVehicle);
                }
            }

        }

        currentVehiclesMap.put(location, currentVehicles);

        return movedCars;
    }

    public static class Car2GoVehicleResult {

        private Set<Car2GoVehicle> placemarks;

        public Set<Car2GoVehicle> getPlacemarks() {
            return placemarks;
        }

        public void setPlacemarks(Set<Car2GoVehicle> placemarks) {
            this.placemarks = placemarks;
        }
    }

    public static class Car2GoLocationResult {

        private Set<Car2GoLocation> location;

        public Set<Car2GoLocation> getLocation() {
            return location;
        }

        public void setLocation(Set<Car2GoLocation> location) {
            this.location = location;
        }
    }
}
