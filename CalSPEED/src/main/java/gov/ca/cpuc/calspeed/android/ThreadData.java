/*
Copyright (c) 2020, California State University Monterey Bay (CSUMB).
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above
       copyright notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. Neither the name of the CPUC, CSU Monterey Bay, nor the names of
       its contributors may be used to endorse or promote products derived from
       this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
