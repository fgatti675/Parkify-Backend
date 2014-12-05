package com.cahue.config.guice;

import com.cahue.DataSource;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

public class ListerlyServletModule extends ServletModule {

	@Override
	protected void configureServlets() {
//	    serve("/hi").with(HelloWorldServlet.class);
        bind(DataSource.class);
	}

}
