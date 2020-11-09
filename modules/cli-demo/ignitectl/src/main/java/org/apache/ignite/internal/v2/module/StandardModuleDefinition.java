package org.apache.ignite.internal.v2.module;

import java.util.List;

public class StandardModuleDefinition {
    public final String name;
    public final String description;
    public final List<String> artifacts;
    public final List<String> cliArtifacts;

    public StandardModuleDefinition(String name, String description, List<String> artifacts, List<String> cliArtifacts) {
        this.name = name;
        this.description = description;
        this.artifacts = artifacts;
        this.cliArtifacts = cliArtifacts;
    }

    public String toString() {
        return this.name + ":\t" + this.description;
    }
}
