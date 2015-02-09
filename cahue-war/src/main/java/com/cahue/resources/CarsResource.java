package com.cahue.resources;

import com.cahue.gcm.GCMSender;
import com.cahue.model.Car;
import com.cahue.model.User;
import com.cahue.persistence.OfyService;
import com.cahue.util.UserService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.List;
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
    public void save(List<Car> cars, @Context HttpHeaders headers) {

            User user = userService.getFromHeaders(headers);
            if (user != null) {
                logger.fine("Found user: " + user.getGoogleUser().getEmail());
            } else {
                throw new WebApplicationException("Every car must have a name assigned");
            }

            save(cars, user);

    }

    public void save(List<Car> cars, User user) {

        for (Car car : cars) {

            // check the car belongs to this user
            if (!car.getUser().equals(user))
                throw new WebApplicationException("This car doesn't belong to this user.");

        }
        ofy().save().entities(cars).now();
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
