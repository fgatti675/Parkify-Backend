package com.cahue.resources;

import com.cahue.model.User;
import com.cahue.util.UserService;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * Date: 12.09.14
 *
 * @author francesco
 */
@Path("/users")
public class UsersResource {

    @Inject
    UserService userService;

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    @POST
    @Path("/createGoogle")
    public User createGoogleUser(@Context HttpHeaders headers) {

        User user = userService.getFromHeaders(headers);

        if (user == null)
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("A header named GoogleAuth is compulsory. It must contain a valid Google access token.")
                            .build());

        return user;
    }


}
