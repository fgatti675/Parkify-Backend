package com.cahue.resources;

import com.cahue.gcm.GCMSender;
import com.cahue.gcm.MessageFactory;
import com.cahue.model.Device;
import com.cahue.model.User;
import com.cahue.persistence.DataSource;
import com.cahue.util.UserService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * @author Francesco
 */
@Path("/devices")
public class DeviceResource {

    @Inject
    GCMSender sender;

    @Inject
    UserService userService;

    @Inject
    DataSource dataSource;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void register(String regId, @Context HttpHeaders headers) {

        User user = userService.getFromHeaders(headers);

        registerNewDevice(regId, user);
    }

    public void registerNewDevice(String regId, User user) {
        EntityManager em = dataSource.createDatastoreEntityManager();

        em.getTransaction().begin();

        Device device = Device.createDevice(regId, user);
        em.persist(device);

        user.getDevices().add(device);

        em.getTransaction().commit();
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
