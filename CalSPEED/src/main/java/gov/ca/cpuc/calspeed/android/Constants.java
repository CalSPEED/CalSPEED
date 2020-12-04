/* Original work: Copyright 2009 Google Inc. All Rights Reserved.
 
   Modified work: The original source code (AndroidNdt.java) comes from the NDT Android app
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

/**
 * Definition for constant values .
 */
public class Constants {
    /*
     * Account information
     */
    static final String USERNAME = BuildConfig.SFTP_USERNAME;
    static final String SFTP_PASSWORD = BuildConfig.SFTP_PASSWORD;
    static final String DATASERVER = BuildConfig.UPLOAD_SERVER;
    static final int DEFAULT_SERVER = 0;
    static final String[] SERVER_NAME = {"N. California Server", "N. Virginia Server"};
    static final String[] SERVER_HOST = BuildConfig.SERVER_HOST;
    static final String[] ports = {};

    /* SMOOTHING NUMBER BETWEEN 0 AND 1 FOR LOW PASS FILTERING
     *
	 */
    static final float SMOOTH = 0.15f;

    /* Preference static variables */
    static final int MODE_PRIVATE = 0;

    /*
     * Maximum test steps for ProgressBar setting.
     */
    static final int TEST_STEPS = 20;
    static final String privacyPolicyURL = "http://www.cpuc.ca.gov/General.aspx?id=1778";
    static final float METERS_TO_FEET = 3.28084f;

    /*
     * Timeouts
     */
    static final int IPERF_TCP_TIMEOUT = 90000;
    static final int IPERF_UDP_TIMEOUT = 60000;
    static final int PING_TIMEOUT = 30000;

    /*
     * Integer code for communicating to CalspeedFragment
     */
    static final int THREAD_MAIN_APPEND = 0;
    static final int THREAD_STAT_APPEND = 1;
    static final int THREAD_BEGIN_TEST = 2;
    static final int THREAD_END_TEST = 3;
    static final int THREAD_ADD_PROGRESS = 4;
    static final int THREAD_SUMMARY_APPEND = 5;
    static final int THREAD_LAT_LONG_APPEND = 6;
    static final int THREAD_COMPLETE_PROGRESS = 7;
    static final int THREAD_CLEAR_PROCESS_HANDLE = 8;
    static final int THREAD_GOOD_GPS_SIGNAL = 9;
    static final int THREAD_NO_GPS_SIGNAL = 10;
    static final int THREAD_UPDATE_LATLONG = 11;
    static final int THREAD_RESULTS_SAVED = 12;
    static final int THREAD_RESULTS_NOT_SAVED = 13;
    static final int THREAD_RESULTS_UPLOADED = 14;
    static final int THREAD_RESULTS_NOT_UPLOADED = 15;
    static final int THREAD_RESULTS_ATTEMP_UPLOAD = 16;
    static final int THREAD_ADD_TO_RESULTS_LIST = 17;
    static final int THREAD_UPDATE_RESULTS_LIST = 18;
    static final int THREAD_TEST_TIMED_OUT = 19;
    static final int THREAD_TEST_INTERRUPTED = 20;
    static final int THREAD_SET_STATUS_TEXT = 21;
    static final int THREAD_PRINT_BSSID_SSID = 22;
    static final int THREAD_NO_MOBILE_CONNECTION = 23;
    static final int THREAD_GOT_MOBILE_CONNECTION = 24;
    static final int THREAD_START_ANIMATION = 25;
    static final int THREAD_STOP_ANIMATION = 26;
    static final int THREAD_WRITE_UPLOAD_DATA = 27;
    static final int THREAD_WRITE_DOWNLOAD_DATA = 28;
    static final int THREAD_WRITE_LATENCY_DATA = 29;
    static final int THREAD_WRITE_JITTER_DATA = 30;
    static final int THREAD_CONNECTIVITY_FAIL = 31;
    static final int FINISH_PHASE_1 = 32;
    static final int THREAD_START_UPLOAD_TIMER = 33;
    static final int THREAD_STOP_UPLOAD_TIMER = 34;
    static final int THREAD_START_DOWNLOAD_TIMER = 35;
    static final int THREAD_STOP_DOWNLOAD_TIMER = 36;
    static final int THREAD_UPDATE_DOWNLOAD_NUMBER = 37;
    static final int THREAD_UPDATE_UPLOAD_NUMBER = 38;
    static final int THREAD_SET_DOWNLOAD_NUMBER = 39;
    static final int THREAD_SET_DOWNLOAD_NUMBER_STOP_TIMER = 40;
    static final int THREAD_SET_UPLOAD_NUMBER = 41;
    static final int THREAD_SET_UPLOAD_NUMBER_STOP_TIMER = 42;
    static final int THREAD_WRITE_MOS_DATA = 43;
    static final int THREAD_SET_MOS_VALUE = 44;
    static final int THREAD_UPDATE_NETWORK_INFO = 45;
    static final int START_PRELIM = 46;
    static final int FINISH_PRELIM = 47;

