package com.cahue.auth;

import com.cahue.model.AuthToken;
import com.cahue.model.GoogleUser;
import com.cahue.model.User;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.objectify.Key;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import javax.ws.rs.core.HttpHeaders;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Francesco on 13/02/2015.
 */
public class UserAuthenticationService {

    private static final String APPLICATION_NAME = "iweco";
    private static final int API_VERSION = 1;

    private static final String SECRET = "ABph75AciNZOw0dlgD3E_JK3";

    public static final String AUTH_HEADER = "Authorization";
    public static final String GOOGLE_AUTH_HEADER = "GoogleAuth";

    private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static final String AUTH_TOKENS_MEMCACHE = "AUTH_TOKENS_MEMCACHE";

    private StandardPBEStringEncryptor jasypt;

    Logger logger = Logger.getLogger(getClass().getName());

    public UserAuthenticationService() {
        jasypt = new StandardPBEStringEncryptor();
        jasypt.setPassword(SECRET);
    }

    public void storeAuthToken(User user, String authTokenString) {
        AuthToken token = new AuthToken();
        token.setUser(user);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        token.setExpirationDate(c.getTime());
        token.setToken(authTokenString);
        ofy().save().entity(token);
        MemcacheService cache = getAuthTokensMemcacheService();
        cache.put(authTokenString, user.getId());
    }


    public User retrieveUser(final String authTokenValue) {

        /**
         * If in memcache return by ID
         */
        Long userId = (Long) getAuthTokensMemcacheService().get(authTokenValue);
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
     * Get the user based on the HTTP headers. It may create a new User.
     *
     * @param headers
     * @return
     */
    public User getFromHeaders(HttpHeaders headers) {

        String authToken = headers.getHeaderString(AUTH_HEADER);

        User user = null;

        if (authToken != null) {
            user = retrieveUser(authToken);
        }

        if (user == null) {
            String googleAuthToken = headers.getHeaderString(GOOGLE_AUTH_HEADER);
            user = googleAuthToken == null ? null : retrieveGoogleUser(googleAuthToken);
        }

        return user;
    }


    private MemcacheService getAuthTokensMemcacheService() {
        return MemcacheServiceFactory.getMemcacheService(AUTH_TOKENS_MEMCACHE);
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
                    logger.log(Level.SEVERE, "Google Auth token failed to be exchanged for a real Google person.");
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
        ofy().save().entity(user).now();

        googleUser.setUser(user);
        ofy().save().entity(googleUser).now();

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
            e.printStackTrace();
            return null;
        }
    }

    @Deprecated
    private Person getPlusPerson(GoogleCredential credential) throws IOException {
        Plus plus = new Plus.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        return plus.people().get("me").execute();
    }

    Pattern tokenPattern =
            Pattern.compile(".+" +
                    "|" + APPLICATION_NAME +
                    "|" + "\\d+" +
                    "|" + ".+");

    public String generateToken() {
        String key = UUID.randomUUID().toString().toUpperCase() +
                "|" + APPLICATION_NAME +
                "|" + API_VERSION +
                "|" + new Date().getTime();


        // this is the authentication token user will send in order to use the web service
        return jasypt.encrypt(key);
    }

    public boolean validateToken(String userToken) {
        if (userToken == null) return false;
        return tokenPattern.matcher(userToken).matches();
    }

}
