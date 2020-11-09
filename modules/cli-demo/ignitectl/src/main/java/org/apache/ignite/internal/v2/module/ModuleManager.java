package org.apache.ignite.internal.v2.module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import io.micronaut.context.annotation.Prototype;
import org.apache.ignite.internal.installer.MavenArtifactResolver;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.Info;
import org.apache.ivy.plugins.repository.TransferListener;

@Prototype
public class ModuleManager {

    private List<StandardModuleDefinition> modules;
    private MavenArtifactResolver mavenArtifactResolver;
    private Info info;

    public ModuleManager(List<StandardModuleDefinition> modules) {
        this.modules = readBuiltinModules();
    }

    @Inject
    public void setMavenArtifactResolver(MavenArtifactResolver resolver) {
        this.mavenArtifactResolver = resolver;
    }

    @Inject
    public void setInfo(Info info) {
        this.info = info;
    }

    public static ModuleManager load() {
        return new ModuleManager(readBuiltinModules());
    }

    public void addModule(String name, Config config, TransferListener transferListener) {
        if (name.startsWith("mvn:")) {
            MavenCoordinates mavenCoordinates = MavenCoordinates.of(name);

            try {
                mavenArtifactResolver.resolve(
                    config.libsDir(info.version),
                    mavenCoordinates.groupId,
                    mavenCoordinates.artifactId,
                    mavenCoordinates.version,
                    transferListener);
            }
            catch (IOException e) {
                throw new IgniteCLIException("Error during resolving maven module " + name, e);
            }

        }
        else if (name.startsWith("file://"))
            throw new RuntimeException("File urls is not implemented yet");
        else if (isStandardModuleName(name)) {
            StandardModuleDefinition moduleDescription = readBuiltinModules()
                .stream()
                .filter(m -> m.name.equals(name))
                .findFirst().get();
            for (String artifact: moduleDescription.artifacts) {
                MavenCoordinates mavenCoordinates = MavenCoordinates.of(artifact, info.version);
                try {
                    mavenArtifactResolver.resolve(
                        config.libsDir(info.version),
                        mavenCoordinates.groupId,
                        mavenCoordinates.artifactId,
                        mavenCoordinates.version,
                        transferListener);
                }
                catch (IOException e) {
                    throw new IgniteCLIException("Error during resolving standard module " + name, e);
                }
            }

            for (String artifact: moduleDescription.cliArtifacts) {
                MavenCoordinates mavenCoordinates = MavenCoordinates.of(artifact, info.version);
                try {
                    mavenArtifactResolver.resolve(
                        config.cliLibsDir(info.version),
                        mavenCoordinates.groupId,
                        mavenCoordinates.artifactId,
                        mavenCoordinates.version,
                        transferListener);
                }
                catch (IOException e) {
                    throw new IgniteCLIException("Error during resolving module " + name, e);
                }
            }

        }
        else {
            throw new IgniteCLIException(
                "Module coordinates for non-standard modules must be started with mvn:|file://");
        }
    }

    private boolean isStandardModuleName(String name) {
        return modules.stream().anyMatch(m -> m.name.equals(name));
    }



    public static List<StandardModuleDefinition> readBuiltinModules() {
        com.typesafe.config.ConfigObject config = ConfigFactory.load("modules.conf").getObject("modules");
        List<StandardModuleDefinition> modules = new ArrayList<>();
        for (Map.Entry<String, ConfigValue> entry: config.entrySet()) {
            ConfigObject configObject = (ConfigObject) entry.getValue();
            modules.add(new StandardModuleDefinition(
                entry.getKey(),
                configObject.toConfig().getString("description"),
                configObject.toConfig().getStringList("artifacts"),
                configObject.toConfig().getStringList("cli-artifacts")
            ));
        }
        return modules;
    }

}
