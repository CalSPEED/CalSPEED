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
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;


public class AdvertisedMobileFragment extends SherlockFragment {
    static AdvertisedData data = new AdvertisedData();
    static String address;

    public static void newInstance(AdvertisedData d, String add) {
        data = d;
        address = add;
    }

    public static Bundle createBundle(String title) {
        Bundle bundle = new Bundle();
        bundle.putString("mobile", title);
        return bundle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("Viewer", "***************** AdvertisedMobileFragment.java - onCreateView() invoked");
        View view = inflater.inflate(R.layout.adver_mobile_frag, container, false);
        ListView listView = view.findViewById(R.id.list);
        TextView addressDisplay = view.findViewById(R.id.address);
        addressDisplay.setText(address);

        ImageView legendImage = view.findViewById(R.id.legend);
        legendImage.setImageResource(R.drawable.legend);

        TreeSet<DisplayResults> dataList = new TreeSet<>(new DisplayResultsComparator());
        for (int i = 0; i < data.features.size(); i++) {
            AdvertisedData.Attributes attributes = data.features.get(i).attributes;
            if (attributes == null) {
                addressDisplay.setText(R.string.no_mobile_data);
                Log.i(getClass().getName(), "Data Attributes are null");
                listView.setAdapter(new MyCustomBaseAdapter(getActivity(),
                        new ArrayList<DisplayResults>()));
                return view;
            }
            if (attributes.CONS_BUS.equalsIgnoreCase(getString(R.string.consumer))) {
                if (attributes.serviceTyp.equalsIgnoreCase(getString(R.string.viewer_mobile))) {
                    String dbaName = attributes.dbaName;
                    String adUp = attributes.minAdUp;
                    String adDn = attributes.minAdDown;
                    String techCode = attributes.techCode;
                    Log.v(getClass().getName(), "Mobile: " + dbaName + " / UP: " + adUp +
                            " / DOWN: " + adDn + " / Tech Code: " + techCode);
                    if ((adUp != null) && (adDn != null)) {
                        DisplayResults displayResult = new DisplayResults(dbaName,
                                Key.getUpBucketKey(adUp), Key.getDownBucketKey(adDn), techCode);
                        if ((displayResult.getDownloadIndex() != 23) &&
                                (displayResult.getUploadIndex() != 11)) {
                            Log.v(getClass().getName(), "Adding: " + dbaName);
                            dataList.add(displayResult);
                        }
                    }
                }
            }
        }
        // If no carrier exists, display a message.
        if (dataList.size() == 0) {
            addressDisplay.setText(R.string.no_mobile_data);
        }
        listView.setAdapter(new MyCustomBaseAdapter(getActivity(), new ArrayList<>(dataList)));
        return view;
    }
}