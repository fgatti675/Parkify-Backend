package com.cahue.config.jersey;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;
import freemarker.template.Configuration;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

public class TemplateObjectFactory extends Configuration {

    @Inject
    private ServletContext servletContext;

    public TemplateObjectFactory() {
        // Create different loaders.
        final List<TemplateLoader> loaders = new ArrayList<>();
        if (servletContext != null) {
            loaders.add(new WebappTemplateLoader(servletContext));
        }
        loaders.add(new ClassTemplateLoader(this.getClass(), "/"));

        setTemplateLoader(new MultiTemplateLoader(loaders.toArray(new TemplateLoader[loaders.size()])));
    }
}
