/* Original work: Copyright 2009 Google Inc. All Rights Reserved.
 
   Modified work: The original source code (AndroidNdt.java) comes from the NDT Android app
                  that is available from http://code.google.com/p/ndt/.
                  It's modified for the CalSPEED Android app by California 
                  State University Monterey Bay (CSUMB) on April 29, 2013.
*/

package gov.ca.cpuc.calspeed.android;

import android.content.Context;
import android.location.Criteria;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.Iterator;

import gov.ca.cpuc.calspeed.android.CalspeedFragment.LatLong;
import gov.ca.cpuc.calspeed.android.CalspeedFragment.NetworkLatLong;

/**
 * Handle the location related functions and listeners.
 */
class NdtLocation implements LocationListener {
    public Location location;
    private final Location networkLocation;
    LocationManager locationManager;
    String bestProvider;
    Boolean gpsEnabled;
    Boolean networkEnabled;
    Location networkLastKnownLocation;
    Location gpsLastKnownLocation;
    private final NetworkLatLong networkLatLong;
    private final LatLong latLongptr;


    /**
     * Passes context to this class to initialize members.
     *
     * @param context context which is currently running
     */
    NdtLocation(Context context, Handler handler, NetworkLatLong networkLatLong, LatLong latLong) {
        /*
         * Location variable, publicly accessible to provide access to geographic data.
         */
        this.networkLatLong = networkLatLong;
        this.latLongptr = latLong;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Iterator<String> providers = locationManager.getAllProviders().iterator();
        location = null;
        networkLocation = new Location(LocationManager.NETWORK_PROVIDER);
        networkLocation.setLatitude(0.0);
        networkLocation.setLongitude(0.0);
        while (providers.hasNext()) {
            Log.v(getClass().getName(), providers.next());
        }
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        bestProvider = locationManager.getBestProvider(criteria, true);
        Log.v("debug", "Best provider is:" + bestProvider);
        addGPSStatusListener();
        addNetworkListener();
    }

    void addGPSStatusListener() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.addGpsStatusListener(onGpsStatusChange);
                gpsEnabled = true;
            } else {
                gpsEnabled = false;
            }
        } catch (SecurityException e) {
            gpsEnabled = false;
        }

    }

    void removeGPSStatusListener() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.removeGpsStatusListener(onGpsStatusChange);
            gpsEnabled = false;
        }

    }

    private final GpsStatus.Listener onGpsStatusChange = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.d(getClass().getSimpleName(), "GPS event started");
                    startListen();
                    latLongptr.updateLatitudeLongitude();
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.d(getClass().getSimpleName(), "GPS first fix");
                    latLongptr.updateLatitudeLongitude();
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.d(getClass().getSimpleName(), "GPS event stopped");
                    stopListen();
                    location = null;
                    latLongptr.updateLatitudeLongitude();
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.v(getClass().getSimpleName(), "Satellite status changed");
                    break ;
            }
        }
    } ;

    void addNetworkListener() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        2000, 0, NetworkLocationListener);
                networkEnabled = true;
            } else {
                networkEnabled = false;
            }
        } catch (SecurityException e) {
            networkEnabled = false;
        }
    }

    void stopNetworkListenerUpdates() {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.removeUpdates(NetworkLocationListener);
            networkEnabled = false;
        }
    }

    void startNetworkListenerUpdates() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        2000, 0, NetworkLocationListener);
                networkEnabled = true;
            }
        } catch (SecurityException e) {
            networkEnabled = false;
        }
    }

    //Define a listener that responds to Network location updates
    private final LocationListener NetworkLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            networkLatLong.updateNetworkLatitudeLongitude(location);
            networkLocation.set(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };


    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        latLongptr.updateLatitudeLongitude();

    }

    @Override
    public void onProviderDisabled(String provider) {
        stopListen();
        location = null;
        latLongptr.updateLatitudeLongitude();
    }

    @Override
    public void onProviderEnabled(String provider) {
        startListen();
        latLongptr.updateLatitudeLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                if (Constants.DEBUG)
                    Log.v("debug", "Status Changed: Out of Service");
                stopListen();
                location = null;
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                if (Constants.DEBUG)
                    Log.v("debug", "Status Changed: Temporarily Unavailable");
                stopListen();
                location = null;
                break;
            case LocationProvider.AVAILABLE:
                if (Constants.DEBUG)
                    Log.v("debug", "Status Changed: Available");
                startListen();
                break;
        }
    }

    /**
     * Stops requesting the location update.
     */
    void stopListen() {
        locationManager.removeUpdates(this);
    }

    /**
     * Begins to request the location update.
     */
    void startListen() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        } catch (SecurityException e) {
            Log.e(getClass().getName(), "No permission: GPS provider");
        }
    }
}
