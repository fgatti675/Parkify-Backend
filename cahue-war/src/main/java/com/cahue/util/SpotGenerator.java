package com.cahue.util;


import com.cahue.model.ParkingSpot;
import com.cahue.persistence.MySQLIndex;

import javax.inject.Inject;
import java.util.Date;
import java.util.Random;

/**
 * Date: 17.12.14
 *
 * @author francesco
 */
public class SpotGenerator {

    @Inject
    MySQLIndex persistence = new MySQLIndex();

    public void generate() {
        System.out.println("Init");
        Random r = new Random();
        int amount = 1000000;
        for (int i = 0; i < amount; i++) {
            ParkingSpot spot = new ParkingSpot();
//            spot.setId((long) i);
            spot.setLatitude(r.nextDouble() * 180 - 90);
            spot.setLongitude(r.nextDouble() * 360 - 180);
            spot.setAccuracy(r.nextFloat() * 12);
            spot.setTime(new Date());
            persistence.put(spot);
            if (i % 1000 == 0) {
                System.out.println(i + " / " + amount);
            }
        }
    }
}
