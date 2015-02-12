package com.cahue.resources;

import com.cahue.gcm.GCMSender;
import com.cahue.model.Car;
import com.cahue.model.Device;
import com.cahue.model.User;
import com.cahue.persistence.OfyService;
import com.cahue.util.AuthenticationException;
import com.cahue.util.UserService;
import com.googlecode.objectify.Key;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * @author Francesco
 */
@Path("/cars")
public class CarsResource {

    Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    GCMSender sender;

    @Inject
    UserService userService;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<Car> retrieve(@Context HttpHeaders headers) {

        User user = userService.getFromHeaders(headers);
        if (user != null) {
            logger.fine("Found user: " + user.getGoogleUser().getEmail());
        } else {
            logger.warning("Auth info missing: " + headers.getHeaderString(UserService.AUTH_HEADER));
            throw new AuthenticationException();
        }

        return retrieveUserCars(user);

    }

    @DELETE
    @Path("/{car}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response delete(@PathParam(value = "car") String carId, @Context HttpHeaders headers) {

        User user = userService.getFromHeaders(headers);
        if (user != null) {
            logger.fine("Found user: " + user.getGoogleUser().getEmail());
        } else {
            logger.warning("Auth info missing: " + headers.getHeaderString(UserService.AUTH_HEADER));
            throw new AuthenticationException();
        }

        ofy().delete().type(Car.class).parent(user).id(carId).now();
        return Response.ok().entity(carId).build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public List<Car> save(List<Car> cars, @Context HttpHeaders headers) {

        User user = userService.getFromHeaders(headers);
        if (user != null) {
            logger.fine("Found user: " + user.getGoogleUser().getEmail());
        } else {
            logger.warning("Auth info missing: " + headers.getHeaderString(UserService.AUTH_HEADER));
            throw new AuthenticationException();
        }

        logger.info("Received cars: " + cars);

        save(cars, user);

        List<Car> retrievedCars = retrieveUserCars(user);

        List<Device> devices = userService.getDevices(user);
        // TODO: remove the device this request was created from
        sender.notifyCarsUpdate(devices, cars);

        return retrievedCars;
    }

    public void save(List<Car> cars, User owner) {
        for (Car car : cars) car.setUser(owner);
        ofy().save().entities(cars).now();
    }

    @Deprecated
    public List<Car> saveAndGetOutdated(List<Car> cars, User owner) {

        for (Car car : cars) car.setUser(owner);

        List<Car> toBeSaved = new ArrayList<>();
        List<Car> outdated = new ArrayList();

        Map<Key<Car>, Car> storedCars = ofy().load().entities(cars);
        for (Car car : cars) {
            Car previous = storedCars.get(car.createKey());
            /**
             * New entry is older
             */
            if (previous.getTime() == null || car.getTime().after(previous.getTime())) toBeSaved.add(car);
            /**
             * Previous entry is older so new is outdated
             */
            else outdated.add(previous);
        }
        ofy().save().entities(toBeSaved).now();
        return outdated;
    }


    public List<Car> retrieveUserCars(User user) {
        return ofy().load().type(Car.class).ancestor(user).list();
    }
}
