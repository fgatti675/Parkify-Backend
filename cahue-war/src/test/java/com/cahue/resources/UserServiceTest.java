package com.cahue.resources;

import com.cahue.auth.UserAuthenticationService;
import com.cahue.auth.UserService;
import com.cahue.config.TestModule;
import com.cahue.config.guice.BusinessModule;
import com.cahue.model.Car;
import com.cahue.model.User;
import com.cahue.model.transfer.RegistrationRequestBean;
import com.cahue.model.transfer.RegistrationResult;
import com.google.inject.Inject;
import com.google.inject.util.Modules;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Date: 05.02.15
 *
 * @author francesco
 */
@RunWith(JukitoRunner.class)
public class UserServiceTest {

    /**
     * Overrides the common bindings from TestBase with the
     * module that has test-specific bindings for Foo.
     */
    public static class Module extends JukitoModule {
        protected void configureTest() {
            install(Modules.override(new BusinessModule()).with(new TestModule()));
        }
    }

    @Inject
    TestHelper testHelper;

    @Before
    public void setUp() {
        testHelper.setUp();
    }

    @After
    public void tearDown() throws IOException {
        testHelper.tearDown();
    }

    @Inject
    UserAuthenticationService authService;

    @Inject
    UsersResource usersResource;

    @Inject
    UserService userService;

    @Inject
    CarsResource carsResource;


    @Test
    public void tokenTest() {
        String token = authService.generateToken();
        assertTrue(authService.validateToken(token));
    }

    @Test
    public void test() {

        RegistrationRequestBean registrationRequestBean = new RegistrationRequestBean();
        registrationRequestBean.setDeviceRegId("Test device");
        registrationRequestBean.setGoogleAuthToken(testHelper.getGoogleAuthToken());

        RegistrationResult result = usersResource.register(registrationRequestBean);
        User user = result.getUser();
        userService.setCurrentUser(user);

        assertEquals(user.getGoogleUser().getEmail(), TestHelper.EMAIL_ADDRESS);

        Car car = new Car();
        car.setId("ferfgerge");
        car.setName("Car name");
        car.setUser(user);
        car.setBtAddress("Test BT address");

        this.carsResource.save(car, user);

        List<Car> retrievedCars = userService.retrieveUserCars();
        assertThat(Arrays.asList(car), is(retrievedCars));

        result = usersResource.register(registrationRequestBean);
        assertThat(Arrays.asList(car), is(result.getCars()));

    }
}
