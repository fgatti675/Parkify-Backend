package com.cahue.auth;

import com.cahue.model.Car;
import com.cahue.model.Device;
import com.cahue.model.User;
import com.google.inject.servlet.RequestScoped;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * Created by Francesco on 18/01/2015.
 */
@RequestScoped
public class UserService {

    Logger logger = Logger.getLogger(getClass().getName());

    /**
     * User starting this request
     */
    private User currentUser;

    /**
     * Device this request was sent from
     */
    private String deviceId;

    /**
     * Gets all registered devices, except the one starting this request
     */
    public List<Device> getUserDevicesButCurrent() {
        List<Device> list = getUserDevices();
        if (deviceId != null) {
            Iterator<Device> iterator = list.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getRegId().equals(deviceId)) iterator.remove();
            }
        }
        return list;
    }

    /**
     * Gets all registered devices.
     */
    public List<Device> getUserDevices() {
        return ofy().load().type(Device.class).ancestor(getCurrentUser()).list();
    }

    public List<Car> retrieveUserCars() {
        return ofy().load().type(Car.class).ancestor(getCurrentUser()).list();
    }

    public User getCurrentUser() {
        if (currentUser == null)
            throw new AuthenticationException();

        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
