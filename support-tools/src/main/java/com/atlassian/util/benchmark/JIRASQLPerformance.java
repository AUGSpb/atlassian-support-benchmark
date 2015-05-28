package com.atlassian.util.benchmark;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

public class JIRASQLPerformance
{
    public static void main(String[] args) throws Exception
    {
        final ConnectionFactory connectionFactory;
        final int noOfRuns;
        try
        {
            connectionFactory = new ConnectionFactory(args[0], args[1], args[2], args[3]);
            // connectionFactory = new ConnectionFactory("jirauser", "jirauser",
            // "jdbc:jtds:sqlserver://192.168.0.89:1433/jed", "net.sourceforge.jtds.jdbc.Driver");
            noOfRuns = (args.length == 5) ? Integer.valueOf(args[4]) : 1000;
        }
        catch (RuntimeException e)
        {
            System.out.println("Usage: java " + JIRASQLPerformance.class.getName() + " user password url driverClass [noOfRuns]");
            throw e;
        }
        new JIRASQLPerformance(connectionFactory, noOfRuns).call();
    }

    private final ConnectionFactory connectionFactory;
    private final int issueCount;

    JIRASQLPerformance(ConnectionFactory connectionFactory, int issueCount)
    {
        this.connectionFactory = connectionFactory;
        this.issueCount = issueCount;
    }

    public Object call() throws Exception
    {
        new Benchmark("JIRA SQL: " + connectionFactory.getURL(), getTests(), issueCount).run();

        return null;
    }

    private List<TimedTestRunner> getTests() throws Exception
    {
        final List<Long> ids = new ArrayList<Long>(issueCount);
        final Connection conn = connectionFactory.getConnection();
        final Random rnd = new Random();
        {
            final PreparedStatement countIssues = conn.prepareStatement("SELECT count(*) FROM jiraissue");
            ResultSet rs = countIssues.executeQuery();
            rs.next();
            int noOfIssues = rs.getInt(1);
            if (noOfIssues < issueCount)
            {
                throw new IllegalArgumentException("Cannot iterate over " + issueCount + " issues as there are only " + noOfIssues
                    + " issues in the database");
            }

            // generate issueCount ints between zero and totalNoOfIssues
            Set<Integer> picks = new HashSet<Integer>(issueCount);
            while (picks.size() < issueCount)
            {
                picks.add(rnd.nextInt(noOfIssues));
            }
            final PreparedStatement selectIDs = conn.prepareStatement("SELECT id FROM jiraissue");
            rs = selectIDs.executeQuery();

            for (int i = 0; i < noOfIssues; i++)
            {
                rs.next();
                if (picks.contains(i))
                {
                    ids.add(rs.getLong(1));
                }
            }
        }

        final PreparedStatement selectIssue = conn.prepareStatement("SELECT * FROM jiraIssue WHERE id = ?");
        final AtomicReference<ResultSet> issueResultSet = new AtomicReference<ResultSet>();
        final Map<String, String> issue = new HashMap<String, String>();

        final PreparedStatement selectWorkFlow = conn.prepareStatement("SELECT * FROM os_currentStep WHERE entry_id = ?");
        final AtomicReference<ResultSet> wfResultSet = new AtomicReference<ResultSet>();
        final Map<String, String> workflow = new HashMap<String, String>();

        final List<TimedTestRunner> result = new ArrayList<TimedTestRunner>();
        result.add(new TimedTestRunner("retrieveIssue", new Callable<Object>()
        {
            public Object call() throws Exception
            {
                selectIssue.setLong(1, ids.get(rnd.nextInt(issueCount)));
                issueResultSet.set(selectIssue.executeQuery());
                return null;
            }
        }));

        result.add(new TimedTestRunner("getIssue", new Callable<Object>()
        {
            public Object call() throws Exception
            {
                issue.clear();
                ResultSet rs = issueResultSet.get();
                rs.next();
                final int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++)
                {
                    issue.put(rs.getMetaData().getColumnName(i), rs.getString(i));
                }
                return null;
            }
        }));

        result.add(new TimedTestRunner("retrieveWF", new Callable<Object>()
        {
            public Object call() throws Exception
            {
                selectWorkFlow.setLong(1, Long.valueOf(issue.get("WORKFLOW_ID")));
                wfResultSet.set(selectWorkFlow.executeQuery());
                return null;
            }
        }));

        result.add(new TimedTestRunner("getWorkflow", new Callable<Object>()
        {
            public Object call() throws Exception
            {
                workflow.clear();
                ResultSet rs = wfResultSet.get();
                rs.next();
                final int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++)
                {
                    workflow.put(rs.getMetaData().getColumnName(i), rs.getString(i));
                }
                return null;
            }
        }));

        return result;
    }
}