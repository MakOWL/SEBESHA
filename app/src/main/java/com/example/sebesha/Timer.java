package com.example.sebesha;

public class Timer {
    private long startTime;
    private long stopTime;
    private boolean is_running;

    public Timer(){
        is_running = false;
    }

    public void start(){
        startTime = System.currentTimeMillis();
        is_running = true;
    }
    public void stop(){
        stopTime = System.currentTimeMillis();
        is_running = false;
    }
    public long getStartTime() {
        return startTime;
    }
    public long getStopTime(){
        return stopTime;
    }
}
