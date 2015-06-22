package com.cahue.gcm;

import com.cahue.model.transfer.CarTransfer;
import com.google.android.gcm.server.Message;
import org.eclipse.persistence.jaxb.JAXBContextFactory;

import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;

/**
 * @author Francesco
 */
@Singleton
public class GCMMessageFactory {


    /**
     * Message parameters
     */
    public static final String UPDATED_CAR = "UPDATED_CAR";
    public static final String DELETED_CAR = "DELETED_CAR";


    public Message getCarUpdateMessage(CarTransfer car) {
        return new Message.Builder()
                .collapseKey(UPDATED_CAR)
                .addData(UPDATED_CAR, marshallCar(car))
                .build();
    }

    public String marshallCar(CarTransfer car) {
        try {
            JAXBContext jc = JAXBContextFactory.createContext(new Class[]{CarTransfer.class}, null);

            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty("eclipselink.media-type", "application/json");
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            Writer writer = new StringWriter();
            marshaller.marshal(car, writer);
            return URLEncoder.encode(writer.toString(), "UTF-8");
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Message getCarDeletedMessage(String carId) {
        return new Message.Builder()
                .collapseKey(DELETED_CAR)
                .addData(DELETED_CAR, carId)
                .build();
    }
}
