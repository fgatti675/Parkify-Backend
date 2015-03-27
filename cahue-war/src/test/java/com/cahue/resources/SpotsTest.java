package com.cahue.resources;

import com.cahue.config.TestModule;
import com.cahue.config.guice.BusinessModule;
import com.cahue.index.ParkingSpotIndexEntry;
import com.cahue.index.SpotsIndex;
import com.cahue.model.Car;
import com.cahue.model.ParkingSpot;
import com.cahue.model.User;
import com.cahue.model.transfer.QueryResult;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(JukitoRunner.class)
public class SpotsTest {

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
    public void setUp() {
        spotsIndex.clear();
        testHelper.setUp();
    }

    @After
    public void tearDown() throws IOException {
        spotsIndex.clear();
        testHelper.tearDown();
    }

    @Inject
    UsersResource usersResource;

    @Inject
    SpotsIndex spotsIndex;

    @Inject
    SpotsResource spotsResource;

    @Inject
    CarsResource carsResource;

    @Test
    public void test() {

        RegistrationResult result = testHelper.registerUser();
        User user = result.getUser();

        Car car = new Car();
        car.setId("ferfgerge");
        car.setName("Car name");
        car.setBtAddress("Test BT address");

        this.carsResource.save(car, null, user);

        ParkingSpot ps1 = new ParkingSpot();
        ps1.setCar(car);
        ps1.setLatitude(10.0);
        ps1.setLongitude(10.0);
        ps1.setAccuracy(5.0F);
        ParkingSpotIndexEntry ps1index = new ParkingSpotIndexEntry(ps1);
        ps1index.setFuture(true);
        spotsResource.store(ps1index);

        ParkingSpot ps2 = new ParkingSpot();
        ps2.setCar(car);
        ps2.setLatitude(5.0);
        ps2.setLongitude(5.0);
        ps2.setAccuracy(5.0F);
        ParkingSpotIndexEntry ps2index = new ParkingSpotIndexEntry(ps2);
        spotsResource.store(ps2index);

        ParkingSpot ps3 = new ParkingSpot();
        ps3.setCar(car);
        ps3.setLatitude(1000.0);
        ps3.setLongitude(1000.0);
        ps3.setAccuracy(5.0F);
        ParkingSpotIndexEntry ps3index = new ParkingSpotIndexEntry(ps3);
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

        Collection<ParkingSpot> values = ofy().load().type(ParkingSpot.class).ids(Arrays.asList(
                ps1index.getId(),
                ps2index.getId(),
                ps3index.getId()
        )).values();

        List<ParkingSpot> actual = new ArrayList(values);
        List<ParkingSpot> value = Arrays.asList(
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


}