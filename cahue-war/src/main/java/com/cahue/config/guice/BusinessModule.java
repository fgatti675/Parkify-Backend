package com.cahue.config.guice;

import com.cahue.auth.UserAuthenticationService;
import com.cahue.auth.UserService;
import com.cahue.gcm.GCMMessageFactory;
import com.cahue.gcm.GCMSender;
import com.cahue.index.MySQLIndex;
import com.cahue.index.SpotsIndex;
import com.cahue.persistence.MySQLDataSource;
import com.cahue.persistence.OfyService;
import com.cahue.resources.CarsResource;
import com.google.inject.AbstractModule;
import com.googlecode.objectify.ObjectifyFilter;

import javax.inject.Singleton;

public class BusinessModule extends AbstractModule {

    @Override
    protected void configure() {

        requestStaticInjection(OfyService.class);

        bind(MySQLDataSource.class);
        bind(SpotsIndex.class).to(MySQLIndex.class);

        bind(CarsResource.class);

        bind(UserService.class);
        bind(UserAuthenticationService.class);

        bind(GCMSender.class);
        bind(GCMMessageFactory.class);

        bind(ObjectifyFilter.class).in(Singleton.class);

    }
}
