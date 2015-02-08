package com.cahue;

import com.cahue.config.TestModule;
import com.cahue.config.guice.ProductionModule;
import com.cahue.model.Device;
import com.cahue.model.User;
import com.cahue.model.transfer.RegistrationBean;
import com.cahue.persistence.AppEngineDataSource;
import com.cahue.persistence.DataSource;
import com.cahue.persistence.MySQLPersistence;
import com.cahue.util.UserService;
import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.util.Modules;
import org.datanucleus.store.query.Query;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import java.util.List;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;

@RunWith(JukitoRunner.class)
public class DeviceDatastoreTest {

    /**
     * Overrides the common bindings from TestBase with the
     * module that has test-specific bindings for Foo.
     */
    public static class Module extends JukitoModule {
        protected void configureTest() {
            install(Modules.override(new ProductionModule()).with(new TestModule()));
        }
    }

    @Inject
    UserService userService;

    @Inject
    DataSource dataSource;

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
    public void test() {
        EntityManager em = dataSource.createDatastoreEntityManager();

        RegistrationBean registrationBean = new RegistrationBean();
        registrationBean.setDeviceRegId("TEST_DEV");

        userService.register(em, registrationBean);

        User user = new User();
        user.setId("randomKey");
        user.setEmail("bla@bla.com");

        Device device = Device.createDevice("bla", user);
        user.getDevices().add(device);

        em.getTransaction().begin();
        em.merge(user);
        em.merge(device);
        em.getTransaction().commit();

        List<User> list = em.createQuery("select u from User u", User.class).getResultList();
        assertEquals(list.size(), 1);

        User retrievedUser = em.find(User.class, user.getId());
        assertEquals(1, retrievedUser.getDevices().size());
        for (Device d : retrievedUser.getDevices()) {
            assertEquals(d, device);
            assertEquals(d.getUser(), user);
        }
    }
}