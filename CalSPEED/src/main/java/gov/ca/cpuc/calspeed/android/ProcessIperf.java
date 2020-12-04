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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.util.Log;

final class ProcessIperf {
    private Boolean isPhase2;
    private Float phase1UploadResult;
    private Float phase1DownloadResult;
    Boolean isUDP;
    Boolean udpSuccess;
    private String udpMessage;
    String jitter;
    public String loss;
    private Integer state;
    Float uploadSpeed;
    Boolean uploadSuccess;
    String uploadMessage;
    private HashMap<Integer, Float> rollingUploadSpeed;
    private HashMap<Integer, Integer> rollingUploadCount;
    private HashMap<Integer, Boolean> rollingUploadDone;
    Boolean finishedUploadTest;
    Float downloadSpeed;
    Boolean downloadSuccess;
    private String downloadMessage;
    public Float down;
    Boolean downFinalStatusFailed;
    private HashMap<Integer, Float> rollingDownloadSpeed;
    private HashMap<Integer, Integer> rollingDownloadCount;
    private HashMap<Integer, Boolean> rollingDownloadDone;
    private HashMap<Integer, Integer> iperfThreadState;
    private AndroidUiServices uiServices;
    private static HashMap<Integer, ThreadData> threadData;
    private static int connectingCounter = 0;
    private static int testNum = 0;
    private static final ArrayList<Float> westUp = new ArrayList<>();
    private static final ArrayList<Float> westDown = new ArrayList<>();
    private static final ArrayList<Float> eastUp = new ArrayList<>();
    private static final ArrayList<Float> eastDown = new ArrayList<>();
    private static float westUpSpeed = 0.0f;
    private static float westDownSpeed = 0.0f;
    private static float eastUpSpeed = 0.0f;
    private static float eastDownSpeed = 0.0f;
    private static boolean sumLine = false;
    private String message = "";
    private String errorMessage = "";
    private boolean success;
    private int threadNumber;
    private int testLength;
    private final String logger;
    private Float finalDownloadSpeed;
    private Float finalUploadSpeed;
    private Float rollingUpAvg;
    private Float rollingDownAvg;


    ProcessIperf(AndroidUiServices uiServices) {
        this.logger = getClass().getSimpleName();
        this.success = false;
        this.uiServices = uiServices;
        this.threadNumber = Constants.DEFAULT_THREAD_NUMBER;
        this.errorMessage = "";
    }

    ProcessIperf(AndroidUiServices uiServices, UdpTestConfig testConfig) {
        this(uiServices);
        isUDP = true;
        this.threadNumber = Constants.DEFAULT_THREAD_NUMBER;
        this.testLength = testConfig.getTestTime();
        this.jitter = "-1";
        this.loss = "-1";
        this.udpSuccess = false;
        this.isPhase2 = false;
        state = 0;
    }

    ProcessIperf(AndroidUiServices uiServices, TcpTestConfig testConfig) {
        this(uiServices);
        isUDP = false;
        finishedUploadTest = false;
        uploadSuccess = true;
        downloadSuccess = true;
        this.threadNumber = testConfig.getThreadNumber();
        this.testLength = testConfig.getTestTime();
        this.uiServices = uiServices;
        uploadSpeed = 0.0f;
        downloadSpeed = 0.0f;
        rollingUpAvg = 0.0f;
        rollingDownAvg = 0.0f;
        finalUploadSpeed = 0.0f;
        finalDownloadSpeed = 0.0f;
        isPhase2 = false;
        phase1UploadResult = 0.0f;
        phase1DownloadResult = 0.0f;
        uploadMessage = "Upload Speed";
        downloadMessage = "Download Speed";
        this.uploadSpeed = 0.0f;
        this.downloadSpeed = 0.0f;
        this.success = false;
        finishedUploadTest = false;
        this.uploadSpeed = 0.0f;
        this.downloadSpeed = 0.0f;
        phase1UploadResult = 0.0f;
        phase1DownloadResult = 0.0f;
        uploadMessage = "Upload Speed";
        downloadMessage = "Download Speed";

        // Initalize all the rolling values;
        rollingUploadSpeed = new HashMap<>(this.threadNumber);
        rollingUploadCount = new HashMap<>(this.threadNumber);
        rollingUploadDone = new HashMap<>(this.threadNumber);
        rollingDownloadSpeed = new HashMap<>(this.threadNumber);
        rollingDownloadCount = new HashMap<>(this.threadNumber);
        rollingDownloadDone = new HashMap<>(this.threadNumber);
        iperfThreadState = new HashMap<>(this.threadNumber);
        threadData = new HashMap<>(this.threadNumber);
        threadData.clear();
        for (Integer key : iperfThreadState.keySet()) {
            iperfThreadState.put(key, 0);
        }
        for (Integer key : rollingUploadSpeed.keySet()) {
            rollingUploadSpeed.put(key, 0.0f);
        }
        for (Integer key : rollingUploadCount.keySet()) {
            rollingUploadCount.put(key, 0);
        }
        for (Integer key : rollingUploadDone.keySet()) {
            rollingUploadDone.put(key, false);
        }
        for (Integer key : rollingDownloadSpeed.keySet()) {
            rollingDownloadSpeed.put(key, 0.0f);
        }
        for (Integer key : rollingDownloadCount.keySet()) {
            rollingDownloadCount.put(key, 0);
        }
        for (Integer key : rollingDownloadDone.keySet()) {
            rollingDownloadDone.put(key, false);
        }
    }

