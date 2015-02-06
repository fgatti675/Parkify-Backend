package com.cahue.config.guice;

import com.cahue.persistence.AppEngineDataSource;
import com.cahue.persistence.DataSource;
import com.cahue.persistence.MySQLPersistence;
import com.cahue.persistence.Persistence;
import com.cahue.resources.CarsResource;
import com.cahue.util.UserService;
import com.google.inject.servlet.ServletModule;

public class ProductionModule extends ServletModule {

	@Override
	protected void configureServlets() {

        bind(DataSource.class).to(AppEngineDataSource.class);
        bind(Persistence.class).to(MySQLPersistence.class);
        bind(UserService.class);
//        bind(GCMSender.class);

//        bind(Persistence.class).annotatedWith(Names.named(Persistence.CartoDB)).to(CartoDBPersistence.class);
//        bind(Persistence.class).annotatedWith(Names.named(Persistence.MySQL)).to(MySQLPersistence.class);
	}

}
