package com.cahue.config.guice;

import com.google.inject.servlet.ServletModule;
import com.googlecode.objectify.ObjectifyFilter;

public class ProductionServletModule extends ServletModule {

    @Override
    protected void configureServlets() {

        filter("/*").through(ObjectifyFilter.class);
        filter("/*").through(AuthenticationFilter.class);

    }

}
