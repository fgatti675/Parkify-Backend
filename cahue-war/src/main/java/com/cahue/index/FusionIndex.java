package com.cahue.index;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.FusiontablesScopes;
import com.google.api.services.fusiontables.model.Column;
import com.google.api.services.fusiontables.model.Sqlresponse;
import com.google.api.services.fusiontables.model.Table;
import com.google.api.services.fusiontables.model.TableList;

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
    private Drive drive;

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    public static final void main(String[] args) throws Exception {

        FusionIndex fusionTablesIndex = new FusionIndex();

//        fusionTablesIndex.deleteTable(TABLE_ID);

//        fusionTablesIndex.listTables();
//        fusionTablesIndex.createTable();

//        fusionTablesIndex.put("AAA", 0.1, 0.1, new Date());
//        fusionTablesIndex.queryByRange(0.1, 0.1, 10000L);

        DateTime dateTime = new DateTime(new Date());
        dateTime.getTimeZoneShift()    ;
        System.out.println(dateTime.getTimeZoneShift() );
        System.out.println(dateTime.toStringRfc3339());


//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) - 24);
//
//        Date time = calendar.getTime();
//        int count = fusionTablesIndex.deleteBefore(time);

//        fusionTablesIndex.permissions("");
    }

    public FusionIndex() {
        try {

            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            List scopes = new ArrayList();
            scopes.addAll(FusiontablesScopes.all());
            scopes.addAll(DriveScopes.all());

            Credential credential = GoogleUtils.authorizeService(httpTransport, jsonFactory, scopes);
            fusiontables = new Fusiontables.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            drive = new Drive.Builder(
                    httpTransport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Set<Long> queryByRange(Double latitude, Double longitude, Long range) {
        try {
            String sqlString = String.format(
                    Locale.ENGLISH,
                    "SELECT * FROM %s WHERE ST_INTERSECTS(Location, CIRCLE(LATLNG(%f, %f), %d))",
                    TABLE_ID,
                    latitude,
                    longitude,
                    range);


            Fusiontables.Query.Sql sql = fusiontables.query().sql(sqlString);
            Sqlresponse execute = sql.execute();

        } catch (IllegalArgumentException e) {
            // For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
            // been thrown.
            // Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
            // http://code.google.com/p/google-api-java-client/issues/detail?id=545
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
                    "INSERT INTO " + TABLE_ID +
                            " (Id, Time, Location) "
                            + "VALUES ('%s', '%s', " + "'%f, %f' )",
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
                    TABLE_ID,
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
                            TABLE_ID,
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

    private void permissions(String email) {
        try {


            Permission permission = new Permission().setType("user").setRole("owner").setValue("empanadamental@gmail.com");
//            Permission permission = new Permission().setType("anyone").setRole("reader");
            permission = drive.permissions().insert(TABLE_ID, permission).execute();


            FileList execute = drive.files().list().execute();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
