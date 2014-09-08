package com.cahue.resources;

import com.cahue.DAO;
import com.cahue.DataSource;
import com.cahue.TestClass;
import com.cahue.entities.Location;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Francesco on 07/09/2014.
 */
@Path("/loc")
public class LocationsResource {

    @Inject
    DAO dao;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return ("Hey there");
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/put")
    public Response put(Location location) {
        dao.saveLocation(location);
        return Response.status(Response.Status.OK).build();
    }
}
