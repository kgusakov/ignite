package org.apache.ignite.internal.v2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.inject.Singleton;
import io.micronaut.core.annotation.Introspected;

@Singleton
@Introspected
public class Info {

    public final String groupId;
    public final String artifactId;
    public final String version;

    public Info() {
        try (InputStream inputStream = Info.class.getResourceAsStream("/version.properties")) {
            Properties prop = new Properties();
            prop.load(inputStream);
            groupId = prop.getProperty("group.id", "undefined");
            artifactId = "ignite-core";
            version = prop.getProperty("version", "undefined");
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can' read ignite version info");
        }
    }

    public Info(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }
}