    void setUDPPhase2() {
        this.isPhase2 = true;
    }

    void setTCPPhase2(Float uploadResult, Float downloadResult) {
        this.isPhase2 = true;
        Log.d("SetPHASE2", "upload: " + uploadResult + " : download: " + downloadResult);
        phase1UploadResult = uploadResult;
        phase1DownloadResult = downloadResult;
        threadData.clear();
    }

    void setMessage(String message) {
        this.message += "\n" + message;
    }

    String getMessage() {
        return this.message;
    }

    boolean getSuccess() {
        return success;
    }

    String getErrorMessage() {
        return this.errorMessage;
    }

    Float getHistoryDownloadSpeed() {
        return (this.finalDownloadSpeed + this.phase1DownloadResult) / 2;
    }

    Float getHistoryUploadSpeed() {
        return (this.finalUploadSpeed + this.phase1UploadResult) / 2;
    }

    Float getDownloadSpeed() {
        return this.finalDownloadSpeed;
    }

    static String formatFloatString(String value) {
        try {
            Float flAverage = Float.valueOf(value);
            NumberFormat numberFormat = new DecimalFormat("#.00");
            return (numberFormat.format(flAverage));
        } catch (Exception e) {
            Log.d("formatFloatString", "format error: " + value);
            return value;
        }}

    private Boolean allUploadThreadsDone() {
        Boolean allDone = true;
        for (Integer i : rollingUploadDone.keySet()) {
            Boolean upDone = rollingUploadDone.get(i);
            if (upDone != null)
                if (!upDone) {
                    allDone = false;
                    break;
                }
        }
        return (allDone);
    }

    private Boolean allDownloadThreadsDone() {
        Boolean allDone = true;
        for (int i: rollingDownloadDone.keySet()) {
            Boolean downDone = rollingDownloadDone.get(i);
            if (downDone != null)
                if (!downDone) {
                    allDone = false;
                    break;
                }
        }
        return (allDone);
    }

    private void setIperfUploadSpeedOnly() {
        Log.i("setIperfUploadSpeedOnly", "<--- Calling this method");
        if (allUploadThreadsDone()) {
            finishedUploadTest = true;
            if (isPhase2) {
                setFinalUploadSpeed();
            } else {
                Log.d("WestUpTCPAverage", "west up size: " + westUp.size());
                if (westUp.size() > 0) {
                    if (westUp.size() == this.threadNumber) for (Float f : westUp) westUpSpeed += f;
                    else westUpSpeed = ThreadData.getTotalWestUP();
                    Log.d("WestUpTCPAverage", String.format("Values: %s",
                            Collections.singletonList(westUp).toString()));
                    Log.d("WestUpTCPAverage", "west up speed: " + westUpSpeed);
                    uploadSpeed = westUpSpeed;
                } else {
                    Log.d("WestUpTCPAverage", "using final download speed:  " + finalUploadSpeed);
                    uploadSpeed = finalUploadSpeed;
                }
                uiServices.setResults(Constants.THREAD_WRITE_UPLOAD_DATA,
                        uploadMessage, formatFloatString(uploadSpeed.toString()),
                        !uploadSuccess, !uploadSuccess);
                Log.v("debug", "in SetIPerfUploadSpeedOnly uploadspeed=" + uploadSpeed.toString());
                uiServices.setUploadNumberStopTimer(formatFloatString(uploadSpeed.toString()));
            }
            uiServices.startDownloadTimer();
        } else {
            displayUploadRollingAverage();
        }
    }

    final void setUDPIperfFinalStatus() {
        Log.i("setUDPIperfFinalStatus", "<--- Calling this method");
        // set success status only--failed test already set

        if (this.jitter.contains("-1")) {
            this.udpSuccess = false;
            udpMessage = "Jitter Incomplete";
        }
        if (udpSuccess) {
            udpMessage = "Jitter";
        }
        if (!isPhase2) {
            uiServices.setResults(Constants.THREAD_WRITE_JITTER_DATA, udpMessage, jitter,
                    !udpSuccess, !udpSuccess);
        }
    }

