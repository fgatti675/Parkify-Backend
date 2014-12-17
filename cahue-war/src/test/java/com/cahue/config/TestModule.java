package com.cahue.config;

import com.cahue.persistence.DataSource;
import com.cahue.persistence.MySQLPersistence;
import com.cahue.persistence.Persistence;
import com.google.inject.servlet.ServletModule;

public class TestModule extends ServletModule {

	@Override
	protected void configureServlets() {
        bind(DataSource.class).to(TestDataSource.class);
        bind(Persistence.class).to(MySQLPersistence.class);
	}

}
