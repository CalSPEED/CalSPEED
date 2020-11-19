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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

class AdvertisedDataResponse extends AsyncTask<String, Integer, AdvertisedData> {

    public interface Callback {
        void onComplete(AdvertisedData data);
    }

    private final Context myContext;
    private ProgressDialog dialog;
    private final Set<Callback> callbacks = new HashSet<Callback>();

    void addObserver(Callback cb) {
        callbacks.add(cb);
    }

    AdvertisedDataResponse(Activity activity, ProgressDialog dialog) {
        myContext = activity;
        this.dialog = dialog;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(myContext);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Downloading Data...");
        dialog.show();
    }

    @Override
    protected AdvertisedData doInBackground(String... params) {
        String advertisedInfoURL = params[0];
        Log.d("AdvertisedDataResponse", "Got the advertised info url: " + advertisedInfoURL);
        AdvertisedData data = new AdvertisedData();
        Reader reader = null;
        try {
            InputStream source = retrieveStream(advertisedInfoURL);
            if (source == null) {
                cancel(true);
                AdvertisedDataResponse.this.cancel(true);
            } else {
                reader = new InputStreamReader(source);
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Got an unexpected exception, cancelling.");
            Log.e(getClass().getName(), e.getMessage());
        }

        if (reader != null) {
            Gson gson = new Gson();
            try {
                data = gson.fromJson(reader, AdvertisedData.class);
                if (data == null) {
                    Log.e(getClass().getName(), "Data is null");
                } else {
                    Log.d(getClass().getName(), "Data contents are: " + data.toString());
                }
            } catch (JsonSyntaxException e) {
                Log.e(getClass().getName(), "Got an unexpected JSON Syntax exception, cancelling.\n"
                        + e.getMessage());
                AdvertisedDataResponse.this.cancel(true);
            }
        } else {
            Log.e(getClass().getName(), "Reader input is null, cancelling");
            AdvertisedDataResponse.this.cancel(true);
        }
        if ((!this.isCancelled())) {
            Log.d(getClass().getName(), "Got advertised data and converted it.");
            return data;
        } else {
            Log.i(getClass().getName(), "Handling the data was cancelled");
            return null;
        }
    }

    public void closeDownloadDialog() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onPostExecute(AdvertisedData data) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        for (Callback cb : callbacks) {
            cb.onComplete(data);
        }
    }

    private InputStream retrieveStream(String urlName) {
        URL url;
        try {
            url = new URL(urlName);
        } catch (MalformedURLException e) {
            Log.e(getClass().getSimpleName(), "URL malformed: " + urlName);
            return null;
        }
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                return new BufferedInputStream(urlConnection.getInputStream());
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            Log.w(getClass().getSimpleName(), "Error for URL " + url, e);
        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), "Regular Error for URL " + url, e);
        }
        return null;
    }

}