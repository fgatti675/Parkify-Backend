package com.cahue.resources;

import com.cahue.model.QueryResult;
import com.cahue.model.User;
import com.cahue.persistence.DataSource;
import com.cahue.model.ParkingSpot;
import com.cahue.persistence.Persistence;
import com.cahue.util.UserService;
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
    private final static int ACCURACY_LIMIT_M = 25;

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    @Inject
    DataSource dataSource;

    @Inject
    @Named(Persistence.MySQL)
    Persistence persistence;

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

        return persistence.queryArea(southwestLatitude, southwestLongitude, northeastLatitude, northeastLongitude);
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

        User user = userService.getFromHeaders();
        if(user != null){
            logger.info("FOUND USER: " + user.getEmail());
        }

        parkingSpot.setTime(new Date());

        /**
         * Save in datastore
         */
        EntityManager em = dataSource.createDatastoreEntityManager();
        em.getTransaction().begin();
        em.persist(parkingSpot);  // sets the id
        em.getTransaction().commit();

        /**
         * Put in index database
         */
        persistence.put(parkingSpot);

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

        return persistence.queryNearest(latitude, longitude, count);
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
