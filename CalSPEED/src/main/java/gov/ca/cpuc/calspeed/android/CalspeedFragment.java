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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


/**
 * UI Thread and Entry Point of mobile client.
 */
public class CalspeedFragment extends SherlockFragment {
    private String statistics;
    private Button buttonStandardTest;
    private ProgressBar progressBar;
    private TextView topText;
    private LinearLayout mosResults;
    private TextView textViewMain;
    private PowerManager.WakeLock wakeLock;
    private NetworkInfo activeNetwork;
    private AndroidUiServices uiServices;
    private NetworkAndDeviceInformation ndi;
    private NdtLocation ndtLocation;
    private static Location lastKnownLocation;
    private AssetManager assetManager;
    private String applicationFilesDir;
    private ConnectivityManager connectivityManager;
    private TelephonyManager telephonyManager;
    private String connectionType;
    private Date date;
    private Double startLatitude;
    private Double startLongitude;
    private Context context;
    Boolean usingUploadButton = false;
    private LatLong myLatLong;
    private NetworkLatLong networkLatLong;
    private String Provider;
    private String TCPPort;
    private String UDPPort;
    private TextView uploadText;
    private TextView uploadNum;
    private TextView uploadUnits;
    private TextView downloadText;
    private TextView downloadNum;
    private TextView downloadUnits;
    private TextView latencyText;
    private TextView latencyNum;
    private TextView latencyUnits;
    private ImageView latencyIcon;
    private TextView jitterText;
    private TextView jitterNum;
    private TextView jitterUnits;
    private ImageView jitterIcon;
    private TextView mosText;
    private TextView mosNum;
    private View resultsView;
    private Animation slideOut;
    private Animation slideIn;
    private Float smoothUpload;
    private Float smoothDownload;
    private Timer uploadTimer;
    private Timer downloadTimer;
    private TimerTask uploadTask;
    private TimerTask downloadTask;
    private Thread testThread;
    private HistoryDatabaseHandler db;
    //variables for Video Metric Calculation
    private StandardTest currentTest;
    private int sum = 0;

    public CalspeedFragment() {
    }

    /**
     * Initializes the activity.
     */

    @Override
    public void onStart() {
        Log.v(this.getClass().getName(), "at onStart()");
        Log.i("SystemBuildInfo", String.valueOf(Build.VERSION.SDK_INT));
        if (context == null) { // check to see if still active fragment
            Log.v(this.getClass().getName(), "context is null, calling setupAll()");
            setupAll();
        } else {
            Log.e(this.getClass().getName(), "context is not null and fragment is active");
            try {
                Log.i(this.getClass().getName(), "trying to create context?");
                String packageName = getActivity().getApplicationContext().getPackageName();
                context = getActivity().createPackageContext(packageName, 0);
                Prefs.resetGPSoverride(context);
            } catch (Exception e) {
                Log.e(getClass().getName(), "unable to set context OnCreate");
            }
        }
        initComponents();
        db = new HistoryDatabaseHandler(getActivity());
        super.onStart();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            ActionBar actionBar = ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);

