package com.cahue.model.transfer;

import com.cahue.model.ParkingSpot;

import java.util.List;

/**
 * Created by Francesco on 11/12/2014.
 */
public class QueryResult {

    /**
     * Was there an error retrieving data
     */
    boolean error = false;

    /**
     * The results included are not complete
     */
    boolean moreResults = false;

    List<ParkingSpot> spots;

    public boolean isMoreResults() {
        return moreResults;
    }

    public void setMoreResults(boolean moreResults) {
        this.moreResults = moreResults;
    }

    public List<ParkingSpot> getSpots() {
        return spots;
    }

    public void setSpots(List<ParkingSpot> spots) {
        this.spots = spots;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
