package com.cahue.config.guice;

import com.cahue.CartoDBPersistence;
import com.cahue.DataSource;
import com.cahue.index.*;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

public class ListerlyServletModule extends ServletModule {

	@Override
	protected void configureServlets() {
//	    serve("/hi").with(HelloWorldServlet.class);
        bind(DataSource.class);
        bind(CartoDBPersistence.class);
        bind(Index.class).annotatedWith(Names.named("CartoDB")).to(CartoDBIndex.class);
        bind(Index.class).annotatedWith(Names.named("Fusion")).to(FusionIndex.class);
        bind(Index.class).annotatedWith(Names.named("FusionTest")).to(TestFusionIndex.class);
	}

}
