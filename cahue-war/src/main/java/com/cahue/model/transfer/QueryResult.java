package com.cahue.model.transfer;

import com.cahue.index.ParkingSpotIndexEntry;

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

    List<ParkingSpotIndexEntry> spots;

    public boolean isMoreResults() {
        return moreResults;
    }

    public void setMoreResults(boolean moreResults) {
        this.moreResults = moreResults;
    }

    public List<ParkingSpotIndexEntry> getSpots() {
        return spots;
    }

    public void setSpots(List<ParkingSpotIndexEntry> spots) {
        this.spots = spots;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
