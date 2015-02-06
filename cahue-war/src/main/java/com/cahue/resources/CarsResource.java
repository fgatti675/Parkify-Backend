package com.cahue.resources;

import com.cahue.gcm.MessageFactory;
import com.cahue.model.Car;
import com.cahue.model.Device;
import com.cahue.model.User;
import com.cahue.persistence.DataSource;
import com.cahue.util.UserService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Francesco
 */
@Path("/cars")
public class CarsResource {

    Logger logger = Logger.getLogger(getClass().getName());

//    @Inject
//    GCMSender sender;

    @Inject
    UserService userService;

    @Inject
    DataSource dataSource;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Set<Car> retrieve(@Context HttpHeaders headers) {

        EntityManager em = dataSource.createDatastoreEntityManager();

        try {

            User user = userService.getFromHeaders(em, headers);
            if (user != null) {
                logger.fine("Found user: " + user.getEmail());
            } else {
                throw new WebApplicationException("Every car must have a name assigned");
            }

            return user.getCars();

        }  finally {
            em.close();
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public void save(List<Car> cars, @Context HttpHeaders headers) {
        EntityManager em = dataSource.createDatastoreEntityManager();

        try {

            User user = userService.getFromHeaders(em, headers);
            if (user != null) {
                logger.fine("Found user: " + user.getEmail());
            } else {
                throw new WebApplicationException("Every car must have a name assigned");
            }

            save(em, cars, user);

        } finally {
            em.close();
        }
    }

    public void save(EntityManager em, List<Car> cars, User user) {
        em.getTransaction().begin();

        for (Car car : cars) {

            // check the car belongs to this user
            if (!car.getUser().equals(user))
                throw new WebApplicationException("This car doesn't belong to this user.");

            car.setUser(user);
            user.getCars().add(car);

            em.merge(car);
        }

        em.getTransaction().commit();
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
