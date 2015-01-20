package com.cahue.util;

import com.cahue.model.Device;
import com.cahue.model.User;
import com.cahue.model.transfer.RegistrationBean;
import com.cahue.persistence.DataSource;
import com.cahue.resources.InvalidTokenException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Francesco on 18/01/2015.
 */
public class UserService {


    public static final int MEMCACHE_ESPIRATION_SECONDS = 60 * 24 * 60 * 60;

    public static final void main(String[] args) throws Exception {
        UserService usersResource = new UserService();
        usersResource.retrieveGoogleUser("ya29._ABph75AciNZOw0dlgD3EUDJK3BtaNR9fXJwNWCJ-OJrJ8KyUn9PY4B7BhnVAr41Xs4_04iCJ3loCQ");
    }


    private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static final String GOOGLE_AUTH_TOKENS_MEMCACHE = "GOOGLE_AUTH_TOKENS_MEMCACHE";

    public static final String GOOGLE_AUTH_HEADER = "GoogleAuth";

    @Inject
    DataSource dataSource;

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    public User retrieveGoogleUser(final String accessToken) {
        EntityManager em = dataSource.createDatastoreEntityManager();
        try {
            return retrieveGoogleUser(em, accessToken);
        } finally {
            em.close();
        }
    }

    public User retrieveGoogleUser(EntityManager em, final String accessToken) {

        User user = null;

        try {

            /**
             * Try to retrieve it directly from the auth token
             */
            MemcacheService cache = MemcacheServiceFactory.getMemcacheService(GOOGLE_AUTH_TOKENS_MEMCACHE);

            Key key = (Key) cache.get(accessToken);
            if (key != null)
                user = em.find(User.class, key);

            /**
             * If not found by access token, retrieve the user from Google and search by GoogleID
             */
            if (user == null) {

                GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);

                Userinfoplus person = getUserInfoPlus(credential);

                if (person == null) {
                    logger.log(Level.SEVERE, "Token failed to be exchanged for a real Google person.");
                    return null;
                }

                String googleId = person.getId();

                /**
                 * Try to retrieve the user via the google id
                 */
                key = User.createGoogleUserKey(googleId);
                user = em.find(User.class, key);

                /**
                 * If it's still null we create it
                 */
                if (user == null)
                    user = createUserFromGoogleAccount(em, person);

                cache.put(accessToken, key, Expiration.byDeltaSeconds(MEMCACHE_ESPIRATION_SECONDS));

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return user;

    }

    /**
     * Register a new device and asign it to a user
     * @param em
     * @param deviceRegistrationId
     * @param user
     */
    private void registerDevice(EntityManager em, String deviceRegistrationId, User user) {
        Device device = Device.createDevice(deviceRegistrationId, user);
        user.getDevices().add(device); // no matter if its duplicated because we are adding it to a set
        em.getTransaction().begin();
        em.persist(device);
        em.getTransaction().commit();
    }

    public User createUserFromGoogleAccount(EntityManager em, Userinfoplus person) {
        try {
            em.getTransaction().begin();
            User user = new User();
            String googleId = person.getId();
            user.setKey(User.createGoogleUserKey(googleId));
            user.setGoogleId(googleId);
            user.setEmail(person.getEmail());

            em.persist(user);

            em.getTransaction().commit();
            logger.fine("Created new user: " + user);
            return user;

        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new WebApplicationException("Error creating new user");
        }

    }

    private Userinfoplus getUserInfoPlus(GoogleCredential credential) throws IOException {
        try {
            Oauth2 userInfoService = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName("Cahue")
                    .build();

            return userInfoService.userinfo().get().execute();
        } catch (GoogleJsonResponseException e) {
            throw new InvalidTokenException(e.getStatusCode(), e.getDetails().getMessage());
        }
    }

    @Deprecated
    private Person getPlusPerson(GoogleCredential credential) throws IOException {
        Plus plus = new Plus.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("Cahue")
                .build();

        return plus.people().get("me").execute();
    }


    public User getFromHeaders(HttpHeaders headers) {
        String authToken = headers.getHeaderString(GOOGLE_AUTH_HEADER);
        return retrieveGoogleUser(authToken);
    }

    public User register(RegistrationBean registration) {
        EntityManager em = dataSource.createDatastoreEntityManager();
        try {
            User user = retrieveGoogleUser(em, registration.getAuthToken());
            registerDevice(em, registration.getDeviceRegId(), user);
            return user;
        } finally {
            em.close();
        }
    }
}
