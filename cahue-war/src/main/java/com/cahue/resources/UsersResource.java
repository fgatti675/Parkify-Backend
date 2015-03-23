package com.cahue.resources;

import com.cahue.auth.UserAuthenticationService;
import com.cahue.model.Car;
import com.cahue.model.Device;
import com.cahue.model.User;
import com.cahue.model.transfer.CarTransfer;
import com.cahue.model.transfer.RegistrationRequestBean;
import com.cahue.model.transfer.RegistrationResult;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
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
    UserAuthenticationService userAuthenticationService;

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

    @GET
    @Path("/refresh")
    public String refresh(@QueryParam("user") Long userId, @QueryParam("refreshToken") String refreshToken) {

        User user = ofy().load().type(User.class).id(userId).now();

        if (user == null)
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("User not found.")
                            .build());

        if (!user.getRefreshToken().equals(refreshToken))
            throw new WebApplicationException(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Wrong refresh token")
                            .build());

        return createAuthToken(user);
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
        User user = userAuthenticationService.retrieveGoogleUser(registration.getGoogleAuthToken());
        result.setUser(user);

        // register the device
        registerDevice(registration.getDeviceRegId(), user);

        // generate a new auth token for the user
        String authToken = createAuthToken(user);

        // store the token as a transient property in the user so it can be returned to the client
        result.setAuthToken(authToken);
        result.setRefreshToken(user.getRefreshToken());

        // set cars
        List<Car> cars = ofy().load().type(Car.class).ancestor(user).list();
        List<CarTransfer> carTransfers = new ArrayList<>();
        for(Car car: cars) carTransfers.add(new CarTransfer(car));
        result.setCars(carTransfers);
        logger.info("Registered result: " + result.getUser());

        return result;
    }


    /**
     * Create and store an auth token for a user
     *
     * @param user
     * @return
     */
    private String createAuthToken(User user) {
        // create new Auth Token
        String authToken = userAuthenticationService.generateToken();

        // store the token
        userAuthenticationService.storeAuthToken(user, authToken);
        return authToken;
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
