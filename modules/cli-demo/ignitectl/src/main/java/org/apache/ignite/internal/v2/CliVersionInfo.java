package org.apache.ignite.internal.v2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.inject.Singleton;
import io.micronaut.core.annotation.Introspected;

@Singleton
@Introspected
public class CliVersionInfo {

    public final String version;

    public CliVersionInfo() {
        try (InputStream inputStream = CliVersionInfo.class.getResourceAsStream("/version.properties")) {
            Properties prop = new Properties();
            prop.load(inputStream);
            version = prop.getProperty("version", "undefined");
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can' read ignite version info");
        }
    }

    public CliVersionInfo(String version) {
        this.version = version;
    }
}