    final void setPhase1TCPIperfStatus() {
        Boolean printDownloadSpeed = true;
        Boolean printUploadSpeed = true;
        Log.d("SetPhase1TCP", "Start upload speed is: " + uploadSpeed);
        Log.d("SetPhase1TCP", "Start download speed is: " + downloadSpeed);
        finishedUploadTest = true;
        if (uploadSuccess) {
            if (!downloadSuccess) {     // either error or timeout
                for (Integer i : rollingDownloadCount.keySet()) {
                    Float downSpeed = rollingDownloadSpeed.get(i);
                    Integer downCount = rollingDownloadCount.get(i);
                    Boolean downDone = rollingDownloadDone.get(i);
                    if ((downCount != null) && (downSpeed != null) && (downDone != null)) {
                        if ((downCount > 0) && !downDone) {
                            downloadSpeed += downSpeed / downCount;
                            Log.v("SetPhase1TCP", "DEBUGGING Rolling speed is: " +
                                    rollingDownloadSpeed.get(i) + "::\tDownload count is: " +
                                    rollingDownloadCount.get(i));
                        }
                    }
                }
                if (downloadSpeed == 0.0f) {
                    printDownloadSpeed = false;
                    downloadMessage = "Download Incomplete";
                } else {
                    printDownloadSpeed = true;
                    downloadMessage = "Download Speed";
                }
            } else {
                if (finalUploadSpeed != 0.0) {
                    uploadSpeed = finalUploadSpeed;
                } else {
                    Float tempUpload = 0.f;
                    for (int i : rollingUploadSpeed.keySet()) {
                        Float upSpeed = rollingUploadSpeed.get(i);
                        Integer upCount = rollingUploadCount.get(i);
                        if ((upCount != null) && (upSpeed != null)) {
                            Log.d("ROLLING UP", upSpeed.toString());
                            Log.d("ROLLING UP",
                                    "Key: " + i + "Rolling count is: " + upCount.toString());
                            tempUpload += upSpeed / upCount;
                        }
                    }
                    Log.d("SetPhase1TCP", "DEBUG upload speed is: " + tempUpload);
                    uploadSpeed = tempUpload;
                }
                if (finalDownloadSpeed != 0.0) {
                    downloadSpeed = finalDownloadSpeed;
                } else {
                    Float tempDownload = 0.0f;
                    for (int i : rollingDownloadSpeed.keySet()) {
                        Float downSpeed = rollingDownloadSpeed.get(i);
                        Integer downCount = rollingDownloadCount.get(i);
                        if ((downCount != null) && (downSpeed != null)) {
                            Log.d("ROLLING DOWN", downSpeed.toString());
                            Log.d("ROLLING DOWN", "Key: " + i + "; Rolling count is: "
                                    + downCount.toString());
                            tempDownload += downSpeed / downCount;
                        }
                    }
                    Log.d("SetPhase1TCP", "DEBUG download speed is: " + tempDownload);
                    downloadSpeed = tempDownload;
                }

            }
        } else {
            downloadSuccess = false;
            printDownloadSpeed = false;
            downloadMessage = "Download Incomplete";
            for (Integer i : rollingUploadCount.keySet()) {
                Float upSpeed = rollingUploadSpeed.get(i);
                Integer upCount = rollingUploadCount.get(i);
                Boolean upDone = rollingUploadDone.get(i);
                if ((upDone != null) && (upCount != null) && (upSpeed != null)) {
                    if ((upCount > 0) && !upDone) {
                        uploadSpeed += upSpeed / upCount;
                        Log.v("SetPhase1TCP", "Rolling speed is: " + rollingUploadSpeed.get(i) +
                                "::\tUpload count is: " + rollingUploadCount.get(i));
                        Log.d("SetPhase1TCP", "Rolling upload speed is: " + uploadSpeed);
                    }
                }
            }
            if (uploadSpeed == 0.0f) {
                printUploadSpeed = false;
                uploadMessage = "Upload Incomplete";
            } else {
                printUploadSpeed = true;
                uploadMessage = "Upload Speed";
            }
            westUpSpeed = uploadSpeed;
        }
        westDownSpeed = downloadSpeed;
        westUpSpeed = uploadSpeed;
        down = downloadSpeed;
        uiServices.setResults(Constants.THREAD_WRITE_UPLOAD_DATA,
                uploadMessage, formatFloatString(uploadSpeed.toString()),
                !printUploadSpeed, !printUploadSpeed);
        if (!uploadSuccess) {
            Log.v("debug", "in TCP end of phase 1 setUploadNumberStopTimer uploadspeed="
                    + uploadSpeed.toString());
            uiServices.setUploadNumberStopTimer(formatFloatString(uploadSpeed.toString()));
        }
        Log.d("SetPhase1TCP", "PHASE 1 upload speed is: " + uploadSpeed);
        Log.d("SetPhase1TCP", "PHASE 1 download speed is: " + downloadSpeed);
        uiServices.setResults(Constants.THREAD_WRITE_DOWNLOAD_DATA, downloadMessage,
                formatFloatString(downloadSpeed.toString()), !printDownloadSpeed,
                !printDownloadSpeed);
        uiServices.setDownloadNumberStopTimer(formatFloatString(downloadSpeed.toString()));
    }

