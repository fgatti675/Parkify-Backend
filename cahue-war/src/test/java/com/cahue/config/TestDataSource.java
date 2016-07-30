package com.cahue.config;

import com.cahue.config.persistence.MySQLDataSource;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Date: 17.12.14
 *
 * @author francesco
 */
public class TestDataSource extends MySQLDataSource {

    private EntityManagerFactory mysqlFactory;

    public TestDataSource() {
        mysqlFactory = Persistence.createEntityManagerFactory("test-mysql");
    }

    @Override
    public EntityManager createRelationalEntityManager() {
        return mysqlFactory.createEntityManager();
    }

}
