package com.cahue;


import com.cahue.api.ParkingSpot;
import com.cahue.config.TestModule;
import com.cahue.config.guice.ProductionModule;
import com.cahue.persistence.MySQLPersistence;
import com.google.inject.util.Modules;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Date;
import java.util.Random;

/**
 * Date: 17.12.14
 *
 * @author francesco
 */
@RunWith(JukitoRunner.class)
public class CapacityTest extends BaseTest {

    /**
     * Overrides the common bindings from TestBase with the
     * module that has test-specific bindings for Foo.
     */
    public static class Module extends JukitoModule {
        protected void configureTest() {
            install(Modules.override(new ProductionModule()).with(new TestModule()));
        }
    }

    @Inject
    MySQLPersistence persistence = new MySQLPersistence();

    @Before
    public void init() {
        System.out.println("Init");
        Random r = new Random();
        int amount = 1000000;
        for (int i = 0; i < amount; i++) {
            ParkingSpot spot = new ParkingSpot();
            spot.setId((long) i);
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

    @Test
    public void responseTest() {


    }
}
