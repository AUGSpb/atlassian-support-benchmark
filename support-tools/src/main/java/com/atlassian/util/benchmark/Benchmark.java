package com.atlassian.util.benchmark;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Benchmark
{
    static final int DEFAULT_RUNS = 1000;
	static final boolean PRINT_DETAILS = false;

    private final String name;
    private final List<TimedTestRunner> runners;
    private final int runs;
    private final PrintWriter writer;

    Benchmark(String name, List<TimedTestRunner> runners)
    {
        this(name, runners, DEFAULT_RUNS);
    }

    Benchmark(String name, List<TimedTestRunner> runners, int runs)
    {
        this(name, runners, runs, new PrintWriter(System.out, true));
    }

    Benchmark(String name, List<TimedTestRunner> runners, int runs, PrintWriter writer)
    {
        this.name = name;
        this.runners = Collections.unmodifiableList(new ArrayList<TimedTestRunner>(runners));
        this.runs = runs;
        this.writer = writer;
    }

    public void run()
    {
        writer.print("Benchmark: ");
        writer.println(name);
        writer.println();

        if (PRINT_DETAILS) {
			// write header
        	writer.print("#");
        	for (TimedTestRunner runner : runners)
        	{
            	writer.print("\t");
            	writer.print(runner.getName());
        	}
        	writer.println();
		}

        for (int i = 0; i < runs; i++)
        {
            if (PRINT_DETAILS) writer.print(i);
            for (TimedTestRunner runner : runners)
            {
                if (PRINT_DETAILS) {
					writer.print("\t");
                	writer.print(runner.run());
				}
				else runner.run();
            }
            if (PRINT_DETAILS) writer.println();
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
