package com.cahue.resources;

import com.cahue.gcm.GCMSender;
import com.cahue.model.Car;
import com.cahue.model.User;
import com.cahue.persistence.OfyService;
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
            throw new WebApplicationException("Every car must have a name assigned");
        }

        return retrieveUserCars(user);

    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public List<Car> save(List<Car> cars, @Context HttpHeaders headers) {

        User user = userService.getFromHeaders(headers);
        if (user != null) {
            logger.fine("Found user: " + user.getGoogleUser().getEmail());
        } else {
            logger.warning("Auth info missing: " + headers.getHeaderString(UserService.AUTH_HEADER));
            throw new WebApplicationException(Response
                    .status(Response.Status.FORBIDDEN)
                    .entity("Auth info missing")
                    .build());
        }

        logger.info("Received cars: " + cars);

        return save(cars, user);
    }

    public List<Car> save(List<Car> cars, User owner) {

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


//    @POST
//    @Path("/send")
//    public void send() {
//        EntityManager em = dataSource.createDatastoreEntityManager();
//        for (Device device : em.createQuery("SELECT d from Device d", Device.class).getResultList()) {
//            try {
//                sender.sendGCMUpdate(device, MessageFactory.getSayHelloMessage());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
