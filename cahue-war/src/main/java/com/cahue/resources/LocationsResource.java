package com.cahue.resources;

import com.cahue.DataSource;
import com.cahue.entities.Location;
import com.google.appengine.api.search.*;

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

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return ("Hey there");
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/put")
    public Response put(Location location) {

        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();

        // Save in datastore
        EntityManager em = dataSource.createEntityManager();
        em.getTransaction().begin();
        em.persist(location);
        em.getTransaction().commit();

        // Save in Index
        GeoPoint geoPoint = new GeoPoint(latitude, longitude)  ;
        String documentId = location.getId().toString();
        Document doc = Document.newBuilder()
                .setId(documentId) // Setting the document identifer is optional. If omitted, the search service will create an identifier.
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
