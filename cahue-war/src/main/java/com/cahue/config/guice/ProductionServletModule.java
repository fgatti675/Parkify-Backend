package com.cahue.config.guice;

import com.cahue.auth.AuthenticationFilter;
import com.google.apphosting.utils.remoteapi.RemoteApiServlet;
import com.google.inject.servlet.ServletModule;
import com.googlecode.objectify.ObjectifyFilter;

import javax.inject.Singleton;

public class ProductionServletModule extends ServletModule {

    @Override
    protected void configureServlets() {

        filter("/*").through(ObjectifyFilter.class);
        filter("^((?!/remote_api).)*$").through(AuthenticationFilter.class); // all but remote_api

        bind(RemoteApiServlet.class).in(Singleton.class);
        serve("/remote_api").with(RemoteApiServlet.class);

    }

}
