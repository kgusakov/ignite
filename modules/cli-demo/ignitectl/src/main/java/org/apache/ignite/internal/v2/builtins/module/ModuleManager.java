package org.apache.ignite.internal.v2.builtins.module;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import org.apache.ignite.internal.v2.IgnitePaths;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.CliVersionInfo;

@Singleton
public class ModuleManager {

    private final List<StandardModuleDefinition> modules;
    private final MavenArtifactResolver mavenArtifactResolver;
    private final CliVersionInfo cliVersionInfo;
    private final ModuleStorage moduleStorage;

    @Inject
    public ModuleManager(MavenArtifactResolver mavenArtifactResolver, CliVersionInfo cliVersionInfo,
        ModuleStorage moduleStorage) {
        this(readBuiltinModules(),
            mavenArtifactResolver,
            cliVersionInfo,
            moduleStorage
        );
    }

    public ModuleManager(
        List<StandardModuleDefinition> modules,
        MavenArtifactResolver mavenArtifactResolver, CliVersionInfo cliVersionInfo,
        ModuleStorage moduleStorage) {
        this.modules = readBuiltinModules();
        this.mavenArtifactResolver = mavenArtifactResolver;
        this.cliVersionInfo = cliVersionInfo;
        this.moduleStorage = moduleStorage;
    }

    public void setOut(PrintWriter out) {
        mavenArtifactResolver.setOut(out);
    }

    public void addModule(String name, IgnitePaths ignitePaths, boolean cli) {
        Path installPath;
        if (cli)
            installPath = ignitePaths.cliLibsDir();
        else
            installPath = ignitePaths.libsDir();
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
            List<ResolveResult> libsResolveResults = new ArrayList<>();
            for (String artifact: moduleDescription.artifacts) {
                MavenCoordinates mavenCoordinates = MavenCoordinates.of(artifact, cliVersionInfo.version);
                try {
                    libsResolveResults.add(mavenArtifactResolver.resolve(
                        ignitePaths.libsDir(),
                        mavenCoordinates.groupId,
                        mavenCoordinates.artifactId,
                        mavenCoordinates.version
                    ));
                }
                catch (IOException e) {
                    throw new IgniteCLIException("Error during resolving standard module " + name, e);
                }
            }

            List<ResolveResult> cliResolvResults = new ArrayList<>();
            for (String artifact: moduleDescription.cliArtifacts) {
                MavenCoordinates mavenCoordinates = MavenCoordinates.of(artifact, cliVersionInfo.version);
                try {
                    cliResolvResults.add(mavenArtifactResolver.resolve(
                        ignitePaths.cliLibsDir(),
                        mavenCoordinates.groupId,
                        mavenCoordinates.artifactId,
                        mavenCoordinates.version
                    ));
                }
                catch (IOException e) {
                    throw new IgniteCLIException("Error during resolving module " + name, e);
                }
            }

            try {
                moduleStorage.saveModule(new ModuleStorage.ModuleDefinition(
                    name,
                    libsResolveResults.stream().flatMap(r -> r.artifacts().stream()).collect(Collectors.toList()),
                    cliResolvResults.stream().flatMap(r -> r.artifacts().stream()).collect(Collectors.toList()),
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

    public boolean removeModule(String name) {
        try {
            return moduleStorage.removeModule(name);
        }
        catch (IOException e) {
            throw new IgniteCLIException(
                "Can't remove module " + name, e);
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
