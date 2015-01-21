package com.cahue.resources;

import com.cahue.model.User;
import com.cahue.model.transfer.RegistrationBean;
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

    Logger logger = Logger.getLogger(getClass().getName());

    @POST
    @Path("/createGoogle")
    public User createGoogleUser(RegistrationBean registration, @Context HttpHeaders headers) {

        if (registration == null)
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("A device registration ID must be set to register.")
                            .build());

        User user = userService.register(registration);

        if (user == null)
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("A header named GoogleAuth is compulsory. It must contain a valid Google access token.")
                            .build());

        logger.info("Logged in user: " + user);


        return user;
    }


}