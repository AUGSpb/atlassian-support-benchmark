package com.atlassian.util.benchmark;

import de.vandermeer.asciitable.AsciiTable;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Benchmark {
    private static final boolean PRINT_DETAILS = false;
    static final int DEFAULT_NUMBER_OF_RUNS = 1000;
    private final String name;
    private final List<TimedTestRunner> runners;
    private final int runs;
    private final PrintWriter writer;

    Benchmark(String name, List<TimedTestRunner> runners) {
        this(name, runners, DEFAULT_NUMBER_OF_RUNS, new PrintWriter(System.out, true));
    }

    Benchmark(String name, List<TimedTestRunner> runners, int runs) {
        this(name, runners, runs, new PrintWriter(System.out, true));
    }

    private Benchmark(String name, List<TimedTestRunner> runners, int runs, PrintWriter writer) {
        this.name = name;
        this.runners = Collections.unmodifiableList(new ArrayList<>(runners));
        this.runs = runs;
        this.writer = writer;
    }

    private void printDetails(List<List<Timer>> runResults) {
        // write header
        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("#", runners.stream().iterator().next().getName());
        table.addRule();

        for (int i = 0; i < runs; i++) {
            table.addRule();
            table.addRow(i, runResults.get(i).stream().iterator().next());
            table.addRule();
        }
        writer.print(table.render());
    }

    /**
     * @return empty list if PRINT_DETAILS is false, a matrix of Timer otherwise
     */
    private List<List<Timer>> runTests() {
        List<List<Timer>> runResults = new ArrayList<>();
        for (int i = 0; i < runs; i++) {
            List<Timer> testList = new ArrayList<>();
            for (TimedTestRunner runner : runners) {
                Timer result = runner.run();
                if (PRINT_DETAILS) {
                    testList.add(result);
                }
            }
            if (PRINT_DETAILS) {
                runResults.add(testList);
            }
        }
        return runResults;
    }

    public void run() {
        List<List<Timer>> runResults = runTests();

        writer.print("Benchmark: " + name + "\n");
        if (PRINT_DETAILS) {
            printDetails(runResults);
        }

        writer.println();
        writer.println("TOTALS");
        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("stat", "avg", "median", "tmin", "tmax");
        table.addRule();
        for (TimedTestRunner runner : runners) {
            TimerList timer = runner.getTimerList();
            table.addRow(runner.getName(), timer.average(), timer.median(), timer.min(), timer.max());
            table.addRule();
        }

        writer.println(table.render());

        writer.println("All times are in nanoseconds.");
        writer.flush();
    }
}
