package com.cahue.debug;

import com.cahue.model.ParkingSpot;

import java.util.Random;

/**
 * Created by Francesco on 22/10/2014.
 */
public class RandomSpotsGenerator {

    private static final String TEST_TABLE_ID = "1Pa5hqK1KxKwgZmbgBFJ5opcbRGHELFsCL6CyE8bf";
    private static final ParkingSpot CENTER = new ParkingSpot() {{
        setLatitude(40.435165);
        setLongitude(-3.69684243);
        setAccuracy(0F);
    }};

    Random random = new Random();


    public static void main(String[] args) throws Exception {
        new RandomSpotsGenerator().generate();
    }

    public void generate() {

        for (int i = 0; i < 1000; i++) {
            ParkingSpot randomLocation = getRandomLocationWithin(CENTER, random.nextInt(5000));
            System.out.println(randomLocation);
//            index.put(UUID.randomUUID().toString(), randomLocation.getLatitude(), randomLocation.getLongitude(), new Date());
        }

    }

    private ParkingSpot getRandomLocationWithin(ParkingSpot center, int radius) {
        //Position, decimal degrees
        double lat = center.getLatitude();
        double lon = center.getLongitude();

        // Earth’s radius, sphere
        double R = 6378137;

        // Coordinate offsets in radians
        double dLat = radius / R;
        double dLon = radius / (R * Math.cos(Math.PI * lat / random.nextDouble() * 360));

        // OffsetPosition, decimal degrees
        double nLat = lat + dLat * (random.nextDouble() * 2 - 1) * 360 / Math.PI;
        double nLon = lon + dLon * (random.nextDouble() * 2 - 1) * 360 / Math.PI;

        ParkingSpot parkingSpot = new ParkingSpot();
        parkingSpot.setLatitude(nLat);
        parkingSpot.setLongitude(nLon);
        parkingSpot.setAccuracy(0F);
        return parkingSpot;
    }

}