    private void setFinalUploadSpeed() {
        Float up;
        uploadMessage = "Upload Speed";
        if (!uploadSuccess) {
            Log.d("SetFinalUpload", "Upload not success");
            uploadSuccess = true;
            downloadSuccess = false;
            downloadMessage = "Download Incomplete";
            for (Integer i : rollingUploadCount.keySet()) {
                Integer upCount = rollingUploadCount.get(i);
                Boolean upDone = rollingUploadDone.get(i);
                Float upSpeed = rollingUploadSpeed.get(i);
                if ((upCount != null) && (upDone != null) && (upSpeed != null)) {
                    if ((upCount > 0) && !upDone)
                        uploadSpeed += upSpeed / upCount;
                }
            }
            up = phase1UploadResult + uploadSpeed;
            if ((phase1UploadResult != 0.0f) && (uploadSpeed != 0.0f)) {
                up = up / 2;
                uploadSpeed = finalUploadSpeed;
            }
            if ((phase1UploadResult == 0.0f) && (uploadSpeed == 0.0f)) {
                uploadSuccess = false;
                uploadMessage = "Upload Incomplete";
            }
        } else {
            Log.d("SetFinalUpload", "Upload success");
            if ((phase1UploadResult != 0.0f) && (finalUploadSpeed > 0.0f)) {
                up = (phase1UploadResult + finalUploadSpeed) / 2;
                uploadSpeed = finalUploadSpeed;
                Log.d("SetFinalUpload",
                        "Phase 1 and 2 upload values are non-zero. Using as final " + up);
            } else {
                Float tempUpload = 0.0f;
                for (int i : rollingUploadSpeed.keySet()) {
                    Float upSpeed = rollingUploadSpeed.get(i);
                    Integer upCount = rollingUploadCount.get(i);
                    if ((upSpeed != null) && (upCount != null)) {
                        Log.d("ROLLING UP", upSpeed.toString());
                        tempUpload += upSpeed / upCount;
                        Log.d("ROLLING UP", "Key: " + i + "; Rolling count is: " +
                                upCount.toString());
                    }
                }
                Log.d("SetFinalUpload",
                        "Using rolling speed upload speed as final : " + tempUpload);
                up = tempUpload;
            }
        }
        eastUpSpeed = uploadSpeed;
        Log.d("WestUpTCPAverage", String.format("Values: %s", Collections.singletonList(westUp).toString()));
        Log.d("EastUpTCPAverage", String.format("Values: %s", Collections.singletonList(eastUp).toString()));
        Log.d("FinalTCPAverage", "West Up: " + westUpSpeed);
        Log.d("FinalTCPAverage", "East Up: " + eastUpSpeed);
        Log.d("FinalTCPAverage", "Final value to history is: " + up.toString());
        uiServices.setResults(Constants.THREAD_WRITE_UPLOAD_DATA,
                uploadMessage, formatFloatString(up.toString()), !uploadSuccess, !uploadSuccess);
        uiServices.setUploadNumberStopTimer(formatFloatString(up.toString()));
    }

    void setIperfTCPAvgFinal() {
        setFinalUploadSpeed();
        down = 0.0f;
        String downFinalMessage = "Download Speed";
        downFinalStatusFailed = false;
        if (!downloadSuccess) {
            for (int i : rollingDownloadCount.keySet()) {
                Float downSpeed = rollingDownloadSpeed.get(i);
                Integer downCount = rollingDownloadCount.get(i);
                Boolean downDone = rollingDownloadDone.get(i);
                if ((downCount != null) && (downSpeed != null) && (downDone != null))
                    if ((downCount > 0) && !downDone)
                        downloadSpeed += downSpeed / downCount;
            }
            down = phase1DownloadResult + downloadSpeed;
            if ((phase1DownloadResult != 0.0f) && (downloadSpeed != 0.0f)) {
                downloadSpeed = finalDownloadSpeed;
                down = down / 2; // take the average if any both are non-zero
            }
            if ((phase1DownloadResult == 0.0f) && (downloadSpeed == 0.0f)) {
                downFinalStatusFailed = true;
                downFinalMessage = "Download Incomplete";
            }
        } else {
            if ((phase1DownloadResult != 0.0f) && (finalDownloadSpeed > 0.0f)) {
                down = (phase1DownloadResult + finalDownloadSpeed) / 2;
            } else {
                Float tempDownload = 0.0f;
                Log.d("SetFinalDownload", "Start download speed is: " + downloadSpeed);
                for (int i : rollingDownloadSpeed.keySet()) {
                    Float downSpeed = rollingDownloadSpeed.get(i);
                    Integer downCount = rollingDownloadCount.get(i);
                    if ((downCount != null) && (downSpeed != null)) {
                        Log.d("ROLLING DOWN", "Key: " + i + "; Rolling speed is : "
                                + downSpeed.toString() + "Rolling count is: "
                                + downCount.toString());
                        tempDownload += downSpeed / downCount;
                    }
                }
                Log.d("SetFinalDownload", "DEBUG FINAL download speed is: " + tempDownload);
                downloadSpeed = tempDownload;
                down = downloadSpeed;
            }
        }
        Log.v("debug", "down=" + down.toString() + " downloadsuccess="
                + downloadSuccess.toString() + " downloadspeed=" + downloadSpeed.toString());
        eastDownSpeed = downloadSpeed;
        Log.d("WestDownTCPAverage", String.format("Values: %s", Collections.singletonList(westDown).toString()));
        Log.d("EastDownTCPAverage", String.format("Values: %s", Collections.singletonList(eastDown).toString()));
        Log.d("FinalTCPAverage", "West Down: " + westDownSpeed);
        Log.d("FinalTCPAverage", "East Down: " + eastDownSpeed);
        downloadSpeed = (westDownSpeed + eastDownSpeed) / 2;
        Log.d("FinalTCPAverage", "Average of westDownSpeed and eastDownSpeed is: " + downloadSpeed);
        Log.d("FinalTCPAverage", "Final value to history is: " + down);
        uiServices.setResults(Constants.THREAD_WRITE_DOWNLOAD_DATA, downFinalMessage,
                ProcessIperf.formatFloatString(down.toString()), downFinalStatusFailed,
                downFinalStatusFailed);
        uiServices.setDownloadNumberStopTimer(formatFloatString(down.toString()));
        reset();
    }

