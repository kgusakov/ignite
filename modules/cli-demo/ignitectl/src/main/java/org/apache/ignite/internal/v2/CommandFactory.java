package org.apache.ignite.internal.v2;

import java.util.Optional;
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
        Optional<K> bean = applicationContext.findOrInstantiateBean(cls);
        return bean.isPresent() ? bean.get() : CommandLine.defaultFactory().create(cls);// custom factory lookup or instantiation
    }
}
