package com.atlassian.util.benchmark;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class TimerList {
    final String name;
    final List<Timer> timers = new ArrayList<>();

    TimerList(String name) {
        this.name = name;
    }

    Timer getTimer() {
        final Timer timer = new Timer();
        timers.add(timer);
        return timer.start();
    }

    String getAverage() {
        long count = 0L;
        for (Timer timer : timers) {
            count = count + timer.total;
        }
        return Util.format(count / timers.size());
    }

    String getMedian() {
        List<Timer> orderedTimers = new ArrayList<>(timers);
        orderedTimers.sort((o1, o2) -> new Long(o1.total - o2.total).intValue());
        return Util.format(orderedTimers.get((int) Math.ceil(timers.size() / 2) - 1).total);
    }

    String getPercentile(double percentile) {
        List<Timer> orderedTimers = new ArrayList<>(timers);
        orderedTimers.sort((o1, o2) -> new Long(o1.total - o2.total).intValue());
        return Util.format(orderedTimers.get((int) Math.ceil((percentile / (double) 100) * (double) timers.size()) - 1 ).total);
    }

    String getMax() {
        long max = 0;
        for (Timer timer : timers) {
            max = Math.max(max, timer.total);
        }
        return Util.format(max);
    }

    String getMin() {
        long min = Long.MAX_VALUE;
        for (Timer timer : timers) {
            min = Math.min(min, timer.total);
        }
        return Util.format(min);
    }

    String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + "\t" + getAverage() + "\t" + getMedian() + "\t" + getMin() + "\t" + getMax();
    }
}