package com.dextender.dextender_h2o;



import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

//============================================================================
// Class: MyActivity
// Author: MLV
//
// Purpose: The main enchilada.
//          After the splash screen is displayed, this guy is called.
//          It sets up the whole tabbing infrastructure, and then calls in
//          the alarmmanager, which sets up the service. In some tutorials
//          they show the alarm manager being called in the 'onresume' which
//          is kind of wrong in two ways.
//          A) That 'stuff' really belongs in a function (which I did)
//          B) It should be called in the onCreate method, so it's not a
//             constant churn to the alarm manager.
//
// Notes  : There's probably a better way of making sure that once the alarm
//          manager is set, that we can ignore it.. but I can see why we need
//          to call it (if we're doing software updates). So.. leave for now as is
//============================================================================
public class MyActivity extends Activity implements ActionBar.TabListener {

    final static int NUMBER_OF_TABS=6;

    //-----------------------------------------------------------------------
    //  The {@link android.support.v4.view.PagerAdapter} that will provide
    //  fragments for each of the sections. We use a
    //  {@link FragmentPagerAdapter} derivative, which will keep every
    //  loaded fragment in memory. If this becomes too memory intensive, it
    //  may be best to switch to a
    //  {@link android.support.v13.app.FragmentStatePagerAdapter}.
    //-----------------------------------------------------------------------
    SectionsPagerAdapter mSectionsPagerAdapter;

