package com.cahue.util.remote;

import com.cahue.model.GoogleUser;
import com.cahue.config.persistence.OfyService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Created by Francesco on 23/06/2015.
 */
public class RemoteApiInsideAppEngineExample {

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        RemoteApiOptions options = new RemoteApiOptions()
                .server("glossy-radio.appspot.com", 443)
                .useApplicationDefaultCredential();

        RemoteApiInstaller installer = new RemoteApiInstaller();
        installer.install(options);

        try {

            Closeable session = ObjectifyService.begin();

            ObjectifyService.register(GoogleUser.class);
//            ObjectifyService.setFactory(new OfyFactory(injector));
            int count= OfyService.ofy().load().type(GoogleUser.class).count();
            System.out.println(count);

//            Query<GoogleUser> q = OfyService.ofy().load().type(GoogleUser.class).limit(100);
//            List<GoogleUser> users = q.list();
//            for (GoogleUser user : users)
//                System.out.println(user.getEmail());
//            OfyService.ofy().save().entities(users).now();

            session.close();
        } finally {
            installer.uninstall();
        }
    }

}