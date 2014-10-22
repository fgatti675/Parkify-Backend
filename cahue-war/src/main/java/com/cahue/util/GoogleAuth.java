package com.cahue.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.PemReader;
import com.google.api.client.util.SecurityUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;

/**
 * Common OAuth code used across the Java examples.
 */
public class GoogleAuth {


    private static final String SERVICE_KEY_FILE = "service_key.json";

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


}