    private void reset() {
        westUp.clear();
        westUpSpeed = 0.0f;
        westDown.clear();
        westDownSpeed = 0.0f;
        eastUp.clear();
        eastUpSpeed = 0.0f;
        eastDown.clear();
        eastDownSpeed = 0.0f;
    }

    private void parseUDPLine(String line) {
        int indexStart;
        int indexEnd;
        switch (state) {
            case 0:
                indexStart = line.indexOf("Server Report");
                if (indexStart != -1) {
                    state = 1;
                }
                break;
            case 1:
                indexStart = line.indexOf("/sec");
                if (indexStart != -1) {
                    indexEnd = line.indexOf("ms", indexStart);
                    if (indexEnd != -1) {
                        jitter = line.substring(indexStart + 5, indexEnd - 1);
                        indexStart = line.indexOf("(");
                        indexEnd = line.indexOf(")", indexStart);
                        loss = line.substring(indexStart + 1, indexEnd - 1);
                        udpSuccess = true;
                        Log.d("parseUDPLine", line);
                        Log.d("parseUDPLine", "udp loss " + loss);
                        MOSCalculation.addUDPLoss(Double.parseDouble(loss));
                        state = 2;
                    }
                }
                break;
            default:
                break;
        }
    }

    private Integer getThreadNum(String line) {
        int indexStart;
        int indexEnd;
        int threadNum = -1;
        String threadNumStr;
        try {
            indexStart = line.indexOf("[");
            if ((indexStart != -1) && (!line.contains("[SUM]"))) {
                indexEnd = line.indexOf("]");
                if (indexEnd != -1) {
                    threadNumStr = line.substring(indexStart + 2, indexEnd);
                    threadNumStr = threadNumStr.replaceAll("^\\s+", "");
                    Log.v(this.logger, "threadstring=" + threadNumStr);
                    threadNum = Integer.parseInt(threadNumStr);
                }
            }
        } catch (Exception e) {
            Log.e(this.logger, "Failed to get thread number from line " + e.getMessage());
            return (-1);
        }
        return (threadNum);
    }

    private Float getTCPBitsPerSec(String line) {
        int indexStart;
        int indexEnd;
        float currentSpeed = 0.0f;

        indexStart = line.indexOf("KBytes");
        if (indexStart != -1) {
            indexEnd = line.indexOf("Kbits/sec");
            String upSpeed = line.substring(indexStart + 7, indexEnd);
            try {
                currentSpeed = Float.parseFloat(upSpeed);
                if (currentSpeed > Constants.IPERF_BIG_NUMBER_ERROR) {
                    currentSpeed = 0.0f;
                }
                return (currentSpeed);
            } catch (Exception e) {
                Log.e(this.logger, "Failed to convert to Float: " + e.getMessage());
                return (0.0f);
            }
        } else {
            return (currentSpeed);
        }
    }

    private void displayUploadRollingAverage() {
        Log.v("displayUpAvg", "START LOOP: display rolling up average");
        Float rollingTemp = 0.0f;
        for (int i : rollingUploadDone.keySet()) {
            Boolean upDone = rollingUploadDone.get(i);
            Float upSpeed = rollingUploadSpeed.get(i);
            Integer upCount = rollingUploadCount.get(i);
            try {
                if ((upDone != null) && (upCount != null) && (upSpeed != null)) {
                    if (upCount != 0) {
                        int divider = upCount;
                        int threshold = (int) (Constants.RAMP_UP_FACTOR * this.testLength);
                        if (upCount < threshold) {
                            divider = threshold;
                        }
                        Float logUploadSpeed = upSpeed / divider;
                        Log.v("displayUpAvg", "step 1: get rolling average=" + rollingTemp);
                        Log.v("displayUpAvg", "step 2: i=" + i + "; up speed=" + upSpeed +
                                "; divider=" + divider + "; count=" + upCount);
                        Log.v("displayUpAvg", "step 3: calc avg speed=" + logUploadSpeed +
                                " and add to rolling average=" + rollingTemp);
                        rollingTemp += logUploadSpeed;
                        Log.v("displayUpAvg", "step 4: Upload rolling average: " + rollingTemp);
                    }
                }
            } catch (Exception e) {
                Log.e("displayUploadRollingAvg", "In displayUploadRollingAverage i = " + i +
                        "\nError message: " + e.getMessage());
            }
        }
        Log.v("displayUpAvg", "END LOOP: Upload rolling average: " + rollingUpAvg +
                " and this rolling average: " + rollingTemp + "; phase 1 result: " +
                phase1UploadResult);
        rollingUpAvg = (rollingTemp + rollingUpAvg) / 2;
        if (isPhase2) {
            rollingUpAvg = (rollingUpAvg + phase1UploadResult) / 2;
        }
        if (rollingUpAvg != 0.0f) {
            uiServices.setResults(Constants.THREAD_WRITE_UPLOAD_DATA, uploadMessage,
                    ProcessIperf.formatFloatString(rollingUpAvg.toString()), false, false);
        }
    }

