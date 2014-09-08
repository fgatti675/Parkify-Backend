package com.cahue;

import com.cahue.entities.Location;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.inject.servlet.RequestScoped;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Date;

/**
 * Date: 08.09.14
 *
 * @author francesco
 */
@RequestScoped
public class DAO {

    @Inject
    DataSource dataSource;

    public void saveLocation(Location location) {

        // Save in datastore
        EntityManager em = dataSource.createEntityManager();
        em.getTransaction().begin();
        em.persist(location);
        em.getTransaction().commit();

        // Save in Index
        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude())  ;
        Document doc = Document.newBuilder()
//                .setId(myDocId) // Setting the document identifer is optional. If omitted, the search service will create an identifier.
                .addField(Field.newBuilder().setName("content").setText("the rain in spain"))
                .addField(Field.newBuilder().setName("location").setGeoPoint(geoPoint))
                .build();
    }
}
