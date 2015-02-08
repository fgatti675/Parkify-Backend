package com.cahue.config;

import com.cahue.persistence.MySQLDataSource;
import com.cahue.persistence.MySQLPersistence;
import com.cahue.persistence.SpotsIndex;
import com.google.inject.servlet.ServletModule;

public class TestModule extends ServletModule {

	@Override
	protected void configureServlets() {
        bind(MySQLDataSource.class).to(TestDataSource.class);
        bind(SpotsIndex.class).to(MySQLPersistence.class);
	}

}
