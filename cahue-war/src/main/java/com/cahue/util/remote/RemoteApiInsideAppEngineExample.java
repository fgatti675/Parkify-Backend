package com.cahue.util.remote;

import com.cahue.model.GoogleUser;
import com.cahue.persistence.OfyService;
import com.google.api.client.util.SecurityUtils;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;
import com.googlecode.objectify.util.Closeable;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.List;

/**
 * Created by Francesco on 23/06/2015.
 */
public class RemoteApiInsideAppEngineExample {

    public static final String EMAIL_ADDRESS = "582791978228-kl51c8scvc1ombariffo8bsnf25qf7st@developer.gserviceaccount.com";
    private final RemoteApiOptions options;


    public RemoteApiInsideAppEngineExample()
            throws IOException, GeneralSecurityException {

        PrivateKey privateKey = SecurityUtils.loadPrivateKeyFromKeyStore(SecurityUtils.getPkcs12KeyStore(),
                this.getClass().getClassLoader().getResourceAsStream("Cahue-4d17cda7873b.p12"), "notasecret", "privatekey", "notasecret");


        this.options = new RemoteApiOptions()
                .useServiceAccountCredential(EMAIL_ADDRESS, privateKey)
                .server("glossy-radio.appspot.com", 443);


        RemoteApiInstaller installer = new RemoteApiInstaller();
        installer.install(options);
        try {

//            Injector injector = Guice.createInjector(
//                    new BusinessModule()
//            );


            Closeable session = ObjectifyService.begin();

            ObjectifyService.register(GoogleUser.class);
//            ObjectifyService.setFactory(new OfyFactory(injector));
            Query<GoogleUser> q = OfyService.ofy().load().type(GoogleUser.class);
            List<GoogleUser> users = q.list();
            for (GoogleUser user : users)
                System.out.println(user.getEmail());
//            OfyService.ofy().save().entities(users).now();

            session.close();
        } finally {
            installer.uninstall();
        }

    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        new RemoteApiInsideAppEngineExample();
    }

    void putInRemoteDatastore(Entity entity) throws IOException {
        RemoteApiInstaller installer = new RemoteApiInstaller();
        installer.install(options);
        try {
            DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
            System.out.println("Key of new entity is " + ds.put(entity));
        } finally {
            installer.uninstall();
        }
    }
}