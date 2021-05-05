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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//========================================================================
// Class: Fragment 4
// Purpose: The purpose of this class is to show the fourth tabbed screen
//
// Classes Called: MyDatabase (get)
//
//========================================================================
public class fragment_4 extends Fragment {

    Integer MAX_SEQUENCES=48;

    final static Integer MAX_ZONES=60;
    final static Integer MAX_DISPLAY_ZONES=18;


    MyHttpPost myHttp = new MyHttpPost();
    boolean dbRc = false;
    String[] Url = new String[1];
    String[] port = new String[1];
    String[] key = new String[1];

    TextView seqName;
    Button   runButton;
    Button   disableButton;
    Button   enableButton;
    Button   deleteButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_4, container, false);                           // Inflate the XML

        ImageButton refreshButton = (ImageButton) view.findViewById(R.id.frag4_refresh);

        //-------------------------------------------------------------------
        // Only going to display 40 items. That "40" really should be a var
        // Keep this initialization.. this was making me nuts !!! If one of the values
        // in the array was null, then I was getting a nullpointerexception.. on the
        // superGetView.. so
        //-------------------------------------------------------------------
        final String[]  seqNameArray = new String[MAX_SEQUENCES];
        final Integer[] seqIdArray   = new Integer[MAX_SEQUENCES];
        final Short[]   seqStatus    = new Short[MAX_SEQUENCES];
        final String[]  seqLastTime  = new String[MAX_SEQUENCES];

        //-------------------------------------------------------------------
        // DATABASE !!! - Fetch records from the database
        //-------------------------------------------------------------------
        int recordsFetched=0;
        final MyDatabase myDb = new MyDatabase(getActivity());                                           // open database class
        try {
            myDb.open();
            recordsFetched=myDb.getSequences(seqNameArray, 0);                                      // fill the array with messages that we have in table "log"
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
        // If our commnunications link is disabled, then don't want to enable
        // this option
        //------------------------------------------------------------------------
        myDb.close();

        int allocate;
        if(recordsFetched==0) allocate=1;                                                           // need this for the dummy record
        else                  allocate=recordsFetched;


        final MyRowStructure[] rowStruct = new MyRowStructure[allocate];                            // declare the "ehhem" class (aka structure)
        String[] recordPiece;                                                                       // pieces of the string broken up into an array
        for (int i=0; i < recordsFetched; i++) {
            rowStruct[i] = new MyRowStructure();                                                    // prelimary step. You need to do this before using the struct

            recordPiece = seqNameArray[i].split("\\|");                                             // Split the database record that's at position 'i'
            // Use the string to show the zone (not the ID)
            seqIdArray[i] = Integer.parseInt(recordPiece[0]);
            seqStatus[i] = Short.parseShort(recordPiece[2]);
            if(seqStatus[i] == 1) {
                rowStruct[i].thisRow(R.mipmap.bsequence, recordPiece[1]);
            }
            else {
                rowStruct[i].thisRow(R.mipmap.off, recordPiece[1]);
            }

            MyTools    myTools  = new MyTools();
            seqLastTime[i] = myTools.epoch2FmtTime(Long.parseLong(recordPiece[3]) - myTools.getOffsetFromUtc(), "MMM d yyyy h:mm a");
        }

        //----------------------------------------------------------------------
        // If there's no record in the database, instead of a null record
        // put something in slot 0 (1st record)
        //----------------------------------------------------------------------
        if (recordsFetched==0)  {
            rowStruct[0] = new MyRowStructure();
            rowStruct[0].thisRow(R.mipmap.unknown, "No sequences found");
        }

        //--------------------------------------------------------------------
        // Declare the custom adapter that we created in MyCustomerAdapter
        // and also the regular "joe-schmoe"  listview.. it's the actual
        // row in the listview that gets all fancy
        //--------------------------------------------------------------------
        ListAdapter frag4CustomAdapter = new MyCustomAdapter(getActivity(), rowStruct );
        ListView lv = (ListView) view.findViewById(R.id.listview4);


        //-----------------------------------------------------------------------
        // Shove into listview the custom adapter
        //-----------------------------------------------------------------------
        lv.setAdapter(frag4CustomAdapter);

        //-----------------------------------------------------------------------
        // Now, depending on what we click on the list, we want to start the
        // activity of getting the zone detail
        //-----------------------------------------------------------------------
        if (recordsFetched != 0) {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                    String item = rowStruct[position].txtTitle;

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    final AlertDialog alert = builder.create();

                    LayoutInflater inflater1 = getActivity().getLayoutInflater();
                    View view1 = inflater1.inflate(R.layout.dialogue_seqaction, (ViewGroup) getActivity().findViewById(R.id.seq_dialogueRoot));

                    seqName = (TextView) view1.findViewById(R.id.seqName);
                    TextView seqStat = (TextView) view1.findViewById(R.id.seqStatus);
                    TextView seqRunTime = (TextView) view1.findViewById(R.id.seqRunTime);

                    runButton     = (Button) view1.findViewById(R.id.sequenceButtonRun);
                    disableButton = (Button) view1.findViewById(R.id.sequenceButtonDisable);
                    enableButton  = (Button) view1.findViewById(R.id.sequenceButtonEnable);
                    deleteButton  = (Button) view1.findViewById(R.id.sequenceButtonDelete);


                    //-------------------------------------------------------------------
                    // Initialize the dialog sequence array
                    //-------------------------------------------------------------------
                    TextView[] z = new TextView[20];

                    z[0] = (TextView) view1.findViewById(R.id.z1);
                    z[1] = (TextView) view1.findViewById(R.id.z2);
                    z[2] = (TextView) view1.findViewById(R.id.z3);
                    z[3] = (TextView) view1.findViewById(R.id.z4);
                    z[4] = (TextView) view1.findViewById(R.id.z5);
                    z[5] = (TextView) view1.findViewById(R.id.z6);
                    z[6] = (TextView) view1.findViewById(R.id.z7);
                    z[7] = (TextView) view1.findViewById(R.id.z8);
                    z[8] = (TextView) view1.findViewById(R.id.z9);
                    z[9] = (TextView) view1.findViewById(R.id.z10);
                    z[10] = (TextView) view1.findViewById(R.id.z11);
                    z[11] = (TextView) view1.findViewById(R.id.z12);
                    z[12] = (TextView) view1.findViewById(R.id.z13);
                    z[13] = (TextView) view1.findViewById(R.id.z14);
                    z[14] = (TextView) view1.findViewById(R.id.z15);
                    z[15] = (TextView) view1.findViewById(R.id.z16);
                    z[16] = (TextView) view1.findViewById(R.id.z17);
                    z[17] = (TextView) view1.findViewById(R.id.z18);


                    TextView[] zid = new TextView[20];

                    zid[0] = (TextView) view1.findViewById(R.id.zid1);
                    zid[1] = (TextView) view1.findViewById(R.id.zid2);
                    zid[2] = (TextView) view1.findViewById(R.id.zid3);
                    zid[3] = (TextView) view1.findViewById(R.id.zid4);
                    zid[4] = (TextView) view1.findViewById(R.id.zid5);
                    zid[5] = (TextView) view1.findViewById(R.id.zid6);
                    zid[6] = (TextView) view1.findViewById(R.id.zid7);
                    zid[7] = (TextView) view1.findViewById(R.id.zid8);
                    zid[8] = (TextView) view1.findViewById(R.id.zid9);
                    zid[9] = (TextView) view1.findViewById(R.id.zid10);
                    zid[10] = (TextView) view1.findViewById(R.id.zid11);
                    zid[11] = (TextView) view1.findViewById(R.id.zid12);
                    zid[12] = (TextView) view1.findViewById(R.id.zid13);
                    zid[13] = (TextView) view1.findViewById(R.id.zid14);
                    zid[14] = (TextView) view1.findViewById(R.id.zid15);
                    zid[15] = (TextView) view1.findViewById(R.id.zid16);
                    zid[16] = (TextView) view1.findViewById(R.id.zid17);
                    zid[17] = (TextView) view1.findViewById(R.id.zid18);


                    TextView[] zt = new TextView[20];

                    zt[0] = (TextView) view1.findViewById(R.id.zt1);
                    zt[1] = (TextView) view1.findViewById(R.id.zt2);
                    zt[2] = (TextView) view1.findViewById(R.id.zt3);
                    zt[3] = (TextView) view1.findViewById(R.id.zt4);
                    zt[4] = (TextView) view1.findViewById(R.id.zt5);
                    zt[5] = (TextView) view1.findViewById(R.id.zt6);
                    zt[6] = (TextView) view1.findViewById(R.id.zt7);
                    zt[7] = (TextView) view1.findViewById(R.id.zt8);
                    zt[8] = (TextView) view1.findViewById(R.id.zt9);
                    zt[9] = (TextView) view1.findViewById(R.id.zt10);
                    zt[10] = (TextView) view1.findViewById(R.id.zt11);
                    zt[11] = (TextView) view1.findViewById(R.id.zt12);
                    zt[12] = (TextView) view1.findViewById(R.id.zt13);
                    zt[13] = (TextView) view1.findViewById(R.id.zt14);
                    zt[14] = (TextView) view1.findViewById(R.id.zt15);
                    zt[15] = (TextView) view1.findViewById(R.id.zt16);
                    zt[16] = (TextView) view1.findViewById(R.id.zt17);
                    zt[17] = (TextView) view1.findViewById(R.id.zt18);
                    //------------------------------------------------------------------------------
                    // Draw the buttons based on status
                    //------------------------------------------------------------------------------
                    try {
                        myDb.open();
                        if (!myDb.getAllWebServicesStatus()) {
                            runButton.setEnabled(false);
                            runButton.setClickable(false);
                            runButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                            disableButton.setEnabled(false);
                            disableButton.setClickable(false);
                            disableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                            enableButton.setEnabled(false);
                            enableButton.setClickable(false);
                            enableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                            deleteButton.setEnabled(false);
                            deleteButton.setClickable(false);
                            deleteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                        } else {                                                                                  // can only do this if communication path is open

                            if (seqStatus[position] == 0) {                                            // if offline, then disable the "disable" button
                                disableButton.setClickable(false);
                                disableButton.setEnabled(false);
                                disableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                                runButton.setClickable(false);
                                runButton.setEnabled(false);
                                runButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                                deleteButton.setClickable(false);
                                deleteButton.setEnabled(false);
                                deleteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);


                            } else {
                                enableButton.setClickable(false);
                                enableButton.setEnabled(false);
                                enableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //-- What to show in the header of the dialog

                    seqName.setText(item);
                    if (seqStatus[position] == 1) {
                        seqStat.setText(R.string.online);
                    } else {
                        seqStat.setText(R.string.offline);
                    }
                    String tempString = "Last runtime : " + String.valueOf(seqLastTime[position]);
                    seqRunTime.setText(tempString);

                    //-----------------------------------------------------------------------------
                    // In the dialog layout, there are a few fields. One is the list of zones that
                    // make up the sequence. This will populate those values in the layout
                    //-----------------------------------------------------------------------------
                    String[] zoneList = new String[MAX_ZONES];
                    Integer zoneCount = myDb.getSequenceZoneList(String.valueOf(seqIdArray[position]), zoneList);
                    for (int i = 0; i < zoneCount; i++) {
                        String[] record=zoneList[i].split("\\|");
                        if (i < MAX_DISPLAY_ZONES) {                                                    //don't populate more than what we can display
                            if (record[1].length() > 40) {
                                tempString = record[1].substring(0, 40) + "...";
                                z[i].setText(tempString);

                            } else {
                                z[i].setText(record[1]);
                            }
                            zid[i].setText(record[0]);
                            zt[i].setText(record[2]);
                        }
                    }

                    //--------------------------------------
                    disableButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (disableSequence(seqIdArray[position])) {
                                disableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                                runButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                                enableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.enable, 0, 0);
                                deleteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                                disableButton.setClickable(false);
                                runButton.setClickable(false);
                                enableButton.setClickable(true);
                                deleteButton.setClickable(false);

                                disableButton.setEnabled(false);
                                runButton.setEnabled(false);
                                enableButton.setEnabled(true);
                                deleteButton.setEnabled(false);

                                refresh();
                            }
                        }
                    });

                    enableButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (enableSequence(seqIdArray[position])) {
                                disableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.disable, 0, 0);
                                runButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.run, 0, 0);
                                enableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                                deleteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.delete, 0, 0);

                                disableButton.setClickable(true);
                                runButton.setClickable(true);
                                enableButton.setClickable(false);
                                deleteButton.setClickable(true);

                                disableButton.setEnabled(true);
                                runButton.setEnabled(true);
                                enableButton.setEnabled(false);
                                deleteButton.setEnabled(true);

                                refresh();
                            }
                        }
                    });

                    runButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] tempString = seqNameArray[position].split("\\|");
                            runSequence(seqIdArray[position], tempString[1]);

                        }
                    });

                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] tempString = seqNameArray[position].split("\\|");
                            deleteSequence(seqIdArray[position], tempString[1]);
                            disableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                            runButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                            enableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                            deleteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                            disableButton.setClickable(false);
                            runButton.setClickable(false);
                            enableButton.setClickable(false);
                            deleteButton.setClickable(false);

                            disableButton.setEnabled(false);
                            runButton.setEnabled(false);
                            enableButton.setEnabled(false);
                            deleteButton.setEnabled(false);

                            alert.cancel();
                            refresh();
                        }

                    });


                    //---------------------------------------
                    alert.setView(view1);
                    alert.show();

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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(r, new IntentFilter("TAG_REFRESH"));
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    }




    //==============================================================================================

    public boolean disableSequence (int seqId) {

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


            String webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_sequences.cgi" +
                    "?uid="        + pref_account +
                    "&access_key=" + key[0] +
                    "&sequence="   + seqId  +
                    "&event=364"   +
                    "&app=1");


            String[] response = myHttp.callHome(webUrl).split("\\|");
            if (response[0].substring(0,3).equals("011")) {
                Toast.makeText(getActivity(), "Sequence disabled", Toast.LENGTH_LONG).show();
                myDb.updateSequenceStatus(seqId, (System.currentTimeMillis() / 1000), 0);
            } else {
                Toast.makeText(getActivity(), "Failed to disable sequence ", Toast.LENGTH_LONG).show();
                myDb.close();
                return false;
            }
            myDb.close();
        }

        return true;
    }



    public boolean enableSequence ( int seqId) {


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

            String webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_sequences.cgi" +
                    "?uid="        + pref_account +
                    "&access_key=" + key[0] +
                    "&sequence="   + seqId  +
                    "&event=362"   +
                    "&app=1");

            String[] response = myHttp.callHome(webUrl).split("\\|");
            if (response[0].substring(0,3).equals("011")) {
                Toast.makeText(getActivity(), "Sequence enabled", Toast.LENGTH_LONG).show();
                myDb.updateSequenceStatus(seqId, (System.currentTimeMillis() / 1000), 1);
            } else {
                Toast.makeText(getActivity(), "Failed to enable sequence ", Toast.LENGTH_LONG).show();
                myDb.close();
                return false;
            }
        }

        return true;
    }


    public boolean runSequence (final int seqId, final String inSequenceName) {

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final MyDatabase myDb = new MyDatabase(getActivity());

        dbRc = false;

        try {
            myDb.open();
            dbRc=myDb.getDexserver(prefs.getString("pref_controller", "1"), Url, port, key); // get controller location
        } catch (Exception e) {
            e.printStackTrace();
            myDb.close();
        }

        if ((!dbRc)) {
            Toast.makeText(getActivity(), "Account information is not correct", Toast.LENGTH_LONG).show();
            myDb.close();
            return false;
        }
        else {

            alert.setTitle("Run Sequence");
            alert.setMessage("Run sequence : " + inSequenceName + " ?");

            alert.setPositiveButton("Run", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {

                    String webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_sequences.cgi" +
                            "?uid="        + //pref_account +
                            "&access_key=" + key[0] +
                            "&sequence="   + seqId  +
                            "&event=332"   +
                            "&app=1");

                    String[] response = myHttp.callHome(webUrl).split("\\|");

                    if (response[0].substring(0, 3).equals("011")) {
                        Toast.makeText(getActivity(), "Sequence submitted to run  ", Toast.LENGTH_LONG).show();
                        myDb.updateSequenceInfo(seqId, (System.currentTimeMillis() / 1000));
                        myDb.insertJobForSequences(String.valueOf(seqId));
                    } else {
                        Toast.makeText(getActivity(), "Sequence failed to run (see log)", Toast.LENGTH_LONG).show();
                        myDb.logIt(1, "Sequence failed to run\nReason: " + response[1], (long) 0);
                    }
                    myDb.close();
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            myDb.close();
                            dialog.cancel();
                        }
                    }
            );
            alert.show();

        }
        return true;
    }


    public boolean deleteSequence (final int inSequenceId, final String inSequenceName) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
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

        alert.setTitle("Delete Sequence");
        alert.setMessage("Are you sure you want to delete sequence :" + inSequenceName);


        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {


                        String webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_sequences.cgi?") +
                                "sequence=" +  inSequenceId +
                                "&event=352" +
                                "&rt=0" +
                                "&app=1";


                        String[] response = myHttp.callHome(webUrl).split("\\|");

                        if (response[0].substring(0,3).equals("011")) {
                            myDb.deleteSequence(String.valueOf(inSequenceId));
                            Toast.makeText(getActivity(), "Sequence deleted", Toast.LENGTH_LONG).show();
                            myDb.close();
                            refresh();
                            dialog.cancel();
                        }
                    }
                }
        );

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        myDb.close();
                        dialog.cancel();
                    }
                }
        );
        alert.show();
        return true;
    }

}

