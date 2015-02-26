package com.cahue.resources;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cache.AsyncCacheFilter;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Francesco on 08/02/2015.
 */
public class TestHelper {

    public static final String EMAIL_ADDRESS = "582791978228-kl51c8scvc1ombariffo8bsnf25qf7st@developer.gserviceaccount.com";

    JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    HttpTransport httpTransport = new NetHttpTransport();

    private static final List<String> SCOPES = Arrays.asList(
            "email",
            "profile");

    Closeable session;

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig(),
            new LocalMemcacheServiceTestConfig());


    @Before
    public void setUp() {
        session = ObjectifyService.begin();
        helper.setUp();
    }

    @After
    public void tearDown() throws IOException {
        AsyncCacheFilter.complete();
        helper.tearDown();
        session.close();
        session = null;
    }

    protected String getGoogleAuthToken(){
        try {

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("Cahue-4d17cda7873b.p12").getFile());
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setServiceAccountId(EMAIL_ADDRESS)
                    .setServiceAccountPrivateKeyFromP12File(file)
                    .setServiceAccountScopes(SCOPES)
                    .build();

            credential.refreshToken();

            return credential.getAccessToken();

        } catch (GeneralSecurityException |IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
