package com.cahue;

import com.cahue.model.Car;
import com.cahue.model.User;
import com.cahue.model.transfer.RegistrationRequestBean;
import com.cahue.model.transfer.RegistrationResult;
import com.cahue.resources.Cars;
import com.cahue.util.UserService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CarPersistenceTest {

    @Inject
    UserService userService;

    @Inject
    Cars cars;


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

        RegistrationRequestBean registrationRequestBean = new RegistrationRequestBean();
        registrationRequestBean.setDeviceRegId("Test device");
        registrationRequestBean.setGoogleAuthToken("ya29.FAEqcYwhewINFfqgMtRfiRBUT2x7OsL21JfpqruqYCucw7xB_dOHA-dHC8m7SHeyLk7O6X_VFqhxOA");

        RegistrationResult result = userService.register(registrationRequestBean);
        User user = result.getUser();

        assertEquals(user.getGoogleUser().getEmail(), "empanadamental@gmail.com");

        Car car = Car.createCar(user, "Car name", "Test BT address");

        List<Car> cars = Arrays.asList(car);
        this.cars.save(cars, user);

        List<Car> retrievedCars = this.cars.retrieveUserCars(user);
        assertThat(cars, is(retrievedCars));
    }
}