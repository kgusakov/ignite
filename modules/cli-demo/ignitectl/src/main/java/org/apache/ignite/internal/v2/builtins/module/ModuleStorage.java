package org.apache.ignite.internal.v2.builtins.module;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;

@Singleton
public class ModuleStorage {

    private final SystemPathResolver pathResolver;

    @Inject
    public ModuleStorage(SystemPathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    private Path moduleFile() {
        return Config.getConfigOrError(pathResolver).installedModulesFile();
    }

    public void saveModule(ModuleDefinition moduleDefinition) throws IOException {
        ModuleDefinitionsRegistry moduleDefinitionsRegistry = listInstalled();
        moduleDefinitionsRegistry.modules.add(moduleDefinition);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(moduleFile().toFile(), moduleDefinitionsRegistry);
    }

    public void removeModule(String name) throws IOException {
        ModuleDefinitionsRegistry moduleDefinitionsRegistry = listInstalled();
        moduleDefinitionsRegistry.modules.removeIf(m -> m.name.equals(name));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(moduleFile().toFile(), moduleDefinitionsRegistry);
    }

    public ModuleDefinitionsRegistry listInstalled() throws IOException {
        if (!moduleFile().toFile().exists())
            return new ModuleDefinitionsRegistry(new ArrayList<>());
        else {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(
                moduleFile().toFile(),
                ModuleDefinitionsRegistry.class);
        }
    }


    public static class ModuleDefinitionsRegistry {
        public final List<ModuleDefinition> modules;

        @JsonCreator
        public ModuleDefinitionsRegistry(
            @JsonProperty("modules") List<ModuleDefinition> modules) {
            this.modules = modules;
        }
    }
    public static class ModuleDefinition {
        public final String name;
        public final List<Path> artifacts;
        public final List<Path> cliArtifacts;
        public final SourceType type;
        public final String source;

        @JsonCreator
        public ModuleDefinition(
            @JsonProperty("name") String name, @JsonProperty("artifacts") List<Path> artifacts,
            @JsonProperty("cliArtifacts") List<Path> cliArtifacts,
            @JsonProperty("type") SourceType type, @JsonProperty("source") String source) {
            this.name = name;
            this.artifacts = artifacts;
            this.cliArtifacts = cliArtifacts;
            this.type = type;
            this.source = source;
        }

        @JsonGetter("artifacts")
        public List<String> getArtifacts() {
            return artifacts.stream().map(a -> a.toAbsolutePath().toString()).collect(Collectors.toList());
        }

        @JsonGetter("cliArtifacts")
        public List<String> getCliArtifacts() {
            return cliArtifacts.stream().map(a -> a.toAbsolutePath().toString()).collect(Collectors.toList());
        }
    }

    public enum SourceType {
        File,
        Maven,
        Standard
    }
}
