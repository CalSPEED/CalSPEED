/*
Copyright (c) 2013, California State University Monterey Bay (CSUMB).
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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DisplayInfo extends TabSwipeActivity {

    ActionBar actionbar;
    Intent myIntent;
    double x;
    double y;
    String address;
    private Intent exceptionIntent;
    private boolean isPaused = false;
    AdvertisedDataResponse adverDataRespHandler;

    public static void setHasEmbeddedTabs(Object inActionBar, final boolean inHasEmbeddedTabs) {
        Class<?> actionBarClass = inActionBar.getClass();

        // if it is a Jelly Bean implementation (ActionBarImplJB), get the super class (ActionBarImplICS)
        if ("android.support.v7.app.ActionBarImplJB".equals(actionBarClass.getName())) {
            actionBarClass = actionBarClass.getSuperclass();
        }

        try {
            final Field actionBarField = actionBarClass.getDeclaredField("mActionBar");
            actionBarField.setAccessible(true);
            inActionBar = actionBarField.get(inActionBar);
            actionBarClass = inActionBar.getClass();
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException e) {
            Log.e(DisplayInfo.class.getName(), e.getMessage());
        }

        try {
            final Method method = actionBarClass.getDeclaredMethod("setHasEmbeddedTabs",
                    Boolean.TYPE);
            method.setAccessible(true);
            method.invoke(inActionBar, inHasEmbeddedTabs);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                IllegalArgumentException e) {
            Log.e(DisplayInfo.class.getName(), e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(getClass().getName(), "DisplayInfo - onCreate()");
        super.onCreate(savedInstanceState);
        exceptionIntent = new Intent(this, MainActivity.class);
        myIntent = getIntent();
        address = myIntent.getStringExtra("address");
        x = myIntent.getDoubleExtra("x", 0);
        y = myIntent.getDoubleExtra("y", 0);
        actionbar = getSupportActionBar();

        // Tell the ActionBar we want to use Tabs.
        setHasEmbeddedTabs(actionbar, false);

        // To enable "Up" (or "Back") function when a user clicks the app icon.
        actionbar.setDisplayHomeAsUpEnabled(true);
        String advertisedURL = String.format(Constants.ARCGIS_MAPSERVER, x, y, Constants.OUT_SECRET,
                Constants.OUT_SECRET);

        final ProgressDialog dialog = new ProgressDialog(DisplayInfo.this);
        adverDataRespHandler = new AdvertisedDataResponse(DisplayInfo.this, dialog);
        adverDataRespHandler.execute(advertisedURL);

        // 15 seconds timeout to get the advertised data.
        Handler dataResponseHandler = new Handler();
        dataResponseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adverDataRespHandler.isCancelled() && !isFinishing() && !isPaused) {
                    dialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(DisplayInfo.this);
                    builder.setTitle("Website Down")
                            .setMessage("Sorry, the Viewer website is down, please try again later.")
                            .setCancelable(false)
                            .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    startActivity(exceptionIntent);
                                    finish();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }, 15000);

        // 30 seconds timeout to get the advertised data.
        dataResponseHandler = new Handler();
        dataResponseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adverDataRespHandler.getStatus() == AsyncTask.Status.RUNNING && !isFinishing()) {
                    adverDataRespHandler.closeDownloadDialog();
                    adverDataRespHandler.cancel(true);
                    dialog.dismiss();

                    AlertDialog.Builder builder = new AlertDialog.Builder(DisplayInfo.this);
                    builder.setTitle("Website Down")
                            .setMessage("Sorry, the Viewer website is down. Please try again later.")
                            .setCancelable(false)
                            .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    startActivity(exceptionIntent);
                                    finish();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }, 30000);
        adverDataRespHandler.addObserver(new AdvertisedDataResponse.Callback() {
            @Override
            public void onComplete(AdvertisedData data) {
                if (isPaused) {
                    adverDataRespHandler.cancel(true);
                    startActivity(exceptionIntent);
                    finish();
                    return;
                }
                Log.d("DisplayInfo", "Got the data: " + data.toString());
                AdvertisedFixedFragment.newInstance(data, address);
                addTab("Fixed Broadband", AdvertisedFixedFragment.class,
                        AdvertisedFixedFragment.createBundle("Fixed Broadband"));
                AdvertisedMobileFragment.newInstance(data, address);
                addTab("Mobile Broadband", AdvertisedMobileFragment.class,
                        AdvertisedMobileFragment.createBundle("Mobile Broadband"));
                setHasEmbeddedTabs(getSupportActionBar(), false);
            }
        });
    }


    @Override
    protected void onPause() {
        isPaused = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        isPaused = false;
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(getClass().getName(), "DisplayInfo - onOptionsItemSelected()");
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
