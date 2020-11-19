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

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HistoryResultsAdapter extends ArrayAdapter<History>{

	private ArrayList<History> result = new ArrayList<History>();

	private final Context context;

	static class ViewHolder {
		public TextView id;
		public TextView date;
		public TextView time;
		public TextView upload;
		public TextView download;
		public TextView latency;
		public TextView videoQuality;
		public TextView mosValue;
		public TextView uploadLabel;
		public TextView downloadLabel;
	}

	public HistoryResultsAdapter(Context context, ArrayList<History> result) {
		super(context, R.layout.historylist,result);
		this.result = result;

		this.context = context;

	}


	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.historylist, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.id = v.findViewById(R.id.historyID);
			viewHolder.date = v.findViewById(R.id.historyDate);
			viewHolder.time = v.findViewById(R.id.historyTime);
			viewHolder.upload = v.findViewById(R.id.historyUpload);
			viewHolder.uploadLabel = v.findViewById(R.id.historyListUpload);
			viewHolder.download = v.findViewById(R.id.historyDownload);
			viewHolder.downloadLabel = v.findViewById(R.id.historyListDownload);
			viewHolder.latency = v.findViewById(R.id.historyLatency);
			viewHolder.videoQuality = v.findViewById(R.id.historyVideoQuality);
			viewHolder.mosValue = v.findViewById(R.id.historyMosValue);
			v.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) v.getTag();
		DecimalFormat df = new DecimalFormat("0.00");
		holder.id.setText(result.get(position).getStringID());
		holder.date.setText(result.get(position).getFormattedDate());
		holder.time.setText(result.get(position).getTime());
		try {
			holder.upload.setText(df.format(Double.parseDouble(result.get(position).getUploadAverage()) / 1000));
		}
		catch(Exception e) {
			holder.upload.setText(result.get(position).getUploadAverage());
		}
		try {
			holder.download.setText(df.format(Double.parseDouble(result.get(position).getDownloadAverage()) / 1000));
		}
		catch(Exception e) {
			holder.download.setText(result.get(position).getDownloadAverage());
		}

		holder.latency.setText(result.get(position).getLatencyAverage());
		Log.v("HistoryResultsAdapter", "video quality is " + result.get(position).getVideo());
		holder.videoQuality.setText(result.get(position).getVideo());
		Log.v("HistoryResultsAdapter", "MOS value is " + result.get(position).getMosValue());
		holder.mosValue.setText(result.get(position).getMosValue());
		return (v);
	}

}
