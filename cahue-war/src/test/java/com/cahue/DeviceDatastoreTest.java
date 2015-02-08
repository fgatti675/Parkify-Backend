package com.cahue;

import com.cahue.config.TestModule;
import com.cahue.config.guice.ProductionModule;
import com.cahue.util.UserService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.util.Modules;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

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
//        EntityManager em = dataSource.createDatastoreEntityManager();
//
//        RegistrationRequestBean registrationRequestBean = new RegistrationRequestBean();
//        registrationRequestBean.setDeviceRegId("TEST_DEV");
//
//        userService.register(em, registrationRequestBean);
//
//        GoogleUser user = new GoogleUser();
//        user.setKey(GoogleUser.createGoogleUserKey("randomKey"));
//        user.setEmail("bla@bla.com");
//
//        Device device = Device.createDevice("bla", user);
//        user.getDevices().add(device);
//
//        em.getTransaction().begin();
//        em.merge(user);
//        em.merge(device);
//        em.getTransaction().commit();
//
//        List<GoogleUser> list = em.createQuery("select u from User u", GoogleUser.class).getResultList();
//        assertEquals(list.size(), 1);
//
//        GoogleUser retrievedUser = em.find(GoogleUser.class, user.getKey());
//        assertEquals(1, retrievedUser.getDevices().size());
//        for (Device d : retrievedUser.getDevices()) {
//            assertEquals(d, device);
//            assertEquals(d.getUser(), user);
//        }
    }
}