package com.cahue.auth;

import com.cahue.auth.AuthenticationException;
import com.cahue.model.*;
import com.google.inject.servlet.RequestScoped;

import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * Created by Francesco on 18/01/2015.
 */
@RequestScoped
public class UserService {

    Logger logger = Logger.getLogger(getClass().getName());

    private User loggedUser;

    /**
     * Gets all registered devices.
     */
    public List<Device> getDevices(User user) {
        return ofy().load().type(Device.class).ancestor(user).list();
    }

    public User getLoggedUser() {
        if (loggedUser == null)
            throw new AuthenticationException();

        return loggedUser;
    }

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }
}
