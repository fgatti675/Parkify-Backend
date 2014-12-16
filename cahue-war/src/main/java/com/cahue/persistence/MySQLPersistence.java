package com.cahue.persistence;

import com.cahue.DataSource;
import com.cahue.api.ParkingSpot;
import com.cahue.api.QueryResult;

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

    @Inject
    DataSource dataSource;

    @Override
    public QueryResult queryNearest(Double latitude, Double longitude, int nearest) {
        EntityManager em = dataSource.createRelationalEntityManager();
        String sql = String.format(
                Locale.ENGLISH,
                "SELECT id, accuracy, time, x(spot) as longitude, y(spot) as latitude \n" +
                        "FROM parkingspot " +
                        "ORDER BY st_distance(GeomFromText('POINT(%f %f)', 4326), spot) LIMIT %d;",
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
                "SELECT id, accuracy, time, x(spot) as longitude, y(spot) as latitude " +
                        "FROM parkingspot " +
                        "WHERE ST_contains(ST_Envelope(GeomFromText('LineString(%f %f,%f %f)', 4326)), spot);",
                southwestLongitude,
                southwestLatitude,
                northeastLongitude,
                northeastLatitude
        );
        List resultList = em.createNativeQuery(sql, ParkingSpot.class).getResultList();

        QueryResult result = new QueryResult();
        result.setSpots(resultList);
        return result;
    }

    @Override
    public void put(ParkingSpot spot) {

        EntityManager em = dataSource.createRelationalEntityManager();

        em.getTransaction().begin();
        String sql = String.format(
                Locale.ENGLISH,
                "INSERT INTO parkingspot (id, accuracy, time, spot) VALUES (%d, %.2f, now(), (GeomFromText('POINT(%f %f)', 4326)));",
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
