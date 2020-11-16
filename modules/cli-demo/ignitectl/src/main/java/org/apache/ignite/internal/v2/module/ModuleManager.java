package org.apache.ignite.internal.v2.module;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import io.micronaut.context.annotation.Prototype;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.Info;
import org.apache.ivy.plugins.repository.TransferListener;

@Singleton
public class ModuleManager {

    private List<StandardModuleDefinition> modules;
    private MavenArtifactResolver mavenArtifactResolver;
    private Info info;
    private PrintWriter out;
    private ModuleStorage moduleStorage;

    public ModuleManager(List<StandardModuleDefinition> modules) {
        this.modules = readBuiltinModules();
    }

    @Inject
    public void setModuleStorage(ModuleStorage moduleStorage) {
        this.moduleStorage = moduleStorage;
    }

    @Inject
    public void setMavenArtifactResolver(MavenArtifactResolver resolver) {
        this.mavenArtifactResolver = resolver;
    }

    @Inject
    public void setInfo(Info info) {
        this.info = info;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
        mavenArtifactResolver.setOut(out);
    }

    public static ModuleManager load() {
        return new ModuleManager(readBuiltinModules());
    }

    public void addModule(String name, Config config, boolean cli) {
        Path installPath;
        if (cli)
            installPath = config.cliLibsDir(info.version);
        else
            installPath = config.libsDir(info.version);
        if (name.startsWith("mvn:")) {
            MavenCoordinates mavenCoordinates = MavenCoordinates.of(name);

            try {
                ResolveResult resolveResult = mavenArtifactResolver.resolve(
                    installPath,
                    mavenCoordinates.groupId,
                    mavenCoordinates.artifactId,
                    mavenCoordinates.version
                );
                moduleStorage.saveModule(new ModuleStorage.ModuleDefinition(
                    name,
                    resolveResult.artifacts(),
                    new ArrayList<>(),
                    ModuleStorage.SourceType.Maven,
                    name
                ));
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
            ResolveResult libsResolveResults = null;
            for (String artifact: moduleDescription.artifacts) {
                MavenCoordinates mavenCoordinates = MavenCoordinates.of(artifact, info.version);
                try {
                    libsResolveResults = mavenArtifactResolver.resolve(
                        config.libsDir(info.version),
                        mavenCoordinates.groupId,
                        mavenCoordinates.artifactId,
                        mavenCoordinates.version
                    );
                }
                catch (IOException e) {
                    throw new IgniteCLIException("Error during resolving standard module " + name, e);
                }
            }

            ResolveResult cliResolvResults = null;
            for (String artifact: moduleDescription.cliArtifacts) {
                MavenCoordinates mavenCoordinates = MavenCoordinates.of(artifact, info.version);
                try {
                    cliResolvResults = mavenArtifactResolver.resolve(
                        config.cliLibsDir(info.version),
                        mavenCoordinates.groupId,
                        mavenCoordinates.artifactId,
                        mavenCoordinates.version
                    );
                }
                catch (IOException e) {
                    throw new IgniteCLIException("Error during resolving module " + name, e);
                }
            }

            try {
                moduleStorage.saveModule(new ModuleStorage.ModuleDefinition(
                    name,
                    (libsResolveResults == null ? new ArrayList<>():libsResolveResults.artifacts()),
                    (cliResolvResults == null ? new ArrayList<>():cliResolvResults.artifacts()),
                    ModuleStorage.SourceType.Maven,
                    name
                ));
            }
            catch (IOException e) {
                throw new IgniteCLIException("Error during saving the installed module info");
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
