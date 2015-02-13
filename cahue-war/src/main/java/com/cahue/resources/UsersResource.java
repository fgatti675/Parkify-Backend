package com.cahue.resources;

import com.cahue.auth.AuthenticationService;
import com.cahue.model.Car;
import com.cahue.model.Device;
import com.cahue.model.User;
import com.cahue.model.transfer.RegistrationRequestBean;
import com.cahue.model.transfer.RegistrationResult;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Date: 12.09.14
 *
 * @author francesco
 */
@Path("/users")
public class UsersResource {


    @Inject
    AuthenticationService authenticationService;

    Logger logger = Logger.getLogger(getClass().getName());

    @POST
    @Path("/createGoogle")
    public RegistrationResult createGoogleUser(RegistrationRequestBean registration) {

        if (registration == null)
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("The registration request is not correct.")
                            .build());

        RegistrationResult result = register(registration);

        return result;
    }


    /**
     * Register a new user from a {@link com.cahue.model.transfer.RegistrationRequestBean}
     *
     * @param registration
     * @return
     */
    public RegistrationResult register(RegistrationRequestBean registration) {

        RegistrationResult result = new RegistrationResult();

        // create or retrieve an existing Google user
        User user = authenticationService.retrieveGoogleUser(registration.getGoogleAuthToken());
        result.setUser(user);

        // register the device
        registerDevice(registration.getDeviceRegId(), user);

        // create new Auth Token
        String authToken = authenticationService.generateToken();

        // store the token
        authenticationService.storeAuthToken(user, authToken);

        // store the token as a transient property in the user so it can be returned to the client
        result.setAuthToken(authToken);
        result.setRefreshToken(user.getRefreshToken());

        // set cars
        List<Car> cars = ofy().load().type(Car.class).ancestor(user).list();
        result.setCars(cars);
        logger.info("Registered result: " + result.getUser());

        return result;
    }

    /**
     * Register a new device and assign it to a user
     *
     * @param deviceRegistrationId
     * @param user
     */
    private void registerDevice(String deviceRegistrationId, User user) {
        Device device = Device.createDevice(deviceRegistrationId, user);
        ofy().save().entity(device).now();
    }



}
