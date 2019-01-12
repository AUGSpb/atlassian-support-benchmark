package com.atlassian.util.benchmark;

import com.atlassian.util.JiraDatabaseConfig;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.util.benchmark.Benchmark.DEFAULT_NUMBER_OF_RUNS;

public class JIRASQLPerformance {

    private static final String RETRIEVE_ISSUE = "retrieve-issue";
    private static final String GET_ISSUE = "get-issue";
    private static final String RETRIEVE_WORKFLOW = "retrieve-workflow";
    private static final String GET_WORKFLOW = "get-workflow";
    private static final String RETRIEVE_CUSTOM_FIELD_VALUES = "retrieve-custom-field-value";
    private static final String GET_CUSTOM_FIELD_VALUE = "get-custom-field-value";
    private static final String COLUMN_ISSUE_WORKFLOW_ID = "WORKFLOW_ID";
    private final ConnectionFactory connectionFactory;
    private final int issueCount;

    public static void main(String[] args) throws Exception {
        int noOfRuns;

        try {
            ConnectionFactory connectionFactory;
            if (args.length < 4) {
                String jiraHome = args[0];
                String jiraInstallDir = args[1];
                JiraDatabaseConfig dbConfig = JiraDatabaseConfig.autoConfigDB(jiraHome, jiraInstallDir);
                connectionFactory = new ConnectionFactory(dbConfig);
                noOfRuns = (args.length == 3) ? Integer.valueOf(args[2]) : DEFAULT_NUMBER_OF_RUNS;
            } else {
                final String username = args[0];
                final String password = args[1];
                final String url = args[2];
                final String driver = args[3];
                connectionFactory = new ConnectionFactory(username, password, url, driver);

                // connectionFactory = new ConnectionFactory("jirauser", "jirauser",
                // "jdbc:jtds:sqlserver://192.168.0.89:1433/jed", "net.sourceforge.jtds.jdbc.Driver");
                noOfRuns = (args.length == 5) ? Integer.valueOf(args[4]) : DEFAULT_NUMBER_OF_RUNS;
            }
            (new JIRASQLPerformance(connectionFactory, noOfRuns)).call();
        } catch (IOException e) {
            System.err.println("There was an error reading your Jira config from " + args[0]);
            throw e;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Usage: "
                    + "\njava " + JIRASQLPerformance.class.getName() + " user password url driverClass [noOfRuns]"
                    + "\njava " + JIRASQLPerformance.class.getName() + " jirahome jirainstalldir [noOfRuns]");
        } catch (NumberFormatException e) {
            System.err.println("noOfRuns must be an integer (e.g. 1000)");
            System.err.println("Usage: "
                    + "\njava " + JIRASQLPerformance.class.getName() + " user password url driverClass [noOfRuns]");
        }
    }

    public JIRASQLPerformance(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.issueCount = DEFAULT_NUMBER_OF_RUNS;
    }

    public JIRASQLPerformance(ConnectionFactory connectionFactory, int issueCount) {
        this.connectionFactory = connectionFactory;
        this.issueCount = issueCount;
    }

    public void call() throws Exception {
        new Benchmark("Jira SQL: " + connectionFactory.getURL(), getTests(), issueCount).run();

    }

    private List<TimedTestRunner> getTests() throws Exception {
        final List<Long> ids = new ArrayList<>(issueCount);
        final Connection conn = connectionFactory.getConnection();
        final Random rnd = new Random();
        {
            final PreparedStatement countIssues = conn.prepareStatement("SELECT count(*) FROM jiraissue");
            ResultSet rs = countIssues.executeQuery();
            rs.next();
            int noOfIssues = rs.getInt(1);
            if (noOfIssues < issueCount) {
                throw new IllegalArgumentException("Cannot iterate over " + issueCount + " issues as there are only " + noOfIssues
                        + " issues in the database");
            }

            // generate issueCount ints between zero and totalNoOfIssues
            Set<Integer> picks = new HashSet<>(issueCount);
            while (picks.size() < issueCount) {
                picks.add(rnd.nextInt(noOfIssues));
            }
            final PreparedStatement selectIDs = conn.prepareStatement("SELECT id FROM jiraissue");
            rs = selectIDs.executeQuery();

            for (int i = 0; i < noOfIssues; i++) {
                rs.next();
                if (picks.contains(i)) {
                    ids.add(rs.getLong(1));
                }
            }
        }

        final PreparedStatement selectIssue = conn.prepareStatement("SELECT * FROM jiraissue WHERE id = ?");
        final AtomicReference<ResultSet> issueResultSet = new AtomicReference<>();
        final Map<String, String> issue = new HashMap<>();

        final PreparedStatement selectWorkFlow = conn.prepareStatement("SELECT * FROM OS_CURRENTSTEP WHERE entry_id = ?");
        final AtomicReference<ResultSet> wfResultSet = new AtomicReference<>();
        final Map<String, String> workflow = new HashMap<>();

        final List<TimedTestRunner> result = new ArrayList<>();

        result.add(new TimedTestRunner(RETRIEVE_ISSUE, () -> {
            selectIssue.setLong(1, ids.get(rnd.nextInt(issueCount)));
            issueResultSet.set(selectIssue.executeQuery());
            return null;
        }));

        result.add(new TimedTestRunner(GET_ISSUE, () -> {
            issue.clear();
            ResultSet rs = issueResultSet.get();
            rs.next();
            final int columnCount = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                issue.put(rs.getMetaData().getColumnName(i), rs.getString(i));
            }
            return null;
        }));

        result.add(new TimedTestRunner(RETRIEVE_WORKFLOW, () -> {
            final String workflowID = issue.get(COLUMN_ISSUE_WORKFLOW_ID);
            if (workflowID != null) {
                selectWorkFlow.setLong(1, Long.valueOf(workflowID));
                wfResultSet.set(selectWorkFlow.executeQuery());
            }
            return null;
        }));

        result.add(new TimedTestRunner(GET_WORKFLOW, () -> {
            workflow.clear();
            ResultSet rs = wfResultSet.get();
            if (rs == null) {
                System.out.println("No workflows found");
                return null;
            }
            rs.next();
            final int columnCount = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                workflow.put(rs.getMetaData().getColumnName(i), rs.getString(i));
            }
            return null;
        }));

        // Custom field field tests
        final PreparedStatement selectCustomFieldValues = conn.prepareStatement("SELECT ID, ISSUE, CUSTOMFIELD, PARENTKEY, STRINGVALUE, NUMBERVALUE, TEXTVALUE, DATEVALUE, VALUETYPE FROM customfieldvalue WHERE ISSUE=?");
        final AtomicReference<ResultSet> customFieldResultSet = new AtomicReference<>();
        final JIRASQLPerformance.StringMap customField = new JIRASQLPerformance.StringMap();

        result.add(new TimedTestRunner(RETRIEVE_CUSTOM_FIELD_VALUES, () -> {
            selectCustomFieldValues.setLong(1, Long.valueOf(issue.get("id")));
            customFieldResultSet.set(selectWorkFlow.executeQuery());
            return null;
        }));

        result.add(new TimedTestRunner(GET_CUSTOM_FIELD_VALUE, () -> {
            customField.clear();
            ResultSet rs = customFieldResultSet.get();
            rs.next();
            int columnCount = rs.getMetaData().getColumnCount();

            for (int i = 1; i <= columnCount; ++i) {
                customField.put(rs.getMetaData().getColumnName(i), rs.getString(i));
            }

            return null;
        }));

        return result;
    }

    private static final class StringMap extends HashMap<String, String> {
        private StringMap() {
        }

        public String put(String key, String value) {
            return super.put(key.toLowerCase(), value);
        }

        public String get(Object key) {
            return super.get(((String) key).toLowerCase());
        }
    }
}