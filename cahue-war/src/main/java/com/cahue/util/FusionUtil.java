package com.cahue.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.FusiontablesScopes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Francesco on 22/10/2014.
 */
public class FusionUtil {

    private static final String APPLICATION_NAME = "Cahue";

    public static Fusiontables createFusionTablesInstance() {

        try {
            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            List scopes = new ArrayList();
            scopes.addAll(FusiontablesScopes.all());

            Credential credential = GoogleAuth.authorizeService(httpTransport, jsonFactory, scopes);
            return new Fusiontables.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
