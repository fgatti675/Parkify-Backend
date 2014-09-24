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

    static final String PROJECT_ID = "13309007342718171126";
    static final String SPOTS_TABLE_ID = "spots-table";
    static final String PUBLIC_SERVER_API_KEY = "AIzaSyDbQbpQJDM0HoNDEstvLZI2y4HD0Pw4GzM";

    private MapsEngine engine;

    public MapsEngineIndex() {
        try {
            HttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            // This request initializer will ensure the API key is sent with every HTTP request.
            MapsEngineRequestInitializer apiKeyInitializer =
                    new MapsEngineRequestInitializer(PUBLIC_SERVER_API_KEY);

            Credential credential = Utils.authorizeService(transport, jsonFactory, MapsEngineScopes.all());
            engine = new MapsEngine.Builder(transport, jsonFactory, credential)
                    .setApplicationName("Cahue Test")
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {

        MapsEngineIndex mapsEngineIndex = new MapsEngineIndex();

        mapsEngineIndex.put("TEST", 0.0, 0.0, new Date());
    }

    private void listProjects() {
        try {
            ProjectsListResponse execute = engine.projects().list().execute();
            System.out.println(execute);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createTable() {

        Schema schema = new Schema();

        schema.setColumns(new ArrayList<TableColumn>());
        schema.getColumns().add(new TableColumn().setName("geometry").setType("points"));
        schema.getColumns().add(new TableColumn().setName("id").setType("string"));
        schema.getColumns().add(new TableColumn().setName("date").setType("string"));
        schema.setPrimaryKey("id");

        Table table = new Table()
                .setName(SPOTS_TABLE_ID)
                .setProjectId(PROJECT_ID)
                .setSchema(schema);
        try {
            Table execute = engine.tables().create(table).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readFeaturesFromTable() throws IOException {
        // Query the table for offices in WA that are within 100km of Perth.
        FeaturesListResponse featResp = engine.tables().features().list(SPOTS_TABLE_ID)
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
    public void put(String id, Double latitude, Double longitude, Date time) {

        try {
            FeaturesBatchInsertRequest insertRequest = new FeaturesBatchInsertRequest();
            insertRequest.setFeatures(Arrays.asList(createFeature(id, latitude, longitude, time)));
            System.out.println(engine.tables().features().batchInsert(SPOTS_TABLE_ID, insertRequest).execute());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void delete(List<String> ids) {

    }

    @Override
    public int deleteBefore(Date date) {
        return 0;
    }

    @Override
    public void reset() {

    }

    private Feature createFeature(String id, Double latitude, Double longitude, Date time) {
        Feature feature = new Feature();
        feature.setType("Feature");
        GeoJsonPoint point = new GeoJsonPoint();
        point.setCoordinates(Arrays.asList(latitude, longitude));
        GeoJsonProperties properties = new GeoJsonProperties();
        properties.put("id", id);
        properties.put("date", time.toString());
        feature.setGeometry(point);
        feature.setProperties(properties);
        System.out.println(feature);
        return feature;
    }
}
