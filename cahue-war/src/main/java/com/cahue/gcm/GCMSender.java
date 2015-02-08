package com.cahue.gcm;

import com.cahue.model.Device;
import com.cahue.model.GoogleUser;
import com.cahue.model.User;
import com.google.android.gcm.server.*;
import com.googlecode.objectify.Key;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * @author Francesco
 */
public class GCMSender {

    /**
     * Google Api key for accessing data not associated with an account
     */
    public static final String GOOGLE_API_KEY = "AIzaSyCsqv-HW3DNxV8wsTyFqFu8ucS9gDhD9vw";

    private static final Logger log = Logger.getLogger(GCMSender.class.getName());

    private Sender sender = new Sender(GOOGLE_API_KEY);


    /**
     * Unregisters a device.
     *
     * @param device
     */
    public void unregister(Device device) {
        log.fine("Unregistering " + device);
        if (device != null)
            ofy().delete().entity(device).now();
    }

    /**
     * Gets all registered devices.
     */
    public List<Device> getDevices() {
        return ofy().load().type(Device.class).list();
    }

    /**
     * Send an update request to the device. This way we tell it that there is new information in the server that should be retrieved.
     *
     * @param device
     * @return
     * @throws java.io.IOException
     */
    public void sendGCMUpdate(Device device, Message message) throws IOException {
        String registrationId = device.getRegId();

        Result result = sender.send(message, registrationId, 5);

        if (result.getMessageId() == null) return;

        String canonicalRegistrationId = result.getCanonicalRegistrationId();

        // Update reg id if there was a change
        if (canonicalRegistrationId != null)
            updateRegistration(registrationId, canonicalRegistrationId);
    }

    public void sendGCMMultiUpdate(Collection<Device> devices, Message message) {
        MulticastResult multicastResult;

        List<String> regIds = new ArrayList<String>();
        Map<String, Device> devicesMap = new HashMap();
        for (Device d : devices) {
            regIds.add(d.getRegId());
            devicesMap.put(d.getRegId(), d);
        }

        try {
            multicastResult = sender.send(message, regIds, 5);
        } catch (IOException e) {
            log.severe("Error posting messages");
            e.printStackTrace();
            return;
        }

        List<Result> results = multicastResult.getResults();
        // analyze the results
        for (int i = 0; i < regIds.size(); i++) {
            String regId = regIds.get(i);
            Result result = results.get(i);
            String messageId = result.getMessageId();
            if (messageId != null) {
                log.fine("Succesfully sent message to device: " + regId +
                        "; messageId = " + messageId);
                String canonicalRegId = result.getCanonicalRegistrationId();
                if (canonicalRegId != null) {
                    // same devices has more than on registration id: update it
                    log.info("canonicalRegId " + canonicalRegId);
                    updateRegistration(regId, canonicalRegId);
                }
            } else {
                String error = result.getErrorCodeName();
                if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                    // application has been removed from devices - unregister it
                    log.info("Unregistered device: " + regId);
                    unregister(devicesMap.get(regId));
                } else {
                    log.severe("Error sending message to " + regId + ": " + error);
                }
            }
        }
    }

    /**
     * Updates the registration id of a device.
     */
    public void updateRegistration(String oldId, String newId) {
        log.info("Updating " + oldId + " to " + newId);
        Device oldDevice = ofy().load().key(Key.create(Device.class, oldId)).now();
        User user = oldDevice.getUser();
        Device device = Device.createDevice(newId, user);
        ofy().save().entity(device).now();
        ofy().clear();
    }
}
