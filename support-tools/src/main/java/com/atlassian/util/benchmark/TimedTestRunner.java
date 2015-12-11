package com.atlassian.util.benchmark;

import java.util.concurrent.Callable;

public class TimedTestRunner {
    private final Callable<?> callable;
    private final TimerList timerList;

    public TimedTestRunner(String name, Callable<?> callable) {
        this.timerList = new TimerList(name);
        this.callable = callable;
    }

    String getName() {
        return timerList.getName();
    }

    Timer run() {
        Timer timer = timerList.getTimer();
        try {
            callable.call();
        } catch (Exception e) {
            e.printStackTrace();
            timer.setThrowable(e);
        } finally {
            timer.stop();
        }
        return timer;
    }

    @Override
    public String toString() {
        return timerList.toString();
    }

    public TimerList getTimerList() {
        return timerList;
    }
}