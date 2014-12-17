package com.cahue;

import com.cahue.config.TestModule;
import com.cahue.config.guice.ProductionModule;
import com.google.inject.util.Modules;
import org.jukito.JukitoModule;

/**
 * Date: 17.12.14
 *
 * @author francesco
 */
public class BaseTest {

    /**
     * Overrides the common bindings from TestBase with the
     * module that has test-specific bindings for Foo.
     */
    public static class Module extends JukitoModule {
        protected void configureTest() {
            install(Modules.override(new ProductionModule()).with(new TestModule()));
        }
    }

}
