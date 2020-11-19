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


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import java.util.ArrayList;
import java.util.TreeSet;

public class AdvertisedFixedFragment extends SherlockFragment {
    static AdvertisedData data = new AdvertisedData();
    static String address;
    View view;

    public static void newInstance(AdvertisedData d, String add) {
        data = d;
        address = add;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ListView listView;
        TextView addressDisplay;
        ImageView legendImage;

        Log.v("Viewer", "****************** AdvertisedFixedFragment.java - onCreateView() invoked");
        view = inflater.inflate(R.layout.adver_fixed_frag, container, false);
        listView = view.findViewById(R.id.list);
        addressDisplay = view.findViewById(R.id.address);
        addressDisplay.setText(address);

        legendImage = view.findViewById(R.id.legend);
        legendImage.setImageResource(R.drawable.legend);
        TreeSet<DisplayResults> dataList = new TreeSet<>(new DisplayResultsComparator());
        if (data == null) {
            addressDisplay.setText(R.string.no_fixed_data);
            Log.i(getClass().getName(), "Data is null");
            listView.setAdapter(new MyCustomBaseAdapter(getActivity(), new ArrayList<DisplayResults>()));
            return view;
        }
        if (data.features != null) {
            for (int i = 0; i < data.features.size(); i++) {
                AdvertisedData.Attributes attributes = data.features.get(i).attributes;
                if (attributes == null) {
                    addressDisplay.setText(R.string.no_fixed_data);
                    Log.i(getClass().getName(), "Data Attributes are null");
                    listView.setAdapter(new MyCustomBaseAdapter(getActivity(), new ArrayList<DisplayResults>()));
                    return view;
                }
                Log.v(getClass().getName(), "Getting service attributes");
                Log.d(getClass().getName(), String.valueOf(attributes.CONS_BUS));
                if ((attributes.CONS_BUS).equalsIgnoreCase("consumer")) {
                    Log.d(getClass().getName(), String.valueOf(attributes));
                    Log.d(getClass().getName(), String.valueOf(attributes.serviceTyp));
                    if (attributes.serviceTyp.equalsIgnoreCase(getString(R.string.ad_wireline)) ||
                            attributes.serviceTyp.equalsIgnoreCase(
                                    getString(R.string.ad_fixed_wireless))) {
                        String dbaName = attributes.dbaName;
                        String adUp = attributes.maxAdUp;
                        String adDown = attributes.maxAdDown;
                        String techCode = attributes.techCode;
                        if (attributes.serviceTyp.equalsIgnoreCase(getString(R.string.ad_wireline))) {
                            Log.v(getClass().getName(), "Wireline: " + dbaName + " / (UP)" + adUp +
                                    " / (DOWN)" + adDown + " / Tech Name " + techCode);
                        } else if (attributes.serviceTyp.equalsIgnoreCase(getString(R.string.ad_fixed_wireless))) {
                            Log.v(getClass().getName(), "Fixed Wireless: " + dbaName + " / (UP)" + adUp +
                                    " / (DOWN)" + adDown + " / Tech Name " + techCode);
                        }
                        if ((adUp != null) && (adDown != null)) {
                            DisplayResults displayResult = new DisplayResults(dbaName,
                                    Key.getUpBucketKey(adUp), Key.getDownBucketKey(adDown), techCode);
                            if ((displayResult.getDownloadIndex() != 23) &&
                                    (displayResult.getUploadIndex() != 11)) {
                                dataList.add(displayResult);
                            }
                        }
                    }
                }
            }
        }
        if (dataList.size() == 0) {
            addressDisplay.setText(R.string.no_fixed_data);
        }
        Log.v(getClass().getName(), "TreeSet is: " + dataList.toString());
        Log.d(getClass().getName(), "ArrayList is: " + (new ArrayList<>(dataList)).toString());
        listView.setAdapter(new MyCustomBaseAdapter(getActivity(), new ArrayList<>(dataList)));
        return view;
    }

    public static Bundle createBundle(String title) {
        Bundle bundle = new Bundle();
        bundle.putString("fixed", title);
        return bundle;
    }

}
