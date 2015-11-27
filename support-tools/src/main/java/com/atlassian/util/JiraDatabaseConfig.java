package com.atlassian.util;

import nu.xom.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Discovers DB configuration and performs benchmarks on it
 */
public class JiraDatabaseConfig
{
    private static final Class[] parameters = new Class[] {URL.class};

    private final String dbType;
    private final String username;
    private final String password;
    private final String url;
    private final String driverClass;

    
    public JiraDatabaseConfig(String username, String password, String url, String dbType, String driverClass) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.dbType = dbType;
        this.driverClass = driverClass;
    }
    
    public static JiraDatabaseConfig parseDBConfig(String jiraHome) {
        try {
            Builder parser = new Builder();
            Document doc = parser.build(jiraHome + "/dbconfig.xml");
            String username = null;
            String password = null;

            Element root = doc.getRootElement();
            String databaseType = root.getFirstChildElement("database-type").getChild(0).getValue();
            Element jdbc = root.getFirstChildElement("jdbc-datasource");

            Element usernameChildren = jdbc.getFirstChildElement("username");
            if (usernameChildren.getChildCount() > 0) {
                username = usernameChildren.getChild(0).getValue();
            }

            Element passwordChildren = jdbc.getFirstChildElement("password");
            if (passwordChildren.getChildCount() > 0) {
                password = usernameChildren.getChild(0).getValue();
            }

            String driverClass = jdbc.getFirstChildElement("driver-class").getChild(0).getValue();
            String url = jdbc.getFirstChildElement("url").getChild(0).getValue();
            return new JiraDatabaseConfig(username, password, url, databaseType, driverClass);
        } catch (ParsingException | IOException ex) {
            throw new Error("There was an error retrieving your database configuration", ex);
        }
    }

    /**
     * Selects the jar that matches the DB
     * @param libPath
     * @return
     * @throws IOException
     */
    private Path findJarForDB(Path libPath) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(libPath)) {
            String dbType = getMinimalDBType();
            for (Path path : directoryStream) {
                if (path.toString().contains(dbType)) {
                    return path.toAbsolutePath();
                }
            }
            throw new RuntimeException("Could not find a driver for " + this.dbType + " in " + libPath);
        } catch (IOException ex) {
            throw new IOException("There was an error finding our DB driverClass", ex);
        }
    }
    
    private String getMinimalDBType() {
        if ("postgres72".equals(dbType)) {
            return "postgres";
        }
        return dbType;
    }

    /**
     * Loads database drives into the system class loader 
     * @param libPath Directory to where driver jars are located
     * @throws IOException
     */
    public void loadJar(Path libPath) throws IOException {
        Path jarPath = findJarForDB(libPath);

        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] {jarPath.toUri().toURL()});
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }

    public String getDBType() {
        return dbType;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getUrl() {
        return url;
    }

    public String getDriverClass() {
        return driverClass;
    }
}
