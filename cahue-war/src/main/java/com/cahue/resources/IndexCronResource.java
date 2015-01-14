package com.cahue.resources;

import com.cahue.persistence.Persistence;
import com.google.inject.name.Named;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Date: 12.09.14
 *
 * @author francesco
 */
@Path("/index")
public class IndexCronResource {

    private final Integer SPOT_TIMEOUT_H = 2; // 2 hours

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    /**
     * Last timeout time. Every entry in the cartoDBPersistence before this time should have been previously removed.
     */
    private static Date lastTimeout;

    @Inject
    @Named(Persistence.MySQL)
    Persistence persistence;

    @GET
    @Path("/cleanStale")
    public synchronized Response cleanIndex() {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -SPOT_TIMEOUT_H);

        Date time = calendar.getTime();

        int deletedCount = persistence.deleteBefore(time);
        logger.fine(String.format("Deleted %d entries from spots index", deletedCount));

        return Response.ok().build();
    }

}