    private void displayDownloadRollingAverage() {
        Log.v("displayDownAvg", "START LOOP: display rolling up average");
        Float rollingTemp = 0.0f;
        for (int i : rollingDownloadDone.keySet()) {
            try {
                Float downSpeed = rollingDownloadSpeed.get(i);
                Integer downCount = rollingDownloadCount.get(i);
                Boolean downDone = rollingDownloadDone.get(i);
                if ((downCount != null) && (downSpeed != null) && (downDone != null)) {
                    if (downCount != 0) {
                        int divider = downCount;
                        int threshold = (int) (Constants.RAMP_UP_FACTOR * this.testLength);
                        if (downCount < threshold) {
                            divider = threshold;
                        }
                        Float logDownloadSpeed = downSpeed / divider;
                        Log.v("displayDownAvg", "step 1: get rolling average=" + rollingTemp);
                        Log.v("displayDownAvg", "step 2: i=" + i + "; down speed=" + downSpeed +
                                "; divider=" + divider + "; count=" + downCount);
                        Log.v("displayDownAvg", "step 3: calc avg speed=" + logDownloadSpeed +
                                " and add to rolling average=" + rollingTemp);
                        rollingTemp += logDownloadSpeed;
                        Log.v("displayDownAvg",
                                "step 4: Download rolling average: " + rollingTemp);
                    }
                }
            } catch (Exception e) {
                Log.e("displayDownAvg", "In displayDownloadRollingAverage i = " + i +
                        "\nError message: " + e.getMessage());
            }
        }
        Log.v("displayDownAvg", "Download rolling average: " + rollingDownAvg +
                " and this rolling average: " + rollingTemp +
                "; phase 1 result: " + phase1DownloadResult);
        rollingDownAvg = (rollingTemp + rollingDownAvg) / 2;
        if (isPhase2) {
            rollingDownAvg = (rollingDownAvg + phase1DownloadResult) / 2;
        }
        uiServices.setResults(Constants.THREAD_WRITE_DOWNLOAD_DATA, downloadMessage,
                ProcessIperf.formatFloatString(rollingDownAvg.toString()), false, false);
    }

    private Integer getInterval(String line) {
        int indexEnd;
        indexEnd = line.indexOf("sec");
        indexEnd = indexEnd - 3;
        String interval = line.substring(indexEnd - 2, indexEnd);
        interval = interval.trim();
        try {
            return Integer.parseInt(interval);
        } catch (Exception e) {
            Log.e(this.logger, "NaN, Failed to convert to Integer: " + e.getMessage());
            return -1;
        }
    }

    private boolean goodLine(String line) {
        sumLine = false;
        int indexEnd;
        Integer end;
        Integer start;
        indexEnd = line.indexOf("sec");
        indexEnd = indexEnd - 3;
        String endInterval = line.substring(indexEnd - 2, indexEnd);
        endInterval = endInterval.trim();
        indexEnd = line.indexOf("sec");
        indexEnd = indexEnd - 8;
        String startInterval = line.substring(indexEnd - 2, indexEnd);
        startInterval = startInterval.trim();
        try {
            start = Integer.parseInt(startInterval);
            end = Integer.parseInt(endInterval);
            if (start.equals(0) && !(end.equals(1))) {
                sumLine = true;
                return false;
            }
        } catch (Exception e) {
            Log.e(this.logger, "NaN: " + e.getMessage());
            return false;
        }
        return true;
    }

