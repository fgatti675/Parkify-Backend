package com.cahue.resources;

import com.cahue.gcm.GCMSender;
import com.cahue.gcm.MessageFactory;
import com.cahue.model.Car;
import com.cahue.model.Device;
import com.cahue.model.User;
import com.cahue.persistence.DataSource;
import com.cahue.util.UserService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Francesco
 */
@Path("/devices")
public class CarsResource {

    Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    GCMSender sender;

    @Inject
    UserService userService;

    @Inject
    DataSource dataSource;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void register(List<Car> cars, @Context HttpHeaders headers) {
        EntityManager em = dataSource.createDatastoreEntityManager();

        try {
            User user = userService.getFromHeaders(headers);
            if (user != null) {
                logger.info("Found user: " + user.getEmail());
            } else {
                logger.fine("User not found");
            }

            em.getTransaction().begin();

            for (Car car : cars) {

                if(car.getName() == null)
                    throw new WebApplicationException("Every car must have a name assigned");

                if(car.getId() == null) car.generateId();   // it's a new car

                car.updateKey();
                car.setUser(user);

                em.merge(car);

                user.getCars().add(car);
            }


            em.getTransaction().commit();

        } catch (InvalidTokenException e) {
            logger.warning("Invalid Token");
        }
    }


    @POST
    @Path("/send")
    public void send() {
        EntityManager em = dataSource.createDatastoreEntityManager();
        for (Device device : em.createQuery("SELECT d from Device d", Device.class).getResultList()) {
            try {
                sender.sendGCMUpdate(device, MessageFactory.getSayHelloMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
