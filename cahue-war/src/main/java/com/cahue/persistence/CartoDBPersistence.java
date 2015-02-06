package com.cahue.persistence;

import com.cahue.model.transfer.QueryResult;
import com.cahue.model.ParkingSpot;
import com.cahue.persistence.Persistence;
import com.cartodb.CartoDBClientIF;
import com.cartodb.CartoDBException;
import com.cartodb.impl.ApiKeyCartoDBClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Date: 14.11.14
 *
 * @author francesco
 */
public class CartoDBPersistence implements Persistence {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat resultDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final String API_KEY = "3037e96df92be2c06ee3d1d1e15c089157c33419";

    private static final String ACCOUNT_NAME = "cahue";
    private static final String TABLE_NAME = "spots";

    private static final String URL = "http://" + ACCOUNT_NAME + ".cartodb.com/api/v2/sql?format=GeoJSON";

    Logger logger = Logger.getLogger(getClass().getName());
    CartoDBClientIF cartoDBClient;


    public static final void main(String[] args) throws Exception {
        Persistence persistence = new CartoDBPersistence();
        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) - 3);

        Date time = calendar.getTime();
        ParkingSpot spot = new ParkingSpot();
//        spot.setId(1L);
        spot.setLatitude(0D);
        spot.setLongitude(0D);
        spot.setTime(new Date());

        persistence.put(spot);
        System.out.println(persistence.queryArea(-1D, -1D, 1D, 1D));

//        int count = cartoDBIndex.deleteBefore(time);
    }

    public CartoDBPersistence() {

        try {
            cartoDBClient = new ApiKeyCartoDBClient(ACCOUNT_NAME, API_KEY);
        } catch (CartoDBException e) {
            e.printStackTrace();
        }
    }


    /**
     * Table the query will be run against.
     * Can be overridden for testing purposes.
     *
     * @return
     */
    protected String getTableId() {
        return TABLE_NAME;
    }

    public QueryResult queryNearest(Double latitude, Double longitude, int nearest) {

        String sqlString = String.format(
                Locale.ENGLISH,
                "SELECT created_at, id, the_geom FROM %s " +
                        "ORDER BY the_geom <-> ST_SetSRID(ST_Point(%f, %f),4326) " +
                        "LIMIT %d",
                getTableId(),
                longitude,
                latitude,
                nearest);

        return retrieve(sqlString);
    }

    public QueryResult queryArea(
            Double southwestLatitude,
            Double southwestLongitude,
            Double northeastLatitude,
            Double northeastLongitude) {

        String sqlString = String.format(
                Locale.ENGLISH,
                "SELECT created_at, id, the_geom FROM %s " +
                        "WHERE the_geom && ST_MakeEnvelope(%f, %f, %f, %f, 4326)",
                getTableId(),
                southwestLongitude,
                southwestLatitude,
                northeastLongitude,
                northeastLatitude
        );

        return retrieve(sqlString);
    }

    private QueryResult retrieve(String sql) {

        List<ParkingSpot> spots = new ArrayList<>();
        QueryResult result = new QueryResult();

        String sqlString = sql;

        JSONObject json = doQuery(sqlString);
        if (json == null) {
            result.setError(true);
        } else {
            try {
                JSONArray rows = json.getJSONArray("features");
                for (int i = 0; i < rows.length(); i++) {

                    JSONObject entry = rows.getJSONObject(i);

                    JSONObject properties = entry.getJSONObject("properties");
                    String id = properties.getString("id");
                    Date date = resultDateFormat.parse(properties.getString("created_at"));

                    JSONObject geometry = entry.getJSONObject("geometry");
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    double lat = coordinates.getDouble(1);
                    double lon = coordinates.getDouble(0);

                    ParkingSpot spot = new ParkingSpot();
//                    spot.setId(Long.parseLong(id));
                    spot.setLatitude(lat);
                    spot.setLongitude(lon);
                    spot.setTime(date);
                    spots.add(spot);
                }

                result.setSpots(spots);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private JSONObject doQuery(String sql) {
        try {

            String url = URL + "&q=" + URLEncoder.encode(sql, "ISO-8859-1");

            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-type", "application/json");


            HttpResponse response = httpclient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity());
                JSONObject json = new JSONObject(result);
                return json;
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void put(ParkingSpot spot) {

        String sqlString = String.format(
                Locale.ENGLISH,
                "INSERT INTO %s (id, the_geom) VALUES ('%s', ST_SetSRID(ST_Point(%f, %f),4326))",
                getTableId(),
                spot.getId(),
                spot.getLongitude(),
                spot.getLatitude()
        );
        logger.finer(sqlString);

        // get rows as a Map
        try {
            cartoDBClient.executeQuery(sqlString);
        } catch (CartoDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int deleteBefore(Date date) {

        String sqlString = String.format(
                Locale.ENGLISH,
                "DELETE FROM %s WHERE created_at < '%s'",
//                "DELETE FROM %s WHERE (NOW() - INTERVAL '1 day') < created_at",

                getTableId(),
                dateFormat.format(date)
        );
        logger.finer(sqlString);

        // get rows as a Map
        try {
            cartoDBClient.executeQuery(sqlString);
        } catch (CartoDBException e) {
            e.printStackTrace();
        }

        return 0;
    }

}
