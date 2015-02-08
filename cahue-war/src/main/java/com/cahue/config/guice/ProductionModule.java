package com.cahue.config.guice;

import com.cahue.persistence.MySQLDataSource;
import com.cahue.persistence.MySQLPersistence;
import com.cahue.persistence.OfyService;
import com.cahue.persistence.SpotsIndex;
import com.cahue.gcm.GCMSender;
import com.cahue.util.UserService;
import com.google.inject.servlet.ServletModule;
import com.googlecode.objectify.ObjectifyFilter;

import javax.inject.Singleton;

public class ProductionModule extends ServletModule {

    @Override
    protected void configureServlets() {

        requestStaticInjection(OfyService.class);

        bind(MySQLDataSource.class);
        bind(SpotsIndex.class).to(MySQLPersistence.class);
        bind(UserService.class);
        bind(GCMSender.class);

        filter("/*").through(ObjectifyFilter.class);

        bind(ObjectifyFilter.class).in(Singleton.class);

//        bind(Persistence.class).annotatedWith(Names.named(Persistence.CartoDB)).to(CartoDBPersistence.class);
//        bind(Persistence.class).annotatedWith(Names.named(Persistence.MySQL)).to(MySQLPersistence.class);
    }

}
