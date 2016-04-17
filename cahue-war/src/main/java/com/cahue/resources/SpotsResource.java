package com.cahue.resources;

import com.cahue.auth.AuthenticationException;
import com.cahue.auth.UserService;
import com.cahue.index.ParkingSpotIndexEntry;
import com.cahue.index.SpotsIndex;
import com.cahue.model.FreeParkingSpot;
import com.cahue.model.ParkingSpot;
import com.cahue.model.User;
import com.cahue.model.transfer.ParkingSpotTransfer;
import com.cahue.model.transfer.QueryResult;
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
    private final static int MINIMUM_SPOT_ACCURACY = 30;

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
    public ParkingSpotTransfer put(ParkingSpotTransfer indexEntry) {

        if (indexEntry.getAccuracy() > MINIMUM_SPOT_ACCURACY) {
            logger.fine("Spot received but too inaccurate : " + indexEntry.getAccuracy() + " m.");
            throw new WebApplicationException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Minimum accuracy is: " + MINIMUM_SPOT_ACCURACY)
                    .build());
        }

        logger.fine("Index : " + indexEntry.toString());

        User user = null;
        try {
            user = userService.getCurrentUser();
            logger.fine(user.toString());
        } catch (AuthenticationException e) {
            // TODO: ok by now
        }

        Key<FreeParkingSpot> psKey = store(indexEntry);
        logger.fine("Stored key : " + psKey);

        return indexEntry;
    }


    /**
     * Store the parking spot both in the datastore and the index
     *
     * @param spotTransfer
     * @return
     */
    public Key<FreeParkingSpot> store(ParkingSpotTransfer spotTransfer) {

        /**
         * Update time
         */
        Calendar calendar = Calendar.getInstance();
        spotTransfer.setTime(calendar.getTime());

        /**
         * Save in datastore
         */
        FreeParkingSpot spot = spotTransfer.createSpot();
        Key<FreeParkingSpot> key = ofy().save().entity(spot).now();
        spotTransfer.setId(key.getId());

        /**
         * Put in index database
         */
        ParkingSpotIndexEntry indexEntry = new ParkingSpotIndexEntry();
        indexEntry.setAccuracy(spotTransfer.getAccuracy());
        indexEntry.setExpiryTime(spotTransfer.getExpiryTime());
        indexEntry.setFuture(spotTransfer.isFuture());
        indexEntry.setId(spotTransfer.getId());
        indexEntry.setLatitude(spotTransfer.getLatitude());
        indexEntry.setLongitude(spotTransfer.getLongitude());
        if (spotTransfer.isFuture()) {
            calendar.add(Calendar.MINUTE, SpotsIndex.FUTURE_SPOT_TIMEOUT_M);
            spotTransfer.setExpiryTime(calendar.getTime());
        } else {
            calendar.add(Calendar.MINUTE, SpotsIndex.SPOT_TIMEOUT_M);
            spotTransfer.setExpiryTime(calendar.getTime());
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
