package com.cahue.resources;

import com.cahue.auth.AuthenticationException;
import com.cahue.index.ParkingSpotIndexEntry;
import com.cahue.model.Car;
import com.cahue.model.ParkingSpot;
import com.cahue.model.User;
import com.cahue.model.transfer.QueryResult;
import com.cahue.index.SpotsIndex;
import com.cahue.auth.UserService;
import com.googlecode.objectify.Key;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * Created by Francesco on 07/09/2014.
 */
@Path("/spots")
public class SpotsResource {

    /**
     * Accuracy threshold for storing parking spots, in meters
     */
    private final static int MINIMUM_SPOT_ACCURACY = 25;

    Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    SpotsIndex spotsIndex;

    @Inject
    UserService userService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public QueryResult getArea(
            @QueryParam("swLat") Double southwestLatitude,
            @QueryParam("swLong") Double southwestLongitude,
            @QueryParam("neLat") Double northeastLatitude,
            @QueryParam("neLong") Double northeastLongitude) {

        if (southwestLatitude == null || southwestLongitude == null || northeastLatitude == null || northeastLongitude == null)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());

        return spotsIndex.queryArea(southwestLatitude, southwestLongitude, northeastLatitude, northeastLongitude);
    }


    /**
     * Store a new parking position
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    public ParkingSpotIndexEntry put(ParkingSpotIndexEntry parkingSpotIndexEntry) {

        if (parkingSpotIndexEntry.getAccuracy() > MINIMUM_SPOT_ACCURACY) {
            logger.fine("Spot received but too inaccurate : " + parkingSpotIndexEntry.getAccuracy() + " m.");
            throw new WebApplicationException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Minimum accuracy is: " + MINIMUM_SPOT_ACCURACY)
                    .build());
        }

        User user = null;
        try {
            user = userService.getCurrentUser();
            logger.fine(user.getGoogleUser().toString());
        } catch (AuthenticationException e) {
            // TODO: ok by now
        }

        Key<ParkingSpot> psKey = store(parkingSpotIndexEntry);

        ParkingSpot stored = ofy().load().key(psKey).now();
        logger.fine("Stored : " + stored);

        return parkingSpotIndexEntry;
    }


    /**
     * Store the parking spot both in the datastore and the index
     *
     * @param indexEntry
     * @return
     */
    public Key<ParkingSpot> store(ParkingSpotIndexEntry indexEntry) {
        /**
         * Update time
         */
        Calendar calendar = Calendar.getInstance();
        indexEntry.setTime(calendar.getTime());

        /**
         * Save in datastore
         */
        ParkingSpot spot = indexEntry.createSpot();
        Key<ParkingSpot> key = ofy().save().entity(spot).now();
        indexEntry.setId(key.getId());
        logger.fine(spot.toString());

        /**
         * Put in index database
         */
        if (indexEntry.isFuture()) {
            calendar.add(Calendar.MINUTE, SpotsIndex.FUTURE_SPOT_TIMEOUT_M);
            indexEntry.setExpiryTime(calendar.getTime());
        } else {
            calendar.add(Calendar.MINUTE, SpotsIndex.SPOT_TIMEOUT_M);
            indexEntry.setExpiryTime(calendar.getTime());
        }
        spotsIndex.put(indexEntry);

        return key;
    }

    @GET
    @Path("/nearest")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public QueryResult getNearest(
            @QueryParam("lat") Double latitude,
            @QueryParam("long") Double longitude,
            @QueryParam("count") Integer count) {

        if (latitude == null || longitude == null || count == null)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());

        return spotsIndex.queryNearest(latitude, longitude, count);
    }

    @GET
    @Path("/area")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public QueryResult getAreaLegacy(
            @QueryParam("swLat") Double southwestLatitude,
            @QueryParam("swLong") Double southwestLongitude,
            @QueryParam("neLat") Double northeastLatitude,
            @QueryParam("neLong") Double northeastLongitude) {

        return getArea(southwestLatitude, southwestLongitude, northeastLatitude, northeastLongitude);
    }

}
