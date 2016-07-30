package com.cahue;

import com.cahue.model.Place;
import com.cahue.places.PlacesManager;
import org.junit.Test;

import java.util.List;

/**
 * Created by f.gatti.gomez on 31/05/16.
 */
public class PlacesTest {

    @Test
    public void placesTest() {
        PlacesManager manager = new PlacesManager();
        List<Place> places = manager.getPlaces(48.1351, 11.5820, 50000);
        for (Place place : places) {
            System.out.println(place);
        }
        System.out.println(places.size());
    }
}
