package com.dextender.dextender_h2o;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


//===========================================================================================
// Created by livolsi on 9/24/2014.
//===========================================================================================
public class fragment_1 extends Fragment implements View.OnClickListener {

    String[] Url = new String[1];
    String[] port = new String[1];
    String[] key = new String[1];


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //----------------------------------------------
        // Call the classes
        //----------------------------------------------
        MyTools    myTools = new MyTools();
        MyDatabase myDb = new MyDatabase(getActivity());
        SharedPreferences prefs      = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean pref_androidService  = prefs.getBoolean("prefsvc", false);                          // should the service do anything ?

        View view = inflater.inflate(R.layout.fragment_1, container, false);


        //--------------------------------------------------------
        // Get the respective ID's from the XML screens
        //--------------------------------------------------------
        TextView zoneCurrent      = (TextView)  view.findViewById(R.id.currentZone);
        TextView zoneCurrentName  = (TextView)  view.findViewById(R.id.currentZoneName);
        TextView zoneLast         = (TextView)  view.findViewById(R.id.lastZone);
        TextView zoneLastRuntime  = (TextView)  view.findViewById(R.id.lastZoneRuntime);
        TextView sunrise          = (TextView)  view.findViewById(R.id.sunriseTime);
        TextView sunset           = (TextView)  view.findViewById(R.id.sunsetTime);

        TextView systemStatus     = (TextView)  view.findViewById(R.id.frag1_appStatus);
        TextView controllerIP     = (TextView)  view.findViewById(R.id.frag1_controllerIP);


        ImageView statAndroid     = (ImageView) view.findViewById(R.id.androidRealStat);
        ImageView statComm        = (ImageView) view.findViewById(R.id.connectionRealStat);
        ImageView statSys         = (ImageView) view.findViewById(R.id.systemRealStat);
        ImageView statServer      = (ImageView) view.findViewById(R.id.serverRealStat);
        ImageView statSched       = (ImageView) view.findViewById(R.id.schedRealStat);


        String[] zoneNameArray    = new String[1];

