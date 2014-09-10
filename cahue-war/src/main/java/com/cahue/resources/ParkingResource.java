package com.cahue.resources;

import com.cahue.DataSource;
import com.cahue.api.ParkingLocation;
import com.google.appengine.api.search.*;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Francesco on 07/09/2014.
 */
@Path("/parking")
public class ParkingResource {

    private final static String PARKING_INDEX = "parkings";
    private final static String LOCATION_FIELD = "location";

    private final static int MAX_RESULTS = 50;

    private IndexSpec indexSpec = IndexSpec.newBuilder().setName(PARKING_INDEX).build();

    @Inject
    DataSource dataSource;

    /**
     * Get
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ParkingLocation> get(
            @QueryParam("lat") Double latitude,
            @QueryParam("long") Double longitude,
            @QueryParam("range") Long range) {

        if(latitude == null || longitude == null || range == null)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());

        /**
         * Query index first
         */
        String queryString = String.format("distance(%s, geopoint(%f, %f)) < %s",
                LOCATION_FIELD,
                latitude,
                longitude,
                range);

        QueryOptions options = QueryOptions.newBuilder()
                .setLimit(MAX_RESULTS)
                .build();

        Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

        Query query = Query.newBuilder().setOptions(options).build(queryString);
        Results<ScoredDocument> documents = index.search(query);

        /**
         * Query datastore with the results from the Index
         */
        EntityManager em = dataSource.createEntityManager();
        List<ParkingLocation> locations = new ArrayList<>();
        for(ScoredDocument document:documents){
            locations.add(em.find(ParkingLocation.class, Long.parseLong(document.getId())));
        }

        //noinspection JpaQlInspection
        return locations;
    }

    /**
     * Store a new parking position
     */
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    public Response put(ParkingLocation parkingLocation) {

        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

        Double latitude = parkingLocation.getLatitude();
        Double longitude = parkingLocation.getLongitude();


        // Save in datastore
        EntityManager em = dataSource.createEntityManager();
        em.getTransaction().begin();
        em.persist(parkingLocation);
        em.getTransaction().commit();

        // Save in Index
        GeoPoint geoPoint = new GeoPoint(latitude, longitude)  ;
        Document doc = Document.newBuilder()
                .setId(parkingLocation.getId().toString())
                .addField(Field.newBuilder().setName(LOCATION_FIELD).setGeoPoint(geoPoint))
                .build();

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