    private void parseTCPLine(String line, Integer threadNum) {
        Log.v("parseTCPLine", "Thread Num [" + threadNum + "]\t::\t" + line);
        if (line.contains("local")) {
            if (threadData.get(threadNum) == null) {
                Log.d("parseTCPLine", "Creating new threadData for thread:" + threadNum);
                threadData.put(threadNum, new ThreadData());
                if (threadData.size() < this.threadNumber + 1) {
                    Log.d("parseTCPLine", "list of dataThread is " +
                            Collections.singletonList(threadData).toString() +
                            ", initializing upload.");
                    iperfThreadState.put(threadNum, 0);
                    rollingUploadSpeed.put(threadNum, 0.0f);
                    rollingUploadCount.put(threadNum, 0);
                    rollingUploadDone.put(threadNum, false);
                } else {
                    Log.d("parseTCPLine", "list of dataThread is " +
                            Collections.singletonList(threadData).toString() +
                            ", initializing download.");
                    iperfThreadState.put(threadNum, 2);
                    rollingDownloadSpeed.put(threadNum, 0.0f);
                    rollingDownloadCount.put(threadNum, 0);
                    rollingDownloadDone.put(threadNum, false);
                }
            } else {
                Log.v("parseTCPLine", "Thread " + threadNum + " exists in threadData. This " +
                        "should be doing the download case.\n The state value is " +
                        iperfThreadState.get(threadNum));
                rollingDownloadSpeed.put(threadNum, 0.0f);
                rollingDownloadCount.put(threadNum, 0);
                rollingDownloadDone.put(threadNum, false);
            }
            for (int i : threadData.keySet()) {
                ThreadData threadDatum = threadData.get(i);
                if (threadDatum != null) {
                    if (threadDatum.getThreadNum() == null) {
                        threadDatum.setThreadNum(threadNum);
                    } else if (threadDatum.getThreadNum().equals(threadNum))
                        threadDatum.toggleDirectionUp();
                } else {
                    Log.d("parseTCPLine", "thread data at " + i + " is null");
                }
            }
            if (++connectingCounter == this.threadNumber) {
                connectingCounter = 0;
                switch (testNum) {
                    case 0:
                        ThreadData.whichTest = ThreadData.PROBE_UPLOAD;
                        break;
                    case 1:
                        ThreadData.whichTest = ThreadData.PROBE_DOWNLOAD;
                        break;
                    case 2:
                        ThreadData.whichTest = ThreadData.WEST_UPLOAD;
                        break;
                    case 3:
                        ThreadData.whichTest = ThreadData.WEST_DOWNLOAD;
                        break;
                    case 4:
                        ThreadData.whichTest = ThreadData.EAST_UPLOAD;
                        break;
                    case 5:
                        ThreadData.whichTest = ThreadData.EAST_DOWNLOAD;
                        testNum = -1;
                        break;
                }
                ++testNum;
                Log.d("WhichTest", "Advancing testNum to " + testNum + "; whichTest is " +
                        ThreadData.whichTest);
            }
        }
        if (line.contains("Kbits/sec")) {
            if (goodLine(line)) {
                Float value = getTCPBitsPerSec(line);
                Integer interval = getInterval(line);
                for (int i : threadData.keySet()) {
                    ThreadData datum = threadData.get(i);
                    if (datum != null) {
                        if (datum.getThreadNum().equals(threadNum)) {
                            datum.addValue(interval, value);
                            if (this.isPhase2) {
                                interval++;
                                interval *= -1;
                            }
                            datum.addTotalVal(interval, value);
                            break;
                        }
                    }
                }
            } else if (sumLine) {
                Log.d("SUM LINE", "this sum line is for test: " + ThreadData.whichTest);
                if (ThreadData.whichTest == ThreadData.WEST_UPLOAD) {
                    Log.d("SUM LINE", "adding " + line + " to westUp");
                    westUp.add(getTCPBitsPerSec(line));
                }
                else if (ThreadData.whichTest == ThreadData.WEST_DOWNLOAD)
                    westDown.add(getTCPBitsPerSec(line));
                else if (ThreadData.whichTest == ThreadData.EAST_UPLOAD)
                    eastUp.add(getTCPBitsPerSec(line));
                else if (ThreadData.whichTest == ThreadData.EAST_DOWNLOAD)
                    eastDown.add(getTCPBitsPerSec(line));
            }
        }
        Log.v("parseTCPLine", "rollingUploadCount keys are: " +
                Collections.singletonList(rollingUploadCount.keySet()).toString() +
                "\nrollingUploadSpeed keys are: " +
                Collections.singletonList(rollingUploadSpeed.keySet()).toString() +
                "\nrollingUploadCount keys are: " +
                Collections.singletonList(rollingUploadCount.keySet()).toString() +
                "\nrollingUploadDone keys are " +
                Collections.singletonList(rollingUploadDone.keySet()).toString());
        Log.v("parseTCPLine", "rollingDownloadCount keys are: " +
                Collections.singletonList(rollingDownloadCount.keySet()).toString() +
                "\nrollingDownloadSpeed keys are: " +
                Collections.singletonList(rollingDownloadSpeed.keySet()).toString() +
                "\nrollingDownloadCount keys are: " +
                Collections.singletonList(rollingDownloadCount.keySet()).toString() +
                "\nrollingDownloadDone keys are " +
                Collections.singletonList(rollingDownloadDone.keySet()).toString());
        if (iperfThreadState.get(threadNum) == null) {
            if (iperfThreadState.size() < this.threadNumber + 1) {
                iperfThreadState.put(threadNum, 0);
            } else {
                iperfThreadState.put(threadNum, 2);
                rollingUploadDone.put(threadNum, true);
            }
        }
        try {
            if (line.contains(" 0.0- 0.0")) { // ignore error line from iperf
                return;
            }
            Log.v("parseTCPLine", "Case: " + iperfThreadState.get(threadNum) +
                    " | threadNum: " + threadNum);
            Integer threadState = iperfThreadState.get(threadNum);
            if (threadState == null) {
                Log.w("parseTCPLine", "Thread State for threadNum=" + threadNum + " is null");
                return;
            }
            switch (threadState) {
                case 0:
                    if (line.contains(" 0.0-")) {
                        Log.v("parseTCPLine", "Changing thread " + threadNum + " threadState to 1");
                        iperfThreadState.put(threadNum, 1);
                        Float upSpeed = rollingUploadSpeed.get(threadNum);
                        Log.d("parseTCPLine", "Up speed from rolling: " + upSpeed + " | thread num: " + threadNum);
                        if (upSpeed == null) {
                            upSpeed = 0.0f;
                        }
                        Log.d("parseTCPLine", "Up speed at case 0 is: " + upSpeed);
                        rollingUploadSpeed.put(threadNum, upSpeed + getTCPBitsPerSec(line));
                        Integer rollingCount = rollingUploadCount.get(threadNum);
                        if (rollingCount != null) {
                            rollingUploadCount.put(threadNum, rollingCount + 1);
                        }
                        displayUploadRollingAverage();
                        Log.d("parseTCPLine", "Upload speed at end of case 1 is: " + uploadSpeed);
                    }
                    break;
                case 1:
                    if (line.contains(" 0.0-")) {
                        finalUploadSpeed += getTCPBitsPerSec(line);
                        Log.d("parseTCPLine", "Upload speed at end of case 1 is: " + finalUploadSpeed);
                        Log.v("parseTCPLine", "Changing thread " + threadNum + " threadState to 2");
                        iperfThreadState.put(threadNum, 2);
                        rollingUploadDone.put(threadNum, true);
                        setIperfUploadSpeedOnly();
                    } else {
                        if (!finishedUploadTest) {
                            Float upSpeed = rollingUploadSpeed.get(threadNum);
                            if (upSpeed != null) {
                                rollingUploadSpeed.put(threadNum, upSpeed + getTCPBitsPerSec(line));
                            }
                            Integer rollingCount = rollingUploadCount.get(threadNum);
                            if (rollingCount != null) {
                                rollingUploadCount.put(threadNum, rollingCount + 1);
                            }
                            displayUploadRollingAverage();
                        }
                    }
                    break;
                case 2:
                    if (line.contains(" 0.0-")) {
                        // reset done flag for new download thread
                        rollingDownloadDone.put(threadNum, false);
                        Log.v("parseTCPLine", "Changing thread " + threadNum + " threadState to 3");
                        iperfThreadState.put(threadNum, 3);
                        Float downSpeed = rollingDownloadSpeed.get(threadNum);
                        if (downSpeed == null) {
                            downSpeed = 0.0f;
                        }
                        Log.d("parseTCPLine", "Download speed at case 2 is: " + downSpeed);
                        rollingDownloadSpeed.put(threadNum, downSpeed + getTCPBitsPerSec(line));
                        Integer rollingCount = rollingDownloadCount.get(threadNum);
                        if (rollingCount != null) {
                            rollingDownloadCount.put(threadNum, rollingCount + 1);
                        }
                        displayDownloadRollingAverage();
                    }
                    break;
                case 3:
                    if (line.contains(" 0.0-")) {
                        finalDownloadSpeed += getTCPBitsPerSec(line);
                        Log.d("parseTCPLine", "Download speed at end of case 3 is: " + finalDownloadSpeed);
                        // keep checking for additional download threads using this thread number
                        Log.v("parseTCPLine", "Changing thread " + threadNum + " threadState to 2");
                        iperfThreadState.put(threadNum, 2);
                        rollingDownloadDone.put(threadNum, true);
                    } else {
                        Float downSpeed = rollingDownloadSpeed.get(threadNum);
                        if (downSpeed != null) {
                            rollingDownloadSpeed.put(threadNum, downSpeed + getTCPBitsPerSec(line));
                        }
                        Integer rollingCount = rollingDownloadCount.get(threadNum);
                        if (rollingCount != null) {
                            rollingDownloadCount.put(threadNum, rollingCount + 1);
                        }
                        displayDownloadRollingAverage();
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e("parseTCPLine", "Exception on thread " + threadNum + "\nMessage: "
                    + e.getMessage());
        }
    }

    final void processOutput(String input) {
        Integer startIndex = 0;
        Integer endIndex;
        Integer threadNum;
        String line;
        while (startIndex < input.length()) {
            endIndex = input.indexOf("\n", startIndex);
            line = input.substring(startIndex, endIndex);
            if (endIndex != -1) {
                if (isUDP) {
                    parseUDPLine(line);
                } else {
                    threadNum = getThreadNum(line);
                    if (threadNum != -1) {
                        parseTCPLine(line, threadNum);
                    }
                }
                startIndex = endIndex + 1;
            } else {
                if (isUDP) {
                    parseUDPLine(line);
                } else {
                    threadNum = getThreadNum(line);
                    if (threadNum != -1) {
                        parseTCPLine(line, threadNum);
                    }
                }
                startIndex = input.length();
            }
        }
    }

    final void processErrorOutput(String input) {
        Integer startIndex = 0;
        Integer endIndex;
        String line;
        while (startIndex < input.length()) {
            endIndex = input.indexOf("\n", startIndex);
            if (endIndex == -1)
                endIndex = input.length();
            line = input.substring(startIndex, endIndex);

            if (isUDP) {
                if (line.matches(".*did not receive ack.*")
                        || line.matches(".*error:.*")) {
                    this.udpMessage = "Jitter Incomplete";
                }
            } else {
                if (line.matches(".*failed.*") || line.matches(".*error:.*")) {
                    downloadSuccess = false;
                    downloadMessage = "Download Incomplete";
                    if (!finishedUploadTest) {
                        uploadSuccess = false;
                        uploadMessage = "Upload Incomplete";
                    }
                }
            }
            startIndex = endIndex + 1;
        }
    }

    void finishPrelim() {
        boolean isCompleted = true;
        if (!allDownloadThreadsDone()) {
            this.success = false;
            Log.i(this.logger, "Download threads are incomplete");
            isCompleted = false;
        }
        if (finalDownloadSpeed == 0.0f) {
            Log.d(this.logger, "Have zero values for upload and download");
            Log.d(this.logger, String.format("final download speed: %f\nfinal upload speeds: %f",
                    downloadSpeed, uploadSpeed));
            Log.i(this.logger, "Upload speed or download speed is zero 0.0");
            isCompleted = false;
            this.success = false;
        } else {
            Log.d(this.logger, "Upload speed and download speed are non-zero");
            uiServices.stopUploadTimer();
            uiServices.stopDownloadTimer();
            Log.i(this.logger, "Success. Final download speeds are non-zero");
            this.success = true;
        }
        if (!isCompleted) {
            Log.w(this.logger, "Something wasn't finished properly");
        }
    }

}
