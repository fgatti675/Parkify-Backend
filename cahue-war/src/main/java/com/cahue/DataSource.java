package com.cahue;

import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchServiceFactory;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by Francesco on 07/09/2014.
 */
@Singleton
public class DataSource {


    private EntityManagerFactory factory = Persistence.createEntityManagerFactory("transactions-optional");

    public EntityManager createEntityManager(){
        return factory.createEntityManager();
    }




}
