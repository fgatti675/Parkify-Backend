package com.cahue.resources;

import com.cahue.car2go.Car2GoLocation;
import com.cahue.car2go.Car2GoManager;
import com.cahue.car2go.Car2GoVehicle;
import com.cahue.index.ParkingSpotIndexEntry;
import com.cahue.index.SpotsIndex;
import com.google.inject.name.Named;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Date: 12.09.14
 *
 * @author francesco
 */
@Path("/index")
public class IndexCronResource {

    Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    @Named(SpotsIndex.MySQL)
    SpotsIndex spotsIndex;

    @Inject
    Car2GoManager car2GoManager;

    @GET
    @Path("/cleanStale")
    public synchronized Response cleanIndex() {

        int deletedCount = spotsIndex.expireStale();
        logger.fine(String.format("Deleted %d entries from spots index", deletedCount));

        return Response.ok().build();
    }

    @GET
    @Path("/checkCar2Go")
    public synchronized Response checkCar2Go() {

        Random random = new Random();

        for (Car2GoLocation location : car2GoManager.getLocations()) {
            Set<Car2GoVehicle> vehicles = car2GoManager.getMovedVehicles(location.getLocationName());
            for (Car2GoVehicle movedVehicle : vehicles) {
                Calendar calendar = Calendar.getInstance();
                ParkingSpotIndexEntry indexEntry = new ParkingSpotIndexEntry();
                indexEntry.setTime(calendar.getTime());
                calendar.add(Calendar.MINUTE, SpotsIndex.SPOT_TIMEOUT_M);
                indexEntry.setExpiryTime(calendar.getTime());
                indexEntry.setLatitude(movedVehicle.getLatitude());
                indexEntry.setLongitude(movedVehicle.getLongitude());
                indexEntry.setId(movedVehicle.getVin().hashCode() + random.nextLong());
                indexEntry.setAccuracy(10F);
                spotsIndex.put(indexEntry);
            }
            logger.fine(String.format("Number of car2go spots in %s added: %d", location.getLocationName(), vehicles.size()));
        }

        return Response.ok().build();
    }

}
