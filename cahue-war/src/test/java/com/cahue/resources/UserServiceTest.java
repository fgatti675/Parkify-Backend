package com.cahue.resources;

import com.cahue.config.TestModule;
import com.cahue.config.guice.ProductionModule;
import com.cahue.model.Car;
import com.cahue.model.User;
import com.cahue.model.transfer.RegistrationRequestBean;
import com.cahue.model.transfer.RegistrationResult;
import com.cahue.util.UserService;
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
            install(Modules.override(new ProductionModule()).with(new TestModule()));
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
    UserService userService;

    @Inject
    CarsResource carsResource;

    @Test
    public void test() {

        RegistrationRequestBean registrationRequestBean = new RegistrationRequestBean();
        registrationRequestBean.setDeviceRegId("Test device");
        registrationRequestBean.setGoogleAuthToken(testHelper.getGoogleAuthToken());

        RegistrationResult result = userService.register(registrationRequestBean);
        User user = result.getUser();

        assertEquals(user.getGoogleUser().getEmail(), "empanadamental@gmail.com");

        Car car = new Car();
        car.setId("ferfgerge");
        car.setName("Car name");
        car.setUser(user);
        car.setBtAddress("Test BT address");

        List<Car> cars = Arrays.asList(car);
        this.carsResource.save(cars, user);

        List<Car> retrievedCars = this.carsResource.retrieveUserCars(user);
        assertThat(cars, is(retrievedCars));

        result = userService.register(registrationRequestBean);
        assertThat(cars, is(result.getCars()));
    }
}
