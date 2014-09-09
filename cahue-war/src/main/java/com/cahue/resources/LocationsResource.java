package com.cahue.resources;

import com.cahue.DataSource;
import com.cahue.api.Location;
import com.cahue.api.ParkingQuery;
import com.cahue.entities.ParkingLocation;
import com.google.appengine.api.search.*;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Francesco on 07/09/2014.
 */
@Path("/loc")
public class LocationsResource {

    private final static String LOCATIONS_INDEX = "locations";

    @Inject
    DataSource dataSource;

    /**
     * Get
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String get(ParkingQuery parkingQuery) {
        

        return ("Hey there");
    }

    /**
     * Store a new parking position
     */
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/put")
    public Response put(Location location) {

        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();

        ParkingLocation parkingLocation = new ParkingLocation();
        parkingLocation.setLatitude(latitude);
        parkingLocation.setLongitude(longitude);

        // Save in datastore
        EntityManager em = dataSource.createEntityManager();
        em.getTransaction().begin();
        em.persist(parkingLocation);
        em.getTransaction().commit();

        // Save in Index
        GeoPoint geoPoint = new GeoPoint(latitude, longitude)  ;
        Document doc = Document.newBuilder()
                .setId(parkingLocation.getId().toString())
                .addField(Field.newBuilder().setName("location").setGeoPoint(geoPoint))
                .build();

        IndexSpec indexSpec = IndexSpec.newBuilder().setName(LOCATIONS_INDEX).build();
        Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
        try {
            index.put(doc);
        } catch (PutException e) {
            if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
                // retry putting the document
            }
        }

        return Response.status(Response.Status.OK).build();
    }
}
