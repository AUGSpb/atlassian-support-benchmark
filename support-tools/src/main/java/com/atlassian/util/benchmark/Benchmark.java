package com.atlassian.util.benchmark;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Benchmark
{
	static final boolean PRINT_DETAILS = false;

    private final String name;
    private final List<TimedTestRunner> runners;
    private final int runs;
    private final PrintWriter writer;

    Benchmark(String name, List<TimedTestRunner> runners, int runs)
    {
        this(name, runners, runs, new PrintWriter(System.out, true));
    }

    Benchmark(String name, List<TimedTestRunner> runners, int runs, PrintWriter writer)
    {
        this.name = name;
        this.runners = Collections.unmodifiableList(new ArrayList<>(runners));
        this.runs = runs;
        this.writer = writer;
    }

    private void printDetails(List<List<Timer>> runResults) {
        // write header
        writer.print("#");
        for (TimedTestRunner runner : runners)
        {
            writer.print("\t");
            writer.print(runner.getName());
        }
        writer.println();

        for (int i = 0; i < runs; i++)
        {
            writer.print(i);
            for (Timer timer : runResults.get(i))
            {
                writer.print("\t");
                writer.print(timer);
            }
            writer.println();
        }
    }

    /**
     *
     * @return empty list if PRINT_DETAILS is false, a matrix of Timer otherwise
     */
    private List<List<Timer>> runTests ()
    {
        List<List<Timer>> runResults = new ArrayList<>();
        for (int i = 0; i < runs; i++)
        {
            for (TimedTestRunner runner : runners)
            {
                Timer result = runner.run();
                if (PRINT_DETAILS) {
                    runResults.get(i).add(result);
                }
            }
        }
        return runResults;
    }
    
    public void run()
    {
        List<List<Timer>> runResults = runTests();
        
        writer.print("Benchmark: " + name + "\n");
        if (PRINT_DETAILS) {
			printDetails(runResults);
        }
        
        writer.println();
        writer.println("TOTALS");
        writer.println("----\t----\t----\t----\t----");
        writer.println("stat\tavg\tmedian\tmin\tmax");
        writer.println("----\t----\t----\t----\t----");
        for (TimedTestRunner runner : runners)
        {
            writer.println(runner);
        }
        writer.println("----\t----\t----\t----\t----");
        writer.println("All times are in nanoseconds.");
        
        writer.flush();
    }
}
