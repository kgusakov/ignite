package org.apache.ignite.internal.v2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Const {

    public final static String IGNITE_GROUP_ID;
    public final static String IGNITE_ARTIFACT_ID = "ignite-core";
    public final static String VERSION;

    static {
        // TODO: code quality
        try (InputStream inputStream = Const.class.getResourceAsStream("/version.properties")) {
            Properties prop = new Properties();
            prop.load(inputStream);
            IGNITE_GROUP_ID = prop.getProperty("group.id", "undefined");
            VERSION = prop.getProperty("version", "undefined");
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can' read ignite version info");
        }

    }
}
