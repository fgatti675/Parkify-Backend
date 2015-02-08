package com.cahue.resources;

import com.cahue.model.ParkingSpot;
import com.cahue.model.User;
import com.cahue.model.transfer.QueryResult;
import com.cahue.persistence.SpotsIndex;
import com.cahue.util.UserService;
import com.google.inject.name.Named;
import com.googlecode.objectify.Key;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.logging.Logger;

import static com.cahue.persistence.OfyService.ofy;

/**
 * Created by Francesco on 07/09/2014.
 */
@Path("/spots")
public class Spots {

    /**
     * Threshold for storing parking spots
     */
    private final static int ACCURACY_LIMIT_M = 25;

    Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    @Named(SpotsIndex.MySQL)
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
    public ParkingSpot put(ParkingSpot parkingSpot, @Context HttpHeaders headers) {

        if (parkingSpot.getAccuracy() > ACCURACY_LIMIT_M) {
            logger.fine("Spot received but too inaccurate : " + parkingSpot.getAccuracy() + " m.");
            return null;
        }

        User user = userService.getFromHeaders(headers);
        if (user != null) {
            logger.fine("Found user: " + user.getGoogleUser().getEmail());
        } else {
            // TODO: this will eventually need to crash
            logger.fine("User not found");
        }


        parkingSpot.setTime(new Date());

        /**
         * Save in datastore
         */
        Key<ParkingSpot> key = ofy().save().entity(parkingSpot).now();

        // TODO: needed?
//        parkingSpot.setId(key.getId());

        /**
         * Put in index database
         */
        spotsIndex.put(parkingSpot);

        logger.fine(parkingSpot.toString());

        return parkingSpot;
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
