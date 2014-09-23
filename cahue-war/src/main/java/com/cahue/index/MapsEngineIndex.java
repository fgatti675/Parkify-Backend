package com.cahue.index;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.mapsengine.MapsEngine;
import com.google.api.services.mapsengine.MapsEngineRequestInitializer;
import com.google.api.services.mapsengine.MapsEngineScopes;
import com.google.api.services.mapsengine.model.*;

import java.io.IOException;
import java.util.*;

/**
 * Date: 23.09.14
 *
 * @author francesco
 */
public class MapsEngineIndex implements Index {

    static final String PROJECT_ID = "582791978228";
    static final String SPOTS_TABLE_ID = "cahue-spots-table";
    static final String PUBLIC_API_KEY = "AIzaSyDbQbpQJDM0HoNDEstvLZI2y4HD0Pw4GzM";
    static final Collection<String> SCOPES = Arrays.asList(MapsEngineScopes.MAPSENGINE);

    public static void main(String[] args) throws Exception {

        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        // This request initializer will ensure the API key is sent with every HTTP request.
        MapsEngineRequestInitializer apiKeyInitializer =
                new MapsEngineRequestInitializer(PUBLIC_API_KEY);

        Credential credential = Utils.authorizeService(transport, jsonFactory, SCOPES);

        MapsEngine engine = new MapsEngine.Builder(transport, jsonFactory, credential)
                .setMapsEngineRequestInitializer(apiKeyInitializer)
                .setApplicationName("Cahue")
                .build();

        createTable(engine);
//        readFeaturesFromTable(engine);
    }

    public static void createTable(MapsEngine me) {

        Table table = new Table();
        table.setName(SPOTS_TABLE_ID);
        table.setProjectId(PROJECT_ID);
        table.setSchema(new Schema());
        table.getSchema().set("geometry ", "points");
        table.getSchema().set("id", "string");
        table.getSchema().set("date", "string");
        table.getSchema().setPrimaryKey("id");

        try {
            Table execute = me.tables().create(table).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void readFeaturesFromTable(MapsEngine me) throws IOException {
        // Query the table for offices in WA that are within 100km of Perth.
        FeaturesListResponse featResp = me.tables().features().list(SPOTS_TABLE_ID)
                .setVersion("published")
                .setWhere("State='WA' AND ST_DISTANCE(geometry,ST_POINT(115.8589,-31.9522)) < 100000")
                .execute();

        for (Feature feat : featResp.getFeatures()) {
            System.out.println(
                    "Properties: " + feat.getProperties().toString() + "\n\t" +
                            "Name: " + feat.getProperties().get("Fcilty_nam") + "\n\t" +
                            "Geometry Type: " + feat.getGeometry().getType());

            if (feat.getGeometry() instanceof GeoJsonPoint) {
                GeoJsonPoint point = (GeoJsonPoint) feat.getGeometry();
                System.out.println("\t" +
                        "Longitude: " + point.getCoordinates().get(0) + ", " +
                        "Latitude: " + point.getCoordinates().get(1));
            } else {
                System.out.println("Only points are expected in this table!");
                return;
            }
        }
    }

    @Override
    public Set<Long> query(Double latitude, Double longitude, Long range) {
        return null;
    }

    @Override
    public void put(Long id, Double latitude, Double longitude, Date time) {

    }

    @Override
    public void delete(List<String> docIds) {

    }

    @Override
    public int deleteBefore(Date date) {
        return 0;
    }

    @Override
    public void reset() {

    }
}
