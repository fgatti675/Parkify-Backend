package com.cahue.config;

import com.cahue.TestHelper;
import com.cahue.persistence.MySQLDataSource;
import com.cahue.persistence.MySQLIndex;
import com.cahue.persistence.SpotsIndex;
import com.cahue.resources.SpotsResource;
import com.google.inject.servlet.ServletModule;

public class TestModule extends ServletModule {

	@Override
	protected void configureServlets() {
//        bind(MySQLDataSource.class).to(TestDataSource.class);
        bind(SpotsIndex.class).to(MySQLIndex.class);
        bind(TestHelper.class);
        bind(SpotsResource.class);
    }

}
