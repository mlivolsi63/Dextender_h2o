package com.dextender.dextender_h2o;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

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
public class MyAbout extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if(actionBar != null) actionBar.hide();
        setContentView(R.layout.fragment_about);

        //----------------------------------------------------
        // Link-a-fi the string
        //----------------------------------------------------
        TextView t2 = (TextView) findViewById(R.id.site);
        t2.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
