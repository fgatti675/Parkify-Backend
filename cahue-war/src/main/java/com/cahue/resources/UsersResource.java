package com.cahue.resources;

import com.cahue.model.User;
import com.cahue.persistence.DataSource;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Date: 12.09.14
 *
 * @author francesco
 */
@Path("/users")
public class UsersResource {

    public static final int MEMCACHE_ESPIRATION_SECONDS = 60 * 24 * 60 * 60;

    public static final void main(String[] args) throws Exception {
        UsersResource usersResource = new UsersResource();
        usersResource.retrieveGoogleUser("ya29._ABph75AciNZOw0dlgD3EUDJK3BtaNR9fXJwNWCJ-OJrJ8KyUn9PY4B7BhnVAr41Xs4_04iCJ3loCQ");
    }


    private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static final String GOOGLE_AUTH_TOKENS_MEMCACHE = "GOOGLE_AUTH_TOKENS_MEMCACHE";

    @Inject
    DataSource dataSource;

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    @POST
    @Path("/createGoogle")
    public User createGoogleUser(@Context HttpHeaders headers) {

        String gAuthToken = headers.getHeaderString("GoogleAuth");

        if (gAuthToken == null)
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("A header named GoogleAuth is compulsory. It must contain a valid Google access token.")
                            .build());

        User user = retrieveGoogleUser(gAuthToken);

        return user;
    }

    public User retrieveGoogleUser(final String accessToken) {

        User user = null;

        try {

            /**
             * Try to retrieve it directly from the auth token
             */
            MemcacheService cache = MemcacheServiceFactory.getMemcacheService(GOOGLE_AUTH_TOKENS_MEMCACHE);
            EntityManager em = dataSource.createDatastoreEntityManager();

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
                key = createGoogleUserKey(googleId);
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

    public User createUserFromGoogleAccount(EntityManager em, Userinfoplus person) {
        try {
            em.getTransaction().begin();
            User user = new User();
            String googleId = person.getId();
            user.setKey(createGoogleUserKey(googleId));
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

    private Key createGoogleUserKey(String googleId) {
        return KeyFactory.createKey(User.class.getSimpleName(), "G" + googleId);
    }

    private Userinfoplus getUserInfoPlus(GoogleCredential credential) throws IOException {
        Oauth2 userInfoService = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("Cahue")
                .build();

        return userInfoService.userinfo().get().execute();
    }

    private Person getPlusPerson(GoogleCredential credential) throws IOException {
        Plus plus = new Plus.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("Cahue")
                .build();

        return plus.people().get("me").execute();
    }
}
