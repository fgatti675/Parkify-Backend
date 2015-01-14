package com.cahue.resources;

import com.cahue.model.User;
import com.cahue.persistence.DataSource;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Date: 12.09.14
 *
 * @author francesco
 */
@Path("/users")
public class UsersResource {

    public static final void main(String[] args) throws Exception {
        UsersResource usersResource = new UsersResource();
        usersResource.retrieveUser("ya29.-wASTGEH1x1LWHA2F-EZ5a5sTFYaKYX5i-2BupIFtrllNHSk0eCP3hhT7F3GqakFLzy3KUcGZmeGIw");
    }


    private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final AppEngineDataStoreFactory appEngineDataStoreFactory = AppEngineDataStoreFactory.getDefaultInstance();

    public static final String GOOGLE_AUTH_TOKENS_DATASTORE = "GOOGLE_AUTH_TOKENS_DATASTORE";

    @Inject
    DataSource dataSource;

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    @POST
    @Path("/createGoogle")
    public synchronized User createGoogleUser(@Context HttpHeaders headers) {

        String authToken = headers.getHeaderString("GoogleAuth");

        if (authToken == null)
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("A header named GoogleAuth is compulsory. It must contain a valid Google access token.")
                            .build());

        User user = retrieveUser(authToken);

        return user;
    }

    public User retrieveUser(final String accessToken) {

        User user = null;

        try {

            /**
             * Try to retrieve it directly from the auth token
             */
            DataStore<User> authTokensDatastore = appEngineDataStoreFactory.getDataStore(GOOGLE_AUTH_TOKENS_DATASTORE);
            user = authTokensDatastore.get(accessToken);

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
                EntityManager em = dataSource.createDatastoreEntityManager();
                try {
                    user = em.createNamedQuery("User.findByGoogleId", User.class).setParameter("googleId", googleId).getSingleResult();
                } catch (NoResultException ignore) {
                }

                /**
                 * If it's still null we create it
                 */
                if (user == null) {
                    user = createUser(em, person);
                }

                authTokensDatastore.set(accessToken, user);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return user;

    }

    public User createUser(EntityManager em, Userinfoplus person) {
        try {
            em.getTransaction().begin();
            User user = new User();
            user.setGoogleId(person.getId());
            user.setEmail(person.getEmail());
            em.persist(user);
            em.getTransaction().commit();
            logger.fine("Created new user: " + user);
            return user;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new WebApplicationException("Error creating new user");
        }

    }

    private Userinfoplus getUserInfoPlus(GoogleCredential credential) throws IOException {
        Oauth2 userInfoService = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
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
