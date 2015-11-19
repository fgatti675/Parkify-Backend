/*
 */

package com.cahue.persistence;

import com.cahue.model.*;
import com.google.inject.Injector;
import com.googlecode.objectify.ObjectifyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Our version of ObjectifyFactory which integrates with Guice.  You could and convenience methods here too.
 *
 * @author Jeff Schnitzer
 */
@Singleton
public class OfyFactory extends ObjectifyFactory {
    /** */
    private Injector injector;

    /**
     * Register our entity types
     */
    @Inject
    public OfyFactory(Injector injector) {
        this.injector = injector;

        long time = System.currentTimeMillis();

        this.register(AuthToken.class);
        this.register(User.class);
        this.register(GoogleUser.class);
        this.register(FacebookUser.class);
        this.register(Car.class);
        this.register(Device.class);
        this.register(ParkingSpot.class);

        long millis = System.currentTimeMillis() - time;
    }

    /**
     * Use guice to make instances instead!
     */
    @Override
    public <T> T construct(Class<T> type) {
        return injector.getInstance(type);
    }

    @Override
    public Ofy begin() {
        return new Ofy(this);
    }
}