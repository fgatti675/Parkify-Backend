package com.cahue.places;

import com.cahue.config.Constants;
import com.cahue.model.Place;
import com.google.appengine.repackaged.com.google.gson.JsonElement;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.appengine.repackaged.com.google.gson.JsonParser;

import javax.inject.Singleton;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by f.gatti.gomez on 23/05/16.
 */
@Singleton
public class PlacesManager {

    public static final JsonParser PARSER = new JsonParser();

    public List<Place> getPlaces(Double latitude, Double longitude, int radius) {

        String response = ClientBuilder.newClient().target("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                .queryParam("location", String.format("%f,%f", latitude, longitude))
                .queryParam("radius", radius)
                .queryParam("types", "parking")
                .queryParam("key", Constants.GOOGLE_API_KEY)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        JsonObject result = PARSER.parse(response).getAsJsonObject();
        List<Place> places = getPlacesFromJson(result);

        return places;
    }

    private List<Place> getPlacesFromJson(JsonObject jsonRoot) {
        List<Place> places = new ArrayList<>();
        for (JsonElement jsonElement : jsonRoot.getAsJsonArray("results")) {
            JsonObject jsonPlace = jsonElement.getAsJsonObject();
            Place place = new Place();
            place.setId(jsonPlace.get("place_id").getAsString());
            JsonObject location = jsonPlace.getAsJsonObject("geometry").getAsJsonObject("location");
            place.setLatitide(location.get("lat").getAsDouble());
            place.setLongitude(location.get("lng").getAsDouble());
            place.setName(jsonPlace.get("name").getAsString());
            place.setAddress(jsonPlace.get("vicinity").getAsString());
            places.add(place);
        }
        return places;
    }

}
