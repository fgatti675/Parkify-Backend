package com.cahue.config.guice;

import com.cahue.DataSource;
import com.google.inject.servlet.ServletModule;
import com.cahue.HelloWorldServlet;
import com.cahue.SecondTest;

public class ListerlyServletModule extends ServletModule {

	@Override
	protected void configureServlets() {
	    serve("/hi").with(HelloWorldServlet.class);
	    bind(SecondTest.class);
        bind(DataSource.class);
	}

}
