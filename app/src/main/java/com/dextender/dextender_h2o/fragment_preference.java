package com.dextender.dextender_h2o;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TimePicker;
import android.widget.Toast;

//------------------------------------------------------------------------------------------
// Author      : http://www.youtube.com/watch?v=Df129IGl31I (aka thenewboston)
//
// Modified by : Mike LiVolsi
// Date        : September 2014
//
// Purpose     : This was tutorial #56 from "thenewboston" with other minor modifications
//               from around the web, since some of his calls are now depracated.
//               This is an added class that we're using the handle the fragments
//               Fragments are "pieces" of screens that we're trying to show.
//               For every change, we need a listener.
//               This class needs to be specified in the manifest.
//
//------------------------------------------------------------------------------------------
public class fragment_preference extends PreferenceActivity  {


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

    }

    //===========================================================================================
    // Class : MyPreferenceFragement
    // Author: Mike LiVolsi
    //===========================================================================================
    public static class MyPreferenceFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener,
            TimePickerDialog.OnTimeSetListener

    {
        public static final String KEY_PREF_SERVICE = "prefsvc";                                    // This is the service key
        public static final String KEY_PREF_UID = "pref_uid_value";                                 // the key in the preferences.xml that I want to work with
        public static final String KEY_PREF_ABOUT = "pref_about";
        public static final String KEY_PREF_INTERVAL = "pref_refresh_interval";                     // how often to refresh
        public static final String KEY_PREF_SERVICE_SNOOZE_INTERVAL="pref_serviceSnoozeElapse";
        public static final String KEY_PREF_TONE_PLAY_TIME="pref_maxPlayTime";
        public static final String KEY_PREF_CONTROLLER="pref_controller";
        public static final String KEY_PREF_CONTROLLER_STATE="pref_ctrlStatus";
        public static final String KEY_PREF_PAUSE="pref_pause";
        public static final String KEY_PREF_KILL="pref_killZones";
        public static final String KEY_PREF_BACKGROUND="pref_backgroundImages";

        final String[] Url      = new String[1];
        final String[] port     = new String[1];
        final String[] key      = new String[1];

        //==========================================================================================
        // Method: onCreate
        // Type  : Built in
        //==========================================================================================    
        @Override
        public void onCreate(final Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            //------------------------------------------------------------------
            // Act as a button in preference code for the 'about' screen
            // When the "About" button is clicked, start an intent (activity)
            // about the 'about' screen (which is a fragment too)
            //------------------------------------------------------------------
            Preference about = findPreference(KEY_PREF_ABOUT);
            about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    Intent openMainActivity =  new Intent("com.dextender.dextender_h2o.ABOUT_H2O");
                    startActivity(openMainActivity);
                    return true;
                }
            });

            //-------------------------------------------------------------------------
            // CHANGE CONTROLLER STATE
            //-------------------------------------------------------------------------
            Preference controlSwitch = findPreference(KEY_PREF_CONTROLLER_STATE);
            controlSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    changeControllerState();
                    return true;
                }
            });

            //-------------------------------------------------------------------------
            // On pause section - Fires up the time spinner
            // Also, note the implements
            //-------------------------------------------------------------------------

            Preference pause = findPreference(KEY_PREF_PAUSE);
            pause.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    showTimeDialog();
                    return false;
                }
            });

            //-------------------------------------------------------------------------
            // KILL RUNNING ZONES
            //-------------------------------------------------------------------------
            Preference killZones = findPreference(KEY_PREF_KILL);
            killZones.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    killRunningZones();
                    return true;
                }
            });
        }

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        // END ON CREATE
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        @Override
        public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
            SharedPreferences prefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Preference setPref = findPreference(KEY_PREF_PAUSE);

            MyDatabase        myDb   = new MyDatabase(getActivity());
            MyHttpPost        myHttp = new MyHttpPost();


            boolean dbRc=false;
            try {
                myDb.open();
                dbRc=myDb.getDexserver(prefs.getString("pref_controller", "1"), Url, port, key);
                dbRc = true;
            } catch (Exception e) {
                e.printStackTrace();
            }


            if(dbRc) {
                String webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_controls.cgi") +
                        "?sysstat=pause&pdays=0" +
                        "&phours=" + String.valueOf(hours) +
                        "&pminutes=" + String.valueOf(minutes) +
                        "&event=530&zone=0&sequence=0&app=1";
                // Log.d("FRAG", "Sending -->" + webUrl);
                String response[]=myHttp.callHome(webUrl).split("\\|");
                if(response[0].substring(0,3).equals("011")) {
                    myDb.updateServiceStatus("Controller switch state", 2);
                    myDb.updatePauseTime((hours * 60) + minutes, System.currentTimeMillis() / 1000);
                    setPref.setSummary("Controller is paused");
                    Toast.makeText(getActivity(), "Controller is paused", Toast.LENGTH_SHORT).show();
                }

                myDb.close();
            }
        }


        private void showTimeDialog(){
            // Use the current date as the default date in the picker
            // final Calendar c = Calendar.getInstance();
            // int hour = c.get(Calendar.HOUR_OF_DAY);
            // int minute = c.get(Calendar.MINUTE);
            int hour = 0;
            int minute = 10;
            new TimePickerDialog(getActivity(), 0, this, hour, minute, true).show();
        }


        private void changeControllerState() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            MyDatabase myDb = new MyDatabase(getActivity());
            MyHttpPost myHttp = new MyHttpPost();

            final boolean pref_ctrlState = prefs.getBoolean("pref_ctrlStatus", false);

            boolean dbRc = false;
            try {
                myDb.open();
                dbRc = myDb.getDexserver(prefs.getString("pref_controller", "1"), Url, port, key);
                dbRc = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            String webUrl;
            if (dbRc) {
                if (!pref_ctrlState) {                                                                // turn the system off
                    webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_controls.cgi") +
                            "?sysstat=sysoff&pdays=0&phours=0&pminutes=0&event=520&zone=0&sequence=0&app=1";
                } else {                                                                              // turn the system on
                    webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_controls.cgi") +
                            "?event=550&zone=0&sequence=0&app=1";
                }
                String response[] = myHttp.callHome(webUrl).split("\\|");
                // Log.d("PREFS", "response -->" + response[0].substring(0, 3));

                if (response[0].substring(0, 3).equals("011")) {
                    myDb.updatePauseTime(0, System.currentTimeMillis()/1000);
                    if (!pref_ctrlState) {                                                            // we were on, now we are off
                        myDb.updateServiceStatus("Controller switch state", 0);
                        Toast.makeText(getActivity(), "Controller is turned off", Toast.LENGTH_SHORT).show();
                    } else {
                        myDb.updateServiceStatus("Controller switch state", 1);                    // we weren't on, now we are
                        Toast.makeText(getActivity(), "Controller is turned on", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Could not change controller state", Toast.LENGTH_SHORT).show();

                    SharedPreferences.Editor editor1 = prefs.edit();
                    editor1.putBoolean("pref_ctrlStatus", false);
                    editor1.commit();
                    Preference setPref = findPreference(KEY_PREF_CONTROLLER_STATE);
                    pref_ctrlState=false;
                }

                myDb.close();
            }
        }

        private void killRunningZones() {
            SharedPreferences prefs  = PreferenceManager.getDefaultSharedPreferences(getActivity());
            MyDatabase        myDb   = new MyDatabase(getActivity());
            MyHttpPost        myHttp = new MyHttpPost();


            boolean dbRc=false;
            try {
                myDb.open();
                dbRc=myDb.getDexserver(prefs.getString("pref_controller", "1"), Url, port, key);
                dbRc = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            String webUrl;
            if(dbRc) {
                webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_controls.cgi") +
                            "?sysstat=stopzone&pdays=0&phours=0&pminutes=10&event=510&zone=0&sequence=0&app=1";

                String response[]=myHttp.callHome(webUrl).split("\\|");

                if(response[0].substring(0,3).equals("011")) {
                    myDb.deleteJobs();
                    Toast.makeText(getActivity(), "Job queue cleared", Toast.LENGTH_SHORT).show();
                }

                myDb.close();
            }
        }
        //----------------- END OF SPINNER SUPPORT -------------------------------------------------

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (
                key.equals(KEY_PREF_SERVICE) ||
                key.equals(KEY_PREF_UID) ||
                key.equals(KEY_PREF_CONTROLLER) ||
                key.equals(KEY_PREF_SERVICE_SNOOZE_INTERVAL) ||
                key.equals(KEY_PREF_INTERVAL) ||
                key.equals(KEY_PREF_TONE_PLAY_TIME) ||
                key.equals(KEY_PREF_BACKGROUND)
               ) {
                Preference exercisesPref = findPreference(key);


                String newValue;
                boolean newBoolValue;

                //------------------------------------------------------------
                // Check if the service was changed. If so, then if the
                // service is off, there's a good chance the scheduler is also
                // off. If so, we call the alarm method and schedule a run
                //------------------------------------------------------------
                if(key.equals(KEY_PREF_SERVICE) ) {

                    newBoolValue= prefs.getBoolean(key, false);
                    if(newBoolValue) {
                        setAlarm(getActivity());
                    }
                }

                if( (key.equals(KEY_PREF_INTERVAL) ) || (key.equals(KEY_PREF_SERVICE_SNOOZE_INTERVAL) ) )                {
                    newValue= prefs.getString(key, "") + " Minutes";
                    exercisesPref.setSummary(newValue);
                }

                // Length to play a tone
                if( key.equals(KEY_PREF_TONE_PLAY_TIME) )                {
                    newValue= prefs.getString(key, "10") + " seconds";
                    exercisesPref.setSummary(newValue);
                }

                if (key.equals(KEY_PREF_CONTROLLER)) {
                    newValue = "Current controller " + prefs.getString(key,"1");
                    exercisesPref.setSummary(newValue);
                }

                if (key.equals(KEY_PREF_BACKGROUND)) {
                    Toast toast = Toast.makeText(getActivity(), "The background image has been changed", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }

        //-----------------------------------------------------------------------------------------
        // Method: onResume
        // This is a Mike Special. I couldn't find anything meaningful on the web re: showing values
        // NOTE: When the preference screen first pops up, ON CREATE is first called, then ONRESUME
        // is called
        //-----------------------------------------------------------------------------------------
        @Override
        public void onResume() {
            String newValue;
            super.onResume();
            MyDatabase        myDb   = new MyDatabase(getActivity());

            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            Preference p2 = findPreference(KEY_PREF_UID);
            p2.setSummary(prefs.getString(KEY_PREF_UID, ""));

            p2 = findPreference(KEY_PREF_INTERVAL);
            if(p2 != null) {
                newValue = prefs.getString(KEY_PREF_INTERVAL,"5") + " minute(s)";
                p2.setSummary(newValue);
            }

            p2 = findPreference(KEY_PREF_SERVICE_SNOOZE_INTERVAL);
            if(p2 != null)  {
                newValue = prefs.getString(KEY_PREF_SERVICE_SNOOZE_INTERVAL,"5") + " Minute(s)";
                p2.setSummary(newValue);
            }

            //---- service snooze
            p2 = findPreference(KEY_PREF_SERVICE_SNOOZE_INTERVAL);
            if(p2 != null)  {
                String tmpStr = prefs.getString(KEY_PREF_SERVICE_SNOOZE_INTERVAL,"0");
                if(!tmpStr.equals("0")) {
                    if (tmpStr.equals("0")) {
                        newValue = "Do not alert";
                    } else {
                        newValue = prefs.getString(KEY_PREF_SERVICE_SNOOZE_INTERVAL, "5") + " Minute(s)";
                    }
                    p2.setSummary(newValue);
                }
            }

            p2 = findPreference(KEY_PREF_TONE_PLAY_TIME);
            if(p2 != null)  {
                newValue = prefs.getString(KEY_PREF_TONE_PLAY_TIME, "10") + " seconds";
                p2.setSummary(newValue);
            }

            p2 = findPreference(KEY_PREF_CONTROLLER);
            if(p2 != null)  {
                newValue = "Current controller " + prefs.getString("pref_controller", "1");
                p2.setSummary(newValue);
            }

            p2 = findPreference(KEY_PREF_PAUSE);
            if(p2 != null) {
                try {
                    myDb.open();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    myDb.close();
                }
                if( myDb.getPauseTime() == 0 ) {
                    p2.setSummary("Pause the controller for a period of time");
                }
                else {
                    p2.setSummary("Controller is paused");
                }
                myDb.close();
            }



        }

        //--------------------------------------------------------------------------
        // Method:  onPause
        // Actions: default actions
        //--------------------------------------------------------------------------
        @Override
        public void onPause() {

            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

}


    public static void setAlarm(Context ctx) {
        //===============================================================
        // Clear the alarm just in case we get hosed up
        //===============================================================
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        int minutes = Integer.parseInt(prefs.getString("pref_refresh_interval", "5"));

        // setup the alarm manager
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);

        // setup the intent to do stuff with the service
        //Intent serviceIntent = new Intent(this, MyService.class);

        //Log.d("MyActivity", "Setting the pending intent to the MyReceiver class" );
        Intent i = new Intent(ctx, MyReceiver.class);

        // Was 'getService' - Changed to 'getBroadcast'
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(ctx, 0, i, 0);

        // Kill any stragglers (if any )
        alarmManager.cancel(pendingIntent);
        //-----------------------------------------------------------
        // set the alarm
        // If minutes > 0, then set the alarm
        // NOTE: MyReceiver will handle any calls at this point
        //-----------------------------------------------------------
        if (minutes > 0) {
            Toast toast = Toast.makeText(ctx, "System wake-up set to every " + minutes + " minutes", Toast.LENGTH_SHORT);
            toast.show();

            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + minutes * 60 * 1000, minutes * 60 * 1000, pendingIntent);
        }
    }




}
