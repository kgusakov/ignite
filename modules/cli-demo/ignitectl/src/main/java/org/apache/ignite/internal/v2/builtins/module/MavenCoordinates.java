package org.apache.ignite.internal.v2.builtins.module;

import org.apache.ignite.internal.v2.IgniteCLIException;

public class MavenCoordinates {
    public final String groupId;
    public final String artifactId;
    public final String version;

    public MavenCoordinates(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    static MavenCoordinates of(String mvnString) {
        String[] coords = mvnString.split(":");

        if (coords.length == 4)
            return new MavenCoordinates(coords[1], coords[2], coords[3]);
        else
            throw new IgniteCLIException("Incorrect maven coordinates " + mvnString);
    }

    static MavenCoordinates of(String mvnString, String version) {
        return of(mvnString + ":" + version);
    }
}