    static final Integer NUM_UDP_TESTS_PER_SERVER = 1;
    static final Integer NUM_TCP_TESTS_PER_SERVER = 1;
    static final Double IPERF_BIG_NUMBER_ERROR = 9999999999.99; //iperf error puts big number in kbytes/sec data

    static final String THREAD_NUMBER = "threadNumber";
    static final String WINDOW_SIZE = "windowSize";
    static final String TEST_TIME = "testTime";

    static final Integer DEFAULT_THREAD_NUMBER = 1;
    static final String DEFAULT_WINDOW_SIZE = "256";
    static final Integer DEFAULT_UDP_LENGTH = 220;
    static final String DEFAULT_UDP_BANDWIDTH = "88k";
    static final String DEFAULT_WINDOW_SIZE_FORMAT = "k";
    static final Integer DEFAULT_TEST_INTERVAL = 1;
    static final Integer DEFAULT_TEST_TIME = 20;
    static final String PRELIM_WINDOW_SIZE = "64k";
    static final Integer PRELIM_THREAD_NUMBER = 1;
    static final Integer PRELIM_TEST_TIME = 10;
    static final int IPERF_PRELIM_TCP_TIMEOUT = 40000;


    /*
     * Test Strings
     */
    static final String CONNECTIVITY_FAIL = "\nConnectivity Test Failed--Exiting Test.\n";
    static final String CONNECTIVITY_CHECK = "\nChecking Connectivity.....\n";
    static final String TEST_ONE_TCP_WEST = "\nStarting Test 1: Iperf TCP West....\n";
    static final String TEST_TWO_PING_WEST = "\nStarting Test 2: Ping West....\n";
    static final String TEST_THREE_UDP_WEST =
            "\nStarting Test 3: Iperf West UDP 1 second test....\n";
    static final String TEST_FOUR_TCP_EAST = "\nStarting Test 4: Iperf TCP East....\n";
    static final String TEST_FIVE_PING_EAST = "\nStarting Test 5: Ping East....\n";
    static final String TEST_SIX_UDP_WEST =
            "\nStarting Test 6: Iperf East UDP 1 second test....\n";
    static final String TEST_CALIFORNIA = "Testing with California server...";
    static final String TEST_VIRGINIA = "Testing with Virginia server...";
    static final String TEST_PRELIM = "Preliminary Testing...";

    /*
     * Debug variables
     */
    public static final boolean DEBUG = false;
    static final boolean UploadDebug = false;
    static final boolean DownloadDebug = false;
    static final Boolean DEBUG_MOS = false;
    static final double RAMP_UP_FACTOR = 0.6;

    /*
     * Test Config Command variables
     */
    static String IPERF_VERSION = "/iperfN";
    static String UDP_COMMAND = "%3$s -c %1$s -u -l %4$d -b %5$s -i %8$d -t %7$d -f %6$s -p %2$s";
    static String TCP_COMMAND = "%3$s -c %1$s -e -w %4$s -P %5$d -i %8$d -t %7$d -f %6$s -p %2$s";
    static String PING_COMMAND = "ping -c %2$s %1$s";

    static final String FAILED_TCP_LINE = "TCP Test Failed";
    static final String FAILED_UDP_LINE = "UDP Test Failed";
    static final String FAILED_PING_LINE = "Ping Test Failed";
    static final String PING_100_PERCENT = "100% packet loss";
    static final int PING_100_PERCENT_LOSS = 3;
    static final int PING_CONNECTIVITY_FAIL = 4;

    static final int RUN_COMMAND_SUCCESS = 0;
    static final int RUN_COMMAND_FAIL = 1;
    static final int RUN_COMMAND_INTERRUPT = 2;

    static final String UDP = "UDP";
    static final String TCP = "TCP";
    static final String PING = "PING";

    // Network type from call TelephonyManager.getNetworkType()
    static final String[][] NETWORK_TYPE = {
            {"7", "1xRTT"},
            {"4", "CDMA"},
            {"2", "EDGE"},
            {"14", "EHRPD"},
            {"5", "EVDO REV 0"},
            {"6", "EVDO REV A"},
            {"12", "EVDO REV B"},
            {"1", "GPRS"},
            {"8", "HSDPA"},
            {"10", "HSPA"},
            {"15", "HSPA+"},
            {"9", "HSUPA"},
            {"11", "IDEN"},
            {"13", "LTE"},
            {"3", "UMTS"},
            {"0", "UNKNOWN"},
            {"16", "GSM"},
            {"17", "IWLAN"},
            {"18", "TD_SCDMA"},
            {"20", "NR_5G"}
    };

    //Network (physical layer) types
    static final String NETWORK_WIFI = "WIFI";
    static final String NETWORK_MOBILE = "MOBILE";
    static final String NETWORK_UNKNOWN = "UNKNOWN";
    static final String WIFI = "WIFI";
    static final String MOBILE = "MOBILE";

    /*
     * URL for reverse geocoding and arcGIS data gathering
     */
    static final String ARCGIS_GEOMETRYSERVER = "";
    static final int IN_SECRET = 0;
    static final int OUT_SECRET = 0;
    static final String ARCGIS_MAPSERVER = "";


    private Constants() {
    }
}
