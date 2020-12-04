/*
Copyright 2003 University of Chicago.  All rights reserved.
The Web100 Network Diagnostic Tool (NDT) is distributed subject to
the following license conditions:
SOFTWARE LICENSE AGREEMENT
Software: Web100 Network Diagnostic Tool (NDT)

1. The "Software", below, refers to the Web100 Network Diagnostic Tool (NDT)
(in either source code, or binary form and accompanying documentation). Each
licensee is addressed as "you" or "Licensee."

2. The copyright holder shown above hereby grants Licensee a royalty-free
nonexclusive license, subject to the limitations stated herein and U.S. Government
license rights.

3. You may modify and make a copy or copies of the Software for use within your
organization, if you meet the following conditions:
a. Copies in source code must include the copyright notice and this Software
License Agreement.
b. Copies in binary form must include the copyright notice and this Software
License Agreement in the documentation and/or other materials provided with the copy.

4. You may make a copy, or modify a copy or copies of the Software or any
portion of it, thus forming a work based on the Software, and distribute copies
outside your organization, if you meet all of the following conditions:
a. Copies in source code must include the copyright notice and this
Software License Agreement;
b. Copies in binary form must include the copyright notice and this
Software License Agreement in the documentation and/or other materials
provided with the copy;
c. Modified copies and works based on the Software must carry prominent
notices stating that you changed specified portions of the Software.

5. Portions of the Software resulted from work developed under a U.S. Government
contract and are subject to the following license: the Government is granted
for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable
worldwide license in this computer software to reproduce, prepare derivative
works, and perform publicly and display publicly.

6. WARRANTY DISCLAIMER. THE SOFTWARE IS SUPPLIED "AS IS" WITHOUT WARRANTY
OF ANY KIND. THE COPYRIGHT HOLDER, THE UNITED STATES, THE UNITED STATES
DEPARTMENT OF ENERGY, AND THEIR EMPLOYEES: (1) DISCLAIM ANY WARRANTIES,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO ANY IMPLIED WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE OR NON-INFRINGEMENT,
(2) DO NOT ASSUME ANY LEGAL LIABILITY OR RESPONSIBILITY FOR THE ACCURACY,
COMPLETENESS, OR USEFULNESS OF THE SOFTWARE, (3) DO NOT REPRESENT THAT USE
OF THE SOFTWARE WOULD NOT INFRINGE PRIVATELY OWNED RIGHTS, (4) DO NOT WARRANT
THAT THE SOFTWARE WILL FUNCTION UNINTERRUPTED, THAT IT IS ERROR-FREE OR THAT
ANY ERRORS WILL BE CORRECTED.

7. LIMITATION OF LIABILITY. IN NO EVENT WILL THE COPYRIGHT HOLDER, THE
UNITED STATES, THE UNITED STATES DEPARTMENT OF ENERGY, OR THEIR EMPLOYEES:
BE LIABLE FOR ANY INDIRECT, INCIDENTAL, CONSEQUENTIAL, SPECIAL OR PUNITIVE
DAMAGES OF ANY KIND OR NATURE, INCLUDING BUT NOT LIMITED TO LOSS OF PROFITS
OR LOSS OF DATA, FOR ANY REASON WHATSOEVER, WHETHER SUCH LIABILITY IS ASSERTED
ON THE BASIS OF CONTRACT, TORT (INCLUDING NEGLIGENCE OR STRICT LIABILITY), OR
OTHERWISE, EVEN IF ANY OF SAID PARTIES HAS BEEN WARNED OF THE POSSIBILITY OF
SUCH LOSS OR DAMAGES.
The Software was developed at least in part by the University of Chicago,
as Operator of Argonne National Laboratory (http://miranda.ctd.anl.gov:7123/).

Modified work: The original source code (NdtTests.java) comes from the NDT Android app
               that is available from http://code.google.com/p/ndt/.
               It's modified for the CalSPEED Android app by California
               State University Monterey Bay (CSUMB) on April 29, 2013.


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

import android.location.Location;
import android.os.SystemClock;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import gov.ca.cpuc.calspeed.android.AndroidUiServices.TextOutputAdapter;
import gov.ca.cpuc.calspeed.android.CalspeedFragment.LatLong;
import gov.ca.cpuc.calspeed.android.CalspeedFragment.NetworkLatLong;


class StandardTest implements Runnable {
    private final TextOutputAdapter statistics;
    private final TextOutputAdapter results;
    private final TextOutputAdapter summary;
    private int runStatus;
    private String textResults;
    private String testType;

    private final String server1;           // west server
    private final String server2;           // east server
    private final AndroidUiServices uiServices;
    private final String networkType;
    private final NdtLocation ndtLocation;
    private final String applicationFilesDir;
    private final Date date;
    private Float gpsDistanceTraveled;
    private Float networkDistanceTraveled;
    private final String connectionType;
    private final NetworkAndDeviceInformation ndi;

    private StringBuilder testStatus;
    private final ConfigurationTable configTable;
    private final String iperfDirectory;
    private final String tcpPort;

    private ExecCommandLine command;
    private final LatLong testLatLong;
    private final NetworkLatLong networkLatLong;
    private ProcessPing pingStatsEast;
    private ProcessPing pingStatsWest;
    private static Boolean pingStatusFailed;
    private static Boolean noNetworkConnection;
    private static Float pingAverage;
    private static Double mosValue;
    private static Double mosValueEast;
    private ProcessIperf[] tcpResultsWest;
    private ProcessIperf[] tcpResultsEast;
    private ProcessIperf[] udpResultsWest;
    private ProcessIperf[] udpResultsEast;
    private Boolean udpStatusFailed;
    private Float udpAverage;
    private final String UDPPort;
    private Double saveLastNetworkLat;
    private Double saveLastNetworkLong;
    private Double saveLastGPSLat;
    private Double saveLastGPSLong;
    private final HistoryDatabaseHandler db;
    private final CalspeedFragment outerCalspeedFrag;
    public static final String UP = "UP";
    private static final String DOWN = "DOWN";
    private static final String WEST = "WEST";
    private static final String EAST = "EAST";
    private final String TCP_WEST = "TCP_WEST";
    private final String TCP_EAST = "TCP_EAST";
    private final String UDP_WEST = "UDP_WEST";
    private final String UDP_EAST = "UDP_EAST";
    private final String PING_WEST = "PING_WEST";
    private final String PING_EAST = "PING_EAST";
    private final String PING_CHECK = "PING_CHECK";
    private final String SIGNAL_INFO = "SIGNAL_INFO";
    private final String PRELIM_WEST = "PRELIM_WEST";
    public String test = "";
    public String ip = "";
    private static final Map<String, Double> WestUp = new HashMap<>();
    private static final Map<String, Double> WestDown = new HashMap<>();
    private static final Map<String, Double> EastUp = new HashMap<>();
    private static final Map<String, Double> EastDown = new HashMap<>();
    private final Map<Integer, String> threadDirection = new HashMap<>();
    private Integer threadCounter = 0;
    private static String[] vid_summary;
    private static String[] vid_conference_summary;
    private static String[] vid_voip_summary;

    private static final String STREAMING = "streaming";
    private static final String CONFERENCE = "conference";
    private static final String VOIP = "voip";
    private final ArrayList<Metric> allMetrics = new ArrayList<>();
    private final HashSet<Integer> threadSet = new HashSet<>();
    private static final HashMap<Integer, ArrayList<Metric>> metricsByThread = new HashMap<>();

    private static int tcpThreadNumber;
    private TestConfig westTcpConfig;
    private TestConfig eastTcpConfig;
    private TestConfig westUdpOneSecConfig;
    private TestConfig eastUdpOneSecConfig;
    private boolean isPrelimTest;

    /*
     * Initializes the network test thread.
	 * 
	 * @param host hostname of the test server
	 * @param uiServices object for UI interaction
	 * @param networkType indicates the type of network, e.g. 4g, 3G, Wifi, Wired, etc.
	 */
    StandardTest(NetworkLatLong networkLatLong, LatLong testLatLong, String server1, String server2,
                 AndroidUiServices uiServices, String networkType, NdtLocation ndtLocation,
                 String applicationFilesDir, Date date, String textResults,
                 String tcpPort, String udpPort, String connectionType, HistoryDatabaseHandler db,
                 NetworkAndDeviceInformation ndi, CalspeedFragment calspeedFragment) {
        this.ndi = ndi;
        this.server1 = server1;
        this.server2 = server2;
        this.uiServices = uiServices;
        this.networkType = networkType;
        this.ndtLocation = ndtLocation;
        this.applicationFilesDir = applicationFilesDir;
        this.date = date;
        saveLastNetworkLat = 0.0;
        saveLastNetworkLong = 0.0;
        saveLastGPSLat = 0.0;
        saveLastGPSLong = 0.0;
        this.textResults = textResults;
        this.command = null;
        this.networkLatLong = networkLatLong;
        this.networkLatLong.getLatitudeLongitude(networkLatLong);
        this.testLatLong = testLatLong;
        this.testLatLong.getLatitudeLongitude(testLatLong);
        this.gpsDistanceTraveled = Float.valueOf("0");
        this.networkDistanceTraveled = Float.valueOf("0");
        this.UDPPort = udpPort;
        this.connectionType = connectionType;
        this.db = db;
        this.outerCalspeedFrag = calspeedFragment;
        this.threadCounter = 0;
        this.isPrelimTest = true;
        this.configTable = new ConfigurationTable();
        this.iperfDirectory = applicationFilesDir;
        this.tcpPort = tcpPort;
        tcpThreadNumber = Constants.DEFAULT_THREAD_NUMBER;
        statistics = new TextOutputAdapter(uiServices, UiServices.STAT_VIEW);
        results = new TextOutputAdapter(uiServices, UiServices.MAIN_VIEW);
        summary = new TextOutputAdapter(uiServices, UiServices.SUMMARY_VIEW);
        setupResultsObjects();
    }

    String[] getVideoCalc() {
        return getVideo(STREAMING);
    }

    String[] getVideoConferenceCalc() {
        return getVideo(CONFERENCE);
    }

    String[] getVideoVoipCalc() {
        return getVideo(VOIP);
    }

    private String[] getVideo(String type) {
        String[] vidSummary = null;
        switch (type) {
            case STREAMING:
                vidSummary = vid_summary;
                break;
            case CONFERENCE:
                vidSummary = vid_conference_summary;
                break;
            case VOIP:
                vidSummary = vid_voip_summary;
                break;
        }
        Log.v("VideoSummary", "There is an existing summary: " + Arrays.toString(vidSummary));
        if (vidSummary == null) {
            Log.d("VideoSummary", "The video summary is null, calculating " + type);
            vidSummary = calcVideo(type);
        }
        if (Objects.equals(vidSummary[2], "")) {
            vidSummary[2] = "N/A";
        }
        return vidSummary;
    }

    private String formatGPSCoordinate(Double coord) {
        DecimalFormat df = new DecimalFormat("#.00000");
        return (df.format(coord));
    }

    private void printLatLong() {
        testLatLong.getLatitudeLongitude(testLatLong);
        showResults("\nGPSLatitude:" + testLatLong.Latitude);
        showResults("\nGPSLongitude:" + testLatLong.Longitude);
        String Lat0 = Double.toString(testLatLong.Latitude);
        statistics.append("\nGPSLatitude:" + Lat0);
        String Long0 = Double.toString(testLatLong.Longitude);
        statistics.append("\nGPSLongitude:" + Long0);
        uiServices.updateLatLong();
        networkLatLong.getLatitudeLongitude(networkLatLong);
        showResults("\nNetworkLatitude:" + networkLatLong.Latitude);
        showResults("\nNetworkLongitude:" + networkLatLong.Longitude);
        sumNewDistances();
        Float gpsDistanceFeet = gpsDistanceTraveled * Constants.METERS_TO_FEET;
        showResults("\nGPSDistanceMoved: " + gpsDistanceFeet.toString());
        Float networkDistanceFeet = networkDistanceTraveled * Constants.METERS_TO_FEET;
        showResults("\nNetworkDistanceMoved: " + networkDistanceFeet.toString());
    }

    private void sumNewDistances() {
        Location oldGPSLocation = new Location("");
        oldGPSLocation.setLatitude(saveLastGPSLat);
        oldGPSLocation.setLongitude(saveLastGPSLong);
        Location oldNetworkLocation = new Location("");
        oldNetworkLocation.setLatitude(saveLastNetworkLat);
        oldNetworkLocation.setLongitude(saveLastNetworkLong);

        if (testLatLong.valid) {
            if ((saveLastGPSLat != 0) && (saveLastGPSLong != 0)) {
                Location newGPSLocation = new Location("");
                newGPSLocation.setLatitude(testLatLong.Latitude);
                newGPSLocation.setLongitude(testLatLong.Longitude);
                gpsDistanceTraveled += oldGPSLocation.distanceTo(newGPSLocation);
            }
            saveLastGPSLat = testLatLong.Latitude;
            saveLastGPSLong = testLatLong.Longitude;
        }
        if (networkLatLong.valid) {
            if ((saveLastNetworkLat != 0) && (saveLastNetworkLong != 0)) {
                Location newNetworkLocation = new Location("");
                newNetworkLocation.setLatitude(networkLatLong.Latitude);
                newNetworkLocation.setLongitude(networkLatLong.Longitude);
                networkDistanceTraveled += oldNetworkLocation.distanceTo(newNetworkLocation);
            }
            saveLastNetworkLat = networkLatLong.Latitude;
            saveLastNetworkLong = networkLatLong.Longitude;
        }
    }

    public void run() {
        long startTime = SystemClock.elapsedRealtime();
        try {
            MOSCalculation.clearData();
            printLatLong();
            showResults(Constants.CONNECTIVITY_CHECK);
            statistics.append(Constants.CONNECTIVITY_CHECK);
            printToSummary(Constants.CONNECTIVITY_CHECK);
            changeDisplayTitle("Checking Connectivity...");
            uiServices.onBeginTest();
            waiting(2);
            uiServices.incrementProgress(2);
            for (int i = 0; i < 3; i++) {
                testStatus = new StringBuilder("Connectivity Check");
                runStatus = pingTest(String.format(Constants.PING_COMMAND, server1, "4"), null,
                        null, true, PING_CHECK);
                if ((runStatus != Constants.PING_100_PERCENT_LOSS)
                        && (runStatus != Constants.RUN_COMMAND_INTERRUPT)) {
                    Log.d("ConnCheck", "Connectivity ping check success after " + i + "tries");
                    break;
                }
                waiting(2);
            }
            if (runStatus == Constants.PING_100_PERCENT_LOSS) {
                ndtLocation.stopListen();
                ndtLocation.stopNetworkListenerUpdates();
                showResults(Constants.CONNECTIVITY_FAIL);
                printToSummary(Constants.CONNECTIVITY_FAIL);
                runStatus = Constants.PING_CONNECTIVITY_FAIL;
                uiServices.setStatusText("No Network Connection.");
                Log.i("INFO", "No network connection 1");
                noNetworkConnection = true;
                saveAllResults();

            } else if (runStatus == Constants.RUN_COMMAND_INTERRUPT) {
                ndtLocation.stopListen();
                ndtLocation.stopNetworkListenerUpdates();
                showResults(Constants.CONNECTIVITY_FAIL);
                printToSummary(Constants.CONNECTIVITY_FAIL);
                runStatus = Constants.PING_CONNECTIVITY_FAIL;
            } else {
                // Start by running the preliminary tests
                changeDisplayTitle(Constants.TEST_PRELIM);
                uiServices.startPreliminaryPhase();
                Log.d(getClass().getName(), "Running prelims");
                runPreliminaryTests();
                if ((tcpResultsWest[0] == null) || (tcpResultsEast[0] == null)) {
                    setDefaultTcpConfig();
                }
                Log.d("prelim", "West Config: " + this.westTcpConfig.toString());
                Log.d("prelim", "East Config: " + this.eastTcpConfig.toString());
                uiServices.probePhaseComplete();
                waiting(1);

                // Start WEST test phase
                printLatLong();
                changeDisplayTitle(Constants.TEST_CALIFORNIA);
                noNetworkConnection = false;
                showResults(Constants.TEST_ONE_TCP_WEST);
                statistics.append(Constants.TEST_ONE_TCP_WEST);
                printToSummary("\nStarting Test 1....\n");
                tcpResultsWest[0] =
                        new ProcessIperf(uiServices, (TcpTestConfig) this.westTcpConfig);
                ProcessIperf tcpWestOne = tcpResultsWest[0];
                tcpTest(this.westTcpConfig.createIperfCommandLine(), tcpWestOne, WEST, TCP_WEST);
                tcpWestOne.setPhase1TCPIperfStatus();

                printLatLong();
                showResults(Constants.TEST_TWO_PING_WEST);
                statistics.append(Constants.TEST_TWO_PING_WEST);
                printToSummary("\nStarting Test 2....\n");
                pingTest(String.format(Constants.PING_COMMAND, server1, "10"), null, pingStatsWest,
                        false, PING_WEST);
                pingStatsWest.setPingFinalStatus();

                printLatLong();
                showResults(Constants.TEST_THREE_UDP_WEST);
                statistics.append(Constants.TEST_THREE_UDP_WEST);
                printToSummary("\nStarting Test 3....\n");
                ProcessIperf udpWestOneSec = new ProcessIperf(uiServices,
                        (UdpTestConfig) this.westUdpOneSecConfig);
                udpTest(this.westUdpOneSecConfig.createIperfCommandLine(), udpWestOneSec, 1,
                        UDP_WEST);
                MOSCalculation.addUDPLoss(Double.parseDouble(udpWestOneSec.loss));

                // Wait for West Phase to complete. Change display to Virginia first
                changeDisplayTitle(Constants.TEST_VIRGINIA);
                uiServices.phase1Complete();
                waiting(2);

                // Start EAST test phase
                printLatLong();

                showResults(Constants.TEST_FOUR_TCP_EAST);
                statistics.append(Constants.TEST_FOUR_TCP_EAST);
                printToSummary("\nStarting Test 4....\n");
                tcpResultsEast[0] =
                        new ProcessIperf(uiServices, (TcpTestConfig) this.eastTcpConfig);
                ProcessIperf tcpEastOne = tcpResultsEast[0];
                tcpEastOne.setTCPPhase2(tcpWestOne.uploadSpeed, tcpWestOne.downloadSpeed);
                tcpTest(this.eastTcpConfig.createIperfCommandLine(), tcpEastOne, EAST, TCP_EAST);
                tcpEastOne.setIperfTCPAvgFinal();

                printLatLong();
                showResults(Constants.TEST_FIVE_PING_EAST);
                statistics.append(Constants.TEST_FIVE_PING_EAST);
                printToSummary("\nStarting Test 5....\n");
                pingStatsEast.setPhase2(pingStatsWest.average);
                pingTest(String.format(Constants.PING_COMMAND, server2, "10"), null, pingStatsEast,
                        false, PING_EAST);
                pingStatsEast.setPingFinalStatus();
                setPingAvgFinal(pingStatsWest, pingStatsEast);

                printLatLong();
                showResults(Constants.TEST_SIX_UDP_WEST);
                statistics.append(Constants.TEST_SIX_UDP_WEST);
                printToSummary("\nStarting Test 6....\n");
                setUDPPhase2();
                ProcessIperf udpEastOneSec = new ProcessIperf(uiServices,
                        (UdpTestConfig) this.eastUdpOneSecConfig);
                udpTest(this.eastUdpOneSecConfig.createIperfCommandLine(), udpEastOneSec, 1,
                        UDP_EAST);
                MOSCalculation.addUDPLoss(Double.parseDouble(udpEastOneSec.loss));
                setUDPJitterFinal(udpWestOneSec, udpEastOneSec);

                // MOS Calculation
                printLatLong();
                mosValue = MOSCalculation.getMOS(pingStatsWest, pingStatsEast, udpResultsWest[0],
                        udpResultsEast[0]);
                Log.d("MOSValue", "mos value for sat and unsat: " + mosValue);
                ProcessPing nullProcessPing = new ProcessPing("Latency", uiServices);
                nullProcessPing.success = false;
                nullProcessPing.average = "0";
                ProcessIperf nullUdpResult = new ProcessIperf(uiServices);
                nullUdpResult.udpSuccess = false;
                nullUdpResult.jitter = "0";
                mosValueEast = MOSCalculation.getMOS(nullProcessPing, pingStatsEast,
                        nullUdpResult, udpResultsEast[0]);
                Double mosValueWest = MOSCalculation.getMOS(pingStatsWest, nullProcessPing,
                        udpResultsWest[0], nullUdpResult);
                Log.d("MOSValue", "Calculated new MOS values for east and west: " + mosValueEast +
                        ", " + mosValueWest);
                uiServices.setResults(Constants.THREAD_WRITE_MOS_DATA, "MOS Value",
                        mosValue.toString(), false, false);
                uiServices.setMosValue(mosValue.toString());

                // Calculate Video Metrics
                calculateSpeed();
                vid_summary = calcVideo(STREAMING);
                vid_conference_summary = calcVideo(CONFERENCE);
                vid_voip_summary = calcVideo(VOIP);
                Log.d("VideoMetrics", "VidSummary: " + Arrays.toString(vid_summary));
                Log.d("VideoMetrics", "VidConf: " + Arrays.toString(vid_conference_summary));
                Log.d("VideoMetrics", "Voip: " + Arrays.toString(vid_voip_summary));
                getSignalInfo();

                // Saving results to file
                runStatus = saveAllResults();
            }

            ndtLocation.stopListen();
            ndtLocation.stopNetworkListenerUpdates();
            // Finish the Test
            if (runStatus == Constants.RUN_COMMAND_INTERRUPT) {
                uiServices.setStatusText("Test Interrupted.");
                uiServices.onTestInterrupt();
                Log.i("INFO", "Test interrupt");
            } else if (runStatus == Constants.RUN_COMMAND_FAIL) {
                Log.i("INFO", "Command fail");
            } else if (runStatus == Constants.PING_CONNECTIVITY_FAIL) {
                uiServices.setStatusText("No Network Connection.");
                uiServices.resetGui();
                Log.i("INFO", "No network connection");
            } else {
                uiServices.setStatusText("Test Complete.");
            }
        } catch (InterruptedException e) {
            finishProgressBar();
            showResults("\nQuitting Operations...\n");
            printToSummary("\nStandard Test Interrupted...\n");
            uiServices.setStatusText("Test Interrupted.");
            uiServices.onTestInterrupt();
            return;
        }
        finishProgressBar();
        waiting(2);
        uiServices.onEndTest();
        outerCalspeedFrag.enableTabs();
        WestDown.clear();
        WestUp.clear();
        EastUp.clear();
        EastDown.clear();
        metricsByThread.clear();
        allMetrics.clear();

        long stopTime = SystemClock.elapsedRealtime();
        long elapsedTime = stopTime - startTime;
        double elapsedSeconds = elapsedTime / 1000.0;
        uiServices.reportTestTime(elapsedSeconds);
    }

    private void runPreliminaryTests() {
        this.isPrelimTest = true;
        String prelimNotes = "\n..................................................................";
        prelimNotes += "\nPRELIM TEST NOTES\n\n";
        Log.d("CONFIGURATION", this.configTable.toString());
        String tcpConfigResult = "";
        Map<String, Integer> prelimTable = configTable.getPrelimTable();
        if (prelimTable != null) {
            Integer windowSize = prelimTable.get(Constants.WINDOW_SIZE);
            String prelimWindowSize = String.format("%dk", windowSize);
            TestConfig prelimConfig = new TcpTestConfig(this.iperfDirectory, server1, this.tcpPort,
                    prelimWindowSize, prelimTable.get(Constants.THREAD_NUMBER),
                    prelimTable.get(Constants.TEST_TIME));
            prelimNotes += "\n\nPrelim test configuration:";
            prelimNotes += configPrinter(prelimTable);
            Log.d("PRELIM_CMD_LINE", prelimConfig.createIperfCommandLine());
            try {
                ProcessIperf tcpPrelim = new ProcessIperf(uiServices, (TcpTestConfig) prelimConfig);
                long prelimTimeout;
                try {
                    Integer testTime = prelimTable.get(Constants.TEST_TIME);
                    if (testTime != null) {
                        prelimTimeout = (long) testTime * 3000;
                    } else {
                        prelimTimeout = Constants.DEFAULT_TEST_TIME;
                    }
                } catch (NullPointerException e) {
                    prelimTimeout = Constants.DEFAULT_TEST_TIME;
                }
                Log.d("PrelimTestTime", String.valueOf(prelimTimeout));
                prelimNotes += prelimTcpTest(prelimConfig.createIperfCommandLine(),
                        tcpPrelim, PRELIM_WEST, Constants.IPERF_PRELIM_TCP_TIMEOUT);
                if (tcpPrelim.getSuccess()) {
                    prelimNotes += "\nDownload speed result is: ";
                    prelimNotes += tcpPrelim.getDownloadSpeed();
                    tcpConfigResult += "\n\nIperf TCP Test params:";
                    tcpConfigResult += setTcpConfig(tcpPrelim.getDownloadSpeed());
                } else {
                    prelimNotes += "\nProbe test result failed, using default configuration.";
                    setDefaultTcpConfig();
                }
            } catch (Exception e) {
                Log.e("RunPrelim", e.toString());
                Log.i("RunPrelim", "Error, using default TCP Config");
                setDefaultTcpConfig();
            }
            prelimNotes += tcpConfigResult;
            textResults += prelimNotes;
        } else {
            Log.w("RunPrelim", "Invalid config, using default TCP Config");
            setDefaultTcpConfig();
        }
        try {
            ProcessPing pingStatsPrelim = new ProcessPing("Latency", uiServices);
            pingTest(String.format(Constants.PING_COMMAND, server1, "1"), null, pingStatsPrelim,
                    false, PRELIM_WEST);
        } catch (Exception e) {
            Log.e("RunPrelim", "Error in Ping Test" + e.toString());
        }
        try {
            ProcessIperf udpPrelimWestOneSec =
                    new ProcessIperf(uiServices, (UdpTestConfig) this.westUdpOneSecConfig);
            udpTest(this.westUdpOneSecConfig.createIperfCommandLine(),
                    udpPrelimWestOneSec, 1, PRELIM_WEST);
        } catch (Exception e) {
            Log.e("RunPrelim", "Error in UDP Test" + e.toString());
        }
        textResults += "\n................................................................\n\n";
        this.isPrelimTest = false;
    }

    private int tcpTest(String commandline, ProcessIperf iperfTest, String server, String testName)
            throws InterruptedException {
        Log.v(testName, commandline);
        testStatus = new StringBuilder();
        testStatus.append("\n");
        testType = getTestType(testName);
        Log.i(getClass().getName(), "Test type: " + testType);
        try {
            Log.v(testName, "Test timeout value: " + Constants.IPERF_TCP_TIMEOUT);
            command = new ExecCommandLine(commandline, Constants.IPERF_TCP_TIMEOUT, results,
                    iperfTest, null, uiServices);
            String commandOutput = command.runIperfCommand();
            textResults += commandOutput;
            parseOutput(commandOutput, server);
            statistics.append("\n" + commandOutput);
            if (command.commandTimedOut) {
                String message = "\nIperf timed out after " + Constants.IPERF_TCP_TIMEOUT / 1000 +
                        " seconds.\n";
                handleMessage(message);
                if (!iperfTest.finishedUploadTest) {
                    iperfTest.setMessage("Upload test not finished");
                }
                testStatus.append(Constants.FAILED_TCP_LINE);
                uiServices.clearProcessHandle();
                return Constants.RUN_COMMAND_FAIL;
            }
        } catch (InterruptedException e) {
            Log.d("Quit", "Interrupting: " + testName);
            testStatus.append(Constants.FAILED_TCP_LINE);
            uiServices.clearProcessHandle();
            throw new InterruptedException();
        } catch (TimeoutException e) {
            String message = "\nIperf timed out after " + Constants.IPERF_TCP_TIMEOUT / 1000
                    + " seconds.\n";
            handleMessage(message);
            uiServices.clearProcessHandle();
            testStatus.append(Constants.FAILED_TCP_LINE);
            return Constants.RUN_COMMAND_FAIL;
        }
        uiServices.clearProcessHandle();
        return Constants.RUN_COMMAND_SUCCESS;
    }

    private int pingTest(String commandline, ProcessIperf iperfTest, ProcessPing pingTest,
                         boolean isPingCheck, String testName) throws InterruptedException {
        String commandOutput;
        testType = getTestType(testName);
        Log.i(getClass().getName(), "Test type: " + testType);
        try {
            ExecCommandLine command = new ExecCommandLine(commandline, Constants.PING_TIMEOUT,
                    results, iperfTest, pingTest, uiServices);
            commandOutput = command.runCommand();
            showResults("\n" + commandOutput);
            statistics.append("\n" + commandOutput);
            if (command.commandTimedOut) {
                String message = "\nPing timed out after "
                        + Constants.PING_TIMEOUT / 1000 + " seconds.\n";
                handleMessage(message);
                if (!isPingCheck) {
                    pingTest.setPingFail("Test Timed Out.");
                }
                return Constants.RUN_COMMAND_FAIL;
            }
        } catch (InterruptedException e) {
            if (isPingCheck) {
                return (Constants.RUN_COMMAND_INTERRUPT);
            } else {
                Log.d("Quit", "Interrupting: " + testName);
                throw new InterruptedException();
            }
        }
        if (isPingCheck) {
            if ((commandOutput.contains(Constants.PING_100_PERCENT))
                    || (!commandOutput.contains("rtt min"))) {
                return (Constants.PING_100_PERCENT_LOSS);
            } else {
                return (Constants.RUN_COMMAND_SUCCESS);
            }
        } else {
            if (Thread.currentThread().isInterrupted()) {
                Log.d("Quit", "Interruption called from end: " + testName);
                throw new InterruptedException();
            }
            return (Constants.RUN_COMMAND_SUCCESS);
        }
    }

    private int udpTest(String commandline, ProcessIperf iperfTest, int numTests, String testName)
            throws InterruptedException {
        testType = getTestType(testName);
        Log.i(getClass().getName(), "Test type: " + testType);
        try {
            for (int i = 0; i < numTests; i++) {
                if (numTests > 1) {
                    String startMessage = "\nStarting UDP 1 second Test #" + (i + 1) + "\n";
                    handleMessage(startMessage);
                }
                command = new ExecCommandLine(commandline,
                        Constants.IPERF_UDP_TIMEOUT, results, iperfTest, null, uiServices);
                String commandOutput = command.runIperfCommand();
                if (!Objects.equals(testName, PRELIM_WEST)) {
                    textResults += commandOutput;
                    statistics.append("\n" + commandOutput);
                }
                if (command.commandTimedOut) {
                    String message = "\nIperf timed out after " + Constants.IPERF_UDP_TIMEOUT / 1000
                            + " seconds.\n";
                    handleMessage(message);
                    testStatus.append(Constants.FAILED_UDP_LINE);
                }
                Log.d("setUDPIperfFinalStatus", "calling setUDPIperfFinalStatus");
                iperfTest.setUDPIperfFinalStatus();
                waiting(2);
            }
        } catch (InterruptedException e) {
            Log.d("Quit", "Interrupting: " + testName);
            uiServices.clearProcessHandle();
            throw new InterruptedException();

        } catch (TimeoutException e) {
            results.append("\n" + e);
            statistics.append("\n" + e);
            uiServices.clearProcessHandle();
            return (Constants.RUN_COMMAND_FAIL);
        }
        if (Thread.currentThread().isInterrupted()) {
            Log.d("Quit", "Interruption called from end: " + testName);
            throw new InterruptedException();
        }
        return Constants.RUN_COMMAND_SUCCESS;
    }

    private void handleMessage(String message) {
        Log.d("Messenger", message);
        printToSummary(message);
        statistics.append(message);
        showResults(message);
    }

    private void changeDisplayTitle(String title) {
        uiServices.setStatusText(title);
    }

    private String getTestType(String type) {
        String testType;
        int progressIncrement = 2;
        switch (type) {
            case TCP_WEST:
                testType = "Testing with California server...";
                break;
            case TCP_EAST:
                testType = "Testing with Virginia server...";
                break;
            case UDP_WEST:
                testType = "Testing with California server...";
                break;
            case UDP_EAST:
                testType = "Testing with Virginia server...";
                break;
            case PING_WEST:
                testType = "Testing with California server...";
                break;
            case PING_EAST:
                testType = "Testing with Virginia server...";
                break;
            case PING_CHECK:
                testType = "Checking Connectivity";
                progressIncrement = 0;
                break;
            case SIGNAL_INFO:
                testType = "Getting Signal Information";
                break;
            case PRELIM_WEST:
                testType = "Preliminary Test";
                progressIncrement = 1;
                break;
            default:
                testType = "";
                progressIncrement = 0;
        }
        Log.d("Progress", "Incrementing by  " + progressIncrement + " for test: " + testType);
        uiServices.incrementProgress(progressIncrement);
        return testType;
    }

    private String prelimTcpTest(String commandline, ProcessIperf iperfTest, String testName,
                                 long timeout) {
        String resultOutput;
        testStatus = new StringBuilder();
        String PRELIM_WEST = "PRELIM_WEST";
        testType = getTestType(PRELIM_WEST);
        Log.i(getClass().getName(), "Test type: " + testType);
        testStatus.append(PRELIM_WEST).append("\n");
        try {
            Log.v(testName, "Test timeout value: " + timeout);
            command = new ExecCommandLine(commandline, timeout, results, iperfTest, null,
                    uiServices);
            resultOutput = command.runIperfCommand();
            if (command.commandTimedOut) {
                String message = "\nIperf timed out after " + timeout / 1000 + " seconds.\n";
                Log.d(testName, iperfTest.getMessage());
                testStatus.append(message);
                return message;
            }
        } catch (InterruptedException e) {
            Log.i(testName, "Interrupt Exception");
            testStatus.append("Test interrupted");
            return null;
        } catch (TimeoutException e) {
            String message = "\nIperf timed out after " + timeout / 1000 + " seconds.\n";
            Log.i(testName, message);
            testStatus.append(message);
            return null;
        }
        if (Thread.currentThread().isInterrupted()) {
            Log.i(testName, "Interrupt Exception");
            testStatus.append("Test interrupted");
            return null;
        }
        iperfTest.finishPrelim();
        if (iperfTest.getSuccess()) {
            testStatus.append(String.format("Download speed: %d Kb/s",
                    Math.round(iperfTest.getDownloadSpeed())));
        } else {
            testStatus.append("Iperf test unsuccessful");
            Log.d(testName, "Iperf test unsuccessful");
            Log.d(testName, iperfTest.getErrorMessage());
        }
        uiServices.clearProcessHandle();
        return resultOutput;
    }

    private String setTcpConfig(Float downloadSpeed) {
        Map<Integer, Map<String, Integer>> table = configTable.getTable();
        Map<String, Integer> tcpConfig = null;
        if (table != null) {
            for (Object key : table.keySet()) {
                if (downloadSpeed < Float.valueOf((Integer) key)) {
                    tcpConfig = table.get(key);
                    Log.d("TCP_TEST_CONFIG", configPrinter(tcpConfig));
                    break;
                }
            }
            if (tcpConfig != null) {
                String windowSizeFormat =
                        String.format("%dk", tcpConfig.get(Constants.WINDOW_SIZE));
                this.westTcpConfig = new TcpTestConfig(this.iperfDirectory, server1, this.tcpPort,
                        windowSizeFormat, tcpConfig.get(Constants.THREAD_NUMBER),
                        tcpConfig.get(Constants.TEST_TIME));
                this.eastTcpConfig = new TcpTestConfig(this.iperfDirectory, server2, this.tcpPort,
                        windowSizeFormat, tcpConfig.get(Constants.THREAD_NUMBER),
                        tcpConfig.get(Constants.TEST_TIME));
                tcpResultsWest[0] = new ProcessIperf(uiServices,
                        (TcpTestConfig) this.westTcpConfig);
                tcpResultsEast[0] = new ProcessIperf(uiServices,
                        (TcpTestConfig) this.eastTcpConfig);
                tcpThreadNumber = ((TcpTestConfig) this.westTcpConfig).getThreadNumber();
                return configPrinter(tcpConfig);
            }
        }
        setDefaultTcpConfig();
        Map<String, Integer> defaultConfig = configTable.getDefaultsTable();
        return configPrinter(defaultConfig);
    }

    private void setDefaultTcpConfig() {
        Map<String, Integer> defaultConfig = configTable.getDefaultsTable();
        if (defaultConfig != null) {
            Log.d("DefaultConfig", String.format("Using default config for TCP tests:\n %s",
                    configPrinter(defaultConfig)));
            String windowSizeFormat =
                    String.format("%dk", defaultConfig.get(Constants.WINDOW_SIZE));
            this.westTcpConfig = new TcpTestConfig(this.iperfDirectory, server1, this.tcpPort,
                    windowSizeFormat, defaultConfig.get(Constants.THREAD_NUMBER),
                    defaultConfig.get(Constants.TEST_TIME));
            this.eastTcpConfig = new TcpTestConfig(this.iperfDirectory, server2, this.tcpPort,
                    windowSizeFormat, defaultConfig.get(Constants.THREAD_NUMBER),
                    defaultConfig.get(Constants.TEST_TIME));
            tcpResultsWest[0] = new ProcessIperf(uiServices,
                    (TcpTestConfig) this.westTcpConfig);
            tcpResultsEast[0] = new ProcessIperf(uiServices,
                    (TcpTestConfig) this.eastTcpConfig);
            tcpThreadNumber = ((TcpTestConfig) this.westTcpConfig).getThreadNumber();
            Log.i("setDefaultTcpConfig", configPrinter(defaultConfig));
        } else {
            Log.e("DefaultConfig", "Unable to get default configuration");
        }
    }

    private String configPrinter(Map<String, Integer> config) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n{\n");
        for (String key : config.keySet()) {
            sb.append("    ").append(key).append(": ");
            if (key.equals("windowSize")) {
                Integer winSize = config.get(key);
                if (winSize != null) {
                    sb.append(winSize * 2);
                } else {
                    Log.w("configPrinter", "Unable to get window size from config");
                }
            } else {
                sb.append(config.get(key));
            }
            sb.append("\n");
        }
        sb.append("\n}\n");
        return sb.toString();
    }

    private void finishProgressBar() {
        uiServices.completeProgress();
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            Log.w("moveProgressBar", "Failed to increment progress bar");
        }
    }

    private void setPingAvgFinal(ProcessPing Ping1, ProcessPing Ping2) {
        pingAverage = 0.0f;
        pingStatusFailed = false;
        String pingMessage = "Latency";

        if (Ping1.success) {
            if (Ping2.success) {
                pingAverage = (Float.parseFloat(Ping1.average.replace(",", ".")) +
                        Float.parseFloat(Ping2.average.replace(",", "."))) / 2;
            } else {
                pingAverage = Float.parseFloat(Ping1.average.replace(",", "."));
            }
        } else {
            if (Ping2.success) {
                pingAverage = Float.parseFloat(Ping2.average.replace(",", "."));
            } else {
                pingAverage = 0.0f;
                pingMessage = "Latency Incomplete";
                pingStatusFailed = true;
            }
        }
        uiServices.setResults(Constants.THREAD_WRITE_LATENCY_DATA, pingMessage,
                ProcessIperf.formatFloatString(pingAverage.toString()), pingStatusFailed,
                pingStatusFailed);
    }

    private void setUDPPhase2() {
        for (int i = 0; i < Constants.NUM_UDP_TESTS_PER_SERVER - 1; i++) {
            udpResultsEast[i].setUDPPhase2();
        }
    }

    private void setUDPJitterFinal(ProcessIperf udp1, ProcessIperf udp2) {
        udpAverage = 0.0f;
        udpStatusFailed = false;
        String udpMessage = "Jitter";
        if (udp1.udpSuccess) {
            if (udp2.udpSuccess) {
                udpAverage = (Float.parseFloat(udp1.jitter) + Float.parseFloat(udp2.jitter)) / 2;
            } else {
                udpAverage = Float.parseFloat(udp1.jitter);
            }
        } else {
            if (udp2.udpSuccess) {
                udpAverage = Float.parseFloat(udp2.jitter);
            } else {
                udpAverage = 0.0f;
                udpMessage = "Jitter Incomplete";
                udpStatusFailed = true;
            }
        }
        uiServices.setResults(Constants.THREAD_WRITE_JITTER_DATA, udpMessage,
                ProcessIperf.formatFloatString(udpAverage.toString()), udpStatusFailed,
                udpStatusFailed);
    }

    private void getSignalInfo() {
        testType = getTestType(SIGNAL_INFO);
        showResults(String.format("\n\nSignalStrength: %d dB\n", ndi.getSignalStrength()));
        showResults(String.format("SignalNoiseRatio: %d\n", ndi.getSnr()));
        showResults(String.format("LocationAreaCode: %d\n", ndi.getLocationAreaCode()));
        showResults(String.format("CellTowerID: %d\n", ndi.getCellTowerId()));
    }

    private Integer saveAllResults() {
        Integer returnStatus = Constants.RUN_COMMAND_SUCCESS;
        saveHistory();
        try {
            showResults("\nSaving Results to sdcard...\n");
            statistics.append("\nSaving Results to sdcard...\n");
            printToSummary("\nSaving Results...\n");
            SaveResults localResult = new SaveResults(results, summary,
                    textResults, date);
            String status = localResult.saveResultsLocally();
            showResults(status);
            if (status.indexOf("successfully") > 0) {
                printToSummary("File successfully saved.\n");
                statistics.append("File successfully saved.\n");
            } else {
                printToSummary(status);
                statistics.append(status);
                uiServices.resultsNotSaved();
                uiServices.setStatusText("Results Not Saved.");
                returnStatus = Constants.RUN_COMMAND_FAIL;
            }

            showResults("\nAttempting Upload to Server...\n");
            printToSummary("\nAttempting Upload to Server...\n");
            statistics.append("\nAttempting Upload to Server...\n");

            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    printToSummary("Upload Timeout. \n");
                    uiServices.setStatusText("Test Complete");
                    Log.w(getClass().getName(), "Upload Timeout");
                    runStatus = Constants.THREAD_RESULTS_NOT_UPLOADED;
                    uiServices.onEndTest();
                    this.cancel();
                }
            };
            timer.schedule(task, 60000);
            if (localResult.uploadAllFiles(summary, results)) {
                timer.cancel();
                printToSummary("All Files successfully uploaded.\n");
                showResults("All Files successfully uploaded.\n");
                statistics.append("All Files successfully uploaded.\n");
                localResult.clearErrorMessage();
            } else {
                timer.cancel();
                printToSummary("Upload Failed. Try again later.\n");
                showResults("Upload Failed. Try again later.\n");
                statistics.append("Upload Failed. Try again later.\n");
                uiServices.setStatusText("Test Complete.");
                Log.w(getClass().getName(), "Upload Failed");
                statistics.append(localResult.getErrorMessage());
                localResult.clearErrorMessage();
                returnStatus = Constants.RUN_COMMAND_FAIL;
            }

        } catch (InterruptedException e) {
            Log.e(getClass().getName(), "saveAllResults() - " + e.getMessage());
        }
        return (returnStatus);
    }

    private void saveHistory() {
        History history;
        history = getTestHistory();
        if (history != null) {
            db.addHistory(history);
        }
    }

    private History getTestHistory() {
        String upload;
        String download;
        String latency;
        String jitter;
        String latitude;
        String longitude;
        String mos;
        String videoCalc;
        String videoConference;
        String videoVoip;
        History history = null;
        latitude = "0";
        longitude = "0";
        try {
            if ((tcpResultsEast[0].uploadSuccess) &&
                    ((tcpResultsEast[0].uploadSpeed != 0) && tcpResultsWest[0].uploadSpeed != 0)) {
                Integer numInt = Math.round(tcpResultsEast[0].getHistoryUploadSpeed());
                upload = ProcessIperf.formatFloatString(numInt.toString());
            } else {
                upload = "N/A";
            }
        } catch (Exception e) {
            upload = "N/A";
        }

        try {
            if (!tcpResultsEast[0].downFinalStatusFailed) {
                Integer numInt = Math.round(tcpResultsEast[0].getHistoryDownloadSpeed());
                download = ProcessIperf.formatFloatString(numInt.toString());
            } else {
                download = "N/A";
            }
        } catch (Exception e) {
            download = "N/A";
        }

        try {
            if (noNetworkConnection) {
                latency = "N/A";
            } else if (!pingStatusFailed) {
                Integer numInt = Math.round(pingAverage);
                latency = ProcessIperf.formatFloatString(numInt.toString());
            } else {
                latency = "N/A";
            }
        } catch (Exception e) {
            latency = "N/A";
        }

        try {
            if (!udpStatusFailed) {
                Integer numInt = Math.round(udpAverage);
                jitter = ProcessIperf.formatFloatString(numInt.toString());
            } else {
                jitter = "N/A";
            }
        } catch (Exception e) {
            jitter = "N/A";
        }

        try {
            if (noNetworkConnection) {
                mos = "N/A";
            } else if (!pingStatusFailed || !udpStatusFailed) {
                Double numDouble = mosValue;
                mos = ProcessIperf.formatFloatString(numDouble.toString());
            } else {
                mos = "N/A";
            }
        } catch (Exception e) {
            mos = "N/A";
        }

        try {
            if (noNetworkConnection) {
                videoCalc = "N/A";
                videoConference = "N/A";
                videoVoip = "N/A";
            } else {
                videoCalc = getVideoCalc()[2];
                videoConference = getVideoConferenceCalc()[2];
                videoVoip = getVideoVoipCalc()[2];
            }
        } catch (Exception e) {
            Log.e("Exception", "Caught an exception that makes video N/A");
            Log.e("Exception", e.getMessage());
            e.printStackTrace();
            videoCalc = "N/A";
            videoConference = "N/A";
            videoVoip = "N/A";
        }
        try {
            String newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
            if ((saveLastGPSLat != 0) && (saveLastGPSLong != 0)) {
                latitude = formatGPSCoordinate(saveLastGPSLat);
                longitude = formatGPSCoordinate(saveLastGPSLong);
            } else {
                if ((saveLastNetworkLat != 0) && (saveLastNetworkLong != 0)) {
                    latitude = formatGPSCoordinate(saveLastNetworkLat);
                    longitude = formatGPSCoordinate(saveLastNetworkLong);
                } else {
                    if ((ndtLocation.gpsLastKnownLocation != null)
                            && (ndtLocation.gpsLastKnownLocation.getLatitude() != 0)
                            && (ndtLocation.gpsLastKnownLocation.getLongitude() != 0)) {
                        latitude = formatGPSCoordinate(
                                ndtLocation.gpsLastKnownLocation.getLatitude());
                        longitude = formatGPSCoordinate(
                                ndtLocation.gpsLastKnownLocation.getLongitude());
                    } else {
                        if ((ndtLocation.networkLastKnownLocation != null)
                                && (ndtLocation.networkLastKnownLocation.getLatitude() != 0)
                                && (ndtLocation.networkLastKnownLocation.getLongitude() != 0)) {
                            latitude = formatGPSCoordinate(
                                    ndtLocation.networkLastKnownLocation.getLatitude());
                            longitude = formatGPSCoordinate(
                                    ndtLocation.networkLastKnownLocation.getLongitude());
                        }
                    }
                }
            }
            history = new History(newDate, upload, download, latency, jitter, networkType,
                    connectionType, latitude, longitude, mos, videoCalc, videoConference,
                    videoVoip);
            Log.i(getClass().getName(), latitude + " " + longitude + "lat and long");
        } catch (Exception e) {
            Log.e(getClass().getName(), "Unable to save history: " + e.getMessage());
        }
        return (history);
    }


    private void setupResultsObjects() {
        pingStatsEast = new ProcessPing("Latency", uiServices);
        pingStatsWest = new ProcessPing("Latency", uiServices);
        tcpResultsWest = new ProcessIperf[Constants.NUM_TCP_TESTS_PER_SERVER];
        tcpResultsEast = new ProcessIperf[Constants.NUM_TCP_TESTS_PER_SERVER];
        udpResultsWest = new ProcessIperf[Constants.NUM_UDP_TESTS_PER_SERVER];
        udpResultsEast = new ProcessIperf[Constants.NUM_UDP_TESTS_PER_SERVER];
        westUdpOneSecConfig = new UdpTestConfig(applicationFilesDir, server1, UDPPort,
                Constants.DEFAULT_UDP_LENGTH, Constants.DEFAULT_UDP_BANDWIDTH, 1);
        eastUdpOneSecConfig = new UdpTestConfig(applicationFilesDir, server2, UDPPort,
                Constants.DEFAULT_UDP_LENGTH, Constants.DEFAULT_UDP_BANDWIDTH, 1);
        for (int i = 0; i < Constants.NUM_UDP_TESTS_PER_SERVER; i++) {
            udpResultsWest[i] = new ProcessIperf(uiServices, (UdpTestConfig) westUdpOneSecConfig);
            udpResultsEast[i] = new ProcessIperf(uiServices, (UdpTestConfig) eastUdpOneSecConfig);
        }
    }

    private void printToSummary(String message) {
        summary.append(message);
    }

    private void showResults(String message) {
        results.append(message);
        textResults += message;
    }

    private static void waiting(int n) {
        long t0, t1;
        t0 = System.currentTimeMillis();
        do {
            t1 = System.currentTimeMillis();
        } while ((t1 - t0) < (n * 1000));
    }

    /**
     * Video Metric Calculations and relevant methods.
     * method to send commandOutput to for parsing.
     */
    private void parseOutput(String commandOutput, String server) {
        this.threadCounter = 0;
        List<String> outputLines = Arrays.asList(commandOutput.split("\n"));
        /* need split the command output into separate lines of text
        *  get rid of white space
		*  then can use calculation method on them
		*/
        if (!this.isPrelimTest) {
            for (int k = 0; k < outputLines.size(); k++) {
                addLineToMetric(outputLines.get(k), server);
            }
            Collections.sort(allMetrics, new MetricComparator());
            for (Integer thread : threadSet) {
                metricsByThread.put(thread, new ArrayList<Metric>());
            }
        }
    }


    /**
     * Put all the output into a List. This method filters through each line
     * and finds the thread number, speed, and time range. Given how many
     * times the thread number appears, the direction changes from up to down
     * and vice versa.
     * This skips other lines and lines that contain [SUM]
     *
     * @param line   A line from the raw data output
     * @param server Either the East or West server passed from the runCommand method
     */
    private void addLineToMetric(String line, String server) {
        List<String> elements = Arrays.asList(line.split("\\s+"));
        int speed, time, threadID;
        if ((elements.size() >= 8) && elements.contains("[")) {
            threadID = elements.indexOf("[") + 1;
            Integer validThreadID = Integer.valueOf(elements.get(threadID).replace("]", ""));
            if (elements.contains("local") && elements.contains("port")) {
                Log.d("ChangeDirection", "calling changing direction for = " + line);
                changeDirection(validThreadID);
            }
            if (elements.contains("Kbits/sec")) {
                speed = elements.indexOf("Kbits/sec") - 1; //find index before speed measurement
                time = elements.indexOf("sec") - 1; //find index before "sec"
                String timeValue = elements.get(time);
                String timeRangeFirst, timeRangeSecond;
                if (timeValue.contains("-")) {
                    timeRangeFirst = timeValue.split("-")[0];
                    timeRangeSecond = timeValue.split("-")[1];
                } else {
                    timeRangeFirst = elements.get(elements.indexOf("sec") - 2).replace("-", "");
                    timeRangeSecond = timeValue;
                }
                if (speed >= 0 && time >= 0
                        && elements.indexOf("[SUM]") == -1
                        && isValidTime(timeRangeFirst, timeRangeSecond)) {
                    String validSpeed = elements.get(speed);
                    String validTime = formatTimeForMetric(elements.get(time));
                    Metric validMetric = new Metric(validThreadID, Double.valueOf(validSpeed),
                            validTime, threadDirection.get(validThreadID), server);
                    threadSet.add(validThreadID);
                    allMetrics.add(validMetric);
                }
            }
        }
    }

    private void changeDirection(Integer threadNumber) {
        /*
         * This is a tricky bit to calculate. The iperf output doesn't give upload or download
          * information. It generally goes upload first, then download.
          * The way we determine this is to look at the
          *     [THREAD NUM] local 127.0.0.1 port XXXXXX connected with 127.0.0.1 port XXXX
          * line to calculate when iperf has switched from upload to download.
          * However, there same threads aren't used for upload, download, east, and west,
          * and so the difficulty is to know globally whether the test is on upload or download
          *
          * First, if this the first time this thread number is being used, it needs to know
          * what the threadCounter value is. If it less than 5, then it means the test is
          * still on upload. If it is greater than or equal to 5, but less than 9, then it means
          * the test is in download. If it is greater than 9, it means we have moved on to the
          * East test, and so we need to reset threadCounter because the first test is upload.
          *
          * Secondly, if the thread has already been used. We just flip UP to DOWN and DOWN to UP.
          * There isn't any reason why after seeing the local 127.0.0.1 line that it would be
          * on the same test.
         */
        Log.v("ChangeDirection", "ThreadCounter is " + this.threadCounter + "| Thread number: " +
                threadNumber);
        Log.d("ChangeDirection",
                "threadCounter=" + this.threadCounter + "; tcpThreadNum=" + tcpThreadNumber);
        if (this.threadCounter < tcpThreadNumber) {
            Log.d("ChangeDirection", "putting UP for thread: " + threadNumber);
            threadDirection.put(threadNumber, UP);
        } else if (this.threadCounter < (2 * tcpThreadNumber)) {
            Log.d("ChangeDirection", "putting DOWN for thread: " + threadNumber);
            threadDirection.put(threadNumber, DOWN);
        } else {
            Log.d("ChangeDirection", "putting UP for thread: " + threadNumber);
            threadDirection.put(threadNumber, UP);
            Log.v("ChangeDirection", "Resetting threadcounter");
            this.threadCounter = -1;
        }
        this.threadCounter++;
    }

    private void calculateSpeed() {
        for (Integer thread : threadDirection.keySet()) {
            Log.d("Metrics", "thread=" + thread + "; direction=" + threadDirection.get(thread));
        }
        for (Metric metric : allMetrics) {
            Log.v("MetricsDEBUG", metric.toString());
            Map<String, Double> mapToUpdate = null;
            if (metric.getServer().equals(WEST)) {
                if (metric.getDirection().equals(UP)) {
                    mapToUpdate = WestUp;
                } else if (metric.getDirection().equals(DOWN)) {
                    mapToUpdate = WestDown;
                }
            } else if (metric.getServer().equals(EAST)) {
                if (metric.getDirection().equals(UP)) {
                    mapToUpdate = EastUp;
                } else if (metric.getDirection().equals(DOWN)) {
                    mapToUpdate = EastDown;
                }
            }
            if (mapToUpdate == null) {
                Log.e("CalculateSpeed", "Didn't find a valid metric: " + metric.toString());
                throw new NullPointerException();
            }
            String time = metric.getTimeRange();
            if (Double.parseDouble(time) < 11.0) {
                Double speed = metric.getSpeed();
                Double oldSpeed = mapToUpdate.get(time);
                if (oldSpeed == null) {
                    oldSpeed = 0.0;
                }
                mapToUpdate.put(time, oldSpeed + speed);

                ArrayList<Metric> valuesForThread = metricsByThread.get(metric.getThreadID());
                if (valuesForThread != null)
                    valuesForThread.add(metric);
            }
        }
    }

    private boolean isValidTime(String timeOne, String timeTwo) {
        Log.v("ValidatingTime", "Comparing times: " + timeOne + " and " + timeTwo);
        if (timeOne == null || timeTwo == null) {
            Log.e("ValidatingTime", "Got invalid times: " + timeOne + ", " + timeTwo);
            return false;
        } else {
            if ((timeTwo.length() == 3) && timeTwo.contains("0") && timeOne.contains("0")) {
                return true;
            } else {
                Double firstTime = Double.valueOf(timeOne);
                Double secondTime = Double.valueOf(timeTwo);
                return secondTime - firstTime == 1.0;
            }
        }
    }

    private String formatTimeForMetric(String rawTime) {
        if (rawTime.contains("-")) {
            return rawTime.split("-")[1];
        } else {
            return rawTime;
        }
    }


    static void resetScores() {
        vid_summary = null;
        vid_voip_summary = null;
        vid_conference_summary = null;
        Map<String, Double> mp = WestUp;
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            mp.put((String) pair.getKey(), 0.0);
            it.remove(); // avoids a ConcurrentModificationException
        }
        mp = WestDown;
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            mp.put((String) pair.getKey(), 0.0);
            it.remove(); // avoids a ConcurrentModificationException
        }
        mp = EastUp;
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            mp.put((String) pair.getKey(), 0.0);
            it.remove(); // avoids a ConcurrentModificationException
        }
        mp = EastDown;
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            mp.put((String) pair.getKey(), 0.0);
            it.remove(); // avoids a ConcurrentModificationException
        }

    }

    private static String[] calcVideo(String type) {
        switch (type) {
            case STREAMING:
                return calcVideo();
            case CONFERENCE:
                return calcConference();
            case VOIP:
                return calcVoip();
            default:
                return new String[]{"", "", ""};
        }
    }


    private static String[] calcVideo() {
        int downHD, downSD, downLS, upHD, upSD, upLS;
        downHD = downSD = downLS = upHD = upSD = upLS = 0;
        vid_summary = new String[3];
        Map mp = WestDown;
        Map<String, Double> westDownCopy = new HashMap<>();
        westDownCopy.putAll(WestDown);
        String westDownRaw = mapToString(westDownCopy);
        Log.d("VideoDEBUG", "West Down: " + westDownRaw);

        Iterator it = mp.entrySet().iterator();
        int counter = 0;
        while (it.hasNext()) {
            if (counter == 10) {
                break;
            }
            Map.Entry pair = (Map.Entry) it.next();
            if ((Double) pair.getValue() > 2500) {
                downHD++;
            } else if ((Double) pair.getValue() > 700)
                downSD++;
            else
                downLS++;
            counter++;
            it.remove(); // avoids a ConcurrentModificationException
        }

        mp = WestUp;
        Map<String, Double> westUpCopy = new HashMap<>();
        westUpCopy.putAll(WestUp);
        String westUpRaw = mapToString(westUpCopy);
        Log.d("VideoDEBUG", "West Up: " + westUpRaw);

        it = mp.entrySet().iterator();
        counter = 0;
        while (it.hasNext()) {
            if (counter == 10) {
                break;
            }
            Map.Entry pair = (Map.Entry) it.next();
            if ((Double) pair.getValue() > 2500)
                upHD++;
            else if ((Double) pair.getValue() > 700)
                upSD++;
            else
                upLS++;
            counter++;
            it.remove(); // avoids a ConcurrentModificationException
        }

        String videoDetails = "West [Down] HD: " + downHD
                + ", SD: " + downSD
                + ", LS: " + downLS;
        vid_summary[0] = videoDetails;
        vid_summary[1] = "West [Up] HD: " + upHD
                + ", SD: " + upSD
                + ", LS : " + upLS;
        Log.d("TestStreaming", "Download | HD: " + downHD + ", SD: " + downSD + ", LD: " + downLS);
        if (mosValue == null || mosValue <= 0) {
            Log.d("TestStreaming", "mos null or zero");
            vid_summary[2] = "N/A";
        } else if (!hasTenValues(WEST, DOWN)) {
            Log.d("TestStreaming", "not ten values");
            vid_summary[2] = "N/A";
        } else if (downHD >= 9) {
            //return "High Definition";
            vid_summary[2] = "HD";
        } else if ((downHD + downSD) >= 9) {
            //return "Standard Definition";
            vid_summary[2] = "SD";
        } else if (((downHD + downSD + downLS) >= 10)) {
            //return "Low Service";
            vid_summary[2] = "LD";
        } else {
            Log.d("TestStreaming", "ELSE?!?");
            vid_summary[2] = "N/A";
        }
        Log.d("TestStreaming", "Final Result is: " + vid_summary[2]);
        return vid_summary;
    }

    private static String[] calcConference() {
        int upHD, upSD, upLS, downHD, downSD, downLS;
        upHD = upSD = upLS = downHD = downSD = downLS = 0;
        vid_conference_summary = new String[3];
        Map<String, Double> mp = EastUp;
        Map<String, Double> eastUpCopy = new HashMap<>();
        eastUpCopy.putAll(EastUp);
        String eastUpRaw = mapToString(eastUpCopy);
        Log.d("VideoDEBUG", "East Up: " + eastUpRaw);

        Iterator it = mp.entrySet().iterator();
        int counter = 0;
        while (it.hasNext()) {
            if (counter == 10) {
                break;
            }
            Map.Entry pair = (Map.Entry) it.next();
            if ((Double) pair.getValue() > 2500)
                upHD++;
            else if ((Double) pair.getValue() > 700)
                upSD++;
            else
                upLS++;
            counter++;
            it.remove(); // avoids a ConcurrentModificationException
        }

        mp = EastDown;
        Map<String, Double> eastDownCopy = new HashMap<>();
        eastDownCopy.putAll(EastDown);
        String eastDownRaw = mapToString(eastDownCopy);
        Log.d("VideoDEBUG", "East Down: " + eastDownRaw);

        it = mp.entrySet().iterator();
        counter = 0;
        while (it.hasNext()) {
            if (counter == 10) {
                break;
            }
            Map.Entry pair = (Map.Entry) it.next();
            if ((Double) pair.getValue() > 2500)
                downHD++;
            else if ((Double) pair.getValue() > 700)
                downSD++;
            else
                downLS++;
            counter++;
            it.remove(); // avoids a ConcurrentModificationException
        }

        String conferenceDetails = "East [Up] HD: " + upHD
                + ", SD: " + upSD
                + ", LS: " + upLS;
        vid_conference_summary[0] = conferenceDetails;
        vid_conference_summary[1] = "East [Down] HD: " + downHD
                + ", SD: " + downSD
                + ", LS: " + downLS;
        Log.d("TestConference", "Download | HD: " + downHD + ", SD: " + downSD + ", LD: "
                + downLS + "\nUpload | HD: " + upHD + ", SD: " + upSD + ", LD: " + upLS);
        if (mosValue == null || mosValue <= 0) {
            vid_conference_summary[2] = "N/A";
        } else if (!hasTenValues(EAST, DOWN) && !hasTenValues(EAST, UP)) {
            vid_conference_summary[2] = "N/A";
        } else if (mosValue < 4.0) {
            vid_conference_summary[2] = "LD";
        } else if (downHD >= 9 && upHD >= 9) {
            vid_conference_summary[2] = "HD";
        } else if (upHD + upSD >= 9 && downHD + downSD >= 9) {
            vid_conference_summary[2] = "SD";
        } else if (upHD + upSD + upLS >= 10 && downHD + downSD + downLS >= 10) {
            vid_conference_summary[2] = "LD";
        } else {
            vid_conference_summary[2] = "N/A";
        }
        Log.d("TestConference", "Final Result is: " + vid_conference_summary[2]);
        return vid_conference_summary;
    }

    /**
     * Translate the Voice over IP value based on the MOS value from the East server.
     *
     * @return String based on the MOS value range
     */
    private static String[] calcVoip() {
        if (mosValueEast == null || mosValueEast <= 0.0) {
            vid_voip_summary = new String[]{"", "", "N/A"};
        } else if (mosValueEast < 3.0) {
            vid_voip_summary = new String[]{"", "", "Poor"};
        } else if (mosValueEast < 4.0) {
            vid_voip_summary = new String[]{"", "", "Fair"};
        } else {
            vid_voip_summary = new String[]{"", "", "Good"};
        }
        Log.d("TestVoIP", "Final Result is: " + vid_voip_summary[2] + " with MOS: "
                + mosValueEast);
        return vid_voip_summary;
    }

    private static boolean hasTenValues(String server, String direction) {
        Log.d("TenValues", "looking for server: " + server + ", direction: " + direction);
        HashSet<Integer> threadsWithTen = new HashSet<>();
        for (Map.Entry<Integer, ArrayList<Metric>> entry : metricsByThread.entrySet()) {
            ArrayList<Metric> matchingMetrics = new ArrayList<>();
            for (Metric metric : entry.getValue()) {
                if ((metric.getServer()).equals(server)
                        && metric.getDirection().equals(direction)) {
                    Log.d("TenValues", "found: " + metric.getThreadID() + ", server: "
                            + metric.getServer() + ", direction: " + metric.getDirection());
                    matchingMetrics.add(metric);
                }
            }
            Log.v("TenValues", "Thread: " + entry.getKey() + " | Size: " + matchingMetrics.size());
            if (matchingMetrics.size() >= 10) {
                threadsWithTen.add(entry.getKey());
            } else {
                Log.i("TenValues", "Found an entry less than 10 | Thread: " + entry.getKey()
                        + " Size: " + matchingMetrics.size());
                Log.d("TenValues", String.valueOf(Collections.singletonList(matchingMetrics)));
            }
        }
        Log.v("hasTenValue", "Size of threads with 10 values: " + threadsWithTen.size());
        return threadsWithTen.size() == tcpThreadNumber;
    }

    private static String mapToString(Map mp) {
        Iterator it = mp.entrySet().iterator();
        String stringMap = "[ ";
        while (it.hasNext()) {
            Map.Entry keyValue = (Map.Entry) it.next();
            stringMap += keyValue.getKey() + " : ";
            if ((Double) keyValue.getValue() > 2500)
                stringMap += keyValue.getValue() + " : HD; ";
            else if ((Double) keyValue.getValue() > 700)
                stringMap += keyValue.getValue() + " : SD; ";
            else {
                stringMap += keyValue.getValue() + " : LD; ";
            }
            it.remove();
        }
        stringMap += "]";
        return stringMap;
    }
}
