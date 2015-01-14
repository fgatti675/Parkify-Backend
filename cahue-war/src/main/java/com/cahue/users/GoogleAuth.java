package com.cahue.users;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.PemReader;
import com.google.api.client.util.SecurityUtils;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;

import java.io.*;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Common OAuth code used across the Java examples.
 */
public class GoogleAuth {


    private static final String REDIRECT_URI = "<YOUR_REGISTERED_REDIRECT_URI>";
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/plus.login");

    private static GoogleAuthorizationCodeFlow flow = null;

    private static final String CLIENT_SECRETS_FILE = "client_secrets.json";
    private static final String SERVICE_KEY_FILE = "service_key.json";

    /**
     * Exchange an authorization code for OAuth 2.0 credentials.
     *
     * @param authorizationCode Authorization code to exchange for OAuth 2.0
     *                          credentials.
     * @return OAuth 2.0 credentials.
     * @throws CodeExchangeException An error occurred.
     */
    static Credential exchangeCode(String authorizationCode)
            throws CodeExchangeException {
        try {
            GoogleAuthorizationCodeFlow flow = getFlow();
//            GoogleTokenResponse response =
//                    flow.newTokenRequest(authorizationCode).execute();

            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccessToken(authorizationCode);
            return flow.createAndStoreCredential(tokenResponse, null);
        } catch (IOException e) {
            System.err.println("An error occurred: " + e);
            throw new CodeExchangeException(null);
        }
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
            InputStream resourceAsStream = GoogleAuth.class.getResourceAsStream(CLIENT_SECRETS_FILE);
            GoogleClientSecrets clientSecrets =
                    GoogleClientSecrets.load(
                            jsonFactory,
                            new InputStreamReader(resourceAsStream));
            flow =
                    new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, SCOPES)
                            .setAccessType("offline").setApprovalPrompt("force").build();
        }
        return flow;
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
        Userinfoplus userinfoplus = null;
        try {
            userinfoplus  = userInfoService.userinfo().get().execute();
        } catch (IOException e) {
            System.err.println("An error occurred: " + e);
        }
        if (userinfoplus != null && userinfoplus.getId() != null) {
            return userinfoplus;
        } else {
            throw new NoUserIdException();
        }
    }


    /**
     * Authorize using service account credentials.
     *
     * @param httpTransport The HTTP transport to use for network requests.
     * @param jsonFactory   The JSON factory to use for serialization / de-serialization.
     * @param scopes        The scopes for which this app should authorize.
     */
    public static Credential authorizeService(HttpTransport httpTransport, JsonFactory jsonFactory,
                                              Collection<String> scopes) throws IOException {
        File secretsFile = null;
        try {
            secretsFile = new File(GoogleAuth.class.getResource(SERVICE_KEY_FILE).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (!secretsFile.exists()) {
            throw new RuntimeException("Private key file not found.\n"
                    + "Follow the instructions at https://developers.google"
                    + ".com/maps-engine/documentation/oauth/serviceaccount#creating_a_service_account\n"
                    + "and save the generated JSON key to " + secretsFile.getAbsolutePath());
        }

        try {
            // Load the client secret details from file.
            GenericJson secrets = jsonFactory.fromReader(new FileReader(secretsFile), GenericJson.class);

            // Extract the raw key from the supplied JSON file
            String privateKeyString = (String) secrets.get("private_key");
            byte[] keyBytes = new PemReader(new StringReader(privateKeyString))
                    .readNextSection()
                    .getBase64DecodedBytes();

            // Turn it into a PrivateKey
            PrivateKey privateKey = SecurityUtils.getRsaKeyFactory()
                    .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

            // And lastly, turn that into a GoogleCredential
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId((String) secrets.get("client_email"))
                    .setServiceAccountPrivateKey(privateKey)
                    .setServiceAccountScopes(scopes)
                    .build();

            // Force a first-time update, so we have a fresh key
            credential.refreshToken();
            return credential;
        } catch (FileNotFoundException e) {
            throw new AssertionError("File not found should already be handled.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("Encountered an unexpected algorithm when "
                    + "processing the supplied private key.", e);
        } catch (InvalidKeySpecException e) {
            throw new AssertionError("Encountered an invalid key specification when "
                    + "processing the supplied private key.", e);
        }
    }
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
    static class NoUserIdException extends Exception {
    }

}