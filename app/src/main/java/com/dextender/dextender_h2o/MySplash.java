package com.dextender.dextender_h2o;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Objects;

//==============================================================
// https://www.youtube.com/watch?v=-zGS_zrL0rY
// Author       : The new boston - Tutorials 11 through 17
// Modifications: So very slight by Mike LiVolsi
// Created by livolsi on 1/6/2015.
//
// Purpose      : Flash a welcome screen, play a little song..
//                then show the real screens
//
//==============================================================
public class MySplash extends Activity {

    MyHttpPost  http    = new MyHttpPost();                                                         // Call the httpd class
    MediaPlayer mySound;

    final int ERROR_LEVEL=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //================================================================================
        // UI PORTION
        //================================================================================

        //-------------------------------------------------------------------
        // Setup the layout
        //-------------------------------------------------------------------
        setContentView(R.layout.splash);

        //--------------------------------------------------------
        // Set the spinner and the and progress bar
        //--------------------------------------------------------
        ProgressBar spinner;
        spinner = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        spinner.setVisibility(View.VISIBLE);

        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LinearLayout relative = (LinearLayout) findViewById(R.id.splashScreen);
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

        //-------------------------------------------------------------------
        // Hide the action bar
        //-------------------------------------------------------------------
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //-------------------------------------------------------------------
        // Get our tune preference , if yes , then play it (for 3 seconds)
        //-------------------------------------------------------------------
        boolean introTune = prefs.getBoolean("pref_intro_tune", true);
        if (introTune) {
            mySound = MediaPlayer.create(this, R.raw.sprinkler);
            mySound.start();
        }

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent openMainActivity = new Intent("com.dextender.dextender_h2o.MYACTIVITY");
                    startActivity(openMainActivity);
                }
            }
        };
        timer.start();

        //-------------------------------------------------------------------------
        // The alarm manager is the way we get the service to run. Instead of always
        // running, the alarm manager kicks it off.
        //-------------------------------------------------------------------------
        setAlarm(Integer.parseInt(prefs.getString("pref_refresh_interval", "5")));


        //==========================================================================================
        // NON UI  WORK
        //==========================================================================================

        Thread httpThread = new Thread(new Runnable() {
            public void run() {
                boolean loadError = false;
                SharedPreferences prefs  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                //----------------------------------------------------
                // Connect to the database. If not, then error out
                //----------------------------------------------------
                boolean dbRc = false, Rc;
                MyDatabase myDb = new MyDatabase(getBaseContext());
                try {
                    myDb.open();
                    dbRc = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }


                final String[] Url = new String[1];
                final String[] port = new String[1];
                final String[] controller_key = new String[1];
                String pref_controller = prefs.getString("pref_controller", "1");

                String[] acctInfo = prefs.getString("pref_uid_value", "unknown/unknown").split("/");
                String userName=acctInfo[0].trim();
                String password=acctInfo[1].trim();

                //-----------------------------
                // Set status of the services
                //-----------------------------
                Rc = prefs.getBoolean("prefsvc", false);
                if (!Rc) myDb.updateServiceStatus("Android service", 0);
                else myDb.updateServiceStatus("Android service", 1);

                //---------------------------------
                // CALL CONTROLLER DIRECTLY
                //---------------------------------
                String[] recordPiece;
                String response = "";


                Rc = false;
                if (dbRc) {                                                                                  // database is available
                    if (myDb.getDexserver(pref_controller, Url, port, controller_key)) {

                        if (!Url[0].equals("-")) {                                                         // it's not just a dash

                            Log.i("Splash", "Calling controller directly at: "+ Url[0] + ":" + port[0] + " using key: " + controller_key[0] );
                            String webUrl = "http://" + Url[0] + ":" + port[0] +
                                    "/cgi-bin/h2o/h2o_appsync.cgi?uid=" + userName +
                                    "&controller=" + prefs.getString("pref_controller", "1") +
                                    "&ping=1" +
                                    "&controller_key=" + controller_key[0];                       // ip, port, serverId #, uid, uno|mega

                            response = http.callHome(webUrl);

                            if(response != null) {
                                Log.i("Splash", "Response from server : " + response);
                                if (response.substring(0, 8).equals("00100000"))
                                    Rc = true;                         // and we didn't get a bogus return code
                            }

                            if(!Rc) {
                                Log.d("Splash", "Controller did not respond. Will try operator");
                            }
                        }
                    }

                    if (!Rc) {
                        //----------------------
                        // CALL OPERATOR
                        //----------------------
                        Log.d("Splash", "Calling operator");
                        response = http.callOperator(getString(R.string.http_operator) + "/cgi-bin/h2o/iamhere.cgi",
                                userName, password,
                                String.valueOf(prefs.getString("pref_controller", "1")));
                        recordPiece = response.split("\\|");
                        // yes, this seems redundant.. because we know the controller is 1, so why pass it back from the web side
                        Log.d("Splash", "Response from operator " + response);

                        if (recordPiece[0].equals("00100000")) {                                            // Good answer from the operator

                            Url[0] = recordPiece[1];
                            port[0] = recordPiece[2];
                            controller_key[0] = recordPiece[4];
                            myDb.updateDexserver(String.valueOf(pref_controller), Url[0], port[0], controller_key[0]);  // update the database with the information we received from the web
                            Rc = true;
                        } else {
                            myDb.logIt(ERROR_LEVEL, "Operator could not find user :" + userName);
                            myDb.updateServiceStatus("Communications", 2);                                 // failed
                            myDb.updateServiceStatus("Controller switch state", -1);
                            myDb.updateServiceStatus("Controller server status", -1);
                            myDb.updateServiceStatus("Controller scheduler status", -1);
                        }
                    }

                    //----------------------
                    // CALL CONTROLLER
                    //----------------------
                    if (Rc) {
                        response = http.callHome("http://" + Url[0] + ":" + port[0] +
                                "/cgi-bin/h2o/h2o_appsync.cgi?uid=" + userName +
                                "&controller=" + prefs.getString("pref_controller", "1") +
                                "&controller_key=" + controller_key[0]); // ip, port, serverId #, uid, uno|mega

                        //Log.d("SPlash", "Response -->" + response);
                        if (response.substring(0, 8).equals("00000000")) {
                            myDb.logIt(ERROR_LEVEL, "the controller at " + Url[0] + " failed to respond");
                            myDb.updateServiceStatus("Communications", 2);                                 // failed
                            Rc = false;
                        }
                    }
                }

                // spinner.setVisibility(View.GONE);

                if (Rc) {
                    Integer i, j;
                    Integer tableId;                       // each table has an id, that we will use to call the appropriate insert statement (see case statement)
                    Integer tableCount = 0;                // expected number of tables
                    Integer tableCounter = 0;              // Current count of tables passed
                    Integer charPos1, charPos2;            // used to find certain characters for parsing (ie. blanks)
                    Integer rowCount;                      // parse through the number of rows by the argument passed instead of trying to slice and dice records
                    Integer fieldCount = 0;                // Future use sanity check
                    String tName, record;
                    Boolean tableFlag = false;

                    //======================================================================
                    // Start Parsing the response and sync
                    //======================================================================
//Log.d("Splash", "Response --> " + response);
                    for (i = 0; i < response.length(); i++) {                                           // loop through everything
                        switch (i) {
                            case 0:
                                if (response.substring(0, 7).equals("<begin>")) {
                                    i = 7;
                                } else {
                                    i = response.length();                                                // let's end the nonsense
                                }
                                break;
                            case 8:
                                if (response.substring(8, 15).equals("<tcount")) {
                                    charPos1 = response.indexOf('>', 15);
                                    tableCount = Integer.parseInt(response.substring(22, charPos1));
                                    i = charPos1;                                                       // point further ahead
                                }
                                break;
                            default:
                                if (response.substring(i, i + 7).equals("<tname=")) {
                                    tableCounter++;

                                    if (tableFlag) {                                                    // shouldn't be
                                        Log.d("MySplash", "ERROR: We should not even be here");
                                    } else {
                                        //<tname=dexserver tableid=10 fields=6 rows=1>
                                        tableFlag = true;
                                        charPos1 = response.indexOf(' ', i);                      // first blank after 'tname='
                                        tName = response.substring(i + 7, charPos1);
                                        i = charPos1 + 1;                                           // bump up past blank

                                        charPos1 = response.indexOf(' ', i);                      // first blank after 'tableid'
                                        tableId = Integer.parseInt(response.substring(i + 8, charPos1));
                                        i = charPos1 + 1;                                           //

                                        charPos1 = response.indexOf(' ', i);
                                        fieldCount = Integer.parseInt(response.substring(i + 7, charPos1));
                                        i = charPos1 + 1;

                                        charPos1 = response.indexOf('>', i);
                                        rowCount = Integer.parseInt(response.substring(i + 5, charPos1));
                                        i = charPos1 + 1;


                                        switch (tableId) {
                                            case 1:  // Dexservers come from the web and not the sprinkler controller
                                                break;
                                            case 2:
                                                myDb.deleteJobGroups();
                                                for (j = 0; j < rowCount; j++) {
                                                    charPos1 = response.indexOf("<r>", i);
                                                    charPos2 = response.indexOf("</r>", i);
                                                    record = response.substring(charPos1 + 3, charPos2);
                                                    recordPiece = record.split("\\|");
                                                    i = charPos2 + 4;                                       // The size of '</rec>' plus carriage return
                                                    myDb.insertJobGroup(Integer.parseInt(recordPiece[0]), Integer.parseInt(recordPiece[1]), Integer.parseInt(recordPiece[2]));
                                                }
                                                break;
                                            case 3:
                                                myDb.deleteJobs();
                                                for (j = 0; j < rowCount; j++) {
                                                    charPos1 = response.indexOf("<r>", i);
                                                    charPos2 = response.indexOf("</r>", i);
                                                    record = response.substring(charPos1 + 3, charPos2);
                                                    recordPiece = record.split("\\|");
                                                    i = charPos2 + 4;                                       // The size of '</rec>' plus carriage return
                                                    myDb.insertJob(
                                                            Long.parseLong(recordPiece[0]), Integer.parseInt(recordPiece[1]), Integer.parseInt(recordPiece[2]),
                                                            Integer.parseInt(recordPiece[3]), Integer.parseInt(recordPiece[4]), Integer.parseInt(recordPiece[5]),
                                                            Integer.parseInt(recordPiece[6]), Integer.parseInt(recordPiece[7]), Long.parseLong(recordPiece[8])
                                                    );
                                                }
                                                break;
                                            case 4:
                                                myDb.deleteLog();
                                                // Log - Should only be local log. Not controller log
                                                for (j = 0; j < rowCount; j++) {
                                                    charPos1 = response.indexOf("<r>", i);
                                                    charPos2 = response.indexOf("</r>", i);
                                                    record = response.substring(charPos1 + 3, charPos2);
                                                    recordPiece = record.split("\\|");
                                                    i = charPos2 + 4;                                       // The size of '</rec>' plus carriage return
                                                    // Record piece 0 is the log id from the server
                                                    // Log.d("SPLASHLOG", record);
                                                    myDb.logIt(Integer.parseInt(recordPiece[1]), recordPiece[2], Long.parseLong(recordPiece[3]));
                                                }
                                                break;
                                            case 5:
                                                myDb.deleteSchedules();
                                                for (j = 0; j < rowCount; j++) {
                                                    charPos1 = response.indexOf("<r>", i);
                                                    charPos2 = response.indexOf("</r>", i);
                                                    record = response.substring(charPos1 + 3, charPos2);
                                                    recordPiece = record.split("\\|");
                                                    i = charPos2 + 4;                                       // The size of '</rec>' plus carriage return
                                                    myDb.insertSchedules(
                                                            Integer.parseInt(recordPiece[0]), recordPiece[1], Integer.parseInt(recordPiece[2]),
                                                            Integer.parseInt(recordPiece[3]), Integer.parseInt(recordPiece[4]), Integer.parseInt(recordPiece[5]),
                                                            Integer.parseInt(recordPiece[6]), Integer.parseInt(recordPiece[7]), Integer.parseInt(recordPiece[8]),
                                                            Integer.parseInt(recordPiece[9]), Integer.parseInt(recordPiece[10]), Integer.parseInt(recordPiece[11]),
                                                            Integer.parseInt(recordPiece[12]), Integer.parseInt(recordPiece[13])
                                                    );
                                                }
                                                break;
                                            case 6:
                                                myDb.deleteSequences();
                                                for (j = 0; j < rowCount; j++) {
                                                    charPos1 = response.indexOf("<r>", i);
                                                    charPos2 = response.indexOf("</r>", i);
                                                    record = response.substring(charPos1 + 3, charPos2);
                                                    recordPiece = record.split("\\|");
                                                    i = charPos2 + 4;                                       // The size of '</rec>' plus carriage return
                                                    myDb.insertSequences(
                                                            Integer.parseInt(recordPiece[0]), Integer.parseInt(recordPiece[1]), recordPiece[2], Integer.parseInt(recordPiece[3])
                                                    );
                                                }
                                                break;
                                            case 7:
                                                myDb.deleteSequenceZones();
                                                for (j = 0; j < rowCount; j++) {
                                                    charPos1 = response.indexOf("<r>", i);
                                                    charPos2 = response.indexOf("</r>", i);
                                                    record = response.substring(charPos1 + 3, charPos2);
                                                    recordPiece = record.split("\\|");
                                                    i = charPos2 + 4;                                       // The size of '</rec>' plus carriage return
                                                    myDb.insertSequenceZones(
                                                            Integer.parseInt(recordPiece[0]), Integer.parseInt(recordPiece[1]), Integer.parseInt(recordPiece[2]),
                                                            Integer.parseInt(recordPiece[3]), Integer.parseInt(recordPiece[4]), Long.parseLong(recordPiece[5])
                                                    );
                                                }
                                                break;
                                            case 8:
                                                myDb.deleteSolar();
                                                for (j = 0; j < rowCount; j++) {
                                                    charPos1 = response.indexOf("<r>", i);
                                                    charPos2 = response.indexOf("</r>", i);
                                                    record = response.substring(charPos1 + 3, charPos2);
                                                    recordPiece = record.split("\\|");
                                                    i = charPos2 + 4;                                       // The size of '</rec>' plus carriage return
                                                    myDb.insertSolar(
                                                            Integer.parseInt(recordPiece[0]), Integer.parseInt(recordPiece[1])
                                                    );
                                                }
                                                break;
                                            case 9:
                                                myDb.deleteStatus();
                                                for (j = 0; j < rowCount; j++) {
                                                    charPos1 = response.indexOf("<r>", i);
                                                    charPos2 = response.indexOf("</r>", i);
                                                    record = response.substring(charPos1 + 3, charPos2);
                                                    recordPiece = record.split("\\|");
                                                    i = charPos2 + 4;                                       // The size of '</rec>' plus carriage return
                                                    myDb.insertStatus(
                                                            Integer.parseInt(recordPiece[0]), recordPiece[1], Integer.parseInt(recordPiece[2]),
                                                            Integer.parseInt(recordPiece[3]), Long.parseLong(recordPiece[4]), Integer.parseInt(recordPiece[5]),
                                                            Integer.parseInt(recordPiece[6]), Long.parseLong(recordPiece[7]), recordPiece[8]);

                                                    myDb.updateServiceStatus("Controller switch state", Integer.parseInt(recordPiece[2]));
                                                    // Need to change the controller state in the preferences
                                                    if(Integer.parseInt(recordPiece[2]) == 1) {
                                                        setBooleanPreference(getApplicationContext(), "pref_ctrlStatus", true);
                                                    }
                                                    else {
                                                        setBooleanPreference(getApplicationContext(), "pref_ctrlStatus", false);
                                                    }
                                                }


                                                break;
                                            case 10:
                                                myDb.deleteZones();
                                                for (j = 0; j < rowCount; j++) {
                                                    charPos1 = response.indexOf("<r>", i);
                                                    charPos2 = response.indexOf("</r>", i);
                                                    record = response.substring(charPos1 + 3, charPos2);
                                                    recordPiece = record.split("\\|");
                                                    i = charPos2 + 4;                                       // The size of '</rec>' plus carriage return
                                                    myDb.insertZones(
                                                            Integer.parseInt(recordPiece[0]), recordPiece[1], Integer.parseInt(recordPiece[2]), Integer.parseInt(recordPiece[3])
                                                    );
                                                }
                                                break;
                                            case 11:
                                                for (j = 0; j < rowCount; j++) {
                                                    charPos1 = response.indexOf("<r>", i);
                                                    charPos2 = response.indexOf("</r>", i);
                                                    record = response.substring(charPos1 + 3, charPos2);
                                                    recordPiece = record.split("\\|");
                                                    i = charPos2 + 4;                                       // The size of '</rec>' plus carriage return
                                                    if (recordPiece[0].equals("h2o_server")) {
                                                        myDb.updateServiceStatus("Controller server status", Integer.parseInt(recordPiece[1]));
                                                    } else {
                                                        if (recordPiece[0].equals("h2o_crond")) {
                                                            myDb.updateServiceStatus("Controller scheduler status", Integer.parseInt(recordPiece[1]));
                                                        }
                                                    }

                                                }
                                                break;
                                        }
                                    }
                                } else {
                                    if (response.substring(i, i + 8).equals("</tname>")) {
                                        tableFlag = false;
                                        i += 8;
                                    } else {
                                        if (response.substring(i, i + 9).equals("</tcount>")) {
                                            if (!Objects.equals(tableCounter, tableCount)) {
                                                loadError = true;
                                            }
                                            i += 9;
                                        } else {
                                            if (response.substring(i, i + 8).equals("</begin>")) {
                                                i = response.length();                                // end;
                                            }
                                        }
                                    }
                                }
                                break;
                        }

                    }
                    //recordPiece = response.split("\\|");
                    //if (recordPiece[0].equals("00100000")) {
                    //    myDb.syncIt(recordPiece[1]);
                    //}
                    //======================================================================
                    // END PARSE SYNC
                    //======================================================================

                    myDb.updateServiceStatus("Communications", 1);                                      // service 1 is communication, 1 is good
                } else {
                    loadError = true;
                }

                myDb.close();
            }
        });

        httpThread.start();

        //try {
        //    httpThread.join();
        //}
        //catch (InterruptedException e){
        //    e.printStackTrace();
        //}
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    public void setAlarm(Integer inMinutes) {
        //===============================================================
        // Clear the alarm just in case we get hosed up
        //===============================================================

        int minutes = inMinutes;

        // setup the alarm manager
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // setup the intent to do stuff with the service
        //Intent serviceIntent = new Intent(this, MyService.class);

        Intent i = new Intent(this, MyReceiver.class);

        // Was 'getService' - Changed to 'getBroadcast'
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(this, 0, i, 0);

        // Kill any stragglers (if any )
        alarmManager.cancel(pendingIntent);
        //-----------------------------------------------------------
        // set the alarm
        // If minutes > 0, then set the alarm
        // NOTE: MyReceiver will handle any calls at this point
        //-----------------------------------------------------------
        if (minutes > 0) {
            Toast toast = Toast.makeText(this, "System wakes up every " + minutes + " minute(s)", Toast.LENGTH_SHORT);
            toast.show();
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + minutes * 60 * 1000, minutes * 60 * 1000, pendingIntent);
        }
    }


    //----------------------------------------------------------------------------------
    // EXACT SAME METHOD IN MYSERVICE .. What you change here, change there
    //----------------------------------------------------------------------------------
    private void setBooleanPreference (Context inContext, String preferenceName, boolean inBoolean) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(inContext);
        SharedPreferences.Editor editor1 = settings.edit();
        editor1.putBoolean(preferenceName, inBoolean);
        editor1.apply();
    }

}
