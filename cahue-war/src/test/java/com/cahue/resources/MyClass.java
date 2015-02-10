package com.cahue.resources;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

// ...

class MyClass {

    // Path to client_secrets.json which should contain a JSON document such as:
    //   {
    //     "web": {
    //       "client_id": "[[YOUR_CLIENT_ID]]",
    //       "client_secret": "[[YOUR_CLIENT_SECRET]]",
    //       "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    //       "token_uri": "https://accounts.google.com/o/oauth2/token"
    //     }
    //   }
    private static final String CLIENTSECRETS_LOCATION = "client_secrets.json";

    private static final String REDIRECT_URI = "<YOUR_REGISTERED_REDIRECT_URI>";
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/drive.file",
            "email",
            "profile");

    private static GoogleAuthorizationCodeFlow flow = null;

    /**
     * Exception thrown when an error occurred while retrieving credentials.
     */
    public static class GetCredentialsException extends Exception {

        protected String authorizationUrl;

        /**
         * Construct a GetCredentialsException.
         *
         * @param authorizationUrl The authorization URL to redirect the user to.
         */
        public GetCredentialsException(String authorizationUrl) {
            this.authorizationUrl = authorizationUrl;
        }

        /**
         * Set the authorization URL.
         */
        public void setAuthorizationUrl(String authorizationUrl) {
            this.authorizationUrl = authorizationUrl;
        }

        /**
         * @return the authorizationUrl
         */
        public String getAuthorizationUrl() {
            return authorizationUrl;
        }
    }

    /**
     * Exception thrown when a code exchange has failed.
     */
    public static class CodeExchangeException extends GetCredentialsException {

        /**
         * Construct a CodeExchangeException.
         *
         * @param authorizationUrl The authorization URL to redirect the user to.
         */
        public CodeExchangeException(String authorizationUrl) {
            super(authorizationUrl);
        }

    }

    /**
     * Exception thrown when no refresh token has been found.
     */
    public static class NoRefreshTokenException extends GetCredentialsException {

        /**
         * Construct a NoRefreshTokenException.
         *
         * @param authorizationUrl The authorization URL to redirect the user to.
         */
        public NoRefreshTokenException(String authorizationUrl) {
            super(authorizationUrl);
        }

    }

    /**
     * Exception thrown when no user ID could be retrieved.
     */
    private static class NoUserIdException extends Exception {
    }

    /**
     * Retrieved stored credentials for the provided user ID.
     *
     * @param userId User's ID.
     * @return Stored Credential if found, {@code null} otherwise.
     */
    static Credential getStoredCredentials(String userId) {
        // TODO: Implement this method to work with your database. Instantiate a new
        // Credential instance with stored accessToken and refreshToken.
        throw new UnsupportedOperationException();
    }

    /**
     * Store OAuth 2.0 credentials in the application's database.
     *
     * @param userId User's ID.
     * @param credentials The OAuth 2.0 credentials to store.
     */
    static void storeCredentials(String userId, Credential credentials) {
        // TODO: Implement this method to work with your database.
        // Store the credentials.getAccessToken() and credentials.getRefreshToken()
        // string values in your database.
        throw new UnsupportedOperationException();
    }

    /**
     * Build an authorization flow and store it as a static class attribute.
     *
     * @return GoogleAuthorizationCodeFlow instance.
     * @throws IOException Unable to load client_secrets.json.
     */
    static GoogleAuthorizationCodeFlow getFlow() throws IOException {
        if (flow == null) {
            HttpTransport httpTransport = new NetHttpTransport();
            JacksonFactory jsonFactory = new JacksonFactory();
            GoogleClientSecrets clientSecrets =
                    GoogleClientSecrets.load(jsonFactory,
                            new InputStreamReader(MyClass.class.getResourceAsStream(CLIENTSECRETS_LOCATION)));
            flow =
                    new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, SCOPES)
                            .setAccessType("offline").setApprovalPrompt("force").build();
        }
        return flow;
    }

    /**
     * Exchange an authorization code for OAuth 2.0 credentials.
     *
     * @param authorizationCode Authorization code to exchange for OAuth 2.0
     *        credentials.
     * @return OAuth 2.0 credentials.
     * @throws CodeExchangeException An error occurred.
     */
    static Credential exchangeCode(String authorizationCode)
            throws CodeExchangeException {
        try {
            GoogleAuthorizationCodeFlow flow = getFlow();
            GoogleTokenResponse response =
                    flow.newTokenRequest(authorizationCode).setRedirectUri(REDIRECT_URI).execute();
            return flow.createAndStoreCredential(response, null);
        } catch (IOException e) {
            System.err.println("An error occurred: " + e);
            throw new CodeExchangeException(null);
        }
    }

    /**
     * Send a request to the UserInfo API to retrieve the user's information.
     *
     * @param credentials OAuth 2.0 credentials to authorize the request.
     * @return User's information.
     * @throws NoUserIdException An error occurred.
     */
    static Userinfoplus getUserInfo(Credential credentials)
            throws NoUserIdException {
        Oauth2 userInfoService =
                new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(), credentials).build();
        Userinfoplus userInfo = null;
        try {
            userInfo = userInfoService.userinfo().get().execute();
        } catch (IOException e) {
            System.err.println("An error occurred: " + e);
        }
        if (userInfo != null && userInfo.getId() != null) {
            return userInfo;
        } else {
            throw new NoUserIdException();
        }
    }

    /**
     * Retrieve the authorization URL.
     *
     * @param emailAddress User's e-mail address.
     * @param state State for the authorization URL.
     * @return Authorization URL to redirect the user to.
     * @throws IOException Unable to load client_secrets.json.
     */
    public static String getAuthorizationUrl(String emailAddress, String state) throws IOException {
        GoogleAuthorizationCodeRequestUrl urlBuilder =
                getFlow().newAuthorizationUrl().setRedirectUri(REDIRECT_URI).setState(state);
        urlBuilder.set("user_id", emailAddress);
        return urlBuilder.build();
    }

    /**
     * Retrieve credentials using the provided authorization code.
     *
     * This function exchanges the authorization code for an access token and
     * queries the UserInfo API to retrieve the user's e-mail address. If a
     * refresh token has been retrieved along with an access token, it is stored
     * in the application database using the user's e-mail address as key. If no
     * refresh token has been retrieved, the function checks in the application
     * database for one and returns it if found or throws a NoRefreshTokenException
     * with the authorization URL to redirect the user to.
     *
     * @param authorizationCode Authorization code to use to retrieve an access
     *        token.
     * @param state State to set to the authorization URL in case of error.
     * @return OAuth 2.0 credentials instance containing an access and refresh
     *         token.
     * @throws NoRefreshTokenException No refresh token could be retrieved from
     *         the available sources.
     * @throws IOException Unable to load client_secrets.json.
     */
    public static Credential getCredentials(String authorizationCode, String state)
            throws CodeExchangeException, NoRefreshTokenException, IOException {
        String emailAddress = "";
        try {
            Credential credentials = exchangeCode(authorizationCode);
            Userinfoplus userInfo = getUserInfo(credentials);
            String userId = userInfo.getId();
            emailAddress = userInfo.getEmail();
            if (credentials.getRefreshToken() != null) {
                storeCredentials(userId, credentials);
                return credentials;
            } else {
                credentials = getStoredCredentials(userId);
                if (credentials != null && credentials.getRefreshToken() != null) {
                    return credentials;
                }
            }
        } catch (CodeExchangeException e) {
            e.printStackTrace();
            // Drive apps should try to retrieve the user and credentials for the current
            // session.
            // If none is available, redirect the user to the authorization URL.
            e.setAuthorizationUrl(getAuthorizationUrl(emailAddress, state));
            throw e;
        } catch (NoUserIdException e) {
            e.printStackTrace();
        }
        // No refresh token has been retrieved.
        String authorizationUrl = getAuthorizationUrl(emailAddress, state);
        throw new NoRefreshTokenException(authorizationUrl);
    }

}