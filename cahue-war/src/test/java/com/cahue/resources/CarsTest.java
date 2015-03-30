package com.cahue.resources;

import com.cahue.auth.UserService;
import com.cahue.config.TestModule;
import com.cahue.config.guice.BusinessModule;
import com.cahue.gcm.GCMSender;
import com.cahue.model.Car;
import com.cahue.model.ParkingSpot;
import com.cahue.model.User;
import com.cahue.model.transfer.CarTransfer;
import com.cahue.model.transfer.RegistrationResult;
import com.google.inject.Inject;
import com.google.inject.util.Modules;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.reflectionassert.ReflectionAssert;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(JukitoRunner.class)
public class CarsTest {

    Logger logger = Logger.getLogger(getClass().getName());

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

    @Inject
    GCMSender gcmSender;

    @Test
    public void addCarsTest() {

        RegistrationResult result = testHelper.registerUser();
        User user = result.getUser();

        Car car = new Car();
        car.setId("ferfgerge");
        car.setName("Car name");
        car.setUser(user);
        car.setBtAddress("Test BT address");
        car.setColor(-468682546);

        carsResource.save(new CarTransfer(car));
        assertThat(userService.retrieveUserCars(), is(Arrays.asList(car)));

        ParkingSpot spot = new ParkingSpot();
        spot.setId(1234L);
        spot.setTime(new Date());
        spot.setLatitude(10.0);
        spot.setLongitude(10.0);
        spot.setAccuracy(10F);
        spot.setCar(car);

        CarTransfer saved = carsResource.save(new CarTransfer(car, spot));
        ReflectionAssert.assertPropertiesNotNull("Null values in the car transfer", saved);
        assertEquals(car, saved.createCar());
        assertEquals(spot, saved.createSpot());

    }


}