package com.cahue.index;

import com.cahue.model.transfer.QueryResult;

/**
 * Date: 16.12.14
 *
 * @author francesco
 */
public interface SpotsIndex {

    int FUTURE_SPOT_TIMEOUT_M = 3; // 3 minutes
    int SPOT_TIMEOUT_M = 20; // 20 minutes

    int MAX_RESULTS = 100;

    String CartoDB = "CartoDB";
    String MySQL = "MySQL";

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
