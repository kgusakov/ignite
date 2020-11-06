package org.apache.ignite.internal.v2;

import javax.inject.Inject;
import javax.inject.Singleton;
import io.micronaut.context.ApplicationContext;
import picocli.CommandLine;

@Singleton
public class CommandFactory implements CommandLine.IFactory {

    @Inject
    private final ApplicationContext applicationContext;

    public CommandFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    @Override public <K> K create(Class<K> cls) throws Exception {
        try {
            return applicationContext.createBean(cls); // custom factory lookup or instantiation
        } catch (Exception e) {
            return CommandLine.defaultFactory().create(cls); // fallback if missing
        }
    }
}
