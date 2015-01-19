package com.cahue.persistence;

import javax.persistence.EntityManager;

/**
 * Date: 17.12.14
 *
 * @author francesco
 */
public interface DataSource {

    EntityManager createDatastoreEntityManager();

    EntityManager createRelationalEntityManager();

}
