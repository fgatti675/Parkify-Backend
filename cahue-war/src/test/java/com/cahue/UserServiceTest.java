package com.cahue;

import com.cahue.config.TestModule;
import com.cahue.config.guice.ProductionModule;
import com.cahue.model.Car;
import com.cahue.model.User;
import com.cahue.persistence.AppEngineDataSource;
import com.cahue.persistence.DataSource;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.util.Modules;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Date: 05.02.15
 *
 * @author francesco
 */
@RunWith(JukitoRunner.class)
public class UserServiceTest {

    DataSource dataSource = new AppEngineDataSource();

    /**
     * Overrides the common bindings from TestBase with the
     * module that has test-specific bindings for Foo.
     */
    public static class Module extends JukitoModule {
        protected void configureTest() {
            install(Modules.override(new ProductionModule()).with(new TestModule()));
        }
    }

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void registrationTest() {
        EntityManager em = dataSource.createDatastoreEntityManager();

        User user = new User();
        user.setKey(User.createGoogleUserKey("randomKey"));
        user.setEmail("bla@bla.com");

        Car car = new Car();
        user.getCars().add(car);

        em.getTransaction().begin();
        em.persist(user);
        em.persist(car);
        em.getTransaction().commit();

        List<User> list = em.createQuery("select u from User u", User.class).getResultList();
        assertEquals(list.size(), 1);

        User x = em.find(User.class, user.getKey());
        for (Car c : x.getCars()) {
            assertEquals(c, car);
        }
    }
}
