package com.cahue;

import com.cahue.config.TestModule;
import com.cahue.config.guice.ProductionModule;
import com.cahue.model.User;
import com.cahue.model.transfer.RegistrationRequestBean;
import com.cahue.model.transfer.RegistrationResult;
import com.cahue.util.UserService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Inject;
import com.google.inject.util.Modules;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cache.AsyncCacheFilter;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Date: 05.02.15
 *
 * @author francesco
 */
@RunWith(JukitoRunner.class)
public class UserServiceTest {

    @Inject
    UserService userService;

    /**
     * Overrides the common bindings from TestBase with the
     * module that has test-specific bindings for Foo.
     */
    public static class Module extends JukitoModule {
        protected void configureTest() {
            install(Modules.override(new ProductionModule()).with(new TestModule()));
        }
    }

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig(),
            new LocalMemcacheServiceTestConfig());

    @BeforeClass
    public static void setUpBeforeClass()
    {
        ObjectifyService.begin();
        // Reset the Factory so that all translators work properly.
        ObjectifyService.setFactory(new ObjectifyFactory());
    }
    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        AsyncCacheFilter.complete();
        helper.tearDown();
    }

    @Test
    public void registrationTest() {
        RegistrationRequestBean registrationRequestBean = new RegistrationRequestBean();
        registrationRequestBean.setDeviceRegId("Test device");
        registrationRequestBean.setGoogleAuthToken("ya29.FAEqcYwhewINFfqgMtRfiRBUT2x7OsL21JfpqruqYCucw7xB_dOHA-dHC8m7SHeyLk7O6X_VFqhxOA");

        RegistrationResult result = userService.register(registrationRequestBean);
        User user = result.getUser();

        assertEquals(user.getGoogleUser().getEmail(), "empanadamental@gmail.com");
    }
}
