package com.cahue.persistence;

import com.cahue.model.QueryResult;
import com.cahue.model.ParkingSpot;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Date: 16.12.14
 *
 * @author francesco
 */
public class MySQLPersistence implements Persistence {

    private static final int MAX_RESULTS = 200;

    @Inject
    DataSource dataSource;

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
                "SELECT ID, ACCURACY, TIME, X(SPOT) AS LONGITUDE, Y(SPOT) AS LATITUDE " +
                        "FROM PARKINGSPOT " +
                        "WHERE ST_CONTAINS(ST_ENVELOPE(GEOMFROMTEXT('LINESTRING(%f %f,%f %f)', 4326)), SPOT) " +
                        "LIMIT %d;",
                southwestLongitude,
                southwestLatitude,
                northeastLongitude,
                northeastLatitude,
                MAX_RESULTS
        );
        List resultList = em.createNativeQuery(sql, ParkingSpot.class).getResultList();

        QueryResult result = new QueryResult();
        result.setSpots(resultList);
        result.setMoreResults(resultList.size() == MAX_RESULTS);
        return result;
    }

    @Override
    public void put(ParkingSpot spot) {

        EntityManager em = dataSource.createRelationalEntityManager();

        em.getTransaction().begin();
        String sql = String.format(
                Locale.ENGLISH,
                "INSERT INTO PARKINGSPOT (ID, ACCURACY, TIME, SPOT) VALUES (%d, %.2f, NOW(), (GEOMFROMTEXT('POINT(%f %f)', 4326)));",
                spot.getId(),
                spot.getAccuracy(),
                spot.getLongitude(),
                spot.getLatitude()
        );
        Query insertQuery = em.createNativeQuery(sql);
        insertQuery.executeUpdate();
        em.getTransaction().commit();

    }

    @Override
    public int deleteBefore(Date date) {

        EntityManager em = dataSource.createRelationalEntityManager();
        em.getTransaction().begin();
        int deleted = em.createQuery("DELETE FROM ParkingSpot p WHERE p.time < :time ").setParameter("time", date).executeUpdate();
        em.getTransaction().commit();

        return deleted;
    }
}
