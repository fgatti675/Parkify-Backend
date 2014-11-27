package com.cahue.debug;

import com.cahue.api.Location;
import com.cahue.index.Index;
import com.cahue.index.TestFusionIndex;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Francesco on 22/10/2014.
 */
public class RandomSpotsGenerator {

    private static final String TEST_TABLE_ID = "1Pa5hqK1KxKwgZmbgBFJ5opcbRGHELFsCL6CyE8bf";
    private static final Location CENTER = new Location(40.435165, -3.69684243, 0F);

    Index index = new TestFusionIndex();

    Random random = new Random();


    public static void main(String[] args) throws Exception {
        new RandomSpotsGenerator().generate();
    }

    public void generate() {

        for (int i = 0; i < 1000; i++) {
            Location randomLocation = getRandomLocationWithin(CENTER, random.nextInt(5000));
            System.out.println(randomLocation);
            index.put(UUID.randomUUID().toString(), randomLocation.getLatitude(), randomLocation.getLongitude(), new Date());
        }

    }

    private Location getRandomLocationWithin(Location center, int radius) {
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

        return new Location(nLat, nLon, 0F);
    }

}