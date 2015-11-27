package com.atlassian.util;

import com.atlassian.util.benchmark.ConnectionFactory;
import com.atlassian.util.benchmark.JIRASQLPerformance;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
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
public class JiraDatabaseConfig
{
    private static final int NUMBER_OF_RUNS = 1000;
    private static final Class[] parameters = new Class[] {URL.class};

    private final String dbType;
    private final String username;
    private final String password;
    private final String url;
    private final String driverClass;

    public static void main(String[] args) throws Exception {
        try {
            String jiraHome = args[0];
            String jiraInstallDir = args[1];
            JiraDatabaseConfig config = parseDBConfig(jiraHome);
            
            loadJar(Paths.get(jiraInstallDir, "lib"), config.dbType);
            
            final ConnectionFactory connectionFactory = new ConnectionFactory(config.username,
                    config.password, config.url, config.driverClass);
            
            new JIRASQLPerformance(connectionFactory, NUMBER_OF_RUNS).call();

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Required parameters: jirahome_dir jira_install_dir");
        }
    }
    
    public JiraDatabaseConfig(String username, String password, String url, String dbType, String driverClass) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.dbType = dbType;
        this.driverClass = driverClass;
    }
    
    private static JiraDatabaseConfig parseDBConfig(String jiraHome) {
        try {
            Builder parser = new Builder();
            Document doc = parser.build(jiraHome + "/dbconfig.xml");
            String username = null;
            String password = null;

            Element root = doc.getRootElement();
            String databaseType = root.getFirstChildElement("database-type").getChild(0).getValue();
            Element jdbc = root.getFirstChildElement("jdbc-datasource");
            
            Elements usernameChildren = jdbc.getFirstChildElement("username").getChildElements();
            if (usernameChildren.size() > 0) {
                username = usernameChildren.get(0).getValue();
            }

            Elements passwordChildren = jdbc.getFirstChildElement("password").getChildElements();
            if (passwordChildren.size() > 0) {
                password = usernameChildren.get(0).getValue();
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
     * @param dbType
     * @return
     * @throws IOException
     */
    private static Path findJarForDB(Path libPath, String dbType) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(libPath)) {
            for (Path path : directoryStream) {
                if (path.toString().contains(dbType)) {
                    return path.toAbsolutePath();
                }
            }
            throw new RuntimeException("Could not find a driver for " + dbType + "in " + libPath);
        } catch (IOException ex) {
            throw new IOException("There was an error finding our DB driverClass", ex);
        }
    }

    /**
     * Loads database drives into the system class loader 
     * @param libPath Directory to where driver jars are located
     * @param dbType Type of database
     * @throws IOException
     */
    private static void loadJar(Path libPath, String dbType) throws IOException {
        Path jarPath = findJarForDB(libPath, dbType);

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
}
