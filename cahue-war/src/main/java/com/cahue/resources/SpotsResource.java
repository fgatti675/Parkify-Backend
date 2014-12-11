package com.cahue.resources;

import com.cahue.CartoDBPersistence;
import com.cahue.DataSource;
import com.cahue.api.Location;
import com.cahue.api.ParkingSpot;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Francesco on 07/09/2014.
 */
@Path("/spots")
public class SpotsResource {

    /**
     * Threshold for storing parking spots
     */
    private final static int ACCURACY_LIMIT_M = 20;

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    @Inject
    DataSource dataSource;

    @Inject
    CartoDBPersistence cartoDBPersistence;

    @GET
    @Path("/nearest")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ParkingSpot> getNearest(
            @QueryParam("lat") Double latitude,
            @QueryParam("long") Double longitude,
            @QueryParam("count") Integer count) {

        if (latitude == null || longitude == null || count == null)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());

        return cartoDBPersistence.queryNearest(latitude, longitude, count);
    }

    @GET
    @Path("/area")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ParkingSpot> getArea(
            @QueryParam("swLat") Double southwestLatitude,
            @QueryParam("swLong") Double southwestLongitude,
            @QueryParam("neLat") Double northeastLatitude,
            @QueryParam("neLong") Double northeastLongitude) {

        if (southwestLatitude == null || southwestLongitude == null || northeastLatitude == null || northeastLongitude == null)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());

        return cartoDBPersistence.queryArea(southwestLatitude, southwestLongitude, northeastLatitude, northeastLongitude);
    }

    /**
     * Store a new parking position
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public ParkingSpot put(Location location, @Context HttpHeaders headers) {

        if (location.getAccuracy() > ACCURACY_LIMIT_M) {
            logger.fine("Spot received but too inaccurate : " + location.getAccuracy() + " m.");
            return null;
        }

        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

        ParkingSpot parkingSpot = new ParkingSpot();
        parkingSpot.setLatitude(location.getLatitude());
        parkingSpot.setLongitude(location.getLongitude());
        parkingSpot.setAccuracy(location.getAccuracy());
        parkingSpot.setTime(new Date());

        /**
         * Save in datastore
         */
        EntityManager em = dataSource.createEntityManager();
        em.getTransaction().begin();
        em.persist(parkingSpot);
        em.getTransaction().commit();

        /**
         * Put in cartoDBPersistence
         */
        cartoDBPersistence.put(parkingSpot);

        logger.fine(parkingSpot.toString());

        return parkingSpot;
    }
}