            // If we are becoming invisible, then...
            if (!isVisibleToUser) {
                Log.d(this.getClass().getName(), "Not visible to user");
                // test to map page, set search bar visible
                Log.d(this.getClass().getName(),
                        String.format("setUserVisibleHint(): Navigation index: %d",
                                actionBar.getSelectedNavigationIndex()));
                if (actionBar.getSelectedNavigationIndex() == 2) {
                    actionBar.setCustomView(R.layout.actionbar_for_viewer);
                    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                            | ActionBar.DISPLAY_SHOW_HOME);
                }
            }
        }
    }

    void startWakeLock() {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    void stopWakeLock() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void setupAll() {
        Log.i(getClass().getName(), "setupAll() - getActivity() - " + getActivity());
        SharedPreferences legal = getActivity().getSharedPreferences("Legal", Context.MODE_PRIVATE);
        if (!(legal.getBoolean("privacyPolicyAccepted", false))) {
            createPrivacyPolicyAlert();
        }
        try {
            String packageName = getActivity().getApplicationContext().getPackageName();
            context = getActivity().createPackageContext(packageName, 0);
            Prefs.resetGPSoverride(context);
        } catch (Exception e) {
            Log.e(getClass().getName(), "unable to set context OnCreate");
        }

        // Set the default server
        int serverNumber = Constants.DEFAULT_SERVER;
        String serverName = Constants.SERVER_NAME[serverNumber];
        PowerManager powerManager =
                (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        this.connectivityManager = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();

        String WAKELOCK_TAG = "CalSPEED Testing";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wakeLock = powerManager.newWakeLock(
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WAKELOCK_TAG);
        } else {
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, WAKELOCK_TAG);
        }
        UiHandler uiHandler = new UiHandler(Looper.myLooper());
        uiServices = new AndroidUiServices(getActivity(), uiHandler);
        assetManager = getActivity().getAssets();
        setupLocationListeners();
        applicationFilesDir = getApplicationFilesDir();
        setupIperf();
        textViewMain = getActivity().findViewById(R.id.TextViewMain);
        textViewMain.setMovementMethod(ScrollingMovementMethod.getInstance());
        textViewMain.setClickable(false);
        textViewMain.setLongClickable(false);
        textViewMain.append(getString(R.string.nonofficial) + BuildConfig.VERSION_NAME + "\n");
        uiServices.appendString(getString(R.string.nonofficial) + BuildConfig.VERSION_NAME + "\n",
                UiServices.STAT_VIEW);
        textViewMain.append("\n" + getString(R.string.default_server_indicator, serverName));
        textViewMain.append("\n");
        date = new Date();
        textViewMain.append(date.toString() + "\n");
        statistics = "";
        ndtLocation.addGPSStatusListener();
        startGPS();
        setupUploadTimer();
        setupDownloadTimer();
        this.startLatitude = 0.0;
        this.startLongitude = 0.0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        if (ndtLocation != null) {
            ndtLocation.stopListen();
            ndtLocation.removeGPSStatusListener();
            ndtLocation.stopNetworkListenerUpdates();
        }
        if (db != null) {
            db.close();
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            Log.d("WAKELOCK", "release wakeLock");
            wakeLock.release();
            stopWakeLock();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        ndtLocation.stopListen();
        ndtLocation.stopNetworkListenerUpdates();
        if (db != null) {
            db.close();
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            Log.d("WAKELOCK", "release wakeLock");
            wakeLock.release();
            stopWakeLock();
        }
        super.onPause();
    }

    /**
     * {@inheritDoc}
     */

    public static Bundle createBundle(String title) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        return bundle;
    }

    @Override
    public void onResume() {
        ndtLocation.startListen();
        ndtLocation.startNetworkListenerUpdates();
        if (wakeLock != null && !wakeLock.isHeld() && testThread != null) {
            if (testThread.isAlive()) {
                Log.d("WAKELOCK", "acquire wakeLock");
                wakeLock.acquire();
                startWakeLock();
            }
        }
        Log.v(this.getClass().getName(), "called from onResume()");
        super.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        ndtLocation.stopListen();
        ndtLocation.stopNetworkListenerUpdates();
        if (db != null) {
            db.close();
        }
        Log.v("debug", "onStop");
        super.onStop();
    }

    /**
     * Initializes the components on main view.
     */
    private void initComponents() {

        buttonStandardTest = getActivity().findViewById(R.id.ButtonStandardTest);
        buttonStandardTest.setOnClickListener(new StandardTestButtonListener());

        progressBar = getActivity().findViewById(R.id.ProgressBar);
        progressBar.setIndeterminate(false);

        topText = getActivity().findViewById(R.id.topText);
        mosResults = getActivity().findViewById(R.id.mosResults);

        uploadText = getActivity().findViewById(R.id.uploadLabel);
        uploadNum = getActivity().findViewById(R.id.uploadSpeed);
        uploadUnits = getActivity().findViewById(R.id.uploadUnits);

        downloadText = getActivity().findViewById(R.id.downloadLabel);
        downloadNum = getActivity().findViewById(R.id.downloadSpeed);
        downloadUnits = getActivity().findViewById(R.id.downloadUnits);

        latencyText = getActivity().findViewById(R.id.latencyLabel);
        latencyNum = getActivity().findViewById(R.id.latencySpeed);
        latencyUnits = getActivity().findViewById(R.id.latencyUnits);
        latencyIcon = getActivity().findViewById(R.id.latencyIcon);

        jitterText = getActivity().findViewById(R.id.jitterLabel);
        jitterNum = getActivity().findViewById(R.id.jitterSpeed);
        jitterUnits = getActivity().findViewById(R.id.jitterUnits);
        jitterIcon = getActivity().findViewById(R.id.jitterIcon);

        mosText = getActivity().findViewById(R.id.mosLabel);
        mosNum = getActivity().findViewById(R.id.mosGrade);

        resultsView = getActivity().findViewById(R.id.testResults);
        slideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slideout);
        slideOut.setAnimationListener(new SlideOutAnimationListener());
        slideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slidein);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(this.getClass().getName(), "entering into onCreateView()");
        return inflater.inflate(R.layout.tester_fragment, container, false);
    }

    private void setupLocationListeners() {
        Log.d("CalSPEEDLocation", "setupLocationListeners()");
        networkLatLong = new NetworkLatLong();
        myLatLong = new LatLong();
        Handler locationHandler = new Handler(Looper.myLooper());
        Log.i("CalSPEEDLocation", "setupAll() - getActivity() - " + getActivity());
        if (context == null) {
            try {
                String packageName = getActivity().getApplicationContext().getPackageName();
                context = getActivity().createPackageContext(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("CalSPEEDLocation", e.getMessage());
            } catch (Exception e) {
                Log.e("CalSPEEDLocation", "Exception: " + e.getMessage());
            }
        }
        if (context != null) {
            Log.d("CalSPEEDLocation", "creating new NdtLocation");
            ndtLocation = new NdtLocation(context, locationHandler, networkLatLong, myLatLong);
            Log.d("CalSPEEDLocation", "finished creating new NdtLocation");
        }
    }

    private void getLastKnownLocationInfo() {
        Log.d("CalSPEEDLocation", "getLastKnownLocationInfo()");
        String gpsLocationProvider = LocationManager.GPS_PROVIDER;
        String networkLocationProvider = LocationManager.NETWORK_PROVIDER;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                Log.d("CalSPEEDLocation",
                        "getLastKnownLocationInfo(): DOES NOT HAVE location permissions");
            }
        }
        Log.d("CalSPEEDLocation", "getLastKnownLocationInfo(): has location permissions");
        ndtLocation.gpsLastKnownLocation =
                ndtLocation.locationManager.getLastKnownLocation(gpsLocationProvider);
        ndtLocation.networkLastKnownLocation =
                ndtLocation.locationManager.getLastKnownLocation(networkLocationProvider);
        if (ndtLocation.gpsLastKnownLocation != null) {
            Log.d("CalSPEEDLocation", "has last known GPS location");
            lastKnownLocation = ndtLocation.gpsLastKnownLocation;
            return;
        }
        if (ndtLocation.networkLastKnownLocation != null) {
            Log.d("CalSPEEDLocation", "has last known network location");
            lastKnownLocation = ndtLocation.networkLastKnownLocation;
        } else {
            lastKnownLocation = null;
        }
    }


    private void setupIperf() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            copyBinaryFile("android_iperf_2_0_2_3", "iperfT");
            Constants.IPERF_VERSION = "/iperfT";
        } else {
            copyBinaryFile("ndk_iperf", "iperfN");
            Constants.IPERF_VERSION = "/iperfN";
        }
        ExecCommandLine command = new ExecCommandLine("chmod 755 " + this.applicationFilesDir +
                Constants.IPERF_VERSION, 60000, null, null, null, uiServices);
        try {
            String output = command.runCommand();
            Log.d("SetupIperf", "Making iperf command executable: " + output);
        } catch (InterruptedException e) {
            Log.e(getClass().getName(),
                    "Unable to make " + Constants.IPERF_VERSION + " executable");
        }
        printAppDirectoryInfo();
    }

    private String getApplicationFilesDir() {
        File pathForAppFiles = getActivity().getFilesDir();
        return (pathForAppFiles.getAbsolutePath());
    }

    private void printAppDirectoryInfo() {
        File pathForAppFiles = getActivity().getFilesDir();
        if (Constants.DEBUG)
            Log.i("debug", "Listing Files in " + pathForAppFiles.getAbsolutePath());
        String[] fileList = pathForAppFiles.list();
        File[] fileptrs = pathForAppFiles.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (Constants.DEBUG)
                Log.i("debug", "Filename " + i + ": " + fileList[i] + " size: "
                        + fileptrs[i].length());
        }
    }

    public void copyBinaryFile(String inputFilename, String outputFilename) {
        try {
            InputStream inputFile = this.assetManager.open(inputFilename);
            FileOutputStream outputFile = getActivity().openFileOutput(outputFilename,
                    Context.MODE_PRIVATE);
            copy(inputFile, outputFile);
            inputFile.close();
            outputFile.flush();
            outputFile.close();
        } catch (IOException e) {
            if (Constants.DEBUG)
                Log.e("Asset File Error", e.getMessage());
        }
    }

    private static void copy(InputStream in, FileOutputStream out) throws IOException {
        byte[] b = new byte[4096];
        int read;
        try {
            while ((read = in.read(b)) != -1) {
                out.write(b, 0, read);
            }
        } catch (EOFException e) {
            Log.e("CalSPEED_copy", "EOF Exception caught: " + e.getMessage());
        } catch (Exception e) {
            Log.e("CalSPEED_copy", "Unable to copy. " + e.getMessage());
        }
    }

    private class StandardTestButtonListener implements OnClickListener {
        public void onClick(View view) {
            statistics = "";
            ndtLocation.addGPSStatusListener();
            ndtLocation.addNetworkListener();
            if (!ndtLocation.gpsEnabled && !ndtLocation.networkEnabled) {
                createGpsDisabledAlert();
            } else {
                ConnectivityManager connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                if (isNetworkActive()) {
                    WifiManager wifiManager = (WifiManager) getActivity()
                            .getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    boolean isWifi;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                        isWifi = activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                    } else {
                        Network activeNetwork = connectivityManager.getActiveNetwork();
                        usingUploadButton = false;
                        context = view.getContext();
                        NetworkCapabilities netCap =
                                connectivityManager.getNetworkCapabilities(activeNetwork);
                        isWifi = netCap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                    }
                    if (isWifi) {
                        createWifiAlert();
                    } else {
                        if (wifiManager.isWifiEnabled()) {
                            createDisableWifiAlert();
                        } else {
                            finishStartButton();
                        }
                    }
                } else {
                    finishStartButton();
                }
            }
        }
    }

    public Boolean isNetworkActive() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = null; // reset
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null;
    }

    public void finishApp() {
        ndtLocation.stopListen();
        ndtLocation.removeGPSStatusListener();
        ndtLocation.stopNetworkListenerUpdates();
        System.exit(0);
    }

    public void finishStartButton() {
        ((MainActivity) getActivity()).disableTabsWhileTesting();
        String gpsEnabled = "off";
        String networkLocationEnabled = "off";

        //resultsView.setBackgroundColor(Color.BLACK);
        ProgressBar loadingIcon = getActivity().findViewById(R.id.loadingIcon);
        loadingIcon.setVisibility(View.VISIBLE);
        ToggleButton indoorOutdoorToggle = getActivity().findViewById(R.id.indoorOutdoorToggle);
        indoorOutdoorToggle.setVisibility(View.GONE);
        buttonStandardTest.setVisibility(View.GONE);
        indoorOutdoorToggle.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        topText.setVisibility(View.VISIBLE);
        mosResults.setVisibility(View.GONE);
        resetResults();
        progressBar.setProgress(0);
        textViewMain.setText("");
        date = new Date();
        this.connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = null;
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        String mobileInfo = getMobileProperty();
        String telephoneInfo = getTelephoneProperty();

        Log.d("SignalStrength", "Starting signal strength collecting");
        Thread signalStrengthCollector = new Thread(new SignalStrengthCollector());
        signalStrengthCollector.start();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(getString(R.string.nonofficial))
                .append(BuildConfig.VERSION_NAME)
                .append("\n");
        stringBuilder.append(
                getString(R.string.test_begins_at, date.toString() + "\n"))
                .append("\n");

        if (Prefs.getGPSoverride(context)) {
            stringBuilder.append("GPS override set by Tester.\n");
        }
        ndtLocation.startListen();
        ndtLocation.startNetworkListenerUpdates();
        uiServices.appendString(stringBuilder.toString(), UiServices.MAIN_VIEW);
        uiServices.appendString(stringBuilder.toString(), UiServices.SUMMARY_VIEW);
        if (isNetworkActive()) {
            stringBuilder.append("\nThe network is active\n");
        } else {
            stringBuilder.append("\nThe network is not active\n");
        }
        statistics += stringBuilder.toString();
        if (ndtLocation.gpsEnabled) {
            gpsEnabled = "on";
        }
        if (ndtLocation.networkEnabled) {
            networkLocationEnabled = "on";
        }
        getLastKnownLocationInfo();
        String GPSLastLat = "No Value";
        String GPSLastLong = "No Value";
        String NetworkLastLat = "No Value";
        String NetworkLastLong = "No Value";
        if (ndtLocation.gpsLastKnownLocation != null) {
            GPSLastLat = Double.toString(ndtLocation.gpsLastKnownLocation.getLatitude());
            GPSLastLong = Double.toString(ndtLocation.gpsLastKnownLocation.getLongitude());
        }
        if (ndtLocation.networkLastKnownLocation != null) {
            NetworkLastLat = Double.toString(ndtLocation.networkLastKnownLocation.getLatitude());
            NetworkLastLong = Double.toString(ndtLocation.networkLastKnownLocation.getLongitude());
        }
        stringBuilder = new StringBuilder().append("\n")
                .append(getSystemProperty()).append("\n").append(mobileInfo)
                .append("\n\n").append(getString(R.string.GPS_Enabled))
                .append(gpsEnabled).append("\n").append(getString(R.string.Network_Enabled))
                .append(networkLocationEnabled).append("\n")
                .append(getString(R.string.GPS_latitude_last)).append(GPSLastLat).append("\n")
                .append(getString(R.string.GPS_longitude_last)).append(GPSLastLong).append("\n")
                .append(getString(R.string.Network_latitude_last)).append(NetworkLastLat)
                .append("\n").append(getString(R.string.Network_longitude_last))
                .append(NetworkLastLong).append("\n")
                .append("\n").append(telephoneInfo).append("\n");
        uiServices.appendString(stringBuilder.toString(), UiServices.MAIN_VIEW);
        statistics += stringBuilder.toString();
        checkGpsOverride();
    }

    private void startGPS() {
        if (ndtLocation.bestProvider != null && ndtLocation.gpsEnabled) {
            ndtLocation.startListen();
        }
    }

    private class AcquireGPS extends Thread {
        private final AndroidUiServices uiServices;
        private final LatLong gpsLatLong;

        AcquireGPS(AndroidUiServices uiServices) {
            this.uiServices = uiServices;
            this.gpsLatLong = new LatLong();
        }

        @Override
        public void run() {
            for (int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                gpsLatLong.getLatitudeLongitude(gpsLatLong);
                if (gpsLatLong.valid) {
                    uiServices.goodGpsSignal();
                    break;
                }
            }
            if (!gpsLatLong.valid) {
                ndtLocation.stopListen();
                ndtLocation.stopNetworkListenerUpdates();
                uiServices.noGpsSignal();
            }
        }

    }

    private void checkGpsOverride() {
        if (Prefs.getGPSoverride(context)) {
            startTest();
        } else {
            acquiringGPS();
        }
    }

    private void acquiringGPS() {
        Thread checkGPS = new Thread(new AcquireGPS(uiServices));
        checkGPS.start();
    }

    private void resultsNotSaved() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(
                "Unable to save results to SD card. Please check your settings.")
                .setCancelable(false)
                .setPositiveButton("Okay",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createGpsDisabledAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Your Location service is disabled! Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable Location",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showGpsOptions();
                            }
                        });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finishApp();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showGpsOptions() {
        Intent gpsOptionsIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }

    public void openWebURL(String inURL) {
        Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
        startActivity(browse);
    }

    private void createPrivacyPolicyAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(
                "Have you read and agree to our terms and conditions?")
                .setCancelable(false)
                .setPositiveButton("Read",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
        builder.setNegativeButton("Yes, I agree.",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences legal = getActivity().getSharedPreferences("Legal",
                                Constants.MODE_PRIVATE);
                        SharedPreferences.Editor legalEditor = legal.edit();
                        legalEditor.putBoolean("privacyPolicyAccepted", true);
                        legalEditor.apply();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        Button readButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        readButton.setOnClickListener(new PrivacyPolicyReadListener());
    }

    private class PrivacyPolicyReadListener implements OnClickListener {

        PrivacyPolicyReadListener() {
        }

        @Override
        public void onClick(View v) {
            openWebURL(Constants.privacyPolicyURL);
        }
    }

    private void createWifiAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(
                "You're connected to WiFi!\n Would you like to use WiFi?")
                .setCancelable(false)
                .setPositiveButton("WiFi",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                uiServices.printWifiID();
                                finishStartButton();
                            }
                        });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createDisableWifiAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(
                "Wifi is turned on, but you may not be logged in. Please log into your WiFi network or turn it off before running CalSPEED.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Gets the system related properties.
     *
     * @return a string describing the OS and Java environment
     */
    private String getSystemProperty() {
        String osName, osArch, osVer, javaVer, javaVendor;
        osName = System.getProperty("os.name");
        osArch = System.getProperty("os.arch");
        osVer = System.getProperty("os.version");
        javaVer = System.getProperty("java.version");
        javaVendor = System.getProperty("java.vendor");
        return String.format("\n%s\n%s", getString(R.string.os_line, osName, osArch, osVer),
                getString(R.string.java_line, javaVer, javaVendor));
    }


    /**
     * Gets the mobile device related properties.
     *
     * @return a string about location, network type (MOBILE or WIFI)
     */
    private String getMobileProperty() {
        StringBuilder sb = new StringBuilder();
        if (ndtLocation.gpsEnabled && ndtLocation.location != null) {
            LatLong newLatLong = new LatLong();
            newLatLong.getLatitudeLongitude(newLatLong);
            if (newLatLong.valid) {
                sb.append(getString(R.string.latitude_result, newLatLong.Latitude));
                sb.append("\n");
                sb.append(getString(R.string.longitude_result, newLatLong.Longitude));
            }
        } else {
            sb.append("").append(getString(R.string.no_GPS_info, ""));
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        String networkLine = "UNKNOWN";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            networkLine = getString(R.string.network_type_indicator, activeNetwork.getTypeName());
        } else {
            if (activeNetwork != null) {
                Network network = connectivityManager.getActiveNetwork();
                NetworkCapabilities netCap = connectivityManager.getNetworkCapabilities(network);
                String networkName;
                if (netCap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    networkName = "WIFI";
                } else {
                    networkName = "MOBILE";
                }
                networkLine = getString(R.string.network_type_indicator, networkName);
            }
        }
        sb.append("\n");
        sb.append(networkLine);
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Gets the mobile provider related properties.
     *
     * @return a string about network providers and network type
     */
    @SuppressLint("NewApi")
    private String getTelephoneProperty() {
        StringBuilder sb = new StringBuilder();
        try {
            this.telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            Log.d("TelephonyManager", "is null? " + (this.telephonyManager == null));
            String providerName = telephonyManager.getSimOperatorName();
            String deviceModel = Build.MODEL;
            String manufacturer = Build.MANUFACTURER;
            String APIVersion = Build.VERSION.RELEASE;
            int SDKVersion = Build.VERSION.SDK_INT;
            if (providerName == null) {
                providerName = "Unknown";
            }
            sb.append("\n").append(
                    getString(R.string.network_provider, providerName));
            Provider = sb.substring(18, sb.length());
            String operatorName = telephonyManager.getNetworkOperatorName();
            if (Provider.equalsIgnoreCase("")) {
                Provider = operatorName;
            }
            getPorts();
            if (operatorName == null) {
                operatorName = "Unknown";
            }
            if (Constants.DEBUG)
                Log.v("debug", operatorName);
            sb.append("\n").append(
                    getString(R.string.network_operator, operatorName));
            if (telephonyManager.isNetworkRoaming()) {
                sb.append("\n").append("Network is Roaming.");
            } else {
                sb.append("\n").append("Network is Not Roaming.");
            }
            ToggleButton indoorOutdoor = getActivity().findViewById(R.id.indoorOutdoorToggle);
            if (indoorOutdoor.isChecked()) {
                sb.append("\nThis device was: ").append(indoorOutdoor.getTextOn());
            } else {
                sb.append("\nThis device was ").append(indoorOutdoor.getTextOff());
            }
            connectionType = getConnectionType();
            if (Constants.DEBUG)
                Log.v("debug", connectionType);
            sb = printInfoLine(R.string.connection_type, connectionType, sb);
            sb.append("\n").append("Phone Model: ").append(deviceModel);
            sb.append("\n").append("Phone Manufacturer: ").append(manufacturer);
            sb.append("\n").append("API Version: ").append(APIVersion);
            sb.append("\n").append("SDK Version: ").append(SDKVersion);
        } catch (Exception e) {
            Log.e("getTelephoneProperty", "Something wrong with setup. " + e.getMessage());
        }
        return sb.toString();
    }

    private void getPorts() {
        if (Provider.equalsIgnoreCase("at&t")) {
            TCPPort = Constants.ports[0];
            UDPPort = Constants.ports[1];
        } else if (Provider.equalsIgnoreCase("sprint")) {
            TCPPort = Constants.ports[2];
            UDPPort = Constants.ports[3];
        } else if (Provider.equalsIgnoreCase("t-mobile")) {
            TCPPort = Constants.ports[4];
            UDPPort = Constants.ports[5];
        } else if (Provider.equalsIgnoreCase("verizon")) {
            TCPPort = Constants.ports[6];
            UDPPort = Constants.ports[7];
        } else {
            TCPPort = Constants.ports[8];
            UDPPort = Constants.ports[9];
        }
    }

    private StringBuilder printInfoLine(int label, String variable, StringBuilder buffer) {
        buffer.append("\n").append(getString(label, variable));
        return buffer;
    }

    private String getConnectionType() {
        if (Objects.equals(getNetworkType(), Constants.NETWORK_WIFI)) {
            return Constants.NETWORK_WIFI;
        }
        Integer intcon;
        String type = "UNKNOWN";
        final int connection = this.telephonyManager.getNetworkType();
        Log.d("getConnectionType", "data network type: " +
                this.telephonyManager.getDataNetworkType());
        for (int i = 0; i < Constants.NETWORK_TYPE.length; i++) {
            intcon = Integer.valueOf(Constants.NETWORK_TYPE[i][0]);
            if (intcon == connection) {
                Log.d("getConnectionType", "Original connection: " + connection);
                type = Constants.NETWORK_TYPE[i][1];
                break;
            }
        }
        return type;
    }

    /**
     * Gets the type of the active network, activeNetwork should be initialized
     * before called this function.
     */
    private String getNetworkType() {
        if (activeNetwork != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                int networkName = activeNetwork.getType();
                Log.d(getClass().getSimpleName(), "getNetworkType: " + networkName);
                if (networkName == ConnectivityManager.TYPE_WIFI) {
                    return Constants.NETWORK_WIFI;
                } else if (networkName == ConnectivityManager.TYPE_MOBILE) {
                    return Constants.NETWORK_MOBILE;
                } else {
                    return Constants.NETWORK_UNKNOWN;
                }
            } else {
                Network thisNetwork = connectivityManager.getActiveNetwork();
                NetworkCapabilities netCap = connectivityManager.getNetworkCapabilities(thisNetwork);
                if (netCap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return Constants.NETWORK_MOBILE;
                } else if (netCap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return Constants.NETWORK_WIFI;
                } else {
                    return Constants.NETWORK_UNKNOWN;
                }
            }
        } else {
            return Constants.NETWORK_UNKNOWN;
        }
    }

    class LatLong {
        Double Latitude;
        Double Longitude;
        Boolean valid;

        LatLong() {
            this.Latitude = 0.0;
            this.Longitude = 0.0;
            this.valid = false;
        }

        synchronized void setLatitudeLongitude(Double latitude, Double longitude, Boolean valid) {
            myLatLong.Latitude = latitude;
            myLatLong.Longitude = longitude;
            myLatLong.valid = valid;
        }

        synchronized Boolean getLatitudeLongitude(LatLong structLatLong) {
            structLatLong.Latitude = myLatLong.Latitude;
            structLatLong.Longitude = myLatLong.Longitude;
            structLatLong.valid = myLatLong.valid;
            return (structLatLong.valid);
        }

        void updateLatitudeLongitude() {
            if (ndtLocation.location != null) {
                myLatLong.setLatitudeLongitude(ndtLocation.location.getLatitude(),
                        ndtLocation.location.getLongitude(), true);
                Log.v("LatLong", "Updating lastKnownLocation: " + ndtLocation.location.toString());
                lastKnownLocation = ndtLocation.location;
            } else {
                myLatLong.setLatitudeLongitude(0.0, 0.0, false);
            }
        }
    }

    class NetworkLatLong {
        Double Latitude;
        Double Longitude;
        Boolean valid;

        NetworkLatLong() {
            this.Latitude = 0.0;
            this.Longitude = 0.0;
            this.valid = false;
        }

        synchronized void setLatitudeLongitude(Double latitude, Double longitude, Boolean valid) {
            networkLatLong.Latitude = latitude;
            networkLatLong.Longitude = longitude;
            networkLatLong.valid = valid;
        }

        synchronized Boolean getLatitudeLongitude(NetworkLatLong structLatLong) {
            structLatLong.Latitude = networkLatLong.Latitude;
            structLatLong.Longitude = networkLatLong.Longitude;
            structLatLong.valid = networkLatLong.valid;
            return (structLatLong.valid);
        }

        void updateNetworkLatitudeLongitude(Location location) {
            if (location != null) {
                networkLatLong.setLatitudeLongitude(location.getLatitude(),
                        location.getLongitude(), true);
                Log.v("NetworkLatLong", "Updating lastKnownLocation: " + location.toString());
                lastKnownLocation = location;
            } else {
                networkLatLong.setLatitudeLongitude(0.0, 0.0, false);
            }
        }
    }

    private void setupUploadTimer() {
        smoothUpload = 0.0f;
        uploadTimer = new Timer();
        uploadTask = new TimerTask() {
            @Override
            public void run() {
                if (smoothUpload != 0.0f) {
                    Log.d("UploadTimer", "trigger update: " + smoothUpload);
                    uiServices.updateUploadNumber();
                }
            }
        };
    }

    private void setupDownloadTimer() {
        smoothDownload = 0.0f;
        downloadTimer = new Timer();
        downloadTask = new TimerTask() {
            @Override
            public void run() {
                Log.d("DownloadTimer", "trigger update: " + smoothDownload);
                if (smoothDownload != 0.0f) {
                    uiServices.updateDownloadNumber();
                }
            }
        };
    }

    private void unBoldAll() {
        uploadText.setTypeface(null, Typeface.NORMAL);
        uploadNum.setTypeface(null, Typeface.NORMAL);
        uploadUnits.setTypeface(null, Typeface.NORMAL);

        downloadText.setTypeface(null, Typeface.NORMAL);
        downloadNum.setTypeface(null, Typeface.NORMAL);
        downloadUnits.setTypeface(null, Typeface.NORMAL);

        latencyText.setTypeface(null, Typeface.NORMAL);
        latencyNum.setTypeface(null, Typeface.NORMAL);
        latencyUnits.setTypeface(null, Typeface.NORMAL);

        jitterText.setTypeface(null, Typeface.NORMAL);
        jitterNum.setTypeface(null, Typeface.NORMAL);
        jitterUnits.setTypeface(null, Typeface.NORMAL);
    }

    private class UiHandler extends Handler {
        UiHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            String results;
            try {
                switch (message.what) {
                    case Constants.THREAD_MAIN_APPEND:
                        textViewMain.append(message.obj.toString());
                        break;
                    case Constants.THREAD_STAT_APPEND:
                        statistics += message.obj.toString();
                        break;

                    case Constants.THREAD_LAT_LONG_APPEND:
                        textViewMain.append("\nLatitude: " + startLatitude);
                        textViewMain.append("\nLongitude: " + startLongitude + "\n");

                        break;
                    case Constants.THREAD_BEGIN_TEST:
                        buttonStandardTest.setEnabled(false);
                        StandardTest.resetScores();
                        progressBar.setProgress(0);
                        progressBar.setMax(Constants.TEST_STEPS * 100);
                        if (!wakeLock.isHeld()) {
                            wakeLock.acquire();
                            startWakeLock();
                        }

                        latencyIcon.setVisibility(View.VISIBLE);
                        latencyText.setVisibility(View.VISIBLE);
                        latencyNum.setVisibility(View.VISIBLE);
                        latencyUnits.setVisibility(View.VISIBLE);

                        jitterText.setTypeface(null, Typeface.NORMAL);
                        jitterNum.setTypeface(null, Typeface.NORMAL);
                        jitterUnits.setTypeface(null, Typeface.NORMAL);
                        jitterIcon.setVisibility(View.VISIBLE);
                        jitterText.setVisibility(View.VISIBLE);
                        jitterNum.setVisibility(View.VISIBLE);
                        jitterUnits.setVisibility(View.VISIBLE);

                        break;
                    case Constants.THREAD_END_TEST:
                        textViewMain.append("\n-----End of Test------\n");
                        statistics += "\n";
                        buttonStandardTest.setEnabled(true);
                        if (wakeLock.isHeld()) {
                            Log.d("WAKELOCK", "release wakeLock");
                            wakeLock.release();
                            stopWakeLock();
                        }
                        Button toggleButton =
                                (ToggleButton) getActivity().findViewById(R.id.indoorOutdoorToggle);
                        toggleButton.setVisibility(View.VISIBLE);
                        toggleButton.setEnabled(true);
                        ProgressBar loadingIcon = getActivity().findViewById(R.id.loadingIcon);
                        loadingIcon.setVisibility(ProgressBar.GONE);
                        buttonStandardTest.setText(R.string.testAgain);
                        buttonStandardTest.setVisibility(Button.VISIBLE);
                        progressBar.setVisibility(ProgressBar.GONE);
                        sum = 0;
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(getActivity(),
                                android.R.style.Theme_Material_Light_Dialog_Alert);
                        dlgAlert.setCancelable(true);
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.dialog_scorecard, null);
                        dlgAlert.setView(dialogView)
                                // Add action buttons
                                .setPositiveButton(R.string.done_button, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        Double mosNumber = Double.parseDouble(mosNum.getText().toString());
                        ImageView mosImage = dialogView.findViewById(R.id.mosIcon);
                        if (mosNumber < 4) {
                            mosImage.setImageResource(R.drawable.thumbsdown);
                        } else {
                            mosImage.setImageResource(R.drawable.thumbsup);
                        }
                        /*
                        * run calculation methods here. could also send in MOS value to be sure
                        * it's accurate.
                        * get the video quality details
                        */
                        String[] videoSummary = currentTest.getVideoCalc();
                        Log.d("VideoSummary", "Voip: " + Arrays.toString(videoSummary));
                        String videoClassification = videoSummary[2];

                        //get the conference quality details
                        String[] conferenceSummary = currentTest.getVideoConferenceCalc();
                        Log.d("VideoSummary", "Voip: " + Arrays.toString(conferenceSummary));
                        String conferenceClassification = conferenceSummary[2];

                        // Get the Voice IP quality details
                        String[] voipSummary = currentTest.getVideoVoipCalc();
                        Log.d("VideoSummary", "Voip: " + Arrays.toString(voipSummary));
                        String voipClassification = voipSummary[2];

                        Log.d("Video Streaming", videoClassification);
                        Log.d("Video Conference", conferenceClassification);
                        Log.d("VoIP", voipClassification);
                        TextView downValueLabel = dialogView.findViewById(R.id.downValue);
                        TextView downUnitsLabel = dialogView.findViewById(R.id.downloadUnits);
                        TextView upValueLabel = dialogView.findViewById(R.id.upValue);
                        TextView upUnitsLabel = dialogView.findViewById(R.id.uploadUnits);

                        TextView delayValueLabel = dialogView.findViewById(R.id.delayValue);
                        TextView videoValueLabel = dialogView.findViewById(R.id.videoQuality);
                        StandardTest.resetScores();

                        DecimalFormat decf;
                        if (Double.parseDouble(downloadNum.getText().toString()) < 10)
                            decf = new DecimalFormat("0.00");
                        else if (Double.parseDouble(downloadNum.getText().toString()) < 100)
                            decf = new DecimalFormat("0.0");
                        else decf = new DecimalFormat("0");
                        downValueLabel.setText(decf.format(Double.parseDouble(downloadNum.getText().toString())));
                        downUnitsLabel.setText(downloadUnits.getText());

                        if (Double.parseDouble(uploadNum.getText().toString()) < 10)
                            decf = new DecimalFormat("0.00");
                        else if (Double.parseDouble(uploadNum.getText().toString()) < 100)
                            decf = new DecimalFormat("0.0");
                        else decf = new DecimalFormat("0");
                        upValueLabel.setText(decf.format(Double.parseDouble(uploadNum.getText().toString())));
                        upUnitsLabel.setText(uploadUnits.getText());

                        if (Double.parseDouble(latencyNum.getText().toString()) < 10)
                            decf = new DecimalFormat("0.00");
                        else if (Double.parseDouble(latencyNum.getText().toString()) < 100)
                            decf = new DecimalFormat("0.0");
                        else decf = new DecimalFormat("0");
                        delayValueLabel.setText(decf.format(Double.parseDouble(latencyNum.getText().toString())));
                        videoValueLabel.setText(videoClassification);
                        unBoldAll();
                        dlgAlert.create().show();
                        break;
                    case Constants.THREAD_TEST_INTERRUPTED:
                        textViewMain.append("\n-----End of Test------\n");
                        statistics += "\n";
                        buttonStandardTest.setEnabled(true);
                        if (wakeLock.isHeld()) {
                            Log.d("WAKELOCK", "release wakeLock");
                            wakeLock.release();
                            stopWakeLock();
                        }
                        break;
                    case Constants.THREAD_ADD_PROGRESS:
                        Integer increment = message.getData().getInt("increment");
                        Log.d("ProgressBar", "Adding increment to progress bar: " + increment +
                                ", current progress " + progressBar.getProgress());
                        incrementProgressAnimate(progressBar, increment, 250);
                        break;
                    case Constants.THREAD_COMPLETE_PROGRESS:
                        int progressSoFar = progressBar.getProgress();
                        int stepsSoFar = -1;
                        try {
                            stepsSoFar = progressSoFar / 100;
                        } catch (Exception e) {
                            Log.w("ProgressBar", "unable to convert to int" + progressSoFar);
                        }
                        if ((stepsSoFar > 0) && (stepsSoFar < Constants.TEST_STEPS)) {
                            int finishIncrement = Constants.TEST_STEPS - stepsSoFar;
                            Log.d("ProgressBar", "Filling rest of progress bar: " +
                                    finishIncrement + " / " + progressBar.getMax());
                            incrementProgressAnimate(progressBar, finishIncrement, 100);
                        } else {
                            Log.d("ProgressBar", "Not enough steps or progress greater than max " +
                                    "test steps; current progress " + progressBar.getProgress());
                        }
                        break;
                    case Constants.THREAD_CLEAR_PROCESS_HANDLE:
                        break;
                    case Constants.THREAD_GOOD_GPS_SIGNAL:
                        startTest();
                        break;
                    case Constants.THREAD_NO_GPS_SIGNAL:
                        break;
                    case Constants.THREAD_NO_MOBILE_CONNECTION:
                        break;
                    case Constants.THREAD_GOT_MOBILE_CONNECTION:
                        finishStartButton();
                        break;
                    case Constants.THREAD_UPDATE_LATLONG:
                        LatLong newLatLong = new LatLong();
                        newLatLong.getLatitudeLongitude(newLatLong);
                        break;
                    case Constants.THREAD_RESULTS_SAVED:
                        break;
                    case Constants.THREAD_RESULTS_NOT_SAVED:
                        resultsNotSaved();
                        break;
                    case Constants.THREAD_RESULTS_UPLOADED:
                        break;
                    case Constants.THREAD_RESULTS_NOT_UPLOADED:
                        break;
                    case Constants.THREAD_RESULTS_ATTEMP_UPLOAD:
                        break;
                    case Constants.THREAD_SET_STATUS_TEXT:
                        results = message.getData().getString("text");
                        topText.setText(results);
                        break;
                    case Constants.THREAD_PRINT_BSSID_SSID:
                        textViewMain.append("Wifi BSSID: " + "NO DATA" + "\nWifi SSID: "
                                + "NO DATA" + "\n");
                        statistics += "Wifi BSSID: NO DATA" + "\nWifi SSID: NO DATA\n";
                        break;
                    case Constants.THREAD_WRITE_UPLOAD_DATA:
                        if (message.getData().getBoolean("redText")) {
                            uploadText.setTextColor(Color.RED);
                            unBoldAll();
                        }
                        if (message.getData().getBoolean("numbersHidden")) {
                            uploadNum.setVisibility(View.GONE);
                            uploadUnits.setVisibility(View.GONE);
                        } else {
                            uploadNum.setVisibility(View.VISIBLE);
                            uploadUnits.setVisibility(View.VISIBLE);
                            String finalUpload = message.getData().getString("number");
                            if (finalUpload != null) {
                                Float oldSmooth = smoothUpload;
                                smoothUpload = Float.valueOf(finalUpload);
                                Log.v("WRITE_UPLOAD_DATA", "writing smooth upload: "
                                        + smoothUpload + ", final upload: " + finalUpload + "old" +
                                        " smooth: " + oldSmooth);
                            } else {
                                Log.i("WRITE_UPLOAD_DATA", "null finalUpload ");
                            }
                        }
                        uploadText.setText(message.getData().getString("text"));
                        break;
                    case Constants.THREAD_WRITE_DOWNLOAD_DATA:
                        if (message.getData().getBoolean("redText")) {
                            downloadText.setTextColor(Color.RED);
                            unBoldAll();
                        }
                        if (message.getData().getBoolean("numbersHidden")) {
                            downloadNum.setVisibility(View.GONE);
                            downloadUnits.setVisibility(View.GONE);
                        } else {
                            downloadNum.setVisibility(View.VISIBLE);
                            downloadUnits.setVisibility(View.VISIBLE);
                            String finalDownload = message.getData().getString("number");
                            if (finalDownload != null) {
                                smoothDownload = Float.valueOf(finalDownload);
                                Log.v("WRITE_DOWNLOAD_DATA", "writing smooth download: "
                                        + smoothDownload + ", final download: " + finalDownload);
                            } else {
                                Log.i("WRITE_DOWNLOAD_DATA", "null finalDownload ");
                            }
                        }
                        downloadText.setText(message.getData().getString("text"));
                        break;
                    case Constants.THREAD_WRITE_LATENCY_DATA:
                        if (message.getData().getBoolean("redText")) {
                            latencyText.setTextColor(Color.RED);
                            unBoldAll();
                        }

                        if (message.getData().getBoolean("numbersHidden")) {
                            latencyNum.setVisibility(View.GONE);
                            latencyUnits.setVisibility(View.GONE);
                        } else {
                            latencyNum.setVisibility(View.VISIBLE);
                            latencyUnits.setVisibility(View.VISIBLE);
                        }

                        latencyText.setText(message.getData().getString("text"));
                        latencyNum.setText(properFormat(message.getData()
                                .getString("number"), false));

                        downloadText.setTypeface(null, Typeface.NORMAL);
                        downloadNum.setTypeface(null, Typeface.NORMAL);
                        downloadUnits.setTypeface(null, Typeface.NORMAL);

                        latencyText.setTypeface(null, Typeface.BOLD);
                        latencyNum.setTypeface(null, Typeface.BOLD);
                        latencyUnits.setTypeface(null, Typeface.BOLD);

                        break;
                    case Constants.THREAD_WRITE_JITTER_DATA:
                        if (message.getData().getBoolean("redText")) {
                            jitterText.setTextColor(Color.RED);
                            unBoldAll();
                        }
                        if (message.getData().getBoolean("numbersHidden")) {
                            jitterNum.setVisibility(View.GONE);
                            jitterUnits.setVisibility(View.GONE);
                        } else {
                            jitterNum.setVisibility(View.VISIBLE);
                            jitterUnits.setVisibility(View.VISIBLE);
                        }
                        jitterText.setText(message.getData().getString("text"));
                        jitterNum.setText(properFormat(message.getData()
                                .getString("number"), false));
                        jitterText.setTypeface(null, Typeface.BOLD);
                        jitterNum.setTypeface(null, Typeface.BOLD);
                        jitterUnits.setTypeface(null, Typeface.BOLD);

                        latencyText.setTypeface(null, Typeface.NORMAL);
                        latencyNum.setTypeface(null, Typeface.NORMAL);
                        latencyUnits.setTypeface(null, Typeface.NORMAL);

                        break;
                    case Constants.THREAD_WRITE_MOS_DATA:
                        if (message.getData().getBoolean("redText")) {
                            mosText.setTextColor(Color.RED);
                            unBoldAll();
                        }
                        if (message.getData().getBoolean("numbersHidden")) {
                            mosNum.setVisibility(View.GONE);
                        } else {
                            mosNum.setVisibility(View.VISIBLE);
                        }
                        String mosValue = message.getData().getString("number");
                        mosNum.setText(mosValue);
                        mosText.setText(message.getData().getString("text"));
                        jitterText.setTypeface(null, Typeface.NORMAL);
                        jitterNum.setTypeface(null, Typeface.NORMAL);
                        jitterUnits.setTypeface(null, Typeface.NORMAL);

                        break;
                    case Constants.START_PRELIM:
                        uploadText.setTypeface(null, Typeface.BOLD);
                        uploadNum.setTypeface(null, Typeface.BOLD);
                        uploadUnits.setTypeface(null, Typeface.BOLD);
                        break;
                    case Constants.FINISH_PRELIM:
                        resultsView.startAnimation(slideOut);
                        resultsView.setVisibility(View.INVISIBLE);
                        latencyIcon.setVisibility(View.VISIBLE);
                        latencyText.setVisibility(View.VISIBLE);
                        latencyNum.setVisibility(View.VISIBLE);
                        latencyUnits.setVisibility(View.VISIBLE);
                        jitterText.setTypeface(null, Typeface.NORMAL);
                        jitterNum.setTypeface(null, Typeface.NORMAL);
                        jitterUnits.setTypeface(null, Typeface.NORMAL);
                        jitterIcon.setVisibility(View.VISIBLE);
                        jitterText.setVisibility(View.VISIBLE);
                        jitterNum.setVisibility(View.VISIBLE);
                        jitterUnits.setVisibility(View.VISIBLE);
                        unBoldAll();
                        resetResults();
                        break;
                    case Constants.FINISH_PHASE_1:
                        resultsView.startAnimation(slideOut);
                        resultsView.setVisibility(View.INVISIBLE);
                        unBoldAll();
                        Log.d("SET_UPLOAD_NUMBER", "reset results");
                        resetResults();
                        break;
                    case Constants.THREAD_START_UPLOAD_TIMER:
                        smoothUpload = 0.0f;
                        jitterText.setTypeface(null, Typeface.NORMAL);
                        jitterNum.setTypeface(null, Typeface.NORMAL);
                        jitterUnits.setTypeface(null, Typeface.NORMAL);
                        Log.d("UploadTimer", "starting timer");
                        uploadTimer.scheduleAtFixedRate(uploadTask, 0, 1000);
                        break;
                    case Constants.THREAD_STOP_UPLOAD_TIMER:
                        uploadTimer.cancel();
                        uploadTimer.purge();
                        uploadTimer = null;
                        setupUploadTimer();
                        break;
                    case Constants.THREAD_UPDATE_UPLOAD_NUMBER:
                        if (smoothUpload != 0.0f) {
                            uploadNum.setText(properFormat(smoothUpload.toString(), true));
                        }
                        break;
                    case Constants.THREAD_SET_UPLOAD_NUMBER:
                        String upNum = message.getData().getString("number");
                        Log.v("SET_UPLOAD_NUMBER", "Straight set upload num: " + upNum);
                        uploadNum.setText(properFormat(upNum, true));
                        break;
                    case Constants.THREAD_SET_UPLOAD_NUMBER_STOP_TIMER:
                        uploadTimer.cancel();
                        uploadTimer.purge();
                        uploadTimer = null;
                        String num1 = message.getData().getString("number");
                        Log.v("debug", "in handler stop Upload Timer number=" + num1);
                        uploadNum.setText(properFormat(message.getData().getString("number"), true));
                        uploadText.setTypeface(null, Typeface.NORMAL);
                        uploadNum.setTypeface(null, Typeface.NORMAL);
                        uploadUnits.setTypeface(null, Typeface.NORMAL);

                        downloadText.setTypeface(null, Typeface.BOLD);
                        downloadNum.setTypeface(null, Typeface.BOLD);
                        downloadUnits.setTypeface(null, Typeface.BOLD);
                        setupUploadTimer();
                        break;
                    case Constants.THREAD_START_DOWNLOAD_TIMER:
                        smoothDownload = 0.0f;
                        Log.d("DownloadTimer", "starting timer");
                        downloadTimer.scheduleAtFixedRate(downloadTask, 0, 1000);
                        break;
                    case Constants.THREAD_STOP_DOWNLOAD_TIMER:
                        downloadTimer.cancel();
                        downloadTimer.purge();
                        downloadTimer = null;
                        setupDownloadTimer();
                        break;
                    case Constants.THREAD_UPDATE_DOWNLOAD_NUMBER:
                        if (smoothDownload != 0.0f) {
                            downloadNum.setText(properFormat(smoothDownload.toString(), true));
                        }
                        break;
                    case Constants.THREAD_SET_DOWNLOAD_NUMBER:
                        String downNum = message.getData().getString("number");
                        downloadNum.setText(properFormat(downNum, true));
                        break;
                    case Constants.THREAD_SET_DOWNLOAD_NUMBER_STOP_TIMER:
                        downloadTimer.cancel();
                        downloadTimer.purge();
                        downloadTimer = null;
                        downloadNum.setText(properFormat(message.getData().getString("number"),
                                true));
                        setupDownloadTimer();
                        break;
                    case Constants.THREAD_UPDATE_NETWORK_INFO:
                        getNetworkAndDeviceInfo();
                        break;
                    case Constants.THREAD_START_ANIMATION:
                        AlertDialog.Builder elapsed = new AlertDialog.Builder(getActivity());
                        elapsed.setCancelable(true);
                        final EditText input = new EditText(getActivity());
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        elapsed.setView(input);
                        elapsed.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                Log.e(e.getClass().getName(), e.getMessage(), e);
            }
        }
    }

    private void startTest() {
        String s1 = Constants.SERVER_HOST[0];
        String s2 = Constants.SERVER_HOST[1];
        ThreadData.resetAllThreads();
        StandardTest stdTest = new StandardTest(networkLatLong, myLatLong, s1, s2, uiServices,
                getNetworkType(), ndtLocation, applicationFilesDir, date, statistics, TCPPort,
                UDPPort, connectionType, db, this.ndi, this);
        testThread = new Thread(stdTest);
        currentTest = stdTest;
        testThread.start();
    }

    private void resetResults() {
        uiServices.setResults(Constants.THREAD_WRITE_UPLOAD_DATA,
                "Upload Speed", "0", false, false);
        uiServices.setUploadNumber("0");
        uiServices.setResults(Constants.THREAD_WRITE_DOWNLOAD_DATA,
                "Download Speed", "0", false, false);
        uiServices.setDownloadNumber("0");
        uiServices.setResults(Constants.THREAD_WRITE_LATENCY_DATA, "Latency",
                "0", false, false);
        uiServices.setResults(Constants.THREAD_WRITE_JITTER_DATA,
                "Jitter", "0", false, false);
        uiServices.setResults(Constants.THREAD_WRITE_MOS_DATA,
                "MOS Value", "0", false, false);
        uiServices.setMosValue("0");
        uploadText.setTextColor(Color.BLACK);
        downloadText.setTextColor(Color.BLACK);
        latencyText.setTextColor(Color.BLACK);
        jitterText.setTextColor(Color.BLACK);
        unBoldAll();
    }

    private class SlideOutAnimationListener implements AnimationListener {
        @Override
        public void onAnimationEnd(Animation arg0) {
            resultsView.setVisibility(View.VISIBLE);
            resultsView.startAnimation(slideIn);
            uploadText.setTypeface(null, Typeface.BOLD);
            uploadNum.setTypeface(null, Typeface.BOLD);
            uploadUnits.setTypeface(null, Typeface.BOLD);
        }

        @Override
        public void onAnimationRepeat(Animation arg0) {
            // Nothing
        }

        @Override
        public void onAnimationStart(Animation arg0) {
            // Nothing
        }
    }

    /*
     *  Getter method for Location for use by Viewer Fragment
     */
    public Location getLocation() {
        Log.d("CalSPEEDLocation", "getLocation(): calling locations");
        if (this.ndtLocation != null) {
            getLastKnownLocationInfo();
            if (ndtLocation.gpsLastKnownLocation != null) {
                return ndtLocation.gpsLastKnownLocation;
            } else if (ndtLocation.networkLastKnownLocation != null) {
                return ndtLocation.networkLastKnownLocation;
            } else {
                Log.d("CalSPEEDLocation", "getLocation(): no last known location");
                return null;
            }
        } else {
            Log.d("CalSPEEDLocation", "getLocation(): ndtLocation is null");
            if (lastKnownLocation == null) {
                Log.d("CalSPEEDLocation", "getLocation(): lastKnownLocation is null");
                return null;
            } else {
                Log.d("CalSPEEDLocation", "getLocation(): lastKnownLocation is " +
                        lastKnownLocation.toString());
                return lastKnownLocation;
            }
        }
    }

    /*
     * Call the Main Activity to restore tabs after StandardTest has completed
     */
    public void enableTabs() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                ((MainActivity) getActivity()).enableTabsAfterTesting();
            }
        });
        Log.d("CalspeedFragment", "after enableTabs()");
    }

    private String properFormat(String value, boolean convertMbps) {
        Double newVal;
        try {
            newVal = Double.parseDouble(value);
        } catch (Exception e) {
            return value;
        }
        DecimalFormat df = new DecimalFormat("0.00");
        String returnString;
        if (convertMbps) newVal /= 1000.0;
        returnString = df.format(newVal);
        return returnString;
    }

    private class SignalStrengthCollector implements Runnable {
        SignalStrengthCollector() {
            ndi = new NetworkAndDeviceInformation(context, uiServices, telephonyManager,
                    connectivityManager);
        }
        @Override
        public void run() {
            Log.d("SignalStrengthCollector", "All the work is done in NetworkAndDeviceInfo");
        }
    }

    private void getNetworkAndDeviceInfo() {
    }

    private void incrementProgressAnimate(ProgressBar progressBar, int increment, int speed) {
        if (increment > 0) {
            sum += (increment * 100);
            ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress",
                    progressBar.getProgress(), sum);
            Log.d("ProgressAnimate", String.format("progress is %d / %d",
                    progressBar.getProgress(),progressBar.getMax()));
            animation.setDuration(speed * increment);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }
    }
}