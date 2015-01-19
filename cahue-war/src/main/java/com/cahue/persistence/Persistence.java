package com.cahue.persistence;

import com.cahue.model.transfer.QueryResult;
import com.cahue.model.ParkingSpot;

import java.util.Date;

/**
 * Date: 16.12.14
 *
 * @author francesco
 */
public interface Persistence {

    public final static String CartoDB = "CartoDB";
    public final static String MySQL = "MySQL";


    QueryResult queryNearest(Double latitude, Double longitude, int nearest);

    QueryResult queryArea(
            Double southwestLatitude,
            Double southwestLongitude,
            Double northeastLatitude,
            Double northeastLongitude);

    void put(ParkingSpot spot);

    int deleteBefore(Date date);
}
