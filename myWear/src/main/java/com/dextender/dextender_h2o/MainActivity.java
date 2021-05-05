package com.dextender.dextender_h2o;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wear.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.Timer;
import java.util.TimerTask;


//--------------------------------------------------------------------------------------------------
// MLV Added Implements runnable
// Program: Main Activty on the wear
// Author : MLV
//
// Design considerations. The tutorials on the net, for lack of a better word, are shit.
// They don't delve into the registering and de-registering of the broadcast listener
// (once guy got it right and said you should register in the onStart and unregister on
//  the onpause). The other stumbling block was the interaction between the broadcast listener
// and the main activity. The way I handled that is through the database. Just like the
// MOBILE device, background tasks that take in data will just shove data into the database
// this is what databases are for. Then, the UI will grab that data and display.
// And finally, the last hurdle was dealing with the listener. If you notice, there is nothing
// like that anywhere on the web. The activity needs to be fired up (note: there is no way to
// determine if an activity is started), so the mobile sends a blank message and the listener
// fires up the main activity. Everything else the listener does is ingesting of messsages
//
// REFERENCE
// 0 high target
// 1 low target
// 2 system message
// 3 background
// 4 Chart background
// 5 vibrate
// 6 screen behavior
// 7 Chart lines
// 8 tbd
// 9 tbd
// 10 tbd
// 11 tbd
// 12 receiver date
// 13 bg value
// 14 bg trend
// 15 three hour high
// 16 three hour low
// 17 number of BG records
//
//--------------------------------------------------------------------------------------------------

public class MainActivity extends WearableActivity implements Runnable, MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks{


    private TextView  w_currentTime;
    private TextView  w_bgHighLow03;
    private TextView  w_bgHighLowSetting;
    private TextView  w_bgValue;
    private TextView  w_receiverTime;

    private String    bgString="";
    private int       backGround=0;


    private GoogleApiClient mApiClient;
    final   Tools           tools = new Tools();
            MyDatabase      myDb = new MyDatabase(this);

    //------------------------------------------------------
    // Used to register and unregister with the broadcast
    // receiver. Because it's started in the onresume
    // and removed in the un-pause, we create a global var
    //------------------------------------------------------
    MessageReceiver messageReceiver = new MessageReceiver();
    boolean registered=false;
    boolean stubSet=false;

    long glb_lastRunTime=0;
    String lowLimit;
    String highLimit;

    boolean vibrate=false;

    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();

    //-------------------------------------------------------------------------------------
    // THe onCreate is called by the system
    // THis is basically the main screen of the activity
    //-------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        try{
            myDb.open();
            backGround=myDb.getBackground();
            vibrate= myDb.getVibrate()==1;
            highLimit = String.valueOf(myDb.getHighLimit());
            lowLimit = String.valueOf(myDb.getLowLimit());
            myDb.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        switch(backGround) {
            case 0:  stub.setBackgroundResource(backGround);
                break;
            case 1: bgString = "_bubble01";
                break;
            case 2: bgString = "_christmas01";
                break;
            case 3: bgString = "_christmas02";
                break;
            case 4: bgString = "_christmas04";
                break;
            case 5: bgString = "_fireworks01";
                break;
            case 6: bgString =  "_halloween01";
                break;
            case 7: bgString =  "_halloween02";
                break;
            case 8: bgString =  "_halloween03";
                break;
            case 9: bgString =  "_halloween04";
                break;
            case 10: bgString = "_hanukkah";
                break;
            case 11: bgString = "_milkyway01";
                break;
            case 12: bgString = "_mountain01";
                break;
            case 13: bgString = "_nebula01";
                break;
            case 14: bgString = "_snowman01";
                break;
            case 15: bgString = "_stars01";
                break;
            case 16: bgString = "_stars02";
                break;
            case 17: bgString = "_stars03";
                break;
            case 18: bgString = "_stars04";
                break;
            case 19: bgString = "_summer01";
                break;
            case 20: bgString = "_sunset01";
                break;
            case 21: bgString = "_sunset02";
                break;
            case 22: bgString = "_trees01";
                break;
            case 100: bgString = "_glass1";
                break;
            case 101: bgString = "_glass2";
                break;
            case 102: bgString = "_gradient1";
                break;
            case 103: bgString = "_gradient2";
                break;
            default:
                backGround=0;
                stub.setBackgroundResource(backGround);
                break;
        }


        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                TextView whatami = (TextView) findViewById(R.id.whatami);
                if( (whatami != null) && (backGround != 0)) {
                    String iam = whatami.getText().toString();
                    bgString = "bg" + iam + bgString;

                    stub.setBackgroundResource(getResources().getIdentifier(bgString, "mipmap", getApplication().getPackageName() ) );

                }


