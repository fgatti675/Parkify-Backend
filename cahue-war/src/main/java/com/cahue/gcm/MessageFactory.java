package com.cahue.gcm;

import com.cahue.model.Car;
import com.google.android.gcm.server.Message;
import org.eclipse.persistence.jaxb.JAXBContextFactory;

import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

/**
 * @author Francesco
 */
@Singleton
public class MessageFactory {

    /**
     * Tell a Device that
     */
    public static final String CARS_UPDATE = "CARS_UPDATE";

    /**
     * Message parameters
     */
    public static final String CARS = "CARS";


    public Message getCarsUpdateMessage(List<Car> cars) {
        return new Message.Builder()
                .collapseKey(CARS_UPDATE)
                .addData(CARS, marshallCars(cars))
                .build();
    }

    public String marshallCars(List<Car> cars) {
        try {
            JAXBContext jc = JAXBContextFactory.createContext(new Class[]{Car.class}, null);

            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty("eclipselink.media-type", "application/json");
            Writer writer = new StringWriter();
            marshaller.marshal(cars, writer);
            return writer.toString();
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
