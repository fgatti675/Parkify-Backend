package com.cahue.gcm;

import com.cahue.model.Car;
import com.google.android.gcm.server.Message;

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

    /**
     * Tell a user that they have been invited to a game and by who.
     *
     * @return
     */
    public Message getSayHelloMessage() {
        return new Message.Builder()
//                .collapseKey(GAME_INVITATION)
                .addData("TEST_KEY", "Hi from your really cool server")
                .build();
    }

    public Message getCarsUpdateMessage(List<Car> cars) {
        return new Message.Builder()
                .collapseKey(CARS_UPDATE)
                .addData(CARS, "Hi from your really cool server")
                .build();
    }

    public String marshallCars(List<Car> cars) {
        try {
            JAXBContext jc = JAXBContext.newInstance(Car.class);
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
