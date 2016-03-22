package com.cahue.index;

import com.cahue.model.transfer.QueryResult;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;

/**
 * Created by f.gatti.gomez on 29/02/16.
 */
public class AppEngineSpotsIndex implements SpotsIndex {

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    public QueryResult queryNearest(Double latitude, Double longitude, int nearest) {

//        GeoPt center = new GeoPt(latitude.floatValue(), longitude.floatValue());
//        double radius = valueInMeters;
//        Filter f = new StContainsFilter("location", new Circle(center, radius));
//        Query q = new Query("Kind").setFilter(f);
//
//// Testing for containment within a rectangle
//        GeoPt southwest = ...
//        GeoPt northeast = ...
//        Filter f = new StContainsFilter("location", new Rectangle(southwest, northeast));
//        Query q = new Query("Kind").setFilter(f);

        return null;
    }

    @Override
    public QueryResult queryArea(Double southwestLatitude, Double southwestLongitude, Double northeastLatitude, Double northeastLongitude) {
        return null;
    }

    @Override
    public void put(ParkingSpotIndexEntry spot) {
        Entity station = new Entity("ParkingSpotIndexEntry");
        station.setProperty("id", spot.getId());
        station.setIndexedProperty("location", new GeoPt(spot.getLatitude().floatValue(), spot.getLongitude().floatValue()));
        station.setProperty("accuracy", spot.getAccuracy());
        station.setIndexedProperty("expiryTime", spot.getExpiryTime());
        station.setProperty("future", spot.isFuture());
        datastore.put(station);
    }

    @Override
    public int expireStale() {
        return 0;
    }

    @Override
    public void clear() {

    }
}
