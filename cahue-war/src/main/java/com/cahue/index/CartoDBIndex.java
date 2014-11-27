package com.cahue.index;

import com.cartodb.CartoDBClientIF;
import com.cartodb.CartoDBException;
import com.cartodb.impl.ApiKeyCartoDBClient;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Date: 14.11.14
 *
 * @author francesco
 */
public class CartoDBIndex implements Index {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final String API_KEY = "3037e96df92be2c06ee3d1d1e15c089157c33419";

    private static final String ACCOUNT_NAME = "cahue";
    private static final String TABLE_NAME = "spots";

    CartoDBClientIF cartoDBClient;


    public static final void main(String[] args) throws Exception {
        CartoDBIndex cartoDBIndex = new CartoDBIndex();
        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) - 3);

        Date time = calendar.getTime();
        cartoDBIndex.put("LALALA2", 0D, 0D, new Date());

//        int count = cartoDBIndex.deleteBefore(time);
    }

    public CartoDBIndex() {

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
    public String getTableId() {
        return TABLE_NAME;
    }

    @Override
    public Set<Long> queryByRange(Double latitude, Double longitude, Long range) {
        return null;
    }

    @Override
    public void put(String id, Double latitude, Double longitude, Date time) {

        String sqlString = String.format(
                Locale.ENGLISH,
                "INSERT INTO %s (id, the_geom) VALUES ('%s', ST_SetSRID(ST_Point(%f, %f),4326))",
                getTableId(),
                id,
                longitude,
                latitude
        );
        System.out.println(sqlString);

        // get rows as a Map
        try {
            cartoDBClient.executeQuery(sqlString);
        } catch (CartoDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(List<String> ids) {

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
        System.out.println(sqlString);

        // get rows as a Map
        try {
            cartoDBClient.executeQuery(sqlString);
        } catch (CartoDBException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public void reset() {

    }
}
