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

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class MainActivity extends TabSwipeActivity {

    Fragment calspeedFragment = new CalspeedFragment();
    Fragment viewerFragment = new ViewerFragment();
    Fragment displayHistoryFragment = new DisplayHistory();

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(this.getClass().getName(), "onCreate()");
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_PHONE_STATE
        };

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS, PERMISSION_ALL);
            }
        }
        Log.v(this.getClass().getName(), "Adding each Fragment tab");
        addTab("Speed Test", CalspeedFragment.class, CalspeedFragment.createBundle("Fragment 1"));
        addTab("Results", DisplayHistory.class, DisplayHistory.createBundle("Fragment 2"));
        setHasEmbeddedTabs(getSupportActionBar(), false);

        int checkGooglePlayServices =
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getBaseContext());
        if (checkGooglePlayServices == ConnectionResult.SUCCESS) {
            Log.i("GooglePlayServices", "ConnectionResult=SUCCESS");
            addTab("Map View", ViewerFragment.class, ViewerFragment.createBundle("Fragment 3"));
        }
        HistoryDatabaseHandler db = new HistoryDatabaseHandler(this);
        db.updateTable();
    }

    public static void setHasEmbeddedTabs(Object inActionBar, final boolean inHasEmbeddedTabs) {
        // get the ActionBar class
        Log.d("MainActivity", "calling setHasEmbeddedTabs()");
        Class<?> actionBarClass = inActionBar.getClass();

        // if it is a Jelly Bean implementation (ActionBarImplJB), get the super class (ActionBarImplICS)
        if ("android.support.v7.app.ActionBarImplJB".equals(actionBarClass.getName())) {
            actionBarClass = actionBarClass.getSuperclass();
        }

        try {
            // try to get the mActionBar field, because the current ActionBar is probably just a wrapper Class
            // if this fails, no worries, this will be an instance of the native ActionBar class or from the ActionBarImplBase class
            final Field actionBarField;
            if (actionBarClass != null) {
                actionBarField = actionBarClass.getDeclaredField("mActionBar");
                actionBarField.setAccessible(true);
                inActionBar = actionBarField.get(inActionBar);
                actionBarClass = inActionBar.getClass();
            }
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException e) {
            Log.e("MainActivity", "Unable to get actionBar in embedded tabs");
        }

        try {
            // now call the method setHasEmbeddedTabs, this will put the tabs inside the ActionBar
            // if this fails, you're on you own <img src="http://www.blogc.at/wp-includes/images/smilies/icon_wink.gif" alt=";-)" class="wp-smiley">
            final Method method;
            if (actionBarClass != null) {
                method = actionBarClass.getDeclaredMethod("setHasEmbeddedTabs", Boolean.TYPE);
                method.setAccessible(true);
                method.invoke(inActionBar, inHasEmbeddedTabs);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalArgumentException |
                IllegalAccessException e) {
            Log.e("MainActivity", "Unable to get actionBar in embedded tabs");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.v(this.getClass().getName(), "starting onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v(this.getClass().getName(), "starting onRestoreInstanceState()");
        Log.d(this.getClass().getName(), savedInstanceState.toString());
        savedInstanceState.clear();
        displayHistoryFragment.onCreate(savedInstanceState);
        viewerFragment.onCreate(savedInstanceState);
        calspeedFragment.onCreate(savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(this.getClass().getName(), "starting onCreateOptionsMenu()");
        MenuInflater inflater = getSupportMenuInflater();
        Log.v(this.getClass().getName(), "onCreateOptionsMenu(), inflating main activity");
        inflater.inflate(R.menu.main_activity, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(this.getClass().getName(), "calling onOptionsItemSelected()");
        switch (item.getItemId()) {
            case R.id.menuitem_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                if (isCurrentTab(2)) {
                    dialog.setTitle("About Map View (v" + BuildConfig.VERSION_NAME + ")");
                    TextView viewerHelp = new TextView(this);
                    if (Build.VERSION.SDK_INT >= 24) {
                        viewerHelp.setText(Html.fromHtml(this.getString(R.string.about_text_viewer),
                                Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        viewerHelp.setText(Html.fromHtml(
                                this.getString(R.string.about_text_viewer)));
                    }
                    viewerHelp.setMovementMethod(LinkMovementMethod.getInstance());
                    viewerHelp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    viewerHelp.setPadding(10, 10, 10, 10);
                    ScrollView scroller = new ScrollView(this);
                    scroller.addView(viewerHelp);
                    dialog.setView(scroller);
                } else {
                    // Setting Dialog Title
                    dialog.setTitle(String.format("%s (v%s)", getString(R.string.about_calspeed),
                            BuildConfig.VERSION_NAME));
                    // Setting Dialog Message
                    TextView viewerHelp = new TextView(this);
                    if (Build.VERSION.SDK_INT >= 24) {
                        viewerHelp.setText(Html.fromHtml(this.getString(R.string.about_text),
                                Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        viewerHelp.setText(Html.fromHtml(this.getString(R.string.about_text)));
                    }
                    viewerHelp.setMovementMethod(LinkMovementMethod.getInstance());
                    viewerHelp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    viewerHelp.setPadding(10, 10, 10, 10);
                    ScrollView scroller = new ScrollView(this);
                    scroller.addView(viewerHelp);
                    dialog.setView(scroller);
                }
                dialog.show();
                return true;
            default:
                return false;
        }
    }

    public Location getLocationFromCalspeedFragment() {
        Location newLocation = null;
        int locationTries = 0;
        while (newLocation == null && locationTries < 10) {
            newLocation = ((CalspeedFragment) calspeedFragment).getLocation();
            locationTries++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        return newLocation;
    }

    public void disableTabsWhileTesting() {
        int checkGooglePlayServices =
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getBaseContext());
        if (checkGooglePlayServices == ConnectionResult.SUCCESS) {
            removeTabs(1, 2);
        } else removeTab(1);
    }

    public void enableTabsAfterTesting() {
        addTab("Results", DisplayHistory.class, DisplayHistory.createBundle("Fragment 2"));
        int checkGooglePlayServices =
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getBaseContext());
        if (checkGooglePlayServices == ConnectionResult.SUCCESS) {
            addTab("Map View", ViewerFragment.class, ViewerFragment.createBundle("Fragment 3"));
        }
    }

}

