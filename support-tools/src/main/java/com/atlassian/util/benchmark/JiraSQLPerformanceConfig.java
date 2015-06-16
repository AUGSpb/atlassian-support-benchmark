package com.atlassian.util.benchmark;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jsimon on 3/06/2015.
 */
public class JiraSQLPerformanceConfig
{
    private static final int NUMBER_OF_RUNS = 1000;
    private String username;
    private String password;
    private String url;
    private String driver;
    private String dbType;

    private static final Map<String, String> DRIVERS = new HashMap<String, String>(){{
        //TODO add mysql
        put("hsql","hsqldb-1.8.0.5.jar");
        put("h2", null);
        put("h2", null);
        put("h2", null);
        put("h2", null);
        put("h2", null);
        put("postgres72","postgresql-9.0-801.jdbc4.jar");
    }};


    
    SQL_SERVER("SQL Server", "mssql", "Database", new SqlServerUrlParser(), ImmutableList.of("net.sourceforge.jtds.jdbc.Driver",
        // SQL Server with Microsoft JDBC drivers. Supported for manual setting but not used by default.
        "com.microsoft.jdbc.sqlserver.SQLServerDriver")),
    MY_SQL("MySQL", "mysql", "Database", new MySqlUrlParser(), ImmutableList.of("com.mysql.jdbc.Driver")),
    ORACLE("Oracle", "oracle10g", "SID", new OracleUrlParser(), ImmutableList.of("oracle.jdbc.OracleDriver")),
    POSTGRES("PostgreSQL", "postgres72", "Database", new PostgresUrlParser(), ImmutableList.of("org.postgresql.Driver")),
    UKNOWN("Uknown", "unknown", "unknown", null, ImmutableList.<String>of());
    
    
    public static void main(String[] args) throws Exception
    {
        try
        {
            String jiraHome = args[0];
            String jiraInstallDir = args[1];
            
            JiraSQLPerformanceConfig config = parseDBConfig(jiraHome);
            
            final ConnectionFactory connectionFactory = new ConnectionFactory(config.username,
                    config.password, config.url, config.driver);
            new JIRASQLPerformance(connectionFactory, NUMBER_OF_RUNS).call();

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Required parameters: jirahome_dir jira_install_dir");
        }
    }
    
    public JiraSQLPerformanceConfig(String username, String password, String url, String dbType, String driver) {
        this.dbType = dbType;
        this.username = username;
        this.password = password;
        this.url = url;
        this.driver = driver;
    }
    
    private static JiraSQLPerformanceConfig parseDBConfig(String jiraHome)
    {
        try {
            Builder parser = new Builder();
            Document doc = parser.build(jiraHome + "/dbconfig.xml");
            Element root = doc.getRootElement();
            String dbType = root.getAttributeValue("database-type");
            Element jdbc = root.getFirstChildElement("jdbc-datasource");
            String username = jdbc.getAttributeValue("username");
            String password = jdbc.getAttributeValue("password");
            String driver = jdbc.getAttributeValue("driver-class");
            String url = jdbc.getAttributeValue("url");
            return new JiraSQLPerformanceConfig(username, password, url, dbType, driver);
        }
        catch (ParsingException ex) {
            System.err.println("Cafe con Leche is malformed today. How embarrassing!");
        }
        catch (IOException ex) {
            System.err.println("Could not connect to Cafe con Leche. The site may be down.");
        }
    }
    
    
}
