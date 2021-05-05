package com.dextender.dextender_h2o;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;

//========================================================================
// Class: Fragment 2
// Purpose: The purpose of this class is to show the second tabbed screen
//
// Classes Called: MyDatabase (get)
//
//========================================================================
public class fragment_2 extends Fragment implements View.OnClickListener{

    Integer MAX_ZONES=48;  // This is correct - uno 12 - mega 48

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_2, container, false);                           // Inflate the XML

        ImageButton refreshButton = (ImageButton) v.findViewById(R.id.frag2_refreshButton);
        refreshButton.setOnClickListener(this);

        //-------------------------------------------------------------------
        // Only going to display 40 items. That "40" really should be a var
        // Keep this initialization.. this was making me nuts !!! If one of the values
        // in the array was null, then I was getting a nullpointerexception.. on the
        // superGetView.. so
        //-------------------------------------------------------------------
        int i;

        final String[]  zoneNameArray = new String[MAX_ZONES];                                      // Used as interface into the database


         Short jobType;
         Short jobStatus;


        //-------------------------------------------------------------------
        // DATABASE !!! - Fetch records from the database
        //-------------------------------------------------------------------
        int recordsFetched=0;
        MyDatabase myDb = new MyDatabase(getActivity());                                           // open database class
        try {
            myDb.open();
            recordsFetched=myDb.getJobs(zoneNameArray,0);                                           // fill the array with messages that we have in table "log"
        } catch (Exception e) {
            e.printStackTrace();
        }
        myDb.close();

        int allocate;
        if(recordsFetched==0) {
            allocate=1;                                                     // need this for the dummy record
        }
        else {
            allocate=recordsFetched+1;
        }


        final MyRowStructureForJobs[] rowStruct = new MyRowStructureForJobs[allocate];                        // declare the "ehhem" class (aka structure)
        if(recordsFetched > 0) {
            rowStruct[0] = new MyRowStructureForJobs();
            rowStruct[0].thisRow(R.mipmap.unknown, "Id", "Group", "Type", "Zone\nId", "Slot", "Status", "Run\ntime", "Time\nleft");
        }
        else {
            rowStruct[0] = new MyRowStructureForJobs();
            rowStruct[0].thisRow(R.mipmap.unknown, "No", "jobs", "found", null, null, null, null, null);
        }

        String[] recordPiece;                                                                       // pieces of the string broken up into an array
        Integer  j=1;
        Integer  intRunTime;                                                                         // runtime is in seconds, but display in minutes
        Long     longTimeLeft;
        String   displayRunTime;
        String   timeLeft;

        for (i=0; i < recordsFetched; i++) {
            rowStruct[j] = new MyRowStructureForJobs();                                                  // prelimary step. You need to do this before using the struct

            recordPiece = zoneNameArray[i].split("\\|");                                            // Split the database record that's at position 'i'

            jobType   = Short.parseShort(recordPiece[2]);
            jobStatus = Short.parseShort(recordPiece[5]);
            int displayImage;
            String displayJobType;

            //-------------------------------------------------------
            // Display time nicely
            //-------------------------------------------------------
            if(Integer.parseInt(recordPiece[6]) < 60) {
                displayRunTime=recordPiece[6] +  " sec.";
            }
            else {
                intRunTime   = Integer.parseInt(recordPiece[6])/60;
                displayRunTime=String.valueOf(intRunTime) + " min.";
            }

            if(jobType==1) {
                displayJobType="man.";
            }
            else {
                displayJobType="auto";
            }


            //-------------------------------------------------------
            // Display time left, in a nice manner - thank you.
            // piece 7 is the submit time
            //-------------------------------------------------------
            switch (jobStatus) {
                case 0: timeLeft = "complete";

                        rowStruct[j].thisRow(R.mipmap.off, recordPiece[0], recordPiece[1],
                                displayJobType, recordPiece[3], recordPiece[4],
                                recordPiece[5], displayRunTime, timeLeft);
                        break;

                case 1: //------------------------------------------------------------------------
                        // ie. estimated submit time (piece6) + length to run - current time
                        //  1:00 PM + 30 Minutes - should completed around 1:30:05
                        //  if currenttime is 1:35, then timeleft will be negative.. we should be done
                        //  if it's positive, but less than time to run, it must be running.
                        //  otherwise, it must be pending
                        //------------------------------------------------------------------------
                        longTimeLeft = (Long.parseLong(recordPiece[7]) + Long.parseLong(recordPiece[6])) - (System.currentTimeMillis() / 1000);
                        if(longTimeLeft > 0) {
                            if(longTimeLeft > Long.parseLong(recordPiece[6])) {
                                timeLeft="pending";
                                displayImage=R.mipmap.yellow;
                            }
                            else {
                                if (longTimeLeft < 60) {
                                    timeLeft = String.valueOf(longTimeLeft) + " sec.";
                                }
                                else {
                                    timeLeft = String.valueOf(longTimeLeft / 60) + " min.";
                                }
                                displayImage=R.mipmap.ok;
                            }
                        }
                        else {
                            timeLeft = "complete";
                            displayImage=R.mipmap.off;
                        }

                        rowStruct[j].thisRow(displayImage, recordPiece[0], recordPiece[1],
                                displayJobType, recordPiece[3], recordPiece[4],
                                recordPiece[5], displayRunTime, timeLeft);
                        break;
                case 2: // 2 means it's current.. but we need to double check based on time
                        longTimeLeft = (Long.parseLong(recordPiece[7]) + Long.parseLong(recordPiece[6])) - (System.currentTimeMillis() / 1000);
                        if(longTimeLeft < 0) {
                            timeLeft="complete";
                            displayImage=R.mipmap.off;
                        }
                        else {
                            displayImage=R.mipmap.ok;
                            if (longTimeLeft < 60) {
                                timeLeft = String.valueOf(longTimeLeft) + " sec.";
                            } else {
                                timeLeft = String.valueOf(longTimeLeft / 60) + " min.";
                            }
                        }
                        rowStruct[j].thisRow(displayImage, recordPiece[0], recordPiece[1],
                                displayJobType, recordPiece[3], recordPiece[4],
                                recordPiece[5], displayRunTime, timeLeft);

                    break;
                default:
                        break;
            }



            j++;
        }


        //--------------------------------------------------------------------
        // Declare the custom adapter that we created in MyCustomerAdapter
        // and also the regular "joe-schmoe"  listview.. it's the actual
        // row in the listview that gets all fancy
        //--------------------------------------------------------------------
        ListAdapter frag2CustomAdapter = new MyCustomAdapterForJobs(getActivity(), rowStruct );
        ListView lv = (ListView) v.findViewById(R.id.listview2);


        //-----------------------------------------------------------------------
        // Shove into listview the custom adapter
        //-----------------------------------------------------------------------
        lv.setAdapter(frag2CustomAdapter);

        //-----------------------------------------------------------------------
        // Now, depending on what we click on the list, we want to start the
        // activity of getting the zone detail
        //-----------------------------------------------------------------------

        return v;
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(r, new IntentFilter("TAG_REFRESH"));
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    }

}

