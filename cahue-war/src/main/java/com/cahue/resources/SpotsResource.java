package com.cahue.resources;

import com.cahue.DataSource;
import com.cahue.api.Location;
import com.cahue.api.ParkingSpot;
import com.cahue.index.Index;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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

    /**
     * Get
     *
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ParkingSpot> get(
            @QueryParam("lat") Double latitude,
            @QueryParam("long") Double longitude,
            @QueryParam("range") Long range) {

        if (latitude == null || longitude == null || range == null)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());

        Set<ParkingSpot> spots = new HashSet<>();

        return spots;
    }

    /**
     * Store a new parking position
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public ParkingSpot put(Location location) {

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

        /**
         * Save in datastore
         */
        EntityManager em = dataSource.createDatastoreEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(parkingSpot);  // this sets the ID
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        /**
         * Save in index DB
         */
        EntityManager indexEm = dataSource.createRelationalEntityManager();
        try {
            indexEm.getTransaction().begin();
            indexEm.persist(parkingSpot);
            indexEm.getTransaction().commit();
        } finally {
            indexEm.close();
        }

        logger.fine(parkingSpot.toString());

        return parkingSpot;
    }
}
