package com.cahue.index;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.FusiontablesScopes;
import com.google.api.services.fusiontables.model.Column;
import com.google.api.services.fusiontables.model.Table;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Date: 02.10.14
 *
 * @author francesco
 */
public class FusionIndex implements Index {

    private static final String APPLICATION_NAME = "Cahue";
    private static final String TABLE_NAME = "Spots";
    private static final String SPOTS_TABLE_ID = "parking-spots";

    private Fusiontables fusiontables;

    public static final void main(String[] args) throws Exception {

        FusionIndex fusionTablesIndex = new FusionIndex();
        fusionTablesIndex.createTable();

    }

    public FusionIndex() {
        try {

            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();


            Credential credential = GoogleUtils.authorizeService(httpTransport, jsonFactory, FusiontablesScopes.all());
            fusiontables = new Fusiontables.Builder(
                    httpTransport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Set<Long> query(Double latitude, Double longitude, Long range) {
        return null;
    }

    @Override
    public void put(String id, Double latitude, Double longitude, Date time) {

        try {


            String sqlString = String.format("INSERT INTO " + SPOTS_TABLE_ID +
                    " (Id, Time, Location) "
                    + "VALUES (" + "%s, " + "%s, " + "(%d, %d))",
                    id,
                    new DateTime(time),
                    latitude,
                    longitude);

            System.out.println(sqlString);

            Fusiontables.Query.Sql sql = fusiontables.query().sql(sqlString);
            sql.execute();

        } catch (IllegalArgumentException e) {
            // For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
            // been thrown.
            // Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
            // http://code.google.com/p/google-api-java-client/issues/detail?id=545
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


    /**
     * Create a table for the authenticated user.
     */
    private String createTable() throws IOException {

        // Create a new table
        Table table = new Table();
        table.setTableId(SPOTS_TABLE_ID);
        table.setName(TABLE_NAME);
        table.setIsExportable(false);
        table.setDescription("Parking Spots Table");

        // Set columns for new table
        table.setColumns(Arrays.asList(new Column().setName("Id").setType("STRING"),
                new Column().setName("Time").setType("DATETIME"),
                new Column().setName("Location").setType("LOCATION")));

        // Adds a new column to the table.
        Fusiontables.Table.Insert t = fusiontables.table().insert(table);
        Table r = t.execute();

        return r.getTableId();
    }

}
