package com.cahue.resources;

import com.cahue.model.Place;
import com.cahue.model.transfer.PlaceTransfer;
import com.cahue.model.transfer.PlacesResultTransfer;
import com.cahue.places.PlacesManager;
import com.cahue.places.PlacesResult;

import javax.inject.Inject;
import javax.ws.rs.*;
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
    public PlacesResultTransfer getPlaces(
            @QueryParam("lat") Double latitude,
            @QueryParam("long") Double longitude,
            @QueryParam("radius") @DefaultValue("5000") int radius) {

        if (latitude == null || longitude == null)
            throw new RuntimeException("Places request: Specify lat and long parameters");

        PlacesResultTransfer result = new PlacesResultTransfer();
        List<PlaceTransfer> placeTransfers = new ArrayList<>();
        PlacesResult places = manager.getPlaces(latitude, longitude, radius);
        for (Place place : places.getPlaces()) {
            placeTransfers.add(new PlaceTransfer(place));
        }
        result.setPlaces(placeTransfers);
        result.setMoreResults(places.isMoreResults());

        return result;
    }

}
