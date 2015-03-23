package com.cahue.resources;

import com.cahue.auth.UserService;
import com.cahue.config.TestModule;
import com.cahue.config.guice.BusinessModule;
import com.cahue.gcm.GCMSender;
import com.cahue.model.Car;
import com.cahue.model.User;
import com.cahue.model.transfer.CarTransfer;
import com.cahue.model.transfer.RegistrationRequestBean;
import com.cahue.model.transfer.RegistrationResult;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletModule;
import com.google.inject.util.Modules;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

@RunWith(JukitoRunner.class)
public class CarsJerseyTest extends JerseyTest {

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

    @Override
    protected Application configure() {

        // TODO: Not working
        Injector inj = Guice.createInjector(Modules.override(new BusinessModule()).with(new TestModule()));
        ResourceConfig resourceConfig = new ResourceConfig(CarsResource.class);
        inj.injectMembers(resourceConfig);
        return resourceConfig;
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
    UsersResource usersResource;

    @Inject
    UserService userService;

    @Inject
    GCMSender gcmSender;

    @Test
    public void addCarsTest() {

        RegistrationRequestBean registrationRequestBean = new RegistrationRequestBean();
        registrationRequestBean.setDeviceRegId("Test device");
        registrationRequestBean.setGoogleAuthToken(testHelper.getGoogleAuthToken());

        RegistrationResult regResult = usersResource.register(registrationRequestBean);
        User user = regResult.getUser();
        userService.setCurrentUser(user);

        assertEquals(user.getGoogleUser().getEmail(), TestHelper.EMAIL_ADDRESS);

        Car car = new Car();
        car.setId("ferfgerge");
        car.setName("Car name");
        car.setUser(user);
        car.setBtAddress("Test BT address");

        Entity<CarTransfer> carsEntity = Entity.entity(new CarTransfer(car), MediaType.APPLICATION_JSON);

        Response response = target("cars")
                .request()
                .header("Authorization", regResult.getAuthToken())
                .post(carsEntity); //Here we send POST request
        Object entity = response.getEntity();

        logger.log(Level.INFO, "Response : " + response.getStatus());

        List<Car> userCars = userService.retrieveUserCars();
        assertEquals(userCars.iterator().next(), car);

    }
}