package com.cahue.resources;

import com.cahue.auth.UserService;
import com.cahue.gcm.GCMMessageFactory;
import com.cahue.gcm.GCMSender;
import com.cahue.model.Car;
import com.cahue.model.Device;
import com.cahue.model.ParkingSpot;
import com.cahue.model.User;
import com.cahue.model.transfer.CarTransfer;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * @author Francesco
 */
@Path("/cars")
public class CarsResource {

    Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    GCMSender sender;

    @Inject
    GCMMessageFactory messageFactory;

    @Inject
    UserService userService;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<CarTransfer> retrieve() {
        List<Car> cars = userService.retrieveUserCars();
        List<CarTransfer> transfers = new ArrayList<>();
        for (Car car : cars) transfers.add(new CarTransfer(car));
        return transfers;
    }

    @DELETE
    @Path("/{carId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response delete(@PathParam(value = "carId") String carId) {

        User user = userService.getCurrentUser();

        ofy().delete().type(Car.class).parent(user).id(carId).now();

        List<Device> devices = userService.getUserDevicesButCurrent();
        sender.sendGCMMultiUpdate(user, devices, messageFactory.getCarDeletedMessage(carId));

        return Response.ok().entity(carId).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public CarTransfer save(CarTransfer carTransfer) {

        User user = userService.getCurrentUser();

        Car car = carTransfer.createCar();
        ParkingSpot spot = carTransfer.createSpot();

        logger.info("Received car: " + car);

        car.setLastModified(new Date());

        save(car, spot, user);

        CarTransfer result = new CarTransfer(car, spot);

        List<Device> devices = userService.getUserDevicesButCurrent();
        sender.sendGCMMultiUpdate(user, devices, messageFactory.getCarUpdateMessage(carTransfer));

        return result;
    }

    public void save(Car car, ParkingSpot spot, User owner) {
        car.setUser(owner);

        if (spot != null) {
            spot.setCar(car);
            ofy().save().entity(spot).now();
        }

        car.setSpot(spot);
        ofy().save().entity(car).now();
    }


}
