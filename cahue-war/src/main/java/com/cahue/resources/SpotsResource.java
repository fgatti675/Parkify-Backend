package com.cahue.resources;

import com.cahue.auth.AuthenticationException;
import com.cahue.model.Car;
import com.cahue.model.ParkingSpot;
import com.cahue.model.User;
import com.cahue.model.transfer.QueryResult;
import com.cahue.persistence.SpotsIndex;
import com.cahue.auth.UserService;
import com.googlecode.objectify.Key;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * Created by Francesco on 07/09/2014.
 */
@Path("/spots")
public class SpotsResource {

    private final Integer SPOT_TIMEOUT_H = 2; // 2 hours

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
    public ParkingSpot put(ParkingSpot parkingSpot) {

        if (parkingSpot.getAccuracy() > MINIMUM_SPOT_ACCURACY) {
            logger.fine("Spot received but too inaccurate : " + parkingSpot.getAccuracy() + " m.");
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

        Car car = parkingSpot.getCar();
        if (car != null) {
            logger.fine("Car : " + car);
            car.setUser(user);
            parkingSpot.setCar(car);
        }

        Key<ParkingSpot> psKey = store(parkingSpot);

        ParkingSpot stored = ofy().load().key(psKey).now();
        logger.fine("Stored : " + stored);

        return parkingSpot;
    }

    /**
     * Store the parking spot both in the datastore and the index
     *
     * @param parkingSpot
     * @return
     */
    public Key<ParkingSpot> store(ParkingSpot parkingSpot) {

        Calendar calendar = Calendar.getInstance();
        parkingSpot.setTime(calendar.getTime());
        calendar.add(Calendar.HOUR, -SPOT_TIMEOUT_H);
        parkingSpot.setExpiryTime(calendar.getTime());

        /**
         * Save in datastore
         */
        Key<ParkingSpot> key = ofy().save().entity(parkingSpot).now();

        /**
         * Put in index database
         */
        spotsIndex.put(parkingSpot);

        logger.fine(parkingSpot.toString());

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
