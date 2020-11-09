package org.apache.ignite.internal.v2;

import javax.inject.Inject;
import javax.inject.Singleton;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Introspected;
import picocli.CommandLine;

@Singleton
@Introspected
public class CommandFactory implements CommandLine.IFactory {

    private final ApplicationContext applicationContext;

    @Inject
    public CommandFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    // TODO: Dirty way with silent fails on injecting - must be fixed
    @Override public <K> K create(Class<K> cls) throws Exception {
        try {
            return applicationContext.createBean(cls); // custom factory lookup or instantiation
        } catch (Exception e) {
            return CommandLine.defaultFactory().create(cls); // fallback if missing
        }
    }
}
