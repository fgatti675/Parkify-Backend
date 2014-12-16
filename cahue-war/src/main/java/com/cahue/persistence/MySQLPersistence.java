package com.cahue.persistence;

import com.cahue.DataSource;
import com.cahue.api.ParkingSpot;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * Date: 16.12.14
 *
 * @author francesco
 */
public class MySQLPersistence implements Persistence {

    @Inject
    DataSource dataSource;

    @Override
    public Set<ParkingSpot> queryNearest(Double latitude, Double longitude, int nearest) {
        return null;
    }

    @Override
    public Set<ParkingSpot> queryArea(Double southwestLatitude, Double southwestLongitude, Double northeastLatitude, Double northeastLongitude) {
        return null;
    }

    @Override
    public void put(ParkingSpot spot) {

        EntityManager em = dataSource.createRelationalEntityManager();

        em.getTransaction().begin();
        String sql = String.format("INSERT INTO parkingspot (id, accuracy, time, spot) VALUES (%d, %d, now(), (GeomFromText('POINT(%d %d)')));",
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
        return 0;
    }
}
