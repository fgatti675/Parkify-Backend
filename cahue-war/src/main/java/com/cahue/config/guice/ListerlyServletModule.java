package com.cahue.config.guice;

import com.cahue.DataSource;
import com.cahue.index.IndexManager;
import com.google.inject.servlet.ServletModule;

public class ListerlyServletModule extends ServletModule {

	@Override
	protected void configureServlets() {
//	    serve("/hi").with(HelloWorldServlet.class);
        bind(DataSource.class);
        bind(IndexManager.class);
	}

}
