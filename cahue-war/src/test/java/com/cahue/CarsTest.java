package com.cahue;

import com.cahue.config.TestModule;
import com.cahue.config.guice.ProductionModule;
import com.cahue.model.Car;
import com.cahue.model.ParkingSpot;
import com.cahue.model.User;
import com.cahue.model.transfer.QueryResult;
import com.cahue.model.transfer.RegistrationRequestBean;
import com.cahue.model.transfer.RegistrationResult;
import com.cahue.persistence.SpotsIndex;
import com.cahue.resources.CarsResource;
import com.cahue.resources.SpotsResource;
import com.cahue.util.UserService;
import com.google.inject.Inject;
import com.google.inject.util.Modules;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(JukitoRunner.class)
public class CarsTest extends JerseyTest {

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
    public void addCarsTest() {

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
        car.setBluetoothAddress("Test BT address");

        List<Car> cars = Arrays.asList(car);

        Entity<List<Car>> userEntity = Entity.entity(cars, MediaType.APPLICATION_JSON_TYPE);
        target("cars")
                .request()
                .header("Authentication", result.getAuthToken())
                .post(userEntity); //Here we send POST request

        assertThat(carsResource.retrieveUserCars(user), is(cars));

    }
}