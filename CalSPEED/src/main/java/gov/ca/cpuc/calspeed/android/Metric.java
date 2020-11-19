package gov.ca.cpuc.calspeed.android;

import java.util.Comparator;

/**
 * Created by joshuaahn on 12/12/16.
 */

public class Metric {
    private final int threadID;
    private final Double speed;
    private final String timeRange;
    private final String direction;
    private final String server;

    public Metric(int threadID, Double speed, String timeRange, String direction, String server) {
        this.threadID = threadID;
        this.speed = speed;
        this.timeRange = timeRange;
        this.direction = direction;
        this.server = server;
    }

    public int getThreadID(){
        return threadID;
    }

    public Double getSpeed() {
        return speed;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public String getDirection() {
        return direction;
    }

    public String getServer() {
        return server;
    }

    public String toString() {
        String printString = "[ ";
        printString += "Thread ID: ";
        printString += threadID;
        printString += "| Server: ";
        printString += server;
        printString += " | Direction: ";
        printString += direction;
        printString += " | Time Range: ";
        printString += timeRange;
        printString += " | Speed: ";
        printString += speed;
        printString += " ]";
        return printString;
    }
}


