package gov.ca.cpuc.calspeed.android;

/**
 * Created by Kyle on 6/16/2017.
 */

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ThreadData {
    private static final HashMap<Integer, ArrayList<Float>> totalUploadVals = new HashMap<>();
    private static final HashMap<Integer, ArrayList<Float>> totalDownloadVals = new HashMap<>();
    private static final HashMap<Integer, ArrayList<Float>> westUp = new HashMap<>();
    private static final HashMap<Integer, ArrayList<Float>> westDown = new HashMap<>();
    private static final HashMap<Integer, ArrayList<Float>> eastUp = new HashMap<>();
    private static final HashMap<Integer, ArrayList<Float>> eastDown = new HashMap<>();
    private final HashMap<Integer, Float> uploadVals;
    private final HashMap<Integer, Float> downloadVals;
    private boolean directionUp;
    private Integer threadNum;

    static int PROBE_UPLOAD = 0;
    static int PROBE_DOWNLOAD = 1;
    static int WEST_UPLOAD = 2;
    static int WEST_DOWNLOAD = 3;
    static int EAST_UPLOAD = 4;
    static int EAST_DOWNLOAD = 5;
    static int whichTest = WEST_UPLOAD;

    ThreadData() {
        uploadVals = new HashMap<>();
        downloadVals = new HashMap<>();
        directionUp = true;
        threadNum = null;
    }

    public void setThreadNum(Integer threadNum) { this.threadNum = threadNum; }

    public void setDirectionUp(boolean directionUp) { this.directionUp = directionUp; }

    public boolean addValue(Integer interval, Float val) {
        if(directionUp) this.addUploadVal(interval, val);
        else this.addDownloadVal(interval, val);
        return directionUp;
    }

    private void addUploadVal(Integer interval, Float upVal) {
        uploadVals.put(interval, upVal);
        if(whichTest == WEST_UPLOAD) {
            if(westUp.containsKey(interval)) westUp.get(interval).add(upVal);
            else {
                ArrayList<Float> tempVal = new ArrayList<>();
                tempVal.add(upVal);
                westUp.put(interval, tempVal);
            }
        }
        else {
            if(eastUp.containsKey(interval)) eastUp.get(interval).add(upVal);
            else {
                ArrayList<Float> tempVal = new ArrayList<>();
                tempVal.add(upVal);
                eastUp.put(interval, tempVal);
            }
        }
    }

    private void addDownloadVal(Integer interval, Float downVal) {
        downloadVals.put(interval, downVal);
        if(whichTest == WEST_DOWNLOAD) {
            if(westDown.containsKey(interval)) westDown.get(interval).add(downVal);
            else {
                ArrayList<Float> tempVal = new ArrayList<>();
                tempVal.add(downVal);
                westDown.put(interval, tempVal);
            }
        }
        else {
            if(eastDown.containsKey(interval)) eastDown.get(interval).add(downVal);
            else {
                ArrayList<Float> tempVal = new ArrayList<>();
                tempVal.add(downVal);
                eastDown.put(interval, tempVal);
            }
        }
    }

    public void resetThread() {
        uploadVals.clear();
        downloadVals.clear();
        directionUp = true;
        threadNum = null;
    }

    public static void resetAllThreads() {
        totalUploadVals.clear();
        totalDownloadVals.clear();
        westUp.clear();
        westDown.clear();
        eastUp.clear();
        eastDown.clear();
    }

    public void toggleDirectionUp() { directionUp = !directionUp; }

    public Integer getThreadNum() { return threadNum; }

    public HashMap<Integer, Float> getUploadVals() { return new HashMap<>(uploadVals); }

    public HashMap<Integer, Float> getDownloadVals() { return new HashMap<>(downloadVals); }

    public void addTotalVal(Integer interval, Float val) {
        if(directionUp) addTotalUpVal(interval, val);
        else addTotalDownVal(interval, val);
    }

    private void addTotalUpVal(Integer interval, Float val) {
        if(totalUploadVals.containsKey(interval)) totalUploadVals.get(interval).add(val);
        else {
            ArrayList<Float> tempVal = new ArrayList<>();
            tempVal.add(val);
            totalUploadVals.put(interval, tempVal);
        }
    }

    private void addTotalDownVal(Integer interval, Float val) {
        if(totalDownloadVals.containsKey(interval)) totalDownloadVals.get(interval).add(val);
        else {
            ArrayList<Float> tempVal = new ArrayList<>();
            tempVal.add(val);
            totalDownloadVals.put(interval, tempVal);
        }
    }

    static float getTotalWestUP() {
        float sum = 0.0f;
        ArrayList<Float> avgs = new ArrayList<>();
        for (Integer key : westUp.keySet()) {
            Log.d("getTotalWestUp", String.format("Key: %d, Values: %s", key,
                    Arrays.asList(westUp.get(key)).toString()));
            for(int i = 0; i < westUp.get(key).size(); i++) {
                sum += westUp.get(key).get(i);
            }
            avgs.add(sum);
            sum = 0;
        }
        for(int i = 0; i < avgs.size(); i++) {
            sum += avgs.get(i);
        }
        return sum / avgs.size();
    }

}
