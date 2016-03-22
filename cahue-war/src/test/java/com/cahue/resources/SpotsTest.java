package com.cahue.resources;

import com.cahue.config.TestModule;
import com.cahue.config.guice.BusinessModule;
import com.cahue.index.ParkingSpotIndexEntry;
import com.cahue.index.SpotsIndex;
import com.cahue.model.Car;
import com.cahue.model.FreeParkingSpot;
import com.cahue.model.ParkingSpot;
import com.cahue.model.User;
import com.cahue.model.transfer.ParkingSpotTransfer;
import com.cahue.model.transfer.QueryResult;
import com.cahue.model.transfer.RegistrationResult;
import com.google.appengine.api.datastore.GeoPt;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(JukitoRunner.class)
public class SpotsTest {

    @Inject
    TestHelper testHelper;

    @Inject
    UsersResource usersResource;

    @Inject
    SpotsIndex spotsIndex;

    @Inject
    SpotsResource spotsResource;

    @Inject
    CarsResource carsResource;

    @Before
    public void setUp() {
        spotsIndex.clear();
        testHelper.setUp();
    }

    @After
    public void tearDown() throws IOException {
        spotsIndex.clear();
        testHelper.tearDown();
    }

    @Test
    public void test() {

        RegistrationResult result = testHelper.registerUser();
        User user = result.getUser();

        Car car = new Car();
        car.setId("ferfgerge");
        car.setName("Car name");
        car.setBtAddress("Test BT address");

        this.carsResource.save(car, null, user);

        FreeParkingSpot ps1 = new FreeParkingSpot();
        ps1.setLocation(new GeoPt(10.0f, 10.0f));
        ps1.setAccuracy(5.0F);
        ParkingSpotTransfer ps1index = new ParkingSpotTransfer(ps1);
        ps1index.setFuture(true);
        spotsResource.store(ps1index);

        FreeParkingSpot ps2 = new FreeParkingSpot();
        ps2.setLocation(new GeoPt(5.0f, 5.0f));
        ps2.setAccuracy(5.0F);
        ParkingSpotTransfer ps2index = new ParkingSpotTransfer(ps2);
        spotsResource.store(ps2index);

        FreeParkingSpot ps3 = new FreeParkingSpot();
        ps3.setLocation(new GeoPt(1000.0f, 1000.0f));
        ps3.setAccuracy(5.0F);
        ParkingSpotTransfer ps3index = new ParkingSpotTransfer(ps3);
        spotsResource.store(ps3index);

        List expectedResult = new ArrayList();
        expectedResult.add(ps1index);
        expectedResult.add(ps2index);

        /**
         * Query area and test
         */
        QueryResult area = spotsResource.getArea(-100.0, -100.0, 100.0, 100.0);
        // look for the one in the future
        for (ParkingSpotIndexEntry entry : area.getSpots()) {
            ReflectionAssert.assertPropertiesNotNull("Null values in the car transfer", entry);
            if (entry.getId() == ps1index.getId()) assertTrue(entry.isFuture());
            else if (entry.getId() == ps2index.getId()) assertTrue(!entry.isFuture());
            else if (entry.getId() == ps3index.getId()) assertTrue(!entry.isFuture());
        }
        assertThat(area.getSpots(), is(expectedResult));

        Collection<FreeParkingSpot> values = ofy().load().type(FreeParkingSpot.class).ids(Arrays.asList(
                ps1index.getId(),
                ps2index.getId(),
                ps3index.getId()
        )).values();

        List<FreeParkingSpot> actual = new ArrayList(values);
        List<FreeParkingSpot> value = Arrays.asList(
                ps1index.createSpot(),
                ps2index.createSpot(),
                ps3index.createSpot());
        assertThat(actual, is(value));

        /**
         * Replace future spot with regular one
         */
        ps1index.setLatitude(11.0);
        ps1index.setLongitude(11.0);
        ps1index.setFuture(false);
        spotsResource.store(ps1index);

        QueryResult newArea = spotsResource.getArea(-100.0, -100.0, 100.0, 100.0);
        // look for the one in the future
        for (ParkingSpotIndexEntry entry : newArea.getSpots()) {
            ReflectionAssert.assertPropertiesNotNull("Null values in the car transfer", entry);
            if (entry.getId() == ps1index.getId()) {
                assertEquals(11, entry.getLatitude(), 1e-15);
                assertEquals(11, entry.getLongitude(), 1e-15);
                assertTrue(!entry.isFuture());
            } else if (entry.getId() == ps2index.getId()) {
                assertTrue(!entry.isFuture());
            } else if (entry.getId() == ps3index.getId()) {
                assertTrue(!entry.isFuture());
            }
        }

        expectedResult = new ArrayList();
        expectedResult.add(ps2index);
        expectedResult.add(ps1index);
        assertThat(newArea.getSpots(), is(expectedResult));
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