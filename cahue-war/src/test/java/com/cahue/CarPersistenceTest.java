package com.cahue;

import com.cahue.model.Car;
import com.cahue.model.User;
import com.cahue.model.transfer.RegistrationRequestBean;
import com.cahue.model.transfer.RegistrationResult;
import com.cahue.resources.CarsResource;
import com.cahue.util.UserService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Inject;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CarPersistenceTest extends BaseTest{

    @Inject
    UserService userService;

    @Inject
    CarsResource carsResource;

    @Test
    public void test() {

        RegistrationRequestBean registrationRequestBean = new RegistrationRequestBean();
        registrationRequestBean.setDeviceRegId("Test device");
        registrationRequestBean.setGoogleAuthToken(getGoogleAuthToken());

        RegistrationResult result = userService.register(registrationRequestBean);
        User user = result.getUser();

        assertEquals(user.getGoogleUser().getEmail(), "empanadamental@gmail.com");

        Car car = Car.createCar(user, "Car name", "Test BT address");

        List<Car> cars = Arrays.asList(car);
        this.carsResource.save(cars, user);

        List<Car> retrievedCars = this.carsResource.retrieveUserCars(user);
        assertThat(cars, is(retrievedCars));

        result = userService.register(registrationRequestBean);
        assertThat(cars, is(result.getCars()));
    }
}