                //Log.d("Main(wear)", "Stub Inflater");

                Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/digital_counter_7_italic.ttf");
                w_currentTime       = (TextView) stub.findViewById(R.id.currentTime);
                w_currentTime.setTypeface(myTypeface);

                w_bgHighLow03       = (TextView) stub.findViewById(R.id.bgHighLow03);
                w_bgHighLowSetting  = (TextView) stub.findViewById(R.id.bgHighLowSetting);

                myTypeface          = Typeface.createFromAsset(getAssets(), "fonts/ANA.ttf");
                w_bgValue           = (TextView) stub.findViewById(R.id.bgValue);
                w_bgValue.setTypeface(myTypeface);

                w_receiverTime      = (TextView) stub.findViewById(R.id.receiverTime);
                stubSet=true;

                refresh();

            }
        });

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setAmbientEnabled();
        initGoogleApiClient();

    }


    private void initGoogleApiClient() {
        //Log.d("Main(wear)", "initGoogleApiClient");
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks( this )
                .build();

        if( !( mApiClient.isConnected() || mApiClient.isConnecting() ) ) {
            mApiClient.connect();
        }
    }



    @Override
    public void onMessageReceived ( final MessageEvent messageEvent ) {

        //Log.d("Main(wear)", "UI message received");

        runOnUiThread( new Runnable() {
            @Override
            public void run() {
            }
        });
    }


    @Override
    public void onConnected(Bundle bundle) {

       // Log.d("onConnected(wear)", "OnConnected");
        Wearable.MessageApi.addListener( mApiClient, this );
    }




    @Override
    protected void onStop() {
        //Log.d("onCreate(wear)", "On stop");

        if ( mApiClient != null ) {
            Wearable.MessageApi.removeListener( mApiClient, this );
            if ( mApiClient.isConnected() ) {
                mApiClient.disconnect();

            }
        }
        super.onStop();
    }


    @Override
    protected void onResume() {
        //Log.d("onCreate(wear)", "On resume");

        super.onResume();
        if( mApiClient != null && !( mApiClient.isConnected() || mApiClient.isConnecting() ) ) {
            mApiClient.connect();
        }

        // Register the local broadcast receiver
        if(!registered) {
            IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
            //MessageReceiver messageReceiver = new MessageReceiver();
            // Log.d("Main-onResume(wear)", "registering broadcast receiver");
            LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
            registered = true;
        }
        startTimer();
    }

    @Override
    protected void onPause() {
        //Log.d("onCreate(wear)", "On this pause");

        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        stoptimertask();
        //onDestroy();
    }


    @Override
    protected void onDestroy() {
        //Log.d("onCreate(wear)", "On destroy");

        if( mApiClient != null ) {
            mApiClient.unregisterConnectionCallbacks(this);
        }
        super.onDestroy();
    }


    //-----------------------------------------------------------------------------------------
    // THis bad boy gets called when the wearmessagelistener is called (called by that class)
    // Since that class has already shoved the message into the database (which no native android
    // programer would dream of doing) then it's just a simple matter of calling refresh
    // and process against the database.
    //-----------------------------------------------------------------------------------------

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d("Main", "In message receiver");
            refresh();
        }
    }

    public void refresh() {
        try {

            //Log.d("Main(wear)", "On refresh");
            //   0| 1|          2|3|   4|   5|   6|         7|  8|  9| 10|11| |13| 14|15|16| 17|18
            //"170|65|sys message|0|tbd1|tbd2|tbd3|1473961886|101|180|203|52|2|30|120|90|25|110|90"

            myDb.open();
            String[] bgTime   = new String[1];
            String[] bgVal    = new String[1];
            String[] bgTrend  = new String[1];
            String[] bgHigh03 = new String[1];
            String[] bgLow03  = new String[1];
            long[] recordUpdated = new long[1];
            myDb.getBgSummary(System.currentTimeMillis()/1000, bgTime, bgVal, bgTrend, bgHigh03, bgLow03, recordUpdated);
            highLimit = String.valueOf(myDb.getHighLimit());
            lowLimit = String.valueOf(myDb.getLowLimit());
            glb_lastRunTime = recordUpdated[0];
            myDb.close();

            w_currentTime.setText(tools.now());

            //---------------------------------------------------------------
            // 3 hour high and 3 hour low
            //---------------------------------------------------------------
            String tmpString;
            if(bgHigh03[0].equals("0")) tmpString = "---";
            else                        tmpString = bgHigh03[0];

            if(bgLow03[0].equals("0"))  tmpString = tmpString+ "\n---";
            else                        tmpString = tmpString+ "\n" + bgLow03[0];

            w_bgHighLow03.setText(tmpString);

            //---------------------------------------------------------------
            //
            //---------------------------------------------------------------

            if(Integer.parseInt(highLimit) == 0) tmpString = "OFF\n";
            else                                 tmpString = highLimit+"\n";

            if(Integer.parseInt(lowLimit) == 0) tmpString = tmpString + "OFF";
            else                                tmpString = tmpString + lowLimit;

            w_bgHighLowSetting.setText(tmpString);

            //---------------------------------------------------------------
            // Big Number
            //---------------------------------------------------------------
            if(Integer.parseInt(bgVal[0]) == 0)  w_bgValue.setText("---");
            else {
                w_bgValue.setText(bgVal[0]);
                if (
                        (Integer.parseInt(bgVal[0]) >= Integer.parseInt(highLimit) ||
                         Integer.parseInt(bgVal[0]) <= Integer.parseInt(lowLimit))
                    &&
                         vibrate
                ) {
                    if(Integer.parseInt(bgVal[0]) >= Integer.parseInt(highLimit)) {
                        w_bgValue.setBackgroundResource(R.mipmap.tapir3);                        // Yellow background
                    }
                    else {
                        w_bgValue.setBackgroundResource(R.mipmap.tapir3);                        // red background
                    }
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    long[] vibrationPattern = {0, 500, 50, 300};
                    //-1 - don't repeat
                    final int indexInPatternToRepeat = -1;
                    vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
                }
            }

            //---------------------------------------------------------------
            //
            //---------------------------------------------------------------


            w_receiverTime.setText(tools.epoch2FmtTime(System.currentTimeMillis()/1000, "HH:mm:ss"));



        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //---------------------------------------------
    // Honesty time (again)
    // I didn't write the timer routines.
    // Still a little shacky on setting them up
    //---------------------------------------------

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void run() { }

    @Override
    public void onConnectionSuspended(int i) { }

    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 0, 1000); //

    }

    //---------------------------------------------
    // CLock - Stop the timer
    //---------------------------------------------
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    //---------------------------------------------
    // Initialize timer
    //---------------------------------------------
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {

                        if(stubSet) {
                            w_currentTime.setText(tools.now());
                            if(glb_lastRunTime != 0)
                                w_receiverTime.setText(tools.fuzzyTimeDiff(System.currentTimeMillis()/1000, glb_lastRunTime));
                            else
                                w_receiverTime.setText("");

                        }
                    }
                });
            }
        };
    }
}
