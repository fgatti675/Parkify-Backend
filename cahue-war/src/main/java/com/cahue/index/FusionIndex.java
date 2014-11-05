package com.cahue.index;

import com.cahue.util.GoogleAuth;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.FusiontablesScopes;
import com.google.api.services.fusiontables.model.Column;
import com.google.api.services.fusiontables.model.Sqlresponse;
import com.google.api.services.fusiontables.model.Table;
import com.google.api.services.fusiontables.model.TableList;
import org.mortbay.log.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Date: 02.10.14
 *
 * @author francesco
 */
public class FusionIndex implements Index {

    private static final String APPLICATION_NAME = "Cahue";
    private static final String TABLE_NAME = "Spots";
    private static final String TABLE_ID = "1KdObSc-BOSKNnH9zyei7WG--X1w4AyomUj-pB7Ii";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Fusiontables fusiontables;

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    // TODO: REMOVE
    public static final void main(String[] args) throws Exception {

        FusionIndex fusionTablesIndex = new FusionIndex();

//        fusionTablesIndex.deleteTable(TABLE_ID);

//        fusionTablesIndex.listTables();
//        fusionTablesIndex.createTable();

//        fusionTablesIndex.put("AAA", 0.1, 0.1, new Date());
//        fusionTablesIndex.queryByRange(0.1, 0.1, 10000L);



        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) - 3);

        Date time = calendar.getTime();
        int count = fusionTablesIndex.deleteBefore(time);

//        fusionTablesIndex.permissions("");
    }

    public FusionIndex() {
        fusiontables = createFusionTablesInstance();
    }

    /**
     * Create a Fusion tables object
     * @return
     */
    public Fusiontables createFusionTablesInstance() {

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


    @Override
    public Set<Long> queryByRange(Double latitude, Double longitude, Long range) {
        try {
            String sqlString = String.format(
                    Locale.ENGLISH,
                    "SELECT * FROM %s WHERE ST_INTERSECTS(Location, CIRCLE(LATLNG(%f, %f), %d))",
                    getTableId(),
                    latitude,
                    longitude,
                    range);


            Fusiontables.Query.Sql sql = fusiontables.query().sql(sqlString);
            Sqlresponse execute = sql.execute();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Set<Long> queryRectangle(Double latitudeNE, Double longitudeNE, Double latitudeSW, Double longitudeSW) {
        try {

            String sqlString = String.format(
                    Locale.ENGLISH,
                    "SELECT * FROM %s WHERE ST_INTERSECTS(Location, RECTANGLE(LATLNG(%f, %f), LATLNG(%f, %f)))",
                    getTableId(),
                    latitudeSW,
                    longitudeSW,
                    latitudeNE,
                    longitudeNE
            );


            Fusiontables.Query.Sql sql = fusiontables.query().sql(sqlString);
            Sqlresponse execute = sql.execute();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void put(String id, Double latitude, Double longitude, Date time) {

        try {

            String sqlString = String.format(
                    Locale.ENGLISH,
                    "INSERT INTO %s" +
                            " (Id, Time, Location) "
                            + "VALUES ('%s', '%s', '%f, %f' )",
                    getTableId(),
                    id,
                    dateFormat.format(time),
                    latitude,
                    longitude);

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

        int count = 0;

        try {

            String selectString = String.format(
                    Locale.ENGLISH,
                    "SELECT ROWID FROM %s WHERE Time < '%s'",
                    getTableId(),
                    dateFormat.format(date));

            Sqlresponse idsResponse = fusiontables.query().sql(selectString).execute();

            logger.fine(idsResponse.toPrettyString());

            List<ArrayList> rows = (List<ArrayList>) idsResponse.get("rows");
            if (rows != null) {
                for (ArrayList<String> element : rows) {
                    String id = element.get(0);
                    String deleteString = String.format(
                            Locale.ENGLISH,
                            "DELETE FROM %s WHERE ROWID = '%s'",
                            getTableId(),
                            id);
                    Sqlresponse deleteResponse = fusiontables.query().sql(deleteString).execute();
                    count++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return count;
    }

    protected String getTableId(){
        return TABLE_ID;
    }

    @Override
    public void reset() {

    }

    /**
     * List tables for the authenticated user.
     */
    private void listTables() throws IOException {

        // Fetch the table list
        Fusiontables.Table.List listTables = fusiontables.table().list();
        TableList tablelist = listTables.execute();

        if (tablelist.getItems() == null || tablelist.getItems().isEmpty()) {
            System.out.println("No tables found!");
            return;
        }

        for (Table table : tablelist.getItems()) {
            show(table);
        }
    }

    private void deleteTable(String id) {
        try {
            fusiontables.table().delete(id).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Create a table for the authenticated user.
     */
    private String createTable() throws IOException {

        // Create a new table
        Table table = new Table();
        table.setName(TABLE_NAME);
        table.setIsExportable(true);
        table.setDescription("Parking Spots Table");

        // Set columns for new table
        table.setColumns(Arrays.asList(new Column().setName("Id").setType("STRING"),
                new Column().setName("Time").setType("DATETIME"),
                new Column().setName("Location").setType("LOCATION")));

        // Adds a new column to the table.
        Table r = fusiontables.table().insert(table).execute();

        show(r);

        return r.getTableId();
    }


    static void show(Table table) {
        System.out.println("id: " + table.getTableId());
        System.out.println("name: " + table.getName());
        System.out.println("description: " + table.getDescription());
        System.out.println("exportable: " + table.getIsExportable());
        System.out.println("attribution: " + table.getAttribution());
        System.out.println("attribution link: " + table.getAttributionLink());
        System.out.println("kind: " + table.getKind());
        System.out.println("------------------------------------------------------");
        System.out.println();
    }

}
