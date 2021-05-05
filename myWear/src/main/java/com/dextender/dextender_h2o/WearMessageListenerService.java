package com.dextender.dextender_h2o;

import com.google.android.gms.wearable.WearableListenerService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;


import com.google.android.gms.wearable.MessageEvent;


public class WearMessageListenerService extends WearableListenerService {
    private static final String START_ACTIVITY    = "/dextender_activity";
    private static final String WEAR_MESSAGE_PATH = "/dextender_message";



    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        //------------------------------------------------------------------------
        // Mobile will send 2 messages. 1 which is a blank (to start the activity)
        // the 2nd message (or 3rd and so on) will be data)
        // So.. the 1st just starts up the task
        // the 2nd will update the database with the data, which is then handled
        // by the main activity on the wear device
        //-------------------------------------------------------------------------
        //Log.d("Wear Listener", "Holy Mother - Getpath ->" + messageEvent.getPath());

        if( messageEvent.getPath().equals( START_ACTIVITY ) || messageEvent.getPath().equals( WEAR_MESSAGE_PATH )) {
            final String message = new String(messageEvent.getData());

            if(messageEvent.getPath().equals( START_ACTIVITY )) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // <-- This is what is was, causing multiple screens
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            else {
                MyDatabase myDb = new MyDatabase(this);
                try {
                    myDb.open();
                    myDb.receiveInComingRecord(message);
                    myDb.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                //----------------------------------------------------------------------------------
                // I'm not really liking what Google and the other examples are showing.
                // I'd rather pull an 'oracle' and use the database for everything ..
                // In this case, I've already stored the data in the database.. so instead
                // of sending the message to the activity (and all the good unstabliness that that
                // entails), I'd rather just store the data here, and prod the activity..
                // if it doesn't work..no big whoop.. I've got the data
                //----------------------------------------------------------------------------------
                Intent messageIntent = new Intent();
                messageIntent.setAction(Intent.ACTION_SEND);
                //messageIntent.putExtra("message", message);    // Old Googly Way
                messageIntent.putExtra("message", "wtf");        // New database-ish way
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
            }
        }
        else {
            super.onMessageReceived(messageEvent);
        }

    }
}