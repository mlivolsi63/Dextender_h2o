package com.dextender.dextender_h2o;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


//===========================================================================
// Created by livolsi on 9/24/2014.
//===========================================================================
public class fragment_5 extends Fragment {

    Integer MAX_SCHEDULES=64;


    MyHttpPost myHttp = new MyHttpPost();
    boolean dbRc = false;
    String[] Url = new String[1];
    String[] port = new String[1];
    String[] key = new String[1];

    TextView vscheduleName;

    Button   vdisableButton;
    Button   venableButton;
    Button   vdeleteButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_5, container, false);                           // Inflate the XML

        ImageButton refreshButton = (ImageButton) view.findViewById(R.id.frag5_refresh);

        //-------------------------------------------------------------------
        // Only going to display 40 items. That "40" really should be a var
        // Keep this initialization.. this was making me nuts !!! If one of the values
        // in the array was null, then I was getting a nullpointerexception.. on the
        // superGetView.. so
        //-------------------------------------------------------------------
        final String[]  schedNameArray    = new String[MAX_SCHEDULES];
        final Integer[] schedIdArray      = new Integer[MAX_SCHEDULES];
        final Short[]   schedStatusArray  = new Short[MAX_SCHEDULES];
        final String[]  schedTimeArray    = new String[MAX_SCHEDULES];

        final Short[] sunday     = new Short[MAX_SCHEDULES];
        final Short[] monday     = new Short[MAX_SCHEDULES];
        final Short[] tuesday    = new Short[MAX_SCHEDULES];
        final Short[] wednesday  = new Short[MAX_SCHEDULES];
        final Short[] thursday   = new Short[MAX_SCHEDULES];
        final Short[] friday     = new Short[MAX_SCHEDULES];
        final Short[] saturday   = new Short[MAX_SCHEDULES];
        final Short[] evenodd    = new Short[MAX_SCHEDULES];
        final Short[] suntype    = new Short[MAX_SCHEDULES];

        //-------------------------------------------------------------------
        // DATABASE !!! - Fetch records from the database
        //-------------------------------------------------------------------
        int recordsFetched=0;
        final MyDatabase myDb = new MyDatabase(getActivity());                                           // open database class
        try {
            myDb.open();
            recordsFetched=myDb.getSchedules(schedNameArray);                                      // fill the array with messages that we have in table "log"
        } catch (Exception e) {
            e.printStackTrace();
        }
        //-------------------------------------------------------------------------
        // REFRESH BUTTON
        //-------------------------------------------------------------------------
        refreshButton.setOnClickListener(new View.OnClickListener() {                                // ... and set a listening event for it
            @Override
            public void onClick(final View v) {                                                     // if the onclick is triggered for the (refresh) butto
                refresh();
            }
        });
        //-------------------------------------------------------------------------
        // If our commnunications link is disabled, then don't show this option
        //-------------------------------------------------------------------------
        myDb.close();

        int allocate;
        if(recordsFetched==0) allocate=1;                                                           // need this for the dummy record
        else                  allocate=recordsFetched;


        final MyRowStructure[] rowStruct = new MyRowStructure[allocate];                                // declare the "ehhem" class (aka structure)
        String[] recordPiece;                                                                       // pieces of the string broken up into an array
        for (int i=0; i < recordsFetched; i++) {
            rowStruct[i] = new MyRowStructure();                                                      // prelimary step. You need to do this before using the struct

            recordPiece = schedNameArray[i].split("\\|");                                             // Split the database record that's at position 'i'
            // Use the string to show the zone (not the ID)
            schedIdArray[i] = Integer.parseInt(recordPiece[0]);
            schedStatusArray[i] = Short.parseShort(recordPiece[2]);
            // Log.d("FRAG5", "Status " + recordPiece[2] + " of scchedule " + recordPiece[1]);

            if(schedStatusArray[i] == 1) {
                rowStruct[i].thisRow(R.mipmap.bschedule, recordPiece[1]);
            }
            else {
                rowStruct[i].thisRow(R.mipmap.off, recordPiece[1]);
            }

            MyTools    myTools  = new MyTools();
            if (Long.parseLong(recordPiece[3]) > 86400){
                schedTimeArray[i] = myTools.epoch2FmtTime(Long.parseLong(recordPiece[3]) - myTools.getOffsetFromUtc(), "MMM d yyyy h:mm a");
            }
            else {
                schedTimeArray[i] = myTools.secondsToTimeStr(Long.parseLong(recordPiece[3]));
            }

            sunday[i]     = Short.parseShort(recordPiece[4]);
            monday[i]     = Short.parseShort(recordPiece[5]);
            tuesday[i]    = Short.parseShort(recordPiece[6]);
            wednesday[i]  = Short.parseShort(recordPiece[7]);
            thursday[i]   = Short.parseShort(recordPiece[8]);
            friday[i]     = Short.parseShort(recordPiece[9]);
            saturday[i]   = Short.parseShort(recordPiece[10]);
            evenodd[i]    = Short.parseShort(recordPiece[11]);
            suntype[i]    = Short.parseShort(recordPiece[12]);
        }

        //----------------------------------------------------------------------
        // If there's no record in the database, instead of a null record
        // put something in slot 0 (1st record)
        //----------------------------------------------------------------------
        if (recordsFetched==0)  {
            rowStruct[0] = new MyRowStructure();
            rowStruct[0].thisRow(R.mipmap.unknown, "No schedules found");
        }

        //--------------------------------------------------------------------
        // Declare the custom adapter that we created in MyCustomerAdapter
        // and also the regular "joe-schmoe"  listview.. it's the actual
        // row in the listview that gets all fancy
        //--------------------------------------------------------------------
        ListAdapter frag5CustomAdapter = new MyCustomAdapter(getActivity(), rowStruct );
        ListView lv = (ListView) view.findViewById(R.id.listview5);


        //-----------------------------------------------------------------------
        // Shove into listview the custom adapter
        //-----------------------------------------------------------------------
        lv.setAdapter(frag5CustomAdapter);

        //-----------------------------------------------------------------------
        // Now, depending on what we click on the list, we want to start the
        // activity of getting the zone detail
        //-----------------------------------------------------------------------
        if (recordsFetched != 0) {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                    String item = rowStruct[position].txtTitle;

                    final AlertDialog.Builder scheduleDialogue = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater1 = getActivity().getLayoutInflater();
                    View view1 = inflater1.inflate(R.layout.fragment_5_sched_action_dialog, (ViewGroup) getActivity().findViewById(R.id.sched_dialogueRoot));

                    vscheduleName         = (TextView) view1.findViewById(R.id.schedName);
                    TextView vschedStatus = (TextView) view1.findViewById(R.id.schedStatus);
                    TextView vschedDays   = (TextView) view1.findViewById(R.id.sched_schedule_days_value);
                    TextView vschedTimes  = (TextView) view1.findViewById(R.id.sched_schedule_time_value);


                    vdisableButton  = (Button) view1.findViewById(R.id.sequenceButtonDisable);
                    venableButton   = (Button) view1.findViewById(R.id.sequenceButtonEnable);
                    vdeleteButton   = (Button) view1.findViewById(R.id.sequenceButtonDelete);



                    try {
                        myDb.open();
                        if (!myDb.getAllWebServicesStatus()) {


                            vdisableButton.setEnabled(false);
                            vdisableButton.setClickable(false);
                            vdisableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                            venableButton.setEnabled(false);
                            venableButton.setClickable(false);
                            venableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                            vdeleteButton.setEnabled(false);
                            vdeleteButton.setClickable(false);
                            vdeleteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                        } else {                                                                                  // can only do this if communication path is open

                            if (schedStatusArray[position] == 0) {                                            // if offline, then disable the "disable" button
                                vdisableButton.setClickable(false);
                                vdisableButton.setEnabled(false);
                                vdisableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                                vdeleteButton.setClickable(false);
                                vdeleteButton.setEnabled(false);
                                vdeleteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);


                            } else {
                                venableButton.setClickable(false);
                                venableButton.setEnabled(false);
                                venableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                            }

                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    //-- What to show in the header of the dialog

                    vscheduleName.setText(item);
                    if (schedStatusArray[position] == 1) {
                        vschedStatus.setText(R.string.online);
                    } else {
                        vschedStatus.setText(R.string.offline);
                    }

                    if(evenodd[position] == 0) {
                        String daystring="";
                        if(sunday[position] == 1)    daystring = "Sun";
                        if(monday[position] == 1)    daystring = daystring + " Mon";
                        if(tuesday[position] == 1)   daystring = daystring + " Tues";
                        if(wednesday[position] == 1) daystring = daystring + " Wed";
                        if(thursday[position] == 1)  daystring = daystring + " Thur";
                        if(friday[position] == 1)    daystring = daystring + " Fri";
                        if(saturday[position] == 1)  daystring = daystring + " Sat";

                        vschedDays.setText(daystring);
                    }
                    else {
                        if(evenodd[position] == 1) vschedDays.setText(R.string.odd);
                        else                       vschedDays.setText(R.string.even);
                    }

                    String tempString;
                    switch (suntype[position]) {
                        case 0:
                            tempString=schedTimeArray[position];
                            break;
                        case 1:
                            tempString=schedTimeArray[position] + " before sunrise";
                            break;
                        case 2:
                            tempString=schedTimeArray[position] + " before sunset";
                            break;
                        case 3:
                            tempString=schedTimeArray[position] + " after sunrise";
                            break;
                        case 4:
                            tempString=schedTimeArray[position] + " after sunset";
                            break;
                        default:
                            tempString=schedTimeArray[position];
                            break;
                    }
                    vschedTimes.setText(tempString);

                    //--------------------------------------
                    vdisableButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (disableSchedule(schedIdArray[position])) {
                                vdisableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                                venableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.enable, 0, 0);
                                vdeleteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                                vdisableButton.setClickable(false);
                                venableButton.setClickable(true);
                                vdeleteButton.setClickable(false);

                                vdisableButton.setEnabled(false);
                                venableButton.setEnabled(true);
                                vdeleteButton.setEnabled(false);

                                refresh();
                            }
                        }
                    });

                    venableButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (enableSchedule(schedIdArray[position])) {
                                vdisableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.disable, 0, 0);
                                venableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                                vdeleteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.delete, 0, 0);

                                vdisableButton.setClickable(true);
                                venableButton.setClickable(false);
                                vdeleteButton.setClickable(true);

                                vdisableButton.setEnabled(true);
                                venableButton.setEnabled(false);
                                vdeleteButton.setEnabled(true);

                                refresh();
                            }
                        }
                    });


                    vdeleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] tempString = schedNameArray[position].split("\\|");
                            deleteSchedule(schedIdArray[position], tempString[1]);
                            vdisableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                            venableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                            vdeleteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                            vdisableButton.setClickable(false);
                            venableButton.setClickable(false);
                            vdeleteButton.setClickable(false);

                            vdisableButton.setEnabled(false);
                            venableButton.setEnabled(false);
                            vdeleteButton.setEnabled(false);

                        }
                    });

                    //---------------------------------------
                    scheduleDialogue.setView(view1);
                    scheduleDialogue.show();
                }
            });
        }


        return view;
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


    //==============================================================================================

    public boolean disableSchedule (int inScheduleId) {

        MyDatabase myDb = new MyDatabase(getActivity());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final String pref_account      = prefs.getString("pref_uid_value", "1234-4567-7890");       // should the service do anything ?

        dbRc = false;
        try {
            myDb.open();
            myDb.getDexserver(prefs.getString("pref_controller", "1"), Url, port, key); // get controller location
            dbRc = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if ((!dbRc)) {
            Toast.makeText(getActivity(), "Account information is not correct", Toast.LENGTH_LONG).show();
            return false;
        } else {


            String webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_schedules.cgi" +
                    "?uid="        + pref_account +
                    "&access_key=" + key[0] +
                    "&schedId="   + inScheduleId  +
                    "&event=464"   +
                    "&app=1");

            Log.d("FRAG5", "URL --> " + webUrl);

            String[] response = myHttp.callHome(webUrl).split("\\|");
            if (response[0].substring(0,3).equals("011")) {
                Toast.makeText(getActivity(), "Schedule disabled", Toast.LENGTH_LONG).show();
                myDb.updateScheduleStatus(inScheduleId,  0);
            } else {
                Toast.makeText(getActivity(), "Failed to disable sequence ", Toast.LENGTH_LONG).show();
                myDb.close();
                return false;
            }
            myDb.close();
        }

        return true;
    }



    public boolean enableSchedule ( int inScheduleId) {


        MyDatabase myDb = new MyDatabase(getActivity());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final String pref_account      = prefs.getString("pref_uid_value", "1234-4567-7890");                        // should the service do anything ?


        dbRc = false;
        try {
            myDb.open();
            myDb.getDexserver(prefs.getString("pref_controller", "1"), Url, port, key); // get controller location
            dbRc = true;
        } catch (Exception e) {
            e.printStackTrace();
        }


        if ((!dbRc)) {
            Toast.makeText(getActivity(), "Account information is not correct", Toast.LENGTH_LONG).show();
            return false;
        } else {

            String webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_schedules.cgi" +
                    "?uid="        + pref_account +
                    "&access_key=" + key[0] +
                    "&schedId="   + inScheduleId  +
                    "&event=462"   +
                    "&app=1");

            Log.d("FRAG5", "URL --> " + webUrl);


            String[] response = myHttp.callHome(webUrl).split("\\|");
            if (response[0].substring(0,3).equals("011")) {
                Toast.makeText(getActivity(), "Sequence enabled", Toast.LENGTH_LONG).show();
                myDb.updateScheduleStatus(inScheduleId,  1);
            } else {
                Toast.makeText(getActivity(), "Failed to enable sequence ", Toast.LENGTH_LONG).show();
                myDb.close();
                return false;
            }
        }

        return true;
    }



    public boolean deleteSchedule (final int inScheduleId, final String inScheduleName) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final MyDatabase myDb = new MyDatabase(getActivity());

        dbRc = false;
        try {
            myDb.open();
            myDb.getDexserver(prefs.getString("pref_controller", "1"), Url, port, key); // get controller location
            dbRc = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        builder.setTitle("Delete Sequence");
        builder.setMessage("Are you sure you want to delete schedule :" + inScheduleName);


        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {


                        String webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_schedules.cgi?") +
                                "schedId=" + inScheduleId +
                                "&event=442" +
                                "&rt=0" +
                                "&app=1";

                        String[] response = myHttp.callHome(webUrl).split("\\|");

                        if (response[0].substring(0,3).equals("011")) {
                            myDb.deleteSchedule(String.valueOf(inScheduleId));
                            Toast.makeText(getActivity(), "Schedule deleted", Toast.LENGTH_LONG).show();
                            myDb.close();
                            refresh();
                            dialog.cancel();

                        }
                    }
                }
        );

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        myDb.close();
                        dialog.cancel();
                    }
                }
        );
        builder.show();
        return true;
    }




}
