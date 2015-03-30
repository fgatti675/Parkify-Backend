package com.cahue.resources;

import com.cahue.config.TestModule;
import com.cahue.config.guice.BusinessModule;
import com.cahue.gcm.GCMMessageFactory;
import com.cahue.model.Car;
import com.cahue.model.transfer.CarTransfer;
import com.google.inject.Inject;
import com.google.inject.util.Modules;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Logger;

@RunWith(JukitoRunner.class)
public class DevicesTest {

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
    GCMMessageFactory messageFactory;


    @Test
    public void marshall() {

        Car car = new Car();
        car.setId("ferfgerge");
        car.setName("Car name");
        car.setBtAddress("Test BT address");

        logger.info(messageFactory.marshallCar(new CarTransfer(car)));

    }
}