package com.cahue.resources;

import com.cahue.DataSource;
import com.cahue.api.ParkingSpot;
import com.cahue.index.IndexManager;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Date: 12.09.14
 *
 * @author francesco
 */
@Path("/index")
public class IndexCronResource {

    private final Integer SPOT_TIMEOUT = 2 * 60 * 60 * 1000; // 2 hours in ms

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    /**
     * Last timeout time. Every entry in the index before this time should have been previously removed.
     */
    private static Date lastTimeout;

    @Inject
    DataSource dataSource;

    @Inject
    IndexManager indexManager;

    @GET
    @Path("/cleanStale")
    public synchronized Response cleanIndexFromLastTimeout() {

        Date timeout = getTimeOutDate();

        if (lastTimeout == null) {
            return cleanIndexBeforeYesterday();
        }

        // is this possible?
        if (lastTimeout.after(timeout))
            return Response.notModified().build();

        EntityManager em = dataSource.createEntityManager();
        //noinspection JpaQlInspection
        List<ParkingSpot> staleSpots =
                em.createQuery("SELECT p FROM ParkingSpot p WHERE p.time BETWEEN :startDate AND :endDate", ParkingSpot.class)
                        .setParameter("startDate", lastTimeout)
                        .setParameter("endDate", timeout)
                        .getResultList();

        doIndexCleanUpOf(staleSpots);


        String result = String.format("Cleared %d entries from index from %s to %s",
                staleSpots.size(),
                lastTimeout,
                timeout);
        logger.info(result);

        lastTimeout = timeout;

        return Response.ok(result).build();
    }

    /**
     * Remove all the entries before 00:00 of the day indicated as a parameter
     *
     * @return
     */
    public synchronized Response cleanIndexBeforeYesterday() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        lastTimeout =  calendar.getTime();

        int count = indexManager.deleteBefore(lastTimeout);

        String result = String.format("Cleared %d entries from index in initial run before %s",
                count,
                lastTimeout);

        logger.info(result);

        return Response.ok(result).build();
    }


    /**
     * Remove the index entries of the indicated parking slots
     *
     * @param staleSpots
     */
    private void doIndexCleanUpOf(List<ParkingSpot> staleSpots) {

        List<String> docIds = new ArrayList<>();
        for (ParkingSpot spot : staleSpots) {
            docIds.add(spot.getId().toString());
        }

        indexManager.delete(docIds);

    }

    private Date getTimeOutDate() {
        Date timeout = new Date();
        timeout.setTime(timeout.getTime() - SPOT_TIMEOUT);
        return timeout;
    }

    @GET
    @Path("/reset")
    public Response reset() {

        indexManager.reset();

        return Response.ok(String.format("Index has been reset")).build();
    }
}
