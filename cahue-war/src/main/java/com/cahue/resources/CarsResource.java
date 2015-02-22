package com.cahue.resources;

import com.cahue.gcm.GCMMessageFactory;
import com.cahue.gcm.GCMSender;
import com.cahue.model.Car;
import com.cahue.model.Device;
import com.cahue.model.User;
import com.cahue.auth.UserService;
import com.googlecode.objectify.Key;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
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
    GCMMessageFactory messageFactory;

    @Inject
    UserService userService;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<Car> retrieve() {
        return userService.retrieveUserCars();
    }

    @DELETE
    @Path("/{carId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response delete(@PathParam(value = "carId") String carId, @Context HttpHeaders headers) {

        User user = userService.getCurrentUser();

        ofy().delete().type(Car.class).parent(user).id(carId).now();


        List<Device> devices = userService.getUserDevicesButCurrent();
        sender.sendGCMMultiUpdate(devices, messageFactory.getCarDeletedMessage(carId));

        return Response.ok().entity(carId).build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public Car save(Car car, @Context HttpHeaders headers) {

        User user = userService.getCurrentUser();

        logger.info("Received car: " + car);

        save(car, user);

        List<Device> devices = userService.getUserDevicesButCurrent();
        sender.sendGCMMultiUpdate(devices, messageFactory.getCarUpdateMessage(car));

        return car;
    }

    public void save(Car car, User owner) {
        car.setUser(owner);
        ofy().save().entity(car).now();
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


}
