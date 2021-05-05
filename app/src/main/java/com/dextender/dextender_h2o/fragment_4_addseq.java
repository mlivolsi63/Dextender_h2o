package com.dextender.dextender_h2o;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

//----------------------------------------------------------------------------------------
// When a Sequence is clicked from the drop-down list, this activity is called
//
// if the button is clicked (with the action specified in the XML) we run the:
// - The httpd activity
// - The local database update on success
//----------------------------------------------------------------------------------------
public class fragment_4_addseq extends Activity {


    String  seqString;                                          // Keep this global to all methods
    Integer seqZones;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_4_addseq);

        FrameLayout originalLayout = (FrameLayout)findViewById(R.id.frag_4_addSeq2);                // Need to know this, so I can add to it

        Intent lastIntent = getIntent();
        seqString = lastIntent.getStringExtra("seqName");                                           // Sequence name from the previous screen
        seqZones  = Integer.parseInt(lastIntent.getStringExtra("seqZoneCount"));                    // Number of zones from the previous screen


     //   final float scale = this.getResources().getDisplayMetrics().density;
     //   int pixels = (int) (100 * scale + 0.5f);


        //-----------------------------------------------------------------
        // Each row will be wrapped with an outer linear layout
        //-----------------------------------------------------------------
        LinearLayout subLayout = new LinearLayout(this);
        subLayout.setOrientation(LinearLayout.VERTICAL);

        subLayout.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));



        LinearLayout lHeader = new LinearLayout(this);
        lHeader.setOrientation(LinearLayout.VERTICAL);
        lHeader.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
        TextView headerText = new TextView(this);
        headerText.setText(R.string.frag4B_header);
        headerText.setTextColor(Color.WHITE);
        headerText.setTextSize(getResources().getDimension(R.dimen.iactive_text));
        lHeader.addView(headerText);

        subLayout.addView(lHeader);

        //-----------------------------------------------------------------
        // Create  all the rows
        //-----------------------------------------------------------------
        for(int i=0; i< seqZones; i++) {
            LinearLayout l1 = new LinearLayout(this);
            LinearLayout l2 = new LinearLayout(this);
            l1.setOrientation(LinearLayout.HORIZONTAL);
            l1.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));

            l2.setOrientation(LinearLayout.HORIZONTAL);
            l2.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView viewName = new TextView(this);
            viewName.setText(R.string.frag4B_zone);
            viewName.setId(i);
            viewName.setTextColor(Color.WHITE);
            viewName.setWidth(160);
            viewName.setTextSize(getResources().getDimension(R.dimen.iactive_text));

            final EditText editText = new EditText(this);
            editText.setId(i + 200);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setEms(4);
            editText.setMinimumWidth(100);
            editText.setTextSize(getResources().getDimension(R.dimen.iactive_text));
            editText.setClickable(true);
            editText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    openSequenceDialog(editText);                // << COOLIO - I've never tried this before. I can pass the field !!!!
                    return false;
                }
            });


            //editText.setTextSize(R.dimen.iactive_text);

            TextView viewName2 = new TextView(this);
            viewName2.setText(R.string.frag4B_runtime);
            viewName2.setId(i + 400);
            viewName2.setTextColor(Color.WHITE);
            viewName2.setWidth(225);
            viewName2.setTextSize(getResources().getDimension(R.dimen.iactive_text));

            final EditText editText2 = new EditText(this);
            editText2.setId(i + 600);
            editText2.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText2.setEms(4);
            editText2.setMinimumWidth(100);
            editText2.setPadding(5,0,5,0);
            editText2.setTextSize(getResources().getDimension(R.dimen.iactive_text));
            editText2.setClickable(true);
            editText2.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    openMinutesDialog(editText2);                // << COOLIO - I've never tried this before. I can pass the field !!!!
                    return false;
                }
            });


            l1.addView(viewName);
            l1.addView(editText);
            l1.addView(viewName2);
            l1.addView(editText2);

            TextView buffer = new TextView(this);
            buffer.setHeight(10);
            l2.addView(buffer);

            subLayout.addView(l1);
            subLayout.addView(l2);
        }
        originalLayout.addView(subLayout);  // Add the master wrapper to the original high level
    }


    //=====================================================

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }


    //===================================================================
    // Man.. am I proud of this. Not so much for the method below
    // but i can dynamically create fields, and then access those fields
    // via a dialog !
    //===================================================================
    public void openMinutesDialog(final EditText inField) {

        final String[] minutes = {
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10","15", "20", "25","30", "35","40","45","50","55", "60"};

        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        myDialog.setTitle("Minutes");

        myDialog.setItems(minutes, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                 inField.setText( minutes[which]);
            }});

        myDialog.setNegativeButton("Cancel", null);
        myDialog.show();
    }


    //===================================================================
    // Same general routine as "fragment_5_addschedule"
    // See the method from FRAG5 for more information
    // Man.. am I  ALSO proud of this. Not so much for the method below
    // but i can dynamically create fields, get the data from the database
    // use the data to create a dialog and populate the dynamic fields
    // Woo f*ck*ng woo !
    //===================================================================

    public void openSequenceDialog (final EditText inField) {
        MyDatabase myDb = new MyDatabase(this);

        boolean dbRc=false;
        try {
            myDb.open();
            dbRc = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<String> seqNamesList = new ArrayList<>();
        ArrayList<Integer> seqIdList = new ArrayList<>();


        int records=0;
        if(dbRc) {
            try {
                records=myDb.getZonesArray(seqNamesList, seqIdList);
                myDb.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final String[] sequenceNameArray = seqNamesList.toArray(new String[seqNamesList.size()]);
        final Integer[] sequenceIdArray  = seqIdList.toArray(new Integer[seqIdList.size()]);



        if((dbRc) && (records > 0)) {
            AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
            myDialog.setTitle("Which Sequence");


            myDialog.setItems(sequenceNameArray, new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //setSeqName.setText(sequenceNameArray[which]);
                    inField.setText(String.valueOf(sequenceIdArray[which]) );

                }
            });

            myDialog.setNegativeButton("Cancel", null);
            myDialog.show();
        }
        else {
            Toast.makeText(getApplicationContext(), "There are no sequences to add", Toast.LENGTH_SHORT).show();

        }
    }


    public void submitZones(View v) {

        boolean dbRc=false;
        MyDatabase myDb    = new MyDatabase(this);
        MyHttpPost myHttp  = new MyHttpPost();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        //------------------------------
        // Open the database
        //------------------------------
        try {
            myDb.open();
            dbRc = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        //---------------------------------------
        // We have a good Rc from opening the DB
        //---------------------------------------
        if (dbRc) {

            final String[] Url = new String[1];
            final String[] port = new String[1];
            final String[] access_key = new String[1];
            String zoneId[]  = new String[64];
            String zoneRun[] = new String[64];

            //---------------------------------------------------------------
            // ..and we have sucessfully retrieved data from the database
            //---------------------------------------------------------------
            if (myDb.getDexserver(prefs.getString("pref_controller", "1"), Url, port, access_key)) {

                String webUrl = "http://" + Url[0] + ":" + port[0] +
                        "/cgi-bin/h2o/h2o_sequences.cgi?event=342&sequence=0&app=1&fname=" + seqString.replaceAll(" ", "%20") +
                        "&zoneNumber=" + seqZones;

                boolean blanks=false;
                int zrt;
                for (int i = 0; i < seqZones; i++) {
                    EditText zoneNumberId = (EditText) findViewById(i + 200);
                    EditText zoneRunTimeId = (EditText) findViewById(i + 600);

                    String zoneNumber = zoneNumberId.getText().toString();
                    String zoneRunTime = zoneRunTimeId.getText().toString();

                    if((zoneNumber.length()==0) ||(zoneRunTime.length()==0) ) {
                        blanks=true;
                    }
                    else {
                        zrt=Integer.parseInt(zoneRunTime)*60;
                        webUrl += "&zid" + String.valueOf(i + 1) + "=" + zoneNumber + "&rt" + String.valueOf(i + 1) + "=" + String.valueOf(zrt);
                        zoneId[i] = zoneNumber;
                        zoneRun[i] = zoneRunTime;
                    }
                }

                if(blanks) {
                    Toast toast = Toast.makeText(this, "Some entries are blank", Toast.LENGTH_LONG);
                    toast.show();
                }
                else {
                    String[] response = myHttp.callHome(webUrl).split("\\|");
                    // Response will be 01100000|Success|<new sequence id>

                    boolean procFlag=false;
                    if (response[0].substring(0,3).equals("011")) {

                        try {
                            myDb.insertSequences(Integer.parseInt(response[2]), 1, seqString, (int) System.currentTimeMillis() / 1000);
                            procFlag=true;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        if(procFlag) {
                            procFlag=false;
                            for (int i = 0; i < seqZones; i++) {
                                try {
                                    myDb.insertSequenceZones(Integer.parseInt(response[2]), Integer.parseInt(zoneId[i]),
                                            i, 1, Integer.parseInt(zoneRun[i]) * 60, System.currentTimeMillis() / 1000);
                                    procFlag=true;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if(procFlag)
                            Toast.makeText(getApplicationContext(), "Zone sequence created", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(), "Could not create zone sequence", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Could not save sequence on server", Toast.LENGTH_LONG).show();
                    }
                    myDb.close();
                    finish();
                }
            }
        }
    }
}