package com.atlassian.util.status;

import com.atlassian.util.benchmark.ConnectionFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import thirdparty.DBTablePrinter;

import java.sql.Connection;
import java.sql.SQLException;


public class JIRADatabaseStatus {
    
    private final ConnectionFactory connectionFactory;
    private final String dbType;

    public JIRADatabaseStatus(ConnectionFactory connectionFactory, String dbType) {
        this.connectionFactory = connectionFactory;
        this.dbType = dbType;
    }
    
    public void analyzeConnection () throws SQLException {
        ConnectionTester tester = null;
        switch(dbType) {
            case "mysql":
                throw new NotImplementedException();
            case "postgres72":
                tester = new PostgresTester(connectionFactory.getConnection());
                break;
            case "oracle":
                throw new NotImplementedException();
            case "sqlserver":
                throw new NotImplementedException();
        }
        tester.run();
    }
    
    private abstract class ConnectionTester {
        protected Connection connection;
        
        public ConnectionTester(Connection connection) {
            this.connection = connection;
        }
        
        public abstract void run() throws SQLException;
    }

    private class PostgresTester extends ConnectionTester {

        public PostgresTester(Connection connection) {
            super(connection);
        }

        @Override
        public void run() throws SQLException {
            System.out.println("You will probably need to enable these parameters to get data:"
                    + "The parameter track_activities enables monitoring of the current command being executed by any server process.\n"
                    + "The parameter track_counts controls whether statistics are collected about table and index accesses.\n"
                    + "The parameter track_functions enables tracking of usage of user-defined functions.\n"
                    + "The parameter track_io_timing enables monitoring of block read and write times.");

            String[] tables = {
                    "pg_stat_activity",            // One row per server process, showing information related to the current activity of that process, such as state and current query. See pg_stat_activity for details.
                    "pg_stat_bgwriter",            // One row only, showing statistics about the background writer process's activity. See pg_stat_bgwriter for details.
                    "pg_stat_database",            // One row per database, showing database-wide statistics. See pg_stat_database for details.
                    "pg_stat_all_tables",          // One row for each table in the current database, showing statistics about accesses to that specific table. See pg_stat_all_tables for details.
                    "pg_stat_sys_tables",          // Same as pg_stat_all_tables, except that only system tables are shown.
                    "pg_stat_user_tables",         // Same as pg_stat_all_tables, except that only user tables are shown.
                    "pg_stat_xact_all_tables",     // Similar to pg_stat_all_tables, but counts actions taken so far within the current transaction (which are not yet included in pg_stat_all_tables and related views). The columns for numbers of live and dead rows and vacuum and analyze actions are not present in this view.
                    "pg_stat_xact_sys_tables",     // Same as pg_stat_xact_all_tables, except that only system tables are shown.
                    "pg_stat_xact_user_tables",    // Same as pg_stat_xact_all_tables, except that only user tables are shown.
                    "pg_stat_all_indexes",         // One row for each index in the current database, showing statistics about accesses to that specific index. See pg_stat_all_indexes for details.
                    "pg_stat_sys_indexes",         // Same as pg_stat_all_indexes, except that only indexes on system tables are shown.
                    "pg_stat_user_indexes",        // Same as pg_stat_all_indexes, except that only indexes on user tables are shown.
                    "pg_statio_all_tables",        // One row for each table in the current database, showing statistics about I/O on that specific table. See pg_statio_all_tables for details.
                    "pg_statio_sys_tables",        // Same as pg_statio_all_tables, except that only system tables are shown.
                    "pg_statio_user_tables",       // Same as pg_statio_all_tables, except that only user tables are shown.
                    "pg_statio_all_indexes",       // One row for each index in the current database, showing statistics about I/O on that specific index. See pg_statio_all_indexes for details.
                    "pg_statio_sys_indexes",       // Same as pg_statio_all_indexes, except that only indexes on system tables are shown.
                    "pg_statio_user_indexes",      // Same as pg_statio_all_indexes, except that only indexes on user tables are shown.
                    "pg_statio_all_sequences",     // One row for each sequence in the current database, showing statistics about I/O on that specific sequence. See pg_statio_all_sequences for details.
                    "pg_statio_sys_sequences",     // Same as pg_statio_all_sequences, except that only system sequences are shown. (Presently, no system sequences are defined, so this view is always empty.)
                    "pg_statio_user_sequences",    // Same as pg_statio_all_sequences, except that only user sequences are shown.
                    "pg_stat_user_functions",      // One row for each tracked function, showing statistics about executions of that function. See pg_stat_user_functions for details.
                    "pg_stat_xact_user_functions", // Similar to pg_stat_user_functions, but counts only calls during the current transaction (which are not yet included in pg_stat_user_functions).
                    "pg_stat_replication",         // One row per WAL sender process, showing statistics about replication to that sender's connected standby server. See pg_stat_replication for details.
                    "pg_stat_database_conflicts",  // One row per database, showing database-wide statistics about query cancels due to conflict with recovery on standby servers. See pg_stat_database_conflicts for details.
            };
            for (String table : tables) {
                DBTablePrinter.printTable(connection, table);
            }
        }
    }
}