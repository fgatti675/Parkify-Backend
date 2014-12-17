package com.cahue.resources;

import com.cahue.persistence.DataSource;
import com.cahue.api.Location;
import com.cahue.api.ParkingSpot;
import com.cahue.datastore.ParkingSpotDS;
import com.cahue.persistence.Persistence;
import com.cahue.api.QueryResult;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.name.Named;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    @Named(Persistence.MySQL)
    Persistence persistence;

    @GET
    @Path("/nearest")
    @Produces(MediaType.APPLICATION_JSON)
    public QueryResult getNearest(
            @QueryParam("lat") Double latitude,
            @QueryParam("long") Double longitude,
            @QueryParam("count") Integer count) {

        if (latitude == null || longitude == null || count == null)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());

        return persistence.queryNearest(latitude, longitude, count);
    }

    @GET
    @Path("/area")
    @Produces(MediaType.APPLICATION_JSON)
    public QueryResult getArea(
            @QueryParam("swLat") Double southwestLatitude,
            @QueryParam("swLong") Double southwestLongitude,
            @QueryParam("neLat") Double northeastLatitude,
            @QueryParam("neLong") Double northeastLongitude) {

        if (southwestLatitude == null || southwestLongitude == null || northeastLatitude == null || northeastLongitude == null)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());

        return persistence.queryArea(southwestLatitude, southwestLongitude, northeastLatitude, northeastLongitude);
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

        ParkingSpotDS parkingSpotDS = new ParkingSpotDS();
        parkingSpotDS.setLatitude(location.getLatitude());
        parkingSpotDS.setLongitude(location.getLongitude());
        parkingSpotDS.setAccuracy(location.getAccuracy());
        parkingSpotDS.setTime(new Date());

        /**
         * Save in datastore
         */
        EntityManager em = dataSource.createDatastoreEntityManager();
        em.getTransaction().begin();
        em.persist(parkingSpotDS);
        em.getTransaction().commit();


        ParkingSpot parkingSpot = new ParkingSpot();
        parkingSpot.setId(parkingSpotDS.getId());
        parkingSpot.setLatitude(location.getLatitude());
        parkingSpot.setLongitude(location.getLongitude());
        parkingSpot.setAccuracy(location.getAccuracy());
        parkingSpot.setTime(new Date());
        /**
         * Put in cartoDBPersistence
         */
        persistence.put(parkingSpot);

        logger.fine(parkingSpot.toString());

        return parkingSpot;
    }
}
