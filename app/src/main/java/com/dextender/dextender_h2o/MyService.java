package com.dextender.dextender_h2o;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.util.Objects;


//=================================================================================================
// Class   : MyService
// Author  : Mike LiVolsi
// Date    : October 2014
// Credits : How to create a service: http://www.youtube.com/watch?v=GAOH7XTW7BU
//           Battery Saver          : http://it-ride.blogspot.com/2010/10/android-implementing-notification.html
//
// Notes   : There are 3 (currently) main components to this project.
//            1) The gui front end (what the user sees)
//            2) The back end (ie. services)
//            3) The widget
//           Here is how they are related:
//           - When you click the icon on your phone for "dExtender", it will spawn the services
//           - The "service" grabs and stores info in our local SQLite database. The information
//             in the database is what's displayed on the gui screen (notably , zone information and component status)
//             Screen 2 (aka frag2)
//           - The widget is like "frag1", and all it's doing is grabbing some preference info
//             and data from the database to display
//
// Notes:    - See MyReceiver.java for timer updates
//
//=================================================================================================
public class MyService extends IntentService {


    public MyService() {
        super("MyService");
    }


    //---------------------------------------------------
    // Class wide values
    //---------------------------------------------------
    @Override
    public IBinder onBind(Intent arg0){
        return null;
    }

