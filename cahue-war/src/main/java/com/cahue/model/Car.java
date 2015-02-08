package com.cahue.model;


import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;

import static com.cahue.persistence.OfyService.ofy;


/**
 * Car linked to a User.
 *
 * @author Francesco
 */
@Cache
@Entity
public class Car {

    public static Car createCar(User user, String name, @Nullable String btID) {
        Car car = new Car();
        car.user = Ref.create(user);
        car.bluetoothAddress = btID;
        car.name = name;
        return car;
    }

    @Id
    private String id;

    private String bluetoothAddress;

    private String name;

    @Parent
    private Ref<User> user;

    private Date creationDate = new Date();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBluetoothAddress() {
        return bluetoothAddress;
    }

    @XmlTransient
    public User getUser() {
        return user.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Car car = (Car) o;

        if (id != null ? !id.equals(car.id) : car.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