    //--------------------------------------------------------------
    // The {@link ViewPager} that will host the section contents.
    //--------------------------------------------------------------
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);



        if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof MyExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler("/sdcard"));
            //Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(Environment));
        }

        //----------------------------------------------------------------------------------
        // Since API 11, you can't make http calls on the main thread. since the calls are
        // lightweight (and the background program was written by me in C++), we can override
        // this restriction with the following
        //----------------------------------------------------------------------------------
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.activity_layout);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // MLV - Action bar clicks are different than tab clicks ...
        //       ie. this will get invoked when clicking "settings" "Add Sequence" or "Add Schedule"
        int id = item.getItemId();
        Intent menuIntent;
        switch(id)
        {
            case R.id.action_settings:
                menuIntent = new Intent("com.dextender.dextender_h2o.PREFERENCE");
                startActivity(menuIntent);
                return true;
            case R.id.menu_addSequence:
                LayoutInflater inflater = this.getLayoutInflater();
                final View     layout   = inflater.inflate(R.layout.dialogue_seqadd, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(layout);
                builder.setTitle("Create a new sequence");
                builder.setMessage("");

                final EditText sequenceName   = (EditText) layout.findViewById(R.id.frag4_addseq_input1);
                final EditText sequenceNumber = (EditText) layout.findViewById(R.id.frag4_addseq_input2);

                builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent openActivity = new Intent("com.dextender.dextender_h2o.ADD_SEQUENCE2");
                        openActivity.putExtra("seqName",      sequenceName.getText().toString());
                        openActivity.putExtra("seqZoneCount", sequenceNumber.getText().toString());
                        startActivity(openActivity);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                builder.show();
                //menuIntent = new Intent("com.dextender.dextender_h2o.ADD_SEQUENCE");
                //startActivity(menuIntent);
                return true;
            case R.id.menu_addSchedule:
                menuIntent = new Intent("com.dextender.dextender_h2o.ADD_SCHEDULE");
                startActivity(menuIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        // MLV - Ok.. this is interesting. If you swipe or click the first tab
        //       the tab.getPosition returns 'i0'.. all the way to 3 (which is how many tabs we have)
        // Log.d("MyActivity", "onTabSelected get item number " + tab.getPosition());
        mViewPager.setCurrentItem(tab.getPosition());

        //--------------------- NEW !! --------------------
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        switch (tab.getPosition()) {
            case 0:
                Intent i0 = new Intent("TAG_REFRESH");
                lbm.sendBroadcast(i0);
                break;
            case 1:
                Intent i1 = new Intent("TAG_REFRESH");
                lbm.sendBroadcast(i1);
                break;
            case 2:
                Intent i2 = new Intent("TAG_REFRESH");
                lbm.sendBroadcast(i2);
                break;
            case 3:
                Intent i3 = new Intent("TAG_REFRESH");
                lbm.sendBroadcast(i3);
                break;
            case 4:
                Intent i4 = new Intent("TAG_REFRESH");
                lbm.sendBroadcast(i4);
                break;
            case 5:
                Intent i5 = new Intent("TAG_REFRESH");
                lbm.sendBroadcast(i5);
                break;
        }

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        // Constructor (similar to c++)
        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment=null;
            switch(position)
            {
                case 0: fragment= new fragment_1();
                    break;
                case 1: fragment= new fragment_2();
                    break;
                case 2: fragment= new fragment_3();
                    break;
                case 3: fragment= new fragment_4();
                    break;
                case 4: fragment= new fragment_5();
                    break;
                case 5: fragment= new fragment_6();
                    break;
            }
            return (fragment);
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
        }

        //============================================================================
        // Method : GetCount
        // Author : MLV
        // Purpose: Return the number of tabs that we will display.
        // NOTE   : When I originally created this page, my android skills were nill
        //          so I followed the tutorials. One of the Interesting things, as I
        //          write this, is "why?" I believe this guys is only being called
        //          from within the same class. Nevertheless, I'm not going to touch it
        //          More comments if I do..
        //============================================================================
        @Override
        public int getCount() {
            // Show 4 total pages.
            return NUMBER_OF_TABS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_section1);
                case 1:
                    return getString(R.string.title_section2);
                case 2:
                    return getString(R.string.title_section3);
                case 3:
                    return getString(R.string.title_section4);
                case 4:
                    return getString(R.string.title_section5);
                case 5:
                    return getString(R.string.title_section6);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_my, container, false);
            return rootView;
        }
    }

    //----------------------------------------------------------------
    // This is for the wakeup service
    //----------------------------------------------------------------
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ViewPager relative = (ViewPager) findViewById(R.id.activity_layout);
        switch(Integer.parseInt(prefs.getString("pref_backgroundImages", "0"))) {
            case 0: relative.setBackgroundResource(0);
                break;
            case 1: relative.setBackgroundResource(R.mipmap.earth_from_space);
                break;
            case 2: relative.setBackgroundResource(R.mipmap.lady_bug_up_grass);
                break;
            case 3: relative.setBackgroundResource(R.mipmap.ladybug3_port);
                break;
            case 4: relative.setBackgroundResource(R.mipmap.dew_on_rose_petal);
                break;
            case 5: relative.setBackgroundResource(R.mipmap.dew_on_grass_bw);
                break;
            case 6: relative.setBackgroundResource(R.mipmap.dew_orb_web);
                break;
            case 7: relative.setBackgroundResource(R.mipmap.dew_web_brownish_background);
                break;
            case 8: relative.setBackgroundResource(R.mipmap.drops_on_surface_bluish);
                break;
            case 9: relative.setBackgroundResource(R.mipmap.shooting_star_over_lake);
                break;
            case 10: relative.setBackgroundResource(R.mipmap.bubbles_rising_to_surface_blue);
                break;
            case 11: relative.setBackgroundResource(R.mipmap.dew_on_dandylions);
                break;
            case 12: relative.setBackgroundResource(R.mipmap.water_drop_splash);
                break;
            case 13: relative.setBackgroundResource(R.mipmap.water_surface_blue);
                break;
            case 14: relative.setBackgroundResource(R.mipmap.raindrops_hitting_surface_lightblue);
                break;
            case 15: relative.setBackgroundResource(R.mipmap.drops_on_big_green_leaf);
                break;
            case 16: relative.setBackgroundResource(R.mipmap.dew_on_blade_grass);
                break;
            case 17: relative.setBackgroundResource(R.mipmap.dew_on_blade_grass_light);
                break;
            case 18: relative.setBackgroundResource(R.mipmap.tall_grass);
                break;
            case 19: relative.setBackgroundResource(R.mipmap.water_drops_on_moss);
                break;
            case 20: relative.setBackgroundResource(R.mipmap.water_on_foreground_grass);
                break;
            case 21: relative.setBackgroundResource(R.mipmap.water);
                break;
            default: relative.setBackgroundResource(0);
                break;
        }

    }


    @Override
    public void onBackPressed() {
        int smallIcon = this.getResources().getIdentifier("tapir2s", "drawable", this.getPackageName());
        new AlertDialog.Builder(this)
                .setIcon(smallIcon)
                .setTitle("Exit dExtender-h2o ?")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .setView(R.layout.dialogue)
                .create()
                .show();
    }



}
