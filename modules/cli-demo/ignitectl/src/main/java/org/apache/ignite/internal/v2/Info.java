package org.apache.ignite.internal.v2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Info {

    public final String groupId;
    public final String artifactId = "ignite-core";
    public final String version;

    public Info() {
        try (InputStream inputStream = Info.class.getResourceAsStream("/version.properties")) {
            Properties prop = new Properties();
            prop.load(inputStream);
            groupId = prop.getProperty("group.id", "undefined");
            version = prop.getProperty("version", "undefined");
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can' read ignite version info");
        }
    }
}
