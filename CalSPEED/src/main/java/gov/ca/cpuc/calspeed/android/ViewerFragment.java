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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

public class ViewerFragment extends SherlockFragment implements
		OnMapLongClickListener, OnInfoWindowClickListener, OnMapReadyCallback {
    private LatLng target;
	private Location location;
    private GoogleMap mMap;
	private String address;
	private String request;
    private Marker marker;
	private Intent intent;
	private EditText search;
	private View myview;
	CustomSupportMapFragment mMapFragment;
    AlertDialog alert;


	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// called when tab/page visibility changed
		super.setUserVisibleHint(isVisibleToUser);
		if(mMap != null) {
			mMap.getUiSettings().setScrollGesturesEnabled(true);
		}

		// Make sure that we are currently visible
		if (this.isVisible()) {
            // A custom actionbar with an address search function.
            ActionBar actionBar = ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
            actionBar.setCustomView(R.layout.actionbar_for_viewer);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                    | ActionBar.DISPLAY_SHOW_HOME);
            Log.v(this.getClass().getName(), "Calling enableSearch() from setUserVisibleHint");
            enableSearch();
            Log.v(this.getClass().getName(), "Calling onStart() from setUserVisibleHint");
            // If we are becoming invisible, then...
            if (!isVisibleToUser) {
                Log.d(this.getClass().getName(), "Not visible to user");
                // For non-MapViewer page, set search bar invisible
                if (actionBar.getSelectedNavigationIndex() != 2) {
                    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                            | ActionBar.DISPLAY_SHOW_TITLE);
                }
                this.onStart();
            }
        }
        if (isVisibleToUser) {
            SharedPreferences mapViewIntro = getActivity().getSharedPreferences("MapViewData", Context.MODE_PRIVATE);
            if (!(mapViewIntro.getBoolean("mapViewPreference", false)) && (((TabSwipeActivity) getActivity()).isCurrentTab(2))) {
                if (alert == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.mapview_dialog).setCancelable(false)
                            .setPositiveButton("Okay",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            alert = null;
                                        }
                                    });
                    builder.setNegativeButton("Don't show again",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences mapViewIntro = getActivity()
                                            .getSharedPreferences("MapViewData",
                                                    Constants.MODE_PRIVATE);
                                    SharedPreferences.Editor mapViewIntroEditor = mapViewIntro.edit();
                                    mapViewIntroEditor.putBoolean("mapViewPreference", true);
                                    mapViewIntroEditor.apply();
                                    dialog.cancel();
                                    alert = null;
                                }
                            });
                    alert = builder.create();
                    alert.show();
                } else {
                    Log.d(this.getClass().getName(), "build not null, won't create a new dialog");
                }
            }
        }
    }

	@Override
	public void onStart() {
        Log.v(this.getClass().getName(), "Calling onStart()");
		FragmentManager myFragmentManager = getActivity().getSupportFragmentManager();
		mMapFragment = (CustomSupportMapFragment) myFragmentManager
                .findFragmentById(R.id.mapFragment);
		mMapFragment.getMapAsync(this);
		intent = new Intent(getActivity(), DisplayInfo.class);
		
		// To include an option menu such as "search".
		setHasOptionsMenu(true);

		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// A custom actionbar with an address search function.
		ActionBar actionBar = ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
		actionBar.setCustomView(R.layout.actionbar_for_viewer);
        enableSearch();
		super.onStart();
	}

    
	//added for temporary Google Geocode
	private String jsonCoord(String address) throws IOException {
		URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" +
				address + "&components=administrative_area:CA|country:US&sensor=false&" +
				"key=AIzaSyBErj9HpaVi13AjChFZ2d8DhRJkCHuth0s");
        Log.v("JSONCoord", "Making connection to " + url.toString());
		URLConnection connection = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		String jsonResult = "";
		while ((inputLine = in.readLine()) != null) {
			jsonResult += inputLine;
		}
		in.close();
		Log.v("JSONCoord", jsonResult);
		return jsonResult;
	}

	public static Bundle createBundle(String title) {
		Bundle bundle = new Bundle();
		bundle.putString("title", title);
		return bundle;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        Log.v(this.getClass().getName(), "entering into onCreateView()");
		if (myview != null) {
			ViewGroup parent = (ViewGroup) myview.getParent();
			if (parent != null)
				parent.removeView(myview);
		}
		try {
			myview = inflater.inflate(R.layout.viewer_fragment, container,
					false);
		} catch (InflateException e) {
            Log.e(this.getClass().getName(), e.toString());
        }
		return myview;
	}

	public void enableSearch() {
        ActionBar actionBar = ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
        search = actionBar.getCustomView().findViewById(R.id.mainfield);
        search.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(this.getClass().getName(), "search bar in onEditorAction()");
                if (marker != null) {
                    mMap.clear();
                }
                address = search.getText().toString();

                // Hide the virtual keyboard
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(search.getWindowToken(), 0);

                // No address is entered by a user. Nothing should happen.
                if (address.equals("")) {
                    return false;
                }
                try {
                    request = URLEncoder.encode(address, "UTF-8");
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                Gson gson = new Gson();
                GoogleGeoCodeResponse result;
                try {
                    result = gson.fromJson(jsonCoord(request),GoogleGeoCodeResponse.class);

                    //if not address is found
                    if(!result.status.equalsIgnoreCase("OK")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(
                                "Sorry, that address was not found. Please try entering a more complete address in California.")
                                .setCancelable(false)
                                .setPositiveButton("Okay",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                        return true;
                    }
                    double lat = Double.parseDouble(result.results[0].geometry.location.lat);
                    double lng = Double.parseDouble(result.results[0].geometry.location.lng);
                    String formatAddress = result.results[0].formatted_address;
                    target = new LatLng(lat,lng);
                    try {
                        drawSearchResult(formatAddress);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (JsonSyntaxException | IOException e1) {
                    e1.printStackTrace();
                }
                return true;
            }
        });
    }

	//public void drawSearchResult(final List<Candidates> list)
	public void drawSearchResult(String googleAddress)
			throws InterruptedException {
		// Put the selected address on the center of the map.
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 12));
		marker = mMap.addMarker(new MarkerOptions().position(target));
		address = googleAddress.replace(", USA", "");
		marker.setSnippet(address);
		marker.setTitle("Selected Location:");
		marker.showInfoWindow();
    }

	@Override
	public void onMapLongClick(LatLng arg0) {
		mMap.clear();
		marker = null;
		//check to make sure location is not null
		if(!(location==null)){
			//check to see what zoom level is, then set proximity to blue dot - JC
			int currentZoom = (int)mMap.getCameraPosition().zoom;
			double distanceInMeters;
			switch(currentZoom){
				case 14: distanceInMeters = 200.0;
					break;
				case 15: distanceInMeters = 150.0;
					break;
				case 16: distanceInMeters = 100.0;
					break;
				case 17: distanceInMeters = 50.0;
					break;
				case 18: distanceInMeters = 25.0;
					break;
				case 19: distanceInMeters = 10.0;
					break;
				case 20: distanceInMeters = 10.0;
					break;
				case 21: distanceInMeters = 10.0;
					break;
				default: distanceInMeters = 10.0;
					break;
			}
			
			// Checks to see if user selected a location less than distanceInMeters from current test location. 
			float [] dist = new float[1];
			Location.distanceBetween(arg0.latitude, arg0.longitude, location.getLatitude(),location.getLongitude(), dist);
			if(dist[0] < distanceInMeters){
				target = new LatLng(location.getLatitude(), location.getLongitude());
			}
			else{
				target = new LatLng(arg0.latitude, arg0.longitude);
			}
		}
		else{
			target = new LatLng(arg0.latitude, arg0.longitude);
		}
		// Drop a marker from top to the location.
		// Code from
		// http://stackoverflow.com/questions/16604206/drop-marker-slowly-from-top-of-screen-to-location-on-android-map-v2

		final long duration = 400;
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = mMap.getProjection();

		Point startPoint = proj.toScreenLocation(target);
		startPoint.y = 0;
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		final Interpolator interpolator = new LinearInterpolator();

		marker = mMap.addMarker(new MarkerOptions().position(new LatLng(
				startLatLng.latitude, startLatLng.longitude)));

		handler.post(new Runnable() {
			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed
						/ duration);
				double lng = t * target.longitude + (1 - t)
						* startLatLng.longitude;
				double lat = t * target.latitude + (1 - t)
						* startLatLng.latitude;
				if (t < 1.0) {
					marker.setPosition(new LatLng(lat, lng));
					// Post again 40ms later.
					handler.postDelayed(this, 40);
				} else { // End of animation.
					marker.setPosition(new LatLng(target.latitude,
							target.longitude));
					marker.setTitle("Selected Location:");
					marker.showInfoWindow();
					// Google API
					String reverseGeo = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
							+ target.latitude
							+ ","
							+ target.longitude
							+ "&sensor=true&key=AIzaSyBErj9HpaVi13AjChFZ2d8DhRJkCHuth0s";
                    String geoCodingResults = "";
					try {
						final ReverseGeo geoCoder = new ReverseGeo();
						geoCoder.execute(reverseGeo);
						geoCodingResults = geoCoder.get();
						Handler handler = new Handler();
						handler.postDelayed(new Runnable(){
							@Override
							public void run() {
								if (geoCoder.getStatus() == AsyncTask.Status.RUNNING) {
									geoCoder.cancel(true);

									AlertDialog.Builder builder = new AlertDialog.Builder(
											getActivity());
									builder.setTitle("Timeout")
											.setMessage(
													"A network timeout error occurred. Please try again later.")
											.setCancelable(false)
											.setNegativeButton("Dismiss",
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog, int id) {
															dialog.dismiss();
														}
													});
									AlertDialog alert = builder.create();
									alert.show();
									
									mMap.clear();
									marker = null;
								}
							}
							
						}, 20000);
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
                    try {
						JSONObject jsonObject = new JSONObject(geoCodingResults);
                        Log.d("JSONResult", jsonObject.toString());
						
						String statusGeo = jsonObject.getString("status");
                        Log.d("JSONResult", statusGeo);
						//If Google Reverse Geocode doesn't find it, try MapQuest
						if(!statusGeo.equals("OK")){
							String latVal = String.valueOf(target.latitude);
							String lngVal = String.valueOf(target.longitude);
                            try {
								reverseGeo = "http://www.mapquestapi.com/geocoding/v1/reverse?key=Fmjtd%7Cluubnu6bn1%2Cr2%3Do5-9uyn5w&json="
								   + URLEncoder.encode("{location:{latLng:{lat:", "UTF-8") 
								   + URLEncoder.encode(latVal, "UTF-8")
								   + ",lng:"
								   + URLEncoder.encode(lngVal, "UTF-8")
								   + URLEncoder.encode("}}}","UTF-8");
								final ReverseGeo geoCoder = new ReverseGeo();
								geoCoder.execute(reverseGeo);
								geoCodingResults = geoCoder.get();
								Handler handler = new Handler();
								handler.postDelayed(new Runnable(){
									@Override
									public void run() {
										if (geoCoder.getStatus() == AsyncTask.Status.RUNNING) {
											geoCoder.cancel(true);

											AlertDialog.Builder builder = new AlertDialog.Builder(
													getActivity());
											builder.setTitle("Timeout")
													.setMessage(
															"A network timeout error occurred. Please try again later.")
													.setCancelable(false)
													.setNegativeButton("Dismiss",
															new DialogInterface.OnClickListener() {
																public void onClick(
																		DialogInterface dialog, int id) {
																	dialog.dismiss();
																}
															});
											AlertDialog alert = builder.create();
											alert.show();
											
											mMap.clear();
											marker = null;
										}
									}
									
								}, 20000);
								jsonObject = new JSONObject(geoCodingResults);
								
							} catch (UnsupportedEncodingException e1) {
								e1.printStackTrace();
							}	
							
							JSONArray resultsItems = new JSONArray(jsonObject.getString("results"));
                            Log.d("JSONResult", jsonObject.toString());
							
							// get the first formatted_address value
							if (resultsItems.length() == 0) {
								address = "UNKNOWN ADDRESS. TRY AGAIN!!!";

							} else {
								JSONObject resultItem = resultsItems.getJSONObject(0);
															
								//For Mapquest, replace address with Full Address
								JSONArray newArray = new JSONArray(resultItem.getString("locations"));
								JSONObject resultItem2 = newArray.getJSONObject(0);
								String addStreet = resultItem2.getString("street");
								String addCity = resultItem2.getString("adminArea5");
								String addState = resultItem2.getString("adminArea3");
								String addZip = resultItem2.getString("postalCode");
								address = addStreet + ", " + addCity + ", " + addState + ", " + addZip;
							}
						}
						//Otherwise, just use the Google Reverse Geocode
						else{
							
							JSONArray resultsItems = new JSONArray(jsonObject.getString("results"));
							// get the first formatted_address value
							if (resultsItems.length() == 0) {
								address = "UNKNOWN ADDRESS. TRY AGAIN!!!";
							} else {
								JSONObject resultItem = resultsItems
										.getJSONObject(0);
								address = resultItem.getString("formatted_address");

								// If the address has ", USA" at the end, remove it.
								address = address.replace(", USA", "");
							}
							
						}
					} catch (Exception e) {
						Log.d("Viewer", e.getLocalizedMessage());
						address = null;
					}

					marker.setSnippet(address);
					marker.showInfoWindow();
				}
			}
		});
    }

	@Override
	public void onInfoWindowClick(Marker marker) {
        try {
            if (address.equalsIgnoreCase("UNKNOWN ADDRESS. TRY AGAIN!!!")) {
                return; // Nothing to do if we don't know the address.
            }
        }
        catch (Exception e) {
            Log.d("Viewer", e.getLocalizedMessage());
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    getActivity());
            builder.setTitle("No Connectivity")
                    .setMessage(
                            "A network connectivity error occurred. Please try again later.")
                    .setCancelable(false)
                    .setNegativeButton("Dismiss",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
		//if address is not in California, do nothing
		if (!address.contains(", CA")) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					getActivity());
			alertDialogBuilder.setTitle("No Results");
			alertDialogBuilder
					.setMessage("We are sorry. Map View is available only for California regions.")
					.setCancelable(false)
					.setNegativeButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});

			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
			return;
		}

		// Get x and y coordinates from the target latitude and longitude
        // A better formatted way of getting the geometry
        String URLXY = String.format(Constants.ARCGIS_GEOMETRYSERVER, Constants.IN_SECRET,
                Constants.OUT_SECRET, target.longitude, target.latitude);

		// GetXYCoordinates(MainActivity.this).execute(URLXY).get();
		final ProgressDialog dialog = new ProgressDialog(getActivity());
		final GetXYCoordinates xyDataHandler = new GetXYCoordinates(dialog);
		xyDataHandler.execute(URLXY);

		// 20 seconds timeout to get the X/Y coordinates
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (xyDataHandler.getStatus() == AsyncTask.Status.RUNNING) {
					xyDataHandler.cancel(true);
					dialog.dismiss();

					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					builder.setTitle("Timeout")
							.setMessage(
									"A network timeout error occurred. Please try again later.")
							.setCancelable(false)
							.setNegativeButton("Dismiss",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.dismiss();
										}
									});
					AlertDialog alert = builder.create();
					alert.show();
				}
			}
		}, 20000);

		xyDataHandler.addObserver(new GetXYCoordinates.Callback() {
            @Override
			public void onComplete(XYCoordinates data) {
                if (data != null) {
                    intent.putExtra("address", address);
                    intent.putExtra("x", data.geometries.get(0).x);
                    intent.putExtra("y", data.geometries.get(0).y);
                    startActivity(intent);
                }
			}
		});

	}

    @Override
	public void onPause() {
		if(mMap != null) {
			mMap.setMyLocationEnabled(false);
			mMap.clear();
		}
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
		super.onPause();
	}
	
	@Override
	public void onStop() {
		if(mMap != null) {
			mMap.setMyLocationEnabled(false);
			mMap.clear();
		}
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		if(mMap != null)
			mMap.setMyLocationEnabled(false);
		super.onDestroy();
	}
	
	@Override
	public void onResume() {
        Log.v(this.getClass().getName(), "Calling onResume()");
		if (mMap != null) {
            Log.v(this.getClass().getName(), "mMap is not null");
            mMap.setMyLocationEnabled(false);
        }
		super.onResume();
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
        Log.d("CalSPEEDLocation", "Calling onMapReady()");
		this.mMap = googleMap;
		location = ((MainActivity) getActivity()).getLocationFromCalspeedFragment();
		// If the app doesn't know the location, it displays the center of
		// California.
		mMapFragment.setMap(this.mMap);
        LatLng currentLocation;
        double longitude;
        double latitude;
        if (location == null) {
            Log.v(this.getClass().getName(), "Null location, reset camera");
			mMap.setMyLocationEnabled(false);
			longitude = -119.838867;
			latitude = 36.742993;
			currentLocation = new LatLng(latitude, longitude);
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 6));
		} else {
            Log.v(this.getClass().getName(), "Has location, set camera to long/lat");
			mMap.setMyLocationEnabled(true);
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			currentLocation = new LatLng(latitude, longitude);
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));

		}

		mMap.setOnMapLongClickListener(this);
		mMap.setOnInfoWindowClickListener(this);

	}

}
