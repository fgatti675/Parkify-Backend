package com.cahue.auth;

import com.cahue.model.AuthToken;
import com.cahue.model.FacebookUser;
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
import com.googlecode.objectify.cmd.Query;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static com.restfb.Version.VERSION_2_9;

/**
 * Created by Francesco on 13/02/2015.
 */
public class UserAuthenticationService {

    public static final String AUTH_HEADER = "Authorization";
    public static final String GOOGLE_AUTH_HEADER = "GoogleAuth";
    public static final String AUTH_TOKENS_MEMCACHE = "AUTH_TOKENS_MEMCACHE";
    private static final String APPLICATION_NAME = "iweco";
    private static final int API_VERSION = 1;
    private static final String SECRET = "ABph75AciNZOw0dlgD3E_JK3";
    private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    Logger logger = Logger.getLogger(getClass().getName());
    Pattern tokenPattern =
            Pattern.compile(".+" +
                    "\\|" + APPLICATION_NAME +
                    "\\|" + "\\d+" +
                    "\\|" + ".+");
    private StandardPBEStringEncryptor jasypt;

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
            if (authToken == null) return null;
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
             * Retrieve the user from Google and search by GoogleID
             */
            GoogleCredential credential = new GoogleCredential().setAccessToken(googleAccessToken);
            Userinfoplus userInfoPlus = null;
            try {
                Oauth2 userInfoService = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();

                userInfoPlus = userInfoService.userinfo().get().execute();
            } catch (GoogleJsonResponseException e) {
                e.printStackTrace();
                logger.log(Level.SEVERE, "Google Auth token failed to be exchanged for a real Google person.");
                throw new WebApplicationException("Google Auth token failed to be exchanged for a real Google person.");
            }

            /**
             * Look for a Facebook User with the same email
             */
            if (user == null) {
                String email = userInfoPlus.getEmail();
                user = findUserByFacebookEmail(email);
            }

            String googleId = userInfoPlus.getId();

            /**
             * Try to retrieve the user via the google id
             */
            if (user == null) {
                GoogleUser googleUser = ofy().load().type(GoogleUser.class).id(googleId).now();
                if (googleUser != null)
                    user = googleUser.getUser();
            }

            /**
             * If it's still null we create it
             */
            if (user == null) {
                user = User.create(generateToken());
            }

            if (user.getGoogleUser() == null) {
                createGoogleUser(user, userInfoPlus);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return user;

    }

    /**
     * Retrieve a user from an Google access token.
     *
     * @param facebookAccessToken
     * @return
     */
    public User retrieveFacebookUser(final String facebookAccessToken) {

        User user = null;

        // Init Facebook client
        FacebookClient facebookClient = new DefaultFacebookClient(facebookAccessToken,  VERSION_2_9);

        com.restfb.types.User facebookResultUser = facebookClient.fetchObject("me", com.restfb.types.User.class, Parameter.with("fields", "id,name,email"));
        if (facebookResultUser == null) {
            logger.log(Level.SEVERE, "Facebook Auth token failed to be exchanged for a real Facebook person.");
            throw new WebApplicationException("Facebook Auth token failed to be exchanged for a real Facebook person.");
        }

        logger.log(Level.INFO, "Facebook User login email: " + facebookResultUser.getEmail());


        /**
         * Try to retrieve the user by the facebook ID
         */
        String facebookId = facebookResultUser.getId();
        FacebookUser facebookUser = ofy().load().type(FacebookUser.class).id(facebookId).now();
        if (facebookUser != null) {
            user = facebookUser.getUser();
            user.getFacebookUser().setEmail(facebookResultUser.getEmail());
        }

        /**
         * Look for a Google User with the same email
         */
        if (user == null) {
            String email = facebookResultUser.getEmail();
            user = findUserByGoogleEmail(email);
        }

        if (user == null) {
            user = User.create(generateToken());
        }

        if (user.getFacebookUser() == null) {
            createFacebookUser(user, facebookResultUser);
        }

        return user;

    }

    private FacebookUser createFacebookUser(User user, com.restfb.types.User facebookResultUser) {

        FacebookUser facebookUser = new FacebookUser();
        facebookUser.setFacebookId(facebookResultUser.getId());
        facebookUser.setEmail(facebookResultUser.getEmail());

        user.setFacebookUser(facebookUser);
        ofy().save().entity(user).now();

        facebookUser.setUser(user);
        ofy().save().entity(facebookUser).now();

        logger.fine("Created new facebookUser: " + facebookUser);

        return facebookUser;
    }

    private GoogleUser createGoogleUser(User user, Userinfoplus person) {

        GoogleUser googleUser = new GoogleUser();
        googleUser.setGoogleId(person.getId());
        googleUser.setEmail(person.getEmail());

        user.setGoogleUser(googleUser);
        ofy().save().entity(user).now();

        googleUser.setUser(user);
        ofy().save().entity(googleUser).now();

        logger.fine("Created new googleUser: " + googleUser);

        return googleUser;
    }

    private User findUserByGoogleEmail(String email) {
        Query<GoogleUser> q = ofy().load().type(GoogleUser.class);
        q = q.filter("email", email);
        List<GoogleUser> googleUserList = q.list();

        if (googleUserList.isEmpty()) {
            return null;
        }

        if (googleUserList.size() > 1) {
            logger.warning("More than one GoogleUser with email : " + email);
        }

        return googleUserList.get(0).getUser();
    }

    private User findUserByFacebookEmail(String email) {
        Query<FacebookUser> q = ofy().load().type(FacebookUser.class);
        q = q.filter("email", email);
        List<FacebookUser> facebookUserList = q.list();

        if (facebookUserList.isEmpty()) {
            return null;
        }

        if (facebookUserList.size() > 1) {
            logger.warning("More than one FacebookUser with email : " + email);
        }

        return facebookUserList.get(0).getUser();
    }

    @Deprecated
    private Person getPlusPerson(GoogleCredential credential) throws IOException {
        Plus plus = new Plus.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        return plus.people().get("me").execute();
    }

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
        String decrypted = jasypt.decrypt(userToken);
        return tokenPattern.matcher(decrypted).matches();
    }

}