    protected void onHandleIntent(Intent intent) {

        //---------------------------------------------------------------------------------
        // check the global background data setting and see if we have network connectivity
        // NOTE: Other calls have been depracated. This is using the new call.
        //---------------------------------------------------------------------------------
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() == null) {
            Log.i("MyService", "No network access");
            stopSelf();
            return;
        }
        //-------------------------------------------------------------
        // Meat and potatoes time. Do the work in a separate thread !
        // See comments in the PollTask class
        //-------------------------------------------------------------
        new PollTask().execute();
        MyReceiver.completeWakefulIntent(intent);
    }

    private class PollTask extends AsyncTask<Void, Void, Void> {

        //--------------------------------------------------------------------
        // This is where the work is done.
        // All database calls, httpd posts, etc.. are done here
        // This work is all done in a seperate thread (which is good)
        //--------------------------------------------------------------------
        @Override
        protected Void doInBackground(Void... params) {

            PowerManager.WakeLock mWakeLock;

            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "dexTag");   // Obtain the wakelock
            mWakeLock.acquire();

            //----------------------------------------------------------------
            // Call all our different classes
            //----------------------------------------------------------------
            MyHttpPost http         = new MyHttpPost();                                                  // Call the httpd class
            MyDatabase myDb         = new MyDatabase(getBaseContext());                                  // call the Database class
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

            //----------------------------------------------------------------
            // set some variables that will be used throughout this class
            //----------------------------------------------------------------
            final String[] Url             = new String[1];
            final String[] port            = new String[1];
            final String[] access_key      = new String[1];
            boolean pref_serviceMode       = prefs.getBoolean("prefsvc", false);                        // should the service do anything ?

            String[] recordPiece;
            String response="";                                                           // response from http calls

            //---------------------------
            // Old vars ?
            //---------------------------
            short SERVICE_OFF=-1;
            short SERVICE_ON=0;

            //---------------------------
            // Set the alarm
            //---------------------------
            setAlarm(Integer.parseInt(prefs.getString("pref_refresh_interval", "5")));

            //-----------------------------------------------------------------
            // Open the database, because we will need it.
            //-----------------------------------------------------------------
            boolean dbRc=false;
            try {
                myDb.open();
                dbRc=myDb.getDexserver(prefs.getString("pref_controller", "1"), Url, port, access_key); // get controller location
                dbRc=true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            //---------------------------------------------------------------------------
            // Get controller values from the database and call the controller directly
            //---------------------------------------------------------------------------
            // Log.d("MyService", "In main polltask , mode->" + pref_serviceMode + " DB Return code ->" + dbRc);

            boolean Rc=false;

            String webNotificationResponse="";                                                      // we're going to send this to the update notifier
            if( (pref_serviceMode) && (dbRc)) {
                if (myDb.getDexserver(prefs.getString("pref_controller", "1"), Url, port, access_key)) {
                    if (!Url[0].equals("-")) {                                                      // it's not just a dash , meaning, we're configured


                        //--------------------------------------------------------------------------
                        // get the last master time check
                        //--------------------------------------------------------------------------
                        String webUrl = "http://" + Url[0] + ":" + port[0] +
                                "/cgi-bin/h2o/h2o_appsync.cgi?uid=" + prefs.getString("uid", "1234-4567-7890") +
                                "&controller=" + prefs.getString("pref_controller", "1") +
                                "&access_key=" + access_key[0] +
                                "&synccheck=1";

                        String[] webResponse = http.callHome(webUrl).split("\\|");
                        if(webResponse[0].substring(0,3).equals("011")) {                                     // we got a good answer from the web call

                            // Log.d("dext service", "db time --->" + myDb.getLocalSystemTimestamp() );

                            if (Long.parseLong(webResponse[1]) > myDb.getLocalSystemTimestamp()) {  // if the master is greater than our local timestamp

                                // Log.d("dext service", "Length " + webResponse.length );
                                if(webResponse.length > 2) {
                                    webNotificationResponse=webResponse[2];
                                }


                                webUrl = "http://" + Url[0] + ":" + port[0] +
                                        "/cgi-bin/h2o/h2o_appsync.cgi?uid=" + prefs.getString("uid", "1234-4567-7890") +
                                        "&controller=" + prefs.getString("pref_controller", "1") +
                                        "&access_key=" + access_key[0];                             // ip, port, serverId #, uid, uno|mega

                                response = http.callHome(webUrl);
                                if (response.substring(0, 7).equals("<begin>")) {

                                    myDb.updateSystemTimestamp(1, Long.parseLong(webResponse[1]));  // update the database with the master time
                                    Rc = true;                                                      // and we didn't get a bogus return code
                                }
                            }
                        }
                        else {
                            myDb.logIt(1, "Could not get sync info. Reason:" + webResponse[0], (long)0);
                        }
                    }
                }
            }

            if( (pref_serviceMode) && (Rc) ) {                                                      // our service is 'on' and we got good return codes <-- NOTE Difference of DbRC and Rc

                myDb.updateServiceStatus("Android service", SERVICE_ON);                            // now that the DB is open, I can update the service
                // Log.d("dext srvc", "Calling update notification");
                if (webNotificationResponse.length() > 0) {                                         // if we populated this above with the response from the server, pass it on
                    updateNotification(webNotificationResponse);
                }
                else {
                    updateNotification("Irrigation was run");                                       // otherwise, pass a default.
                }

                Integer i, j;
                Integer tableId;                       // each table has an id, that we will use to call the appropriate insert statement (see case statement)
                Integer tableCount = 0;                // expected number of tables
                Integer tableCounter = 0;              // Current count of tables passed
                Integer charPos1, charPos2;            // used to find certain characters for parsing (ie. blanks)
                Integer rowCount;                      // parse through the number of rows by the argument passed instead of trying to slice and dice records
                Integer fieldCount = 0;                // Future use sanity check
                String  tName, record;
                Boolean tableFlag = false;

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
                                    myDb.logIt(1, "My service encountered an error", (long) 0);
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
                                                        Long.parseLong(recordPiece[0]),   Integer.parseInt(recordPiece[1]), Integer.parseInt(recordPiece[2]),
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
                                                myDb.logIt(3, recordPiece[1], Long.parseLong(recordPiece[2]));
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

                                                // The overall status of the system is controlled by the status table
                                                myDb.updateServiceStatus("Controller switch state", Integer.parseInt(recordPiece[2]));

                                                // Need to change the controller state in the preferences
                                                if(Integer.parseInt(recordPiece[2]) == 1) {
                                                    setBooleanPreference("pref_ctrlStatus", true);
                                                }
                                                else {
                                                    setBooleanPreference("pref_ctrlStatus", false);
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
                                            myDb.logIt(1, "Invalid counts from master refresh", (long) 0);
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
            }
            else {
                if (!pref_serviceMode && dbRc) {
                    myDb.updateServiceStatus("Android service", SERVICE_OFF);                         // now that the DB is open, but service if off
                }
            }

            if(dbRc) {
                myDb.close();
            }
            mWakeLock.release();
            return null;
        }

        /**
         * In here you should interpret whatever you fetched in doInBackground
         * and push any notifications you need to the status bar, using the
         * NotificationManager. I will not cover this here, go check the docs on
         * NotificationManager. *
         * What you HAVE to do is call stopSelf() after you've pushed your
         * notification(s). This will:
         * 1) Kill the service so it doesn't waste precious resources
         * 2) Call onDestroy() which will release the wake lock, so the device
         * can go to sleep again and save precious battery. */

        protected void onPostExecute(Void result) {
            // update notification
            stopSelf();
        }
    }



    //-------------------------------------------------------------------------------------
    // Routine  : onStartCommand
    // Class    : MyService
    // Called by: Class->fragment_preference | onPreferenceChange (listener)
    // Author   : Mike LiVolsi / Originally by various (lots of examples on the web)
    // Date     : Oct. 2014
    //
    // Purpose  : To have an icon and notifications in the notification area and notification
    //            tray, that this service is running
    //-------------------------------------------------------------------------------------
    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {

        onHandleIntent(intent);
        return START_NOT_STICKY;
    }

    //-------------------------------------------------------------------------------------
    // Routine  : onDestroy
    // Class    : MyService
    // Called by: Class->fragment_preference | onPreferenceChange (listener)
    // Author   : Mike LiVolsi / Originally by various (lots of examples on the web)
    // Date     : Oct. 2014
    //
    // Purpose  : To have an icon and notifications in the notification area and notification
    //            tray, that this service is running
    //-------------------------------------------------------------------------------------
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    //-------------------------------------------------------------------------
    // Routine: updateNotication
    // Author : Mike LiVolsi
    // Date   : October 2014
    // Purpose: If there's a notification in the "notification tray", then this routine
    //          updates it
    //
    // http://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html
    //
    //-------------------------------------------------------------------------
    private void updateNotification(String argValue){

        int notificationID=2113;
        //=========================================================================================
        // For codes and their values see:
        // http://character-code.com/arrows-html-codes.php
        //=========================================================================================
        String notificationTitle="dExtender-h2o";

        int smallIcon = this.getResources().getIdentifier("tapiricon", "drawable", this.getPackageName());      // instead of R.drawable.s<whatever>



        int requestID = (int) System.currentTimeMillis();
        NotificationManager mNotificationManager =  (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);

        // Creates an explicit intent for an Activity in your app
        Intent notificationIntent = new Intent(getApplicationContext(), MyActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // new - Jan 2015

        PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), requestID,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mbuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(smallIcon)                                                            // smallIcon - what shows up in the tippy top
                .setContentTitle(notificationTitle)                                                 // The larger text
                .setContentText(argValue)                                                           // the smaller text under the title
                .setPriority(1)                                                                     // Priority min = -2 | max = 2
                ;
        //Bitmap bm = BitmapFactory.decodeResource(getResources(), largeIcon);                        // the large icon needs to be converted into a bitmap (weird)
        Bitmap bm = BitmapFactory.decodeResource(getResources(), smallIcon);                        // the large icon needs to be converted into a bitmap (weird)
        mbuilder.setLargeIcon(bm);                                                                  // Now we set the bitmap'ed icon as part of a notification

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MyActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

         mbuilder.setContentIntent(resultPendingIntent);
        // Because the ID remains unchanged, the existing notification is
        // updated.
        mNotificationManager.notify(notificationID, mbuilder.build());

    }

    //--------------------------------------------------------------------------------------------
    // Method: setAlarm
    // This routine allows us to set the alarm interval. We're going to use it in
    // case we need to change how much we are incrementing.
    // NOTES:  !!!! Almost identical code to what is in the splash class !!!!!
    //
    // PROPOSED ENHANCEMENTS: The gmtOffset and the dextime will be used to set the time closer
    //                        to what the dex has
    //--------------------------------------------------------------------------------------------
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
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + minutes * 60 * 1000, minutes * 60 * 1000, pendingIntent);
        }
    }


    //----------------------------------------------------------------------------------
    // EXACT SAME METHOD IN MYSERVICE .. What you change here, change there
    //----------------------------------------------------------------------------------
    private void setBooleanPreference (String preferenceName, boolean inBoolean) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor1 = settings.edit();
        editor1.putBoolean(preferenceName, inBoolean);
        editor1.apply();
    }

}