package com.cahue.model.transfer;

import java.util.List;

/**
 * Created by f.gatti.gomez on 06/08/16.
 */
public class PlacesResultTransfer {

    private boolean moreResults = false;

    private List<PlaceTransfer> places;

    public List<PlaceTransfer> getPlaces() {
        return places;
    }

    public void setPlaces(List<PlaceTransfer> places) {
        this.places = places;
    }

    public boolean isMoreResults() {
        return moreResults;
    }

    public void setMoreResults(boolean moreResults) {
        this.moreResults = moreResults;
    }
}
