package com.cahue.index;

import com.cahue.model.transfer.QueryResult;
import com.cahue.model.ParkingSpot;

import java.util.Date;

/**
 * Date: 16.12.14
 *
 * @author francesco
 */
public interface SpotsIndex {

    public static final Integer SPOT_TIMEOUT_H = 2; // 2 hours

    public final static String CartoDB = "CartoDB";
    public final static String MySQL = "MySQL";

    QueryResult queryNearest(Double latitude, Double longitude, int nearest);

    QueryResult queryArea(
            Double southwestLatitude,
            Double southwestLongitude,
            Double northeastLatitude,
            Double northeastLongitude);

    void put(ParkingSpotIndexEntry spot);

    int expireStale();

    void clear();
}
