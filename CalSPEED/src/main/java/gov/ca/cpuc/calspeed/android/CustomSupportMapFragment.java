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
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by nigel on 6/8/2017.
 */

public class CustomSupportMapFragment extends SupportMapFragment  {
    public View mOriginalContentView;
    public TouchableWrapper mTouchView;
    int SWIPE_THRESHOLD = 100;
    int SWIPE_VELOCITY_THRESHOLD = 10;
    GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.v(this.getClass().getName(), "entering into onCreateView()");
        mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
        mTouchView = new TouchableWrapper(getActivity());
        mTouchView.addView(mOriginalContentView);
        return mTouchView;

    }
    public void setMap(GoogleMap mMap)
    {
        this.mMap = mMap;
        mTouchView.setMap(mMap);
    }
    private class TouchableWrapper extends FrameLayout {
        private final GestureDetector gestureDetector;
        GoogleMap mMap;
        public void setMap(GoogleMap mMap)
        {
            this.mMap = mMap;
        }
        public TouchableWrapper(Context context) {
            super(context);
            Log.v(this.getClass().getName(), "calling TouchableWrapper constructor");
            gestureDetector = new GestureDetector(context, new GestureDetector.OnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return false;
                }

                @Override
                public void onShowPress(MotionEvent e) {

                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {

                    return false;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                    return false;
                }

                @Override
                public void onLongPress(MotionEvent e) {


                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    boolean result = false;
                    try {
                        float diffY = e2.getY() - e1.getY();
                        float diffX = e2.getX() - e1.getX();
                        if (Math.abs(diffX) > Math.abs(diffY)) {
                            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                                if (diffX > 0 && e1.getX() < 100) {
                                    //((MainActivity)getActivity()).selectPage(1);

                                } else {
                                    //swiping left
                                }
                            }
                        } else {
                            // onTouch(e);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    return result;
                }
            });
        }
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev)
        {
            gestureDetector.onTouchEvent(ev);
            return false;
        }
        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    if(event.getX() < 100 && mMap != null)
                    {
                        mMap.getUiSettings().setScrollGesturesEnabled(false);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if(mMap != null)
                        mMap.getUiSettings().setScrollGesturesEnabled(true);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    if(mMap != null)
                        mMap.getUiSettings().setScrollGesturesEnabled(true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    //Log.d("INPUT", "Raw x: " + event.getRawX() + " Raw y: " + event.getRawY());
                    break;
            }
            return super.dispatchTouchEvent(event);
        }
    }
}
