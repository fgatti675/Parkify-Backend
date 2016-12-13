package com.cahue.places;

import com.cahue.model.Place;

import java.util.List;

/**
 * Created by f.gatti.gomez on 06/08/16.
 */
public class PlacesResult {

    private boolean moreResults;

    private List<Place> places;

    public boolean isMoreResults() {
        return moreResults;
    }

    public void setMoreResults(boolean moreResults) {
        this.moreResults = moreResults;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }
}
