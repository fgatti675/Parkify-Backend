package com.cahue.config.guice;

import com.cahue.DataSource;
import com.cahue.persistence.CartoDBPersistence;
import com.cahue.persistence.MySQLPersistence;
import com.cahue.persistence.Persistence;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

public class ListerlyServletModule extends ServletModule {

	@Override
	protected void configureServlets() {

        bind(DataSource.class);
        bind(Persistence.class).to(MySQLPersistence.class);

//        bind(Persistence.class).annotatedWith(Names.named(Persistence.CartoDB)).to(CartoDBPersistence.class);
//        bind(Persistence.class).annotatedWith(Names.named(Persistence.MySQL)).to(MySQLPersistence.class);
	}

}
