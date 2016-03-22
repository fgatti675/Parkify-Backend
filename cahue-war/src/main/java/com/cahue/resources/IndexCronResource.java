package com.cahue.resources;

import com.cahue.index.SpotsIndex;
import com.google.inject.name.Named;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Date: 12.09.14
 *
 * @author francesco
 */
@Path("/index")
public class IndexCronResource {


    /**
     * Last timeout time. Every entry in the spots index before this time should have been previously removed.
     */
    private static Date lastTimeout;

    Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    @Named(SpotsIndex.MySQL)
    SpotsIndex spotsIndex;

    @GET
    @Path("/cleanStale")
    public synchronized Response cleanIndex() {

        int deletedCount = spotsIndex.expireStale();
        logger.fine(String.format("Deleted %d entries from spots index", deletedCount));

        return Response.ok().build();
    }

}
