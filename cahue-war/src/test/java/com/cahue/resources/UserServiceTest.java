package com.cahue.resources;

import com.cahue.auth.UserAuthenticationService;
import com.cahue.auth.UserService;
import com.cahue.config.TestModule;
import com.cahue.config.guice.BusinessModule;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Date: 05.02.15
 *
 * @author francesco
 */
@RunWith(JukitoRunner.class)
public class UserServiceTest {

    @Inject
    UserAuthenticationService authService;
    @Inject
    CarsResource carsResource;
    @Inject
    UsersResource usersResource;
    @Inject
    UserService userService;
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

    @Test
    public void tokenTest() {
        String token = authService.generateToken();
        assertTrue(authService.validateToken(token));
    }

    @Test
    public void test() {

        RegistrationResult result = testHelper.registerUser();
        User user = result.getUser();

        Car car = new Car();
        car.setId("ferfgerge");
        car.setName("Car name");
        car.setUser(user);
        car.setBtAddress("Test BT address");

        ParkingSpot spot = new ParkingSpot();
        spot.setId(1234L);
        spot.setTime(new Date());
        spot.setLatitude(10.0);
        spot.setLongitude(10.0);
        spot.setAccuracy(10F);
        spot.setCar(car);
        car.setSpot(spot);

        this.carsResource.save(car, spot, user);

        List<Car> retrievedCars = userService.retrieveUserCars();
        assertThat(Arrays.asList(car), is(retrievedCars));

        result = testHelper.registerUser();
        assertThat(Arrays.asList(new CarTransfer(car)), is(result.getCars()));

    }


    public void testFbAuthToken(){
        authService.retrieveFacebookUser("EAAMZCEaxaDUEBALtQYGKrdva5klX2c8gMtXjKlpmdEcszehQwe2EIwaURxZCgegIagVj4cua7DW3dxptiBreNzNlT6VMj25Q3bgSDnwl9v4d9k35X4J5r5tTPCtHme7ZBCvvYZBK0SX1qEIHnQPM0TKwucQbwAaIXUsrQwZC9ZBHmAvtgck9tndKg3AIZA79WcUIgGavZCYhKGqKk4SEl5OUQ8bcliAyWZCUZD");
    }
    /**
     * Overrides the common bindings from TestBase with the
     * module that has test-specific bindings for Foo.
     */
    public static class Module extends JukitoModule {
        protected void configureTest() {
            install(Modules.override(new BusinessModule()).with(new TestModule()));
        }
    }
}
