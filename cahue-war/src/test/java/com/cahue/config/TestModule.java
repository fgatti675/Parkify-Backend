package com.cahue.config;

import com.cahue.resources.TestHelper;
import com.cahue.persistence.MySQLDataSource;
import com.cahue.persistence.MySQLIndex;
import com.cahue.persistence.SpotsIndex;
import com.cahue.resources.SpotsResource;
import com.google.inject.*;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;

public class TestModule extends AbstractModule {


    @Override
    protected void configure() {

        MockRequestScope myMockScope = new MockRequestScope();
        bindScope(RequestScoped.class, myMockScope);

        bind(MySQLDataSource.class).to(TestDataSource.class);

        bind(TestHelper.class);
        bind(SpotsResource.class);
    }

    public class MockRequestScope implements Scope {

        @Override
        public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
            return unscoped;
        }

        @Override
        public String toString() {
            return null;
        }
    }

}
