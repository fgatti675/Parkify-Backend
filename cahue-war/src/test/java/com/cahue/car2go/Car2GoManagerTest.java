package com.cahue.car2go;

import org.junit.Test;

import java.util.Set;

/**
 * Created by f.gatti.gomez on 13/12/2016.
 */
public class Car2GoManagerTest {

    @Test
    public void vehiclesTest() {
        Car2GoManager manager = new Car2GoManager();
        Set<Car2GoVehicle> vehicles = manager.getVehicles("madrid");
        System.out.println(vehicles);
    }

    @Test
    public void movedVehiclesTest() {
        Car2GoManager manager = new Car2GoManager();
        for(int i = 0; i<10; i++){

            Set<Car2GoVehicle> vehicles = manager.getMovedVehicles("München");
            System.out.println(vehicles);
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}