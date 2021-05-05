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

import java.util.Random;

//========================================================================
// Class: Fragment 3
// Purpose: The purpose of this class is to show the second tabbed screen
//
// Classes Called: MyDatabase (get)
//
//========================================================================
public class fragment_3 extends Fragment implements View.OnClickListener{


    Integer MAX_ZONES=24;

    MyHttpPost myHttp = new MyHttpPost();
    boolean dbRc = false;
    String[] Url = new String[1];
    String[] port = new String[1];
    String[] key = new String[1];


    TextView zoneName;
    Button   runButton;
    Button   disableButton;
    Button   enableButton;
    Button   editZoneButton;

    int      circleValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_3, container, false);                           // Inflate the XML

        //-------------------------------------------------------------------
        // Only going to display 40 items. That "40" really should be a var
        // Keep this initialization.. this was making me nuts !!! If one of the values
        // in the array was null, then I was getting a nullpointerexception.. on the
        // superGetView.. so
        //-------------------------------------------------------------------
        int i;

        final String[]  zoneNameArray = new String[MAX_ZONES];
        final Integer[] zoneIdArray   = new Integer[MAX_ZONES];
        final Short[]   zoneStatus    = new Short[MAX_ZONES];
        final String[]  zoneLastTime  = new String[MAX_ZONES];


        ImageButton refreshButton = (ImageButton) v.findViewById(R.id.frag3_refreshButton);
        refreshButton.setOnClickListener(this);

        //-------------------------------------------------------------------
        // DATABASE !!! - Fetch records from the database
        //-------------------------------------------------------------------
        int recordsFetched=0;
        final MyDatabase myDb = new MyDatabase(getActivity());                                           // open database class
        try {
            myDb.open();
            recordsFetched=myDb.getZones(zoneNameArray,0);
            myDb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int allocate;
        if(recordsFetched==0) allocate=1;                                                           // need this for the dummy record
        else                  allocate=recordsFetched;


        final MyRowStructure[] rowStruct = new MyRowStructure[allocate];                            // declare the "ehhem" class (aka structure)
        String[] recordPiece;                                                                       // pieces of the string broken up into an array
        for (i=0; i < recordsFetched; i++) {
            rowStruct[i] = new MyRowStructure();                                                    // prelimary step. You need to do this before using the struct

            recordPiece = zoneNameArray[i].split("\\|");                                            // Split the database record that's at position 'i'
                                                                                                    // Use the string to show the zone (not the ID)
            zoneIdArray[i] = Integer.parseInt(recordPiece[0]);
            zoneStatus[i] = Short.parseShort(recordPiece[2]);
            if(zoneStatus[i] == 1) {
                rowStruct[i].thisRow(R.mipmap.btarget, recordPiece[0] + " - " +  recordPiece[1]);
            }
            else {
                rowStruct[i].thisRow(R.mipmap.off, recordPiece[0] + " - " + recordPiece[1]);
            }

            MyTools    myTools  = new MyTools();
            zoneLastTime[i] = myTools.epoch2FmtTime(Long.parseLong(recordPiece[3]) - myTools.getOffsetFromUtc(), "MMM d yyyy h:mm a");
        }

        //----------------------------------------------------------------------
        // If there's no record in the database, instead of a null record
        // put something in slot 0 (1st record)
        //----------------------------------------------------------------------
        if (recordsFetched==0)  {
            rowStruct[0] = new MyRowStructure();
            rowStruct[0].thisRow(R.mipmap.unknown, "No zones found");
        }

        //--------------------------------------------------------------------
        // Declare the custom adapter that we created in MyCustomerAdapter
        // and also the regular "joe-schmoe"  listview.. it's the actual
        // row in the listview that gets all fancy
        //--------------------------------------------------------------------
        final ListAdapter frag3CustomAdapter = new MyCustomAdapter(getActivity(), rowStruct );
        ListView lv = (ListView) v.findViewById(R.id.listview3);

        //-----------------------------------------------------------------------
        // Shove into listview the custom adapter
        //-----------------------------------------------------------------------
        lv.setAdapter(frag3CustomAdapter);

        //-----------------------------------------------------------------------
        // Listview listener
        // Now, depending on what we click on the list, we want to start the
        // activity of getting the zone detail
        //-----------------------------------------------------------------------
        if (recordsFetched!=0) {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    String item = rowStruct[position].txtTitle;

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    final AlertDialog alert = builder.create();


                    LayoutInflater inflater1 = getActivity().getLayoutInflater();
                    View view1 = inflater1.inflate(R.layout.dialogue_zaction, (ViewGroup) getActivity().findViewById(R.id.zone_dialogueRoot));

                    zoneName    = (TextView) view1.findViewById(R.id.zoneName);
                    TextView zoneStat    = (TextView) view1.findViewById(R.id.zoneStatus);
                    TextView zoneRunTime = (TextView) view1.findViewById(R.id.zoneRunTime);


                    runButton      = (Button) view1.findViewById(R.id.runZone);
                    disableButton  = (Button) view1.findViewById(R.id.disableZone);
                    enableButton   = (Button) view1.findViewById(R.id.enableZone);
                    editZoneButton = (Button) view1.findViewById(R.id.editZone);

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

                            editZoneButton.setEnabled(false);
                            editZoneButton.setClickable(false);
                            editZoneButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                        } else {                                                                                  // can only do this if communication path is open

                            if (zoneStatus[position] == 0) {                                            // if offline, then disable the "disable" button
                                disableButton.setClickable(false);
                                disableButton.setEnabled(false);
                                disableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                                runButton.setClickable(false);
                                runButton.setEnabled(false);
                                runButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                                editZoneButton.setClickable(false);
                                editZoneButton.setEnabled(false);
                                editZoneButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);


                            } else {
                                enableButton.setClickable(false);
                                enableButton.setEnabled(false);
                                enableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                            }

                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }


                    //-- What to show in the header of the dialog

                    zoneName.setText(item);
                    if(zoneStatus[position] == 1) {
                        zoneStat.setText(R.string.online);
                    }
                    else {
                        zoneStat.setText(R.string.offline);
                    }
                    String tempString = "Last runtime : " + String.valueOf(zoneLastTime[position]);
                    zoneRunTime.setText(tempString);


                    //-------------------------------------------------------------------------
                    // This sets the value in the main activity
                    //-------------------------------------------------------------------------
                    //( (MyActivity) getActivity()).setZoneData(item, String.valueOf(zoneIdArray[position]),String.valueOf(zoneStatus[position]),String.valueOf(zoneLastTime[position]) );

                    disableButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(disableZone(zoneIdArray[position]) ){
                                disableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                                runButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                                enableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.enable, 0, 0);
                                editZoneButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);

                                disableButton.setClickable(false);
                                runButton.setClickable(false);
                                enableButton.setClickable(true);
                                editZoneButton.setClickable(false);

                                disableButton.setEnabled(false);
                                runButton.setEnabled(false);
                                enableButton.setEnabled(true);
                                editZoneButton.setEnabled(false);

                                refresh();
                            }
                        }
                    });

                    enableButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(enableZone(zoneIdArray[position]) ){
                                disableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.disable, 0, 0);
                                runButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.run, 0, 0);
                                enableButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.gray_button, 0, 0);
                                editZoneButton.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.edit, 0, 0);

                                disableButton.setClickable(true);
                                runButton.setClickable(true);
                                enableButton.setClickable(false);
                                editZoneButton.setClickable(true);

                                disableButton.setEnabled(true);
                                runButton.setEnabled(true);
                                enableButton.setEnabled(false);
                                editZoneButton.setEnabled(true);

                                refresh();

                            }
                        }
                    });

                    runButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                             runZone(zoneIdArray[position]);

                        }
                    });

                    editZoneButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] tempString = zoneNameArray[position].split("\\|");
                            editZone(zoneIdArray[position], tempString[1]);

                        }
                    });

                    alert.setView(view1);
                    alert.show();

                }
            });
        }

        myDb.close();
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


    //==============================================================================================

    public boolean disableZone (int zoneId) {

        MyDatabase myDb = new MyDatabase(getActivity());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final String pref_controller  = prefs.getString("pref_controller", "1");
        final String pref_account     = prefs.getString("pref_uid_value", "1234-4567-7890");       // should the service do anything ?

        dbRc = false;
        try {
            myDb.open();
            myDb.getDexserver(pref_controller, Url, port, key); // get controller location
            dbRc = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if ((!dbRc)) {
            Toast.makeText(getActivity(), "Account information is not correct", Toast.LENGTH_LONG).show();
            return false;
        } else {

            String webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_zones.cgi" +
                    "?uid=" + pref_account +
                    "&access_key=" + key[0] +
                    "&zone=" + zoneId +
                    "&event=242" +
                    "&app=1");


            String[] response = myHttp.callHome(webUrl).split("\\|");
            if (response[0].substring(0,3).equals("011")) {
                Toast.makeText(getActivity(), "Zone " + zoneId + " disabled", Toast.LENGTH_LONG).show();
                myDb.updateZoneStatus(zoneId, (System.currentTimeMillis() / 1000), 0);
                myDb.close();
            } else {
                Toast.makeText(getActivity(), "Failed to disable zone " + zoneId, Toast.LENGTH_LONG).show();
                myDb.close();
                return false;
            }
        }

        return true;

    }



    public boolean enableZone ( int zoneId) {


        MyDatabase myDb = new MyDatabase(getActivity());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final String pref_account     = prefs.getString("pref_uid_value", "1234-4567-7890");                        // should the service do anything ?
        final String pref_controller  = prefs.getString("pref_controller", "1");


        dbRc = false;
        try {
            myDb.open();
            myDb.getDexserver(pref_controller, Url, port, key); // get controller location
            dbRc = true;
        } catch (Exception e) {
            e.printStackTrace();
        }


        if ((!dbRc)) {
            Toast.makeText(getActivity(), "Account information is not correct", Toast.LENGTH_LONG).show();
            return false;
        } else {

            String webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_zones.cgi" +
                    "?uid=" + pref_account +
                    "&access_key=" + key[0] +
                    "&zone=" + zoneId +
                    "&event=252" +
                    "&app=1");


            String[] response = myHttp.callHome(webUrl).split("\\|");
            if (response[0].substring(0,3).equals("011")) {
                Toast.makeText(getActivity(), "Zone " + zoneId + " enabled", Toast.LENGTH_LONG).show();
                myDb.updateZoneStatus(zoneId, (System.currentTimeMillis() / 1000), 1);
                myDb.close();
            } else {
                Toast.makeText(getActivity(), "Failed to enable zone " + zoneId, Toast.LENGTH_LONG).show();
                myDb.close();
                return false;
            }
        }

        return true;
    }


    public boolean runZone (final int zoneId) {

        final MyDatabase myDb = new MyDatabase(getActivity());
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final String pref_controller = prefs.getString("pref_controller", "1");

        boolean dbRc = false;
        try {
            myDb.open();
            dbRc = myDb.getDexserver(pref_controller, Url, port, key); // get controller location
            myDb.close();
        } catch (Exception e) {
            e.printStackTrace();
            myDb.close();
        }

        if ((!dbRc)) {
            Toast.makeText(getActivity(), "Account information is not correct", Toast.LENGTH_LONG).show();
            return false;
        }


        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final AlertDialog alert = builder.create();


        LayoutInflater inflater1 = getActivity().getLayoutInflater();
        final View view1 = inflater1.inflate(R.layout.dialogue_circle, (ViewGroup) getActivity().findViewById(R.id.dialog_circle_root));
        TextView     title        = (TextView)     view1.findViewById(R.id.circleTitle);
        SeekCircle   seekCircle   = (SeekCircle)   view1.findViewById(R.id.seekCircle);
        TextView     textProgress = (TextView)     view1.findViewById(R.id.textProgress);

        title.setText("");
        seekCircle.setProgress(10);

        textProgress.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                submitZoneRun(zoneId);
                                                alert.cancel();
                                            }
                                        });

        seekCircle.setOnSeekCircleChangeListener(new SeekCircle.OnSeekCircleChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekCircle seekCircle)
            {}

            @Override
            public void onStartTrackingTouch(SeekCircle seekCircle)
            {}

            @Override
            public void onProgressChanged(SeekCircle seekCircle, int progress, boolean fromUser)
            {
                updateText(view1);
            }
        });

        updateText(view1);

        alert.setView(view1);
        alert.show();

        return true;
    }

    private void updateText(View inView)
    {

        String tempString;
        SeekCircle seekCircle = (SeekCircle)inView.findViewById(R.id.seekCircle);
        TextView textProgress = (TextView)inView.findViewById(R.id.textProgress);

        if (textProgress != null && seekCircle != null)
        {
            circleValue = seekCircle.getProgress();
            tempString = Integer.toString(circleValue) + " minutes";                                // doesnt like concat in the next line
            textProgress.setText(tempString);
        }
    }


    public boolean editZone (final int zoneId, final String inZoneName) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String pref_controller  = prefs.getString("pref_controller", "1");
        final MyDatabase myDb = new MyDatabase(getActivity());

        dbRc = false;
        try {
            myDb.open();
            dbRc=myDb.getDexserver(pref_controller, Url, port, key); // get controller location
        } catch (Exception e) {
            e.printStackTrace();
        }


        if(!dbRc) {
            Toast.makeText(getActivity(), "Account information is not correct", Toast.LENGTH_LONG).show();
            return false;
        }
        else {

            final EditText edittext = new EditText(getActivity());
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setMessage("Rename zone \"" + inZoneName + "\"");
            alert.setTitle("Rename Zone");
            alert.setView(edittext);

            alert.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            String argText = edittext.getText().toString();

                            String webUrl;
                            webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_zones.cgi?") +
                                    "zid=" + zoneId + "&zn=" + argText.replaceAll(" ", "%20") +
                                    "&event=292" +
                                    "&sequence=0" +
                                    "&app=1";

                            String[] response = myHttp.callHome(webUrl).split("\\|");

                            if (response[0].substring(0, 3).equals("011")) {
                                Toast.makeText(getActivity(), "Zone " + zoneId + " renamed to " + argText, Toast.LENGTH_LONG).show();
                                myDb.updateZoneName(zoneId, System.currentTimeMillis() / 1000, argText);
                                zoneName.setText(argText);
                            }
                            myDb.close();
                            refresh();
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
        }
        return true;
    }


    public boolean submitZoneRun(final int zoneId) {

        final MyDatabase myDb = new MyDatabase(getActivity());
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final String pref_controller  = prefs.getString("pref_controller", "1");

        boolean dbRc = false;
        try {
            myDb.open();
            dbRc=myDb.getDexserver(pref_controller, Url, port, key); // get controller location
        } catch (Exception e) {
            e.printStackTrace();
            myDb.close();
        }

        if ((!dbRc)) {
            Toast.makeText(getActivity(), "Account information is not correct", Toast.LENGTH_LONG).show();
            return false;
        }

        Random r = new Random();                 // otherwise we get constraint issues
        int randomNumber = r.nextInt(10000); if(randomNumber==0) randomNumber=1;

        String webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_zones.cgi?uid=" +
                prefs.getString("uid", "1234-4567-7890") +
                "&access_key=" + key[0] +
                "&zone=" + zoneId + "" +
                "&rt=" + (circleValue*60) +
                "&event=222" +
                "&jobid="    + randomNumber +
                "&app=1");



        String[] response = myHttp.callHome(webUrl).split("\\|");

        if (response[0].substring(0, 3).equals("011")) {
            Toast.makeText(getActivity(), "Zone " + zoneId + " submitted to run for " + circleValue + " minute(s)", Toast.LENGTH_LONG).show();
            myDb.updateZoneInfo(zoneId, (System.currentTimeMillis() / 1000));
            myDb.updateSysStatusCurrentZone(String.valueOf(zoneId));
            //myDb.deleteSequenceZones();
            myDb.insertJob((long) randomNumber, 0, 0, 1, zoneId, 0, 1, circleValue*60, System.currentTimeMillis() / 1000);
        } else {
            Toast.makeText(getActivity(), "Zone " + zoneId + " failed to run (see log)", Toast.LENGTH_LONG).show();
            myDb.logIt(1, "Zone " + zoneId + "failed to run\nReason: " + response[1], (long) 0);

        }
        myDb.close();

        return true;

    }


}