        //---------------------------------------------------------------------------------
        // Get the 'offset' aka last record we got from the DB and display any new records
        //---------------------------------------------------------------------------------
        try {
            myDb.open();                                                                            // open the database
            String  statusRecord   = myDb.getStatusData(1);                                         // Get the status record
            String  solarRecord    = myDb.getSolar();
            Integer commStatRc     = myDb.getServiceStatus("Communications");
            Integer sysStatRc      = myDb.getServiceStatus("Controller switch state");
            Integer serverStatRc   = myDb.getServiceStatus("Controller server status");
            Integer schedStatRc    = myDb.getServiceStatus("Controller scheduler status");

            final String pref_controller  = prefs.getString("pref_controller", "1");
            myDb.getDexserver(pref_controller, Url, port, key);
            String tempString=Url[0] + ":" + port[0];
            controllerIP.setText(tempString);

            ImageButton refreshButton = (ImageButton) view.findViewById(R.id.frag1_refreshButton);
            refreshButton.setOnClickListener(this);


            Integer recordCount;

            if (myDb.getJobCurrentZone(zoneNameArray)) {                                            // what's in the job queue
                zoneCurrent.setText(zoneNameArray[0]);
                String[] zoneNames = new String[1];
                recordCount = myDb.getZones(zoneNames, Integer.parseInt(zoneNameArray[0]));       // Get the zone name from the database
                if (recordCount > 0) {                                                          // the database returned stuff
                    String[] tmpRec = zoneNames[0].split("\\|");                                // take the DB record and split it
                    zoneCurrentName.setText(tmpRec[1]);                                         // name is the 2nd field
                }
            }
            else {
                zoneCurrent.setText(R.string.dash);
                zoneCurrentName.setText(R.string.dash);
                zoneLastRuntime.setText(R.string.notime);
            }


            //--------------------------------------------------------------------------------------
            // Information from the status record
            //--------------------------------------------------------------------------------------
            if(statusRecord != null) {
                // Log.d("dext fragment_1", "Record from database -->" + statusRecord);

                String[] recordPiece = statusRecord.split("\\|");                                   // Split the record into it's respective parts
                zoneLast.setText(recordPiece[5]);

                //-------------------------------------------------------------
                // last run time
                //-------------------------------------------------------------
                long epochAsLong = Long.parseLong(recordPiece[6]);
                String stringDate = myTools.epoch2FmtTime(epochAsLong, "MMM d yyyy h:mm a");
                zoneLastRuntime.setText(stringDate);
            }



            if(solarRecord != null) {
                String[] recordPiece = solarRecord.split("\\|");
                sunrise.setText(myTools.secondsToTimeStr(Long.parseLong(recordPiece[0])) );
                sunset.setText(myTools.secondsToTimeStr(Long.parseLong(recordPiece[1]))  );
            }
            else {
                sunrise.setText(R.string.notime);
                sunset.setText(R.string.notime);
            }


            //--------------------------------------------------
            // Android service is on or off in the preferences
            //--------------------------------------------------
            if (pref_androidService) {
                statAndroid.setImageResource(R.mipmap.power_on);
            }
            else {
                statAndroid.setImageResource(R.mipmap.power_off);
            }

            //--------------------------------------------------
            // Are we able to communicate with the controller ?
            //--------------------------------------------------
            switch(commStatRc) {
                case -1:
                    statComm.setImageResource(R.mipmap.power_unknown);
                    statSys.setImageResource(R.mipmap.power_unknown);
                    statServer.setImageResource(R.mipmap.power_unknown);
                    statSched.setImageResource(R.mipmap.power_unknown);
                    break;
                case 0:
                    statComm.setImageResource(R.mipmap.power_off);
                    statSys.setImageResource(R.mipmap.power_unknown);
                    statServer.setImageResource(R.mipmap.power_unknown);
                    statSched.setImageResource(R.mipmap.power_unknown);
                    break;
                case 1:
                    statComm.setImageResource(R.mipmap.power_on);
                    break;
                case 2:
                    systemStatus.setBackgroundColor(getResources().getColor(R.color.rust));
                    systemStatus.setTextColor(getResources().getColor(R.color.white));
                    systemStatus.setText(R.string.networkFail01);

                    statComm.setImageResource(R.mipmap.power_err);
                    statSys.setImageResource(R.mipmap.power_unknown);
                    statServer.setImageResource(R.mipmap.power_unknown);
                    statSched.setImageResource(R.mipmap.power_unknown);

                    break;
                default:
                    statComm.setImageResource(R.mipmap.power_unknown);
                    statSys.setImageResource(R.mipmap.power_unknown);
                    statServer.setImageResource(R.mipmap.power_unknown);
                    statSched.setImageResource(R.mipmap.power_unknown);
                    break;
            }


            //-------------------------------------------------------------------------
            // Controller switch state - Only if we can actually talk to the controller
            // -1 unknown
            //  0 Off
            //  1 On
            //  2 Pause
            //-------------------------------------------------------------------------
            if(commStatRc == 1) {
                switch (sysStatRc) {
                    case -1:
                        statSys.setImageResource(R.mipmap.power_unknown);
                        break;
                    case 0:
                        statSys.setImageResource(R.mipmap.power_off);
                        break;
                    case 1:
                        statSys.setImageResource(R.mipmap.power_on);
                        break;
                    case 2:
                        statSys.setImageResource(R.mipmap.power_pause);
                        break;
                    default:
                        statSys.setImageResource(R.mipmap.power_unknown);
                        break;
                }


                switch (serverStatRc) {
                    case -1:
                        statServer.setImageResource(R.mipmap.power_unknown);
                        break;
                    case 0:
                        statServer.setImageResource(R.mipmap.power_off);
                        break;
                    case 1:
                        statServer.setImageResource(R.mipmap.power_on);
                        break;
                    case 2:
                        statServer.setImageResource(R.mipmap.power_err);
                        break;
                    default:
                        statServer.setImageResource(R.mipmap.power_unknown);
                        break;
                }

                switch (schedStatRc) {
                    case -1:
                        statSched.setImageResource(R.mipmap.power_unknown);
                        break;
                    case 0:
                        statSched.setImageResource(R.mipmap.power_off);
                        break;
                    case 1:
                        statSched.setImageResource(R.mipmap.power_on);
                        break;
                    case 2:
                        statSched.setImageResource(R.mipmap.power_err);
                        break;
                    default:
                        statSched.setImageResource(R.mipmap.power_unknown);
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            myDb.close();
        }

        myDb.close();
        return view;
    }


    @Override
    public void onClick(View v) {
        refresh();
    }


    //==========================================================================
    // The code below is to refresh this fragment when you swipe to a different
    // fragment. This is what is pissing me off fragment_about android. You would think
    // that some screens need to be refreshed (ala windows) and yet you have
    // to come up with all this code by scouring the web, since they don't explain
    // this shit at all in their tutorials
    //==========================================================================

    MyReceiver r;

    public void refresh() {
        //This is called every time you swipe a tab. To force a refresh, you have to run the following
        //Log.d("frag1", "Refreshed");
        Fragment        fragment = this;
        FragmentManager manager  = getActivity().getFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.detach(fragment);
        ft.attach(fragment);
        ft.commit();
    }


    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(r);
    }

    public void onResume() {
        super.onResume();
        r = new MyReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(r,
                new IntentFilter("TAG_REFRESH"));
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    }


}
