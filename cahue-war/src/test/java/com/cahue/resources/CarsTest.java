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
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(JukitoRunner.class)
public class CarsTest extends JerseyTest {

    Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Overrides the common bindings from TestBase with the
     * module that has test-specific bindings for Foo.
     */
    public static class Module extends JukitoModule {
        protected void configureTest() {
            install(Modules.override(new ProductionModule()).with(new TestModule()));
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(CarsResource.class);
    }


    @Inject
    TestHelper testHelper;

    @Before
    public void before() {
        testHelper.setUp();
    }

    @After
    public void after() throws IOException {
        testHelper.tearDown();
    }

    @Inject
    UserService userService;

    @Inject
    CarsResource carsResource;

    @Test
    public void addCarsTest() {

        RegistrationRequestBean registrationRequestBean = new RegistrationRequestBean();
        registrationRequestBean.setDeviceRegId("Test device");
        registrationRequestBean.setGoogleAuthToken(testHelper.getGoogleAuthToken());

        RegistrationResult result = userService.register(registrationRequestBean);
        User user = result.getUser();

        assertEquals(user.getGoogleUser().getEmail(), TestHelper.EMAIL_ADDRESS);

        Car car = new Car();
        car.setId("ferfgerge");
        car.setName("Car name");
        car.setUser(user);
        car.setBtAddress("Test BT address");

        List<Car> cars = Arrays.asList(car);

        Entity<List<Car>> carsEntity = Entity.entity(cars, MediaType.APPLICATION_JSON);

        target("cars")
                .request()
                .header("Authentication", result.getAuthToken())
                .post(carsEntity); //Here we send POST request

        assertThat(carsResource.retrieveUserCars(user), is(cars));

    }
}