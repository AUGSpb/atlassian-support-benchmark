package com.atlassian.util;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Discovers DB configuration and performs benchmarks on it
 */
public class JiraDatabaseConfig {
    private static final Class[] parameters = new Class[]{URL.class};

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

    /**
     * Discovers Jira DB config and loads drivers in classpath
     *
     * @param jiraHome       home directory
     * @param jiraInstallDir installation directory
     * @return configuration of config
     * @throws IOException
     */
    public static JiraDatabaseConfig autoConfigDB(String jiraHome, String jiraInstallDir) throws IOException {
        JiraDatabaseConfig config = JiraDatabaseConfig.parseDBConfig(jiraHome);
        System.out.println("Detected following DB configuration:"
                + "\n\tdbType: " + config.getDBType()
                + "\n\tdriverClass: " + config.getDriverClass()
                + "\n\tusername: " + config.getUsername()
                + "\n\turl: " + config.getUrl());
        config.loadJar(jiraInstallDir);
        return config;
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
                password = passwordChildren.getChild(0).getValue();
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
     *
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
     *
     * @param jiraInstallDir Jira installation directory
     * @throws IOException
     */
    public void loadJar(String jiraInstallDir) throws IOException {
        Path libPath = Paths.get(jiraInstallDir, "lib");
        Path jarPath = findJarForDB(libPath);
        System.out.println("\tDriverClass path: " + jarPath.toString());
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> sysClass = URLClassLoader.class;
        try {
            Method method = sysClass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysLoader, jarPath.toUri().toURL());
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

    public static JiraDatabaseConfig autoDiscoverConfig(String jiraHome, String jiraInstallDir) throws IOException {
        JiraDatabaseConfig config = JiraDatabaseConfig.parseDBConfig(jiraHome);
        config.loadJar(jiraInstallDir);
        return config;
    }
}
