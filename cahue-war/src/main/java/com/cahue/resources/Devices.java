package com.cahue.resources;

import com.cahue.gcm.GCMSender;
import com.cahue.util.UserService;

import javax.inject.Inject;
import javax.ws.rs.Path;

/**
 * @author Francesco
 */
@Path("/devices")
public class Devices {

    @Inject
    GCMSender sender;

    @Inject
    UserService userService;


//    @POST
//    @Path("/send")
//    public void send() {
//        for (Device device : em.createQuery("SELECT d from Device d", Device.class).getResultList()) {
//            try {
//                sender.sendGCMUpdate(device, MessageFactory.getSayHelloMessage());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
