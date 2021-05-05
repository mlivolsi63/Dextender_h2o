package com.dextender.dextender_h2o;

//----------------------------------------------------------------------------------------------
// Class      : MyReceiver
// Author     : http://it-ride.blogspot.com/2010/10/android-implementing-notification.html
// Modified by: Mike LiVolsi
// Date       : October 2014
//
// Purpose    : To be honest, not really sure how this works. I'm guessing it sets the alarming
//              feature of android.
// Called by  : Fragments
//----------------------------------------------------------------------------------------------

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class MyReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive (Context context, Intent intent) {
            Intent service = new Intent(context, MyService.class);
            startWakefulService(context, service);
        }
}
