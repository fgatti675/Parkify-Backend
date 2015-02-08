package com.cahue;

import com.cahue.config.TestModule;
import com.cahue.config.guice.ProductionModule;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.util.Modules;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cache.AsyncCacheFilter;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Francesco on 08/02/2015.
 */
@RunWith(JukitoRunner.class)
public class BaseTest {

    Closeable session;

    /**
     * Overrides the common bindings from TestBase with the
     * module that has test-specific bindings for Foo.
     */
    public static class Module extends JukitoModule {
        protected void configureTest() {
            install(Modules.override(new ProductionModule()).with(new TestModule()));
        }
    }

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig(),
            new LocalMemcacheServiceTestConfig());

    @Before
    public void setUp() {
        session = ObjectifyService.begin();
        helper.setUp();
    }

    @After
    public void tearDown() throws IOException {
        AsyncCacheFilter.complete();
        helper.tearDown();
        session.close();
        session = null;
    }

    // TODO
    protected String getGoogleAuthToken(){
        return "ya29.FAEkOikhiGg3pth8zWne3cjm2sXNFmXzPn_rAORmuhA5A0dAhBlXfGM2ppgFFlaWxCN0HlwhFheSxg";
    }

}
