package com.cahue.util;

import com.cahue.model.*;
import com.cahue.model.transfer.RegistrationRequestBean;
import com.cahue.model.transfer.RegistrationResult;
import com.cahue.resources.InvalidTokenException;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.objectify.Key;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cahue.persistence.OfyService.ofy;

/**
 * Created by Francesco on 18/01/2015.
 */
public class UserService {

    public static final int TOKEN_EXPIRATION_MS = 365 * 24 * 60 * 60 * 1000;

    private static final String APPLICATION_NAME = "Cahue";
    private static final String SECRET = "ABph75AciNZOw0dlgD3E_JK3";

    public static final void main(String[] args) throws Exception {
        UserService usersResource = new UserService();
        usersResource.retrieveGoogleUser("ya29._ABph75AciNZOw0dlgD3EUDJK3BtaNR9fXJwNWCJ-OJrJ8KyUn9PY4B7BhnVAr41Xs4_04iCJ3loCQ");
    }

    private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static final String AUTH_TOKENS_MEMCACHE = "AUTH_TOKENS_MEMCACHE";

    public static final String IWECO_AUTH_HEADER = "Authorization";
    public static final String GOOGLE_AUTH_HEADER = "GoogleAuth";


    Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Register a new user from a {@link com.cahue.model.transfer.RegistrationRequestBean}
     *
     * @param registration
     * @return
     */
    public RegistrationResult register(RegistrationRequestBean registration) {

        RegistrationResult result = new RegistrationResult();

        // create or retrieve an existing Google user
        User user = retrieveGoogleUser(registration.getGoogleAuthToken());
        result.setUser(user);

        // register the device
        registerDevice(registration.getDeviceRegId(), user);

        // create new Auth Token
        String authToken = generateToken();

        // store the token
        storeAuthToken(user, authToken);

        // store the token as a transient property in the user so it can be returned to the client
        result.setAuthToken(authToken);

        return result;
    }

    private void storeAuthToken(User user, String authTokenString) {
        AuthToken token = new AuthToken();
        token.setUser(user);
        Date date = new Date();
        date.setTime(date.getTime() + TOKEN_EXPIRATION_MS);
        token.setExpirationDate(date);
        token.setToken(authTokenString);
        ofy().save().entity(token);
        MemcacheService cache = getAuthTokensMemcacheService();
        cache.put(authTokenString, user.getId());
    }

    private MemcacheService getAuthTokensMemcacheService() {
        return MemcacheServiceFactory.getMemcacheService(AUTH_TOKENS_MEMCACHE);
    }


    public User retrieveUser(final String authTokenValue) {

        /**
         * If in memcache return by ID
         */
        String userId = (String) getAuthTokensMemcacheService().get(authTokenValue);
        if (userId != null) {
            return ofy().load().type(User.class).id(userId).now();
        }

        /**
         * Else go through datastore
         */
        else {
            AuthToken authToken = ofy().load().type(AuthToken.class).id(authTokenValue).now();
            return authToken.getUser();
        }
    }

    /**
     * Retrieve a user from an Google access token.
     *
     * @param googleAccessToken
     * @return
     */
    public User retrieveGoogleUser(final String googleAccessToken) {

        User user = null;

        try {

            /**
             * If not found by google access token, retrieve the user from Google and search by GoogleID
             */
            if (user == null) {

                GoogleCredential credential = new GoogleCredential().setAccessToken(googleAccessToken);

                Userinfoplus person = getUserInfoPlus(credential);

                if (person == null) {
                    logger.log(Level.SEVERE, "Token failed to be exchanged for a real Google person.");
                    return null;
                }

                String googleId = person.getId();

                /**
                 * Try to retrieve the user via the google id
                 */
                Key<GoogleUser> key = GoogleUser.createGoogleUserKey(googleId);
                GoogleUser googleUser = ofy().load().key(key).now();
                if (googleUser != null)
                    user = googleUser.getUser();

                /**
                 * If it's still null we create it
                 */
                if (user == null)
                    user = createUserFromGoogleAccount(person);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return user;

    }


    public User createUserFromGoogleAccount(Userinfoplus person) {

        User user = new User();
        user.setRefreshToken(generateToken());

        GoogleUser googleUser = new GoogleUser();
        googleUser.setGoogleId(person.getId());
        googleUser.setEmail(person.getEmail());

        user.setGoogleUser(googleUser);
        googleUser.setUser(user);

        ofy().save().entities(user, googleUser).now();

        logger.fine("Created new googleUser: " + googleUser);
        return user;

    }

    private Userinfoplus getUserInfoPlus(GoogleCredential credential) throws IOException {
        try {
            Oauth2 userInfoService = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            return userInfoService.userinfo().get().execute();
        } catch (GoogleJsonResponseException e) {
            throw new InvalidTokenException(e.getStatusCode(), e.getDetails().getMessage());
        }
    }

    @Deprecated
    private Person getPlusPerson(GoogleCredential credential) throws IOException {
        Plus plus = new Plus.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        return plus.people().get("me").execute();
    }


    /**
     * Get the user based on the HTTP headers. It may create a new User.
     *
     * @param headers
     * @return
     */
    public User getFromHeaders(HttpHeaders headers) {

        String authToken = headers.getHeaderString(IWECO_AUTH_HEADER);
        User user = retrieveUser(authToken);

        if (user == null) {
            String googleAuthToken = headers.getHeaderString(GOOGLE_AUTH_HEADER);
            user = googleAuthToken == null ? null : retrieveGoogleUser(googleAuthToken);
        }

        return user;
    }

    /**
     * Register a new device and assign it to a user
     *
     * @param deviceRegistrationId
     * @param user
     */
    private void registerDevice(String deviceRegistrationId, User user) {
        Device device = Device.createDevice(deviceRegistrationId, user);
        ofy().save().entity(device).now();
    }

    private String generateToken() {
        String key = UUID.randomUUID().toString().toUpperCase() +
                "|" + APPLICATION_NAME +
                "|" + new Date().getTime();

        // TODO: can be initialized somewhere else
        StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
        jasypt.setPassword(SECRET);

        // this is the authentication token user will send in order to use the web service
        return jasypt.encrypt(key);
    }
}
