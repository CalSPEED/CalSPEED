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

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public abstract class DisplayInfoTabSwipeActivity extends SherlockFragmentActivity {

    private ViewPager mViewPager;
    private TabsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
         * Create the ViewPager and our custom adapter
         */
        mViewPager = new ViewPager(this);
        adapter = new TabsAdapter( this, mViewPager );
        mViewPager.setAdapter( adapter );
        mViewPager.setOnPageChangeListener( adapter );

        /*
         * We need to provide an ID for the ViewPager, otherwise we will get an exception like:
         *
         * java.lang.IllegalArgumentException: No view found for id 0xffffffff for fragment TestFragment{40de5b90 #0 id=0xffffffff android:switcher:-1:0}
         * at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:864)
         *
         * The ID 0x7F04FFF0 is large enough to probably never be used for anything else
         */
        mViewPager.setId( R.id.displayInfoViewPager );

        super.onCreate(savedInstanceState);

        /*
         * Set the ViewPager as the content view
         */
        setContentView(mViewPager);
    }

    protected void addTab(int titleRes, Class<? extends Fragment> fragmentClass, Bundle args ) {
        adapter.addTab( getString( titleRes ), fragmentClass, args );
    }

    protected void addTab(CharSequence title, Class<? extends Fragment> fragmentClass, Bundle args ) {
        adapter.addTab( title, fragmentClass, args );
    }

    protected void removeTab(int index ) {
        adapter.removeTab( index );
    }

    protected void removeTabs(int index, int index2) {
        adapter.removeTabs(index, index2);
    }

    protected boolean isCurrentTab(int position) {
        return adapter.isCurrentTab(position);
    }

    protected void selectPage(int position) {
        adapter.onPageSelected(position);
    }



    private static class TabsAdapter extends FragmentPagerAdapter implements TabListener, ViewPager.OnPageChangeListener {

        private final SherlockFragmentActivity mActivity;
        private final ActionBar mActionBar;
        private final ViewPager mPager;

        public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            this.mActivity = activity;
            this.mActionBar = activity.getSupportActionBar();
            this.mPager = pager;

            mActionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_TABS );
        }

        private static class TabInfo {
            public final Class<? extends Fragment> fragmentClass;
            public final Bundle args;
            public TabInfo(Class<? extends Fragment> fragmentClass,
                           Bundle args) {
                this.fragmentClass = fragmentClass;
                this.args = args;
            }
        }

        private final List<TabInfo> mTabs = new ArrayList<TabInfo>();

        public void addTab( CharSequence title, Class<? extends Fragment> fragmentClass, Bundle args ) {
            final TabInfo tabInfo = new TabInfo( fragmentClass, args );

            Tab tab = mActionBar.newTab();
            tab.setText( title );
            tab.setTabListener( this );
            tab.setTag( tabInfo );

            mTabs.add( tabInfo );

            mActionBar.addTab( tab );
            notifyDataSetChanged();
        }

        public void removeTab( int index ) {

            mTabs.remove( index );

            mActionBar.removeTab( mActionBar.getTabAt(index) );
            notifyDataSetChanged();
        }

        public void removeTabs(int index, int index2) {
            mTabs.remove(index2);
            mTabs.remove(index);

            mActionBar.removeTab(mActionBar.getTabAt(index2));
            mActionBar.removeTab(mActionBar.getTabAt(index));
            notifyDataSetChanged();
        }

        public boolean isCurrentTab(int position) {
            return mPager.getCurrentItem() == position;
        }

        @Override
        public Fragment getItem(int position) {
            final TabInfo tabInfo = mTabs.get( position );
            return Fragment.instantiate( mActivity, tabInfo.fragmentClass.getName(), tabInfo.args );
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        public void onPageScrollStateChanged(int arg0) {
            Utils.hideKeyboard(mActivity);
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
            Utils.hideKeyboard(mActivity);
        }

        public void onPageSelected(int position) {
        	/*
        	 * Select tab when user swiped
        	 */
            Utils.hideKeyboard(mActivity);
            mActionBar.setSelectedNavigationItem( position );
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
        	/*
        	 * Slide to selected fragment when user selected tab
        	 */
            Utils.hideKeyboard(mActivity);
            TabInfo tabInfo = (TabInfo) tab.getTag();
            for ( int i = 0; i < mTabs.size(); i++ ) {
                if ( mTabs.get( i ) == tabInfo ) {
                    mPager.setCurrentItem( i );
                }
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            Utils.hideKeyboard(mActivity);
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            Utils.hideKeyboard(mActivity);
        }
    }
}