package com.cahue.config.persistence;

import com.google.appengine.api.utils.SystemProperty;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Francesco on 07/09/2014.
 */
@Singleton
public class MySQLDataSource {

    private EntityManagerFactory mysqlFactory;

    public MySQLDataSource() {
        Map<String, String> properties = new HashMap();
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
            properties.put("javax.persistence.jdbc.driver", "com.mysql.jdbc.GoogleDriver");
            properties.put("javax.persistence.jdbc.url", System.getProperty("cloudsql.url"));
        } else {
            properties.put("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
            properties.put("javax.persistence.jdbc.url", System.getProperty("cloudsql.url.dev"));
        }
        mysqlFactory = Persistence.createEntityManagerFactory("mysql", properties);

    }

    public EntityManager createRelationalEntityManager() {
        return mysqlFactory.createEntityManager();
    }


}
