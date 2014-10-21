package com.cahue.resources;

import com.cahue.DataSource;
import com.cahue.index.Index;

import javax.inject.Inject;
import javax.inject.Named;
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

    private final Integer SPOT_TIMEOUT_H = 24; // 2 hours in ms

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    /**
     * Last timeout time. Every entry in the index before this time should have been previously removed.
     */
    private static Date lastTimeout;

    @Inject
    DataSource dataSource;

    @Inject
    @Named("Fusion")
    Index index;

    @GET
    @Path("/cleanStale")
    public synchronized Response cleanIndex() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) - SPOT_TIMEOUT_H);

        Date time = calendar.getTime();
        int count = index.deleteBefore(time);

        String result = String.format("Cleared %d entries from index before %s", count, time);
        if (count > 0)
            logger.info(result);

        return Response.ok(result).build();
    }

}
