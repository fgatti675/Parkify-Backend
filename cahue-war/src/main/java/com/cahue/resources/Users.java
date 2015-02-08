package com.cahue.resources;

import com.cahue.model.transfer.RegistrationRequestBean;
import com.cahue.model.transfer.RegistrationResult;
import com.cahue.util.UserService;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * Date: 12.09.14
 *
 * @author francesco
 */
@Path("/users")
public class Users {

    @Inject
    UserService userService;

    Logger logger = Logger.getLogger(getClass().getName());

    @POST
    @Path("/createGoogle")
    public RegistrationResult createGoogleUser(RegistrationRequestBean registration) {

        if (registration == null)
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("A device registration ID must be set to register.")
                            .build());

        RegistrationResult result = userService.register(registration);

        logger.info("Registered result: " + result.getUser());

        return result;
    }


}
