package com.cahue.resources;

import com.cahue.config.TestModule;
import com.cahue.config.guice.ProductionModule;
import com.cahue.model.Car;
import com.cahue.model.ParkingSpot;
import com.cahue.model.User;
import com.cahue.model.transfer.QueryResult;
import com.cahue.model.transfer.RegistrationRequestBean;
import com.cahue.model.transfer.RegistrationResult;
import com.cahue.persistence.SpotsIndex;
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

@RunWith(JukitoRunner.class)
public class SpotsTest {

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
        spotsIndex.clear();
        testHelper.setUp();
    }

    @After
    public void tearDown() throws IOException {
        testHelper.tearDown();
    }

    @Inject
    UserService userService;

    @Inject
    SpotsIndex spotsIndex;

    @Inject
    SpotsResource spotsResource;

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
        car.setBluetoothAddress("Test BT address");

        List<Car> cars = Arrays.asList(car);
        this.carsResource.save(cars, user);

        ParkingSpot ps1 = new ParkingSpot();
        ps1.setCar(car);
        ps1.setLatitude(10.0);
        ps1.setLongitude(10.0);
        ps1.setAccuracy(5.0F);
        spotsResource.store(ps1);

        ParkingSpot ps2 = new ParkingSpot();
        ps2.setCar(car);
        ps2.setLatitude(5.0);
        ps2.setLongitude(5.0);
        ps2.setAccuracy(5.0F);
        spotsResource.store(ps2);

        ParkingSpot ps3 = new ParkingSpot();
        ps3.setCar(car);
        ps3.setLatitude(1000.0);
        ps3.setLongitude(1000.0);
        ps3.setAccuracy(5.0F);
        spotsResource.store(ps3);

        QueryResult area = spotsResource.getArea(-100.0, -100.0, 100.0, 100.0);
        assertThat(area.getSpots(), is(Arrays.asList(ps1, ps2)));
    }
}