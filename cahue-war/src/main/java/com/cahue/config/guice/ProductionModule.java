package com.cahue.config.guice;

import com.cahue.gcm.MessageFactory;
import com.cahue.persistence.MySQLDataSource;
import com.cahue.persistence.MySQLIndex;
import com.cahue.persistence.OfyService;
import com.cahue.persistence.SpotsIndex;
import com.cahue.gcm.GCMSender;
import com.cahue.resources.CarsResource;
import com.cahue.util.UserService;
import com.google.inject.servlet.ServletModule;
import com.googlecode.objectify.ObjectifyFilter;

import javax.inject.Singleton;

public class ProductionModule extends ServletModule {

    @Override
    protected void configureServlets() {

        requestStaticInjection(OfyService.class);

        bind(MySQLDataSource.class);
        bind(SpotsIndex.class).to(MySQLIndex.class);
        bind(CarsResource.class);
        bind(UserService.class);
        bind(GCMSender.class);
        bind(MessageFactory.class);

        filter("/*").through(ObjectifyFilter.class);
//        filterRegex("^((?!/authorize).)*$").through(PortSecurityFilter.class);

        bind(ObjectifyFilter.class).in(Singleton.class);

//        bind(Persistence.class).annotatedWith(Names.named(Persistence.CartoDB)).to(CartoDBPersistence.class);
//        bind(Persistence.class).annotatedWith(Names.named(Persistence.MySQL)).to(MySQLPersistence.class);
    }

}
