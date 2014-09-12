package com.cahue.resources;

import com.cahue.DataSource;
import com.cahue.api.Location;
import com.cahue.api.ParkingSpot;
import com.cahue.index.IndexManager;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by Francesco on 07/09/2014.
 */
@Path("/spots")
public class SpotsResource {


    @Inject
    DataSource dataSource;

    @Inject
    IndexManager indexManager;

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

        /**
         * Query index
         */
        Set<Long> ids = indexManager.query(latitude, longitude, range);

        /**
         * Query datastore with the results from the Index
         */
        EntityManager em = dataSource.createEntityManager();
        Set<ParkingSpot> spots = new HashSet<>();
        for (Long id : ids) {
            spots.add(em.find(ParkingSpot.class, id));
        }

        return spots;
    }

    /**
     * Store a new parking position
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public ParkingSpot put(Location location) {

        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

        ParkingSpot parkingSpot = new ParkingSpot();
        parkingSpot.setLatitude(location.getLatitude());
        parkingSpot.setLongitude(location.getLongitude());

        /**
         * Save in datastore
         */
        EntityManager em = dataSource.createEntityManager();
        em.getTransaction().begin();
        em.persist(parkingSpot);
        em.getTransaction().commit();

        /**
         * Put in index
         */
        indexManager.put(parkingSpot.getId(), location.getLatitude(), location.getLongitude(), parkingSpot.getTime());

        return parkingSpot;
    }
}
