package com.atlassian.util.benchmark;

import java.util.ArrayList;
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

    String average() {
        long count = 0L;
        for (Timer timer : timers) {
            count = count + timer.total;
        }
        return Util.format(count / timers.size());
    }

    String median() {
        List<Timer> orderedTimers = new ArrayList<>(timers);
        orderedTimers.sort((o1, o2) -> new Long(o2.total - o1.total).intValue());
        return Util.format(orderedTimers.get(timers.size() / 2).total);
    }

    String max() {
        long max = 0;
        for (Timer timer : timers) {
            max = Math.max(max, timer.total);
        }
        return Util.format(max);
    }

    String min() {
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
        return name + "\t" + average() + "\t" + median() + "\t" + min() + "\t" + max();
    }
}