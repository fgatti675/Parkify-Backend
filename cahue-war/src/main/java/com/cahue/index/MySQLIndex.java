package com.cahue.index;

import com.cahue.model.transfer.QueryResult;
import com.cahue.model.ParkingSpot;
import com.cahue.persistence.MySQLDataSource;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Date: 16.12.14
 *
 * @author francesco
 */
public class MySQLIndex implements SpotsIndex {

    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    @Inject
    MySQLDataSource dataSource;

    @Override
    public QueryResult queryNearest(Double latitude, Double longitude, int nearest) {
        EntityManager em = dataSource.createRelationalEntityManager();
        String sql = String.format(
                Locale.ENGLISH,
                "SELECT ID, ACCURACY, TIME, X(SPOT) AS LONGITUDE, Y(SPOT) AS LATITUDE \n" +
                        "FROM PARKINGSPOT " +
                        "ORDER BY ST_DISTANCE(GEOMFROMTEXT('POINT(%f %f)', 4326), SPOT) LIMIT %d;",
                longitude,
                latitude,
                nearest
        );
        List resultList = em.createNativeQuery(sql, ParkingSpot.class).getResultList();

        QueryResult result = new QueryResult();
        result.setSpots(resultList);
        return result;
    }

    @Override
    public QueryResult queryArea(Double southwestLatitude, Double southwestLongitude, Double northeastLatitude, Double northeastLongitude) {

        EntityManager em = dataSource.createRelationalEntityManager();
        String sql = String.format(
                Locale.ENGLISH,
                "SELECT ID, ACCURACY, TIME, X(SPOT) AS LONGITUDE, Y(SPOT) AS LATITUDE, FUTURE, EXPIRYTIME" +
                        " FROM PARKINGSPOT" +
                        " WHERE ST_CONTAINS(ST_ENVELOPE(GEOMFROMTEXT('LINESTRING(%f %f,%f %f)', 4326)), SPOT)" +
                        " LIMIT %d;",
                southwestLongitude,
                southwestLatitude,
                northeastLongitude,
                northeastLatitude,
                MAX_RESULTS
        );
        List<ParkingSpotIndexEntry> resultList = em.createNativeQuery(sql, ParkingSpotIndexEntry.class).getResultList();

        QueryResult result = new QueryResult();
        result.setSpots(resultList);
        result.setMoreResults(resultList.size() == MAX_RESULTS);
        return result;
    }

    @Override
    public void put(ParkingSpotIndexEntry entry) {

        EntityManager em = dataSource.createRelationalEntityManager();

        em.getTransaction().begin();
        String sql = String.format(
                Locale.ENGLISH,
                "INSERT INTO PARKINGSPOT (ID, TIME, EXPIRYTIME, ACCURACY, SPOT, FUTURE)" +
                        " VALUES (%d,  NOW(), '%s', %.2f, (GEOMFROMTEXT('POINT(%f %f)', 4326)), %d)" +
                        " ON DUPLICATE KEY UPDATE" +
                        " TIME=VALUES(TIME)," +
                        " EXPIRYTIME=VALUES(EXPIRYTIME)," +
                        " ACCURACY=VALUES(ACCURACY)," +
                        " SPOT=VALUES(SPOT)," +
                        " FUTURE=VALUES(FUTURE);",
                entry.getId(),
                dateFormat.format(entry.getExpiryTime()),
                entry.getAccuracy(),
                entry.getLongitude(),
                entry.getLatitude(),
                entry.isFuture() ? 1 : 0
        );

        Query insertQuery = em.createNativeQuery(sql);
        insertQuery.executeUpdate();
        em.getTransaction().commit();

    }

    @Override
    public int expireStale() {

        EntityManager em = dataSource.createRelationalEntityManager();
        em.getTransaction().begin();
        int deleted = em.createQuery("DELETE FROM ParkingSpotIndexEntry p WHERE p.expiryTime < CURRENT_TIMESTAMP").executeUpdate();
        em.getTransaction().commit();

        return deleted;
    }

    public void clear(){
        EntityManager em = dataSource.createRelationalEntityManager();
        em.getTransaction().begin();
        int deleted = em.createQuery("DELETE FROM ParkingSpotIndexEntry p").executeUpdate();
        em.getTransaction().commit();
    }
}
