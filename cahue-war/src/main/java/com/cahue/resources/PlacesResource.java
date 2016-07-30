package com.cahue.resources;

import com.cahue.model.Place;
import com.cahue.model.transfer.PlaceTransfer;
import com.cahue.places.PlacesManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by f.gatti.gomez on 31/05/16.
 */

@Path("/places")
public class PlacesResource {

    @Inject
    PlacesManager manager;

    @GET
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public List<PlaceTransfer> getPlaces(
            @QueryParam("latitude") Double latitude,
            @QueryParam("longitude") Double longitude,
            @QueryParam("radius") int radius) {

        List<PlaceTransfer> result = new ArrayList<>();
        for(Place place: manager.getPlaces(latitude, longitude, radius)){
            result.add(new PlaceTransfer(place));
        }

        return result;
    }

}
