package com.dextender.dextender_h2o;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

//----------------------------------------------------------------------------------------
// Holy Crap is this routine complex !!!
//
// if the button is clicked (with the action specified in the XML) we run the:
// - The httpd activity
// - The local database update on success
//----------------------------------------------------------------------------------------
public class fragment_5_addschedule extends Activity implements View.OnClickListener {


    short glb_timeOption=0;
    short glb_dateOption=-1;
    short glb_sunType=-1;
    int   glb_time=0;


    TextView chooseSchedName;
    TextView setSchedName;

    TextView chooseSeqName;
    TextView setSeqName;
    TextView setSeqId;

    EditText chooseSunHours;
    EditText chooseSunMinutes;
    EditText chooseSunBeforeAfter;
    EditText chooseSunriseSunset;

    CheckBox sunday;
    CheckBox monday;
    CheckBox tuesday;
    CheckBox wednesday;
    CheckBox thursday;
    CheckBox friday;
    CheckBox saturday;


    RadioGroup dayEvenOdd;
    RadioButton radio_even;
    RadioButton radio_odd;

    RadioButton startTime;
    RadioButton sunTime;

    final String[] Url      = new String[1];
    final String[] port     = new String[1];
    final String[] key      = new String[1];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_5_addsched);

        //----------------------------------------------------------------------------
        // First two fields on the screen
        //----------------------------------------------------------------------------
        chooseSchedName      = (TextView) findViewById(R.id.frag5_add_schedName_title);
        setSchedName         = (TextView) findViewById(R.id.frag5_add_schedName_result);

        //----------------------------------------------------------------------------
        // The second field on the screen
        //----------------------------------------------------------------------------
        chooseSeqName        = (TextView) findViewById(R.id.frag5_add_seqName_title);
        setSeqName           = (TextView) findViewById(R.id.frag5_add_seqName_result);
        setSeqId             = (TextView) findViewById(R.id.hidden_forId);
        //----------------------------------------------------------------------------
        // Radio button to choose between start time OR time based on sunrise/sunset
        //----------------------------------------------------------------------------
        startTime            = (RadioButton) findViewById(R.id.frag5_add_radio_startAt);
        sunTime              = (RadioButton) findViewById(R.id.frag5_add_radio_suntime);

        //----------------------------------------------------------------------------
        // If sunTime - then these fields are in play
        //----------------------------------------------------------------------------
        chooseSunHours       = (EditText) findViewById(R.id.frag5_add_sunHour);
        chooseSunMinutes     = (EditText) findViewById(R.id.frag5_add_sunMinute);
        chooseSunBeforeAfter = (EditText) findViewById(R.id.frag5_add_sunBeforeAfter);
        chooseSunriseSunset  = (EditText) findViewById(R.id.frag5_add_sunriseSunset);

        //----------------------------------------------------------------------------
        // Checkboxes for which days
        //----------------------------------------------------------------------------
        sunday               = (CheckBox) findViewById(R.id.chk_sun);
        monday               = (CheckBox) findViewById(R.id.chk_mon);
        tuesday              = (CheckBox) findViewById(R.id.chk_tues);
        wednesday            = (CheckBox) findViewById(R.id.chk_wed);
        thursday             = (CheckBox) findViewById(R.id.chk_thur);
        friday               = (CheckBox) findViewById(R.id.chk_fri);
        saturday             = (CheckBox) findViewById(R.id.chk_sat);

        //----------------------------------------------------------------------------
        // Even or Odd Days (if chosen, above days are blanked out
        //----------------------------------------------------------------------------
        dayEvenOdd           = (RadioGroup) findViewById(R.id.frag5_add_radioEvenOdd);
        radio_even           = (RadioButton) findViewById(R.id.radio_even);
        radio_odd            = (RadioButton) findViewById(R.id.radio_odd);

        //=========================================================================
        chooseSchedName.setOnClickListener(this);
        chooseSeqName.setOnClickListener(this);

        chooseSunHours.setOnClickListener(this);
        chooseSunMinutes.setOnClickListener(this);
        chooseSunBeforeAfter.setOnClickListener(this);
        chooseSunriseSunset.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }


    @Override
    public void onClick(View v) {
        //Log.d("FRAG5ADD", "Clicked !!!!");
        if(v == chooseSeqName){
            openSequenceDialog();
        }
        else if (v == chooseSchedName) {
            openSchedNameDialog();
        }
        else if (v ==chooseSunHours) {
            openHoursDialog();
        }
        else if (v == chooseSunMinutes) {
            openMinutesDialog();
        }
        else if (v == chooseSunBeforeAfter) {
            openSunBeforeAfterDialog();
        }
        else if (v == chooseSunriseSunset) {
            openSunriseSunsetDialog();
        }
    }

    public void openSchedNameDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText edittext = new EditText(this);
        alert.setMessage("Please enter a schedule name");
        alert.setTitle("Name the schedule");

        alert.setView(edittext);

        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String argText = edittext.getText().toString();
                setSchedName.setText(argText);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                }
        );
        alert.show();
    }


    public void openSequenceDialog () {
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
                records=myDb.getSequencesArray(seqNamesList, seqIdList);
                myDb.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //-------------------------------------------------------------------
        // Create the array with the size of our list
        // !!! NOTES !!!!
        // An anonymous class (the stuff after the setItems and onCLick)
        // requires stuff from the outside to be either global or declared
        // as final. I couldn't access the arrayList so I converted that to
        // a regular array. The stuff below does it in one shot, which is good
        // because if you declare the array as final then add members, it wont
        // allow you to (because its 'final').  If you pre-create an array
        // that causes problems because even the empty values will show up
        // in the dialog. What a pain in the ass this was !!!
        //-------------------------------------------------------------------
        final String[] sequenceNameArray = seqNamesList.toArray(new String[seqNamesList.size()]);
        final Integer[] sequenceIdArray  = seqIdList.toArray(new Integer[seqIdList.size()]);



        if((dbRc) && (records > 0)) {
            AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
            myDialog.setTitle("Which Sequence");


            myDialog.setItems(sequenceNameArray, new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setSeqName.setText(sequenceNameArray[which]);
                    setSeqId.setText(String.valueOf(sequenceIdArray[which]) );

                }
            });

            myDialog.setNegativeButton("Cancel", null);
            myDialog.show();
        }
        else {
            Toast.makeText(getApplicationContext(), "There are no sequences to add", Toast.LENGTH_SHORT).show();

        }

    }

    public void openHoursDialog() {

        startTime.setChecked(false);
        sunTime.setChecked(true);
        glb_timeOption=2;

        final String[] hours = {
                "0", "1", "2","3", "4", "5","6", "7","8","9","10","11","12"};


        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        myDialog.setTitle("Hours");

        myDialog.setItems(hours, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String item = hours[which];
                chooseSunHours.setText(item);
            }
        });

        myDialog.setNegativeButton("Cancel", null);
        myDialog.show();
    }

    public void openMinutesDialog() {

        startTime.setChecked(false);
        sunTime.setChecked(true);
        glb_timeOption=2;

        final String[] minutes = {
                "0", "5", "10","15", "20", "25","30", "35","40","45","50","55"};

        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        myDialog.setTitle("Minutes");

        myDialog.setItems(minutes, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String item = minutes[which];
                chooseSunMinutes.setText(item);
            }});

        myDialog.setNegativeButton("Cancel", null);
        myDialog.show();
    }

    public void openSunBeforeAfterDialog() {

        startTime.setChecked(false);
        sunTime.setChecked(true);
        glb_timeOption=2;

        final String[] beforeAfter = {
                "before", "after"};

        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        myDialog.setTitle("Minutes");

        myDialog.setItems(beforeAfter, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String item = beforeAfter[which];
                chooseSunBeforeAfter.setText(item);
            }});

        myDialog.setNegativeButton("Cancel", null);
        myDialog.show();
    }

    //============================================
    // Called By : Defined Listener
    //============================================
    public void openSunriseSunsetDialog() {

        startTime.setChecked(false);
        sunTime.setChecked(true);
        glb_timeOption=2;

        final String[] sunRiseSet = {
                "sunrise", "sunset"};

        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        myDialog.setTitle("Minutes");

        myDialog.setItems(sunRiseSet, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String item = sunRiseSet[which];
                chooseSunriseSunset.setText(item);
            }});

        myDialog.setNegativeButton("Cancel", null);
        myDialog.show();
    }



    public void  onRadioButtonClicked (View view) {

    }


    // DAYS =====================================================
    public void  onCheckboxClicked (View view) {
        radio_even.setChecked(false);
        radio_odd.setChecked(false);
        glb_dateOption=0;
    }

    public void  onDayRadioButtonClicked (View view) {
        int radioButtonID = dayEvenOdd.getCheckedRadioButtonId();
        View radioButton = dayEvenOdd.findViewById(radioButtonID);
        int idx = dayEvenOdd.indexOfChild(radioButton);
        if(idx == 0) {
            glb_dateOption=2;
        }
        else {
            glb_dateOption=1;
        }

        sunday.setChecked(false);
        monday.setChecked(false);
        tuesday.setChecked(false);
        wednesday.setChecked(false);
        thursday.setChecked(false);
        friday.setChecked(false);
        saturday.setChecked(false);
    }

    public void onstartAtButtonClicked (View view) {

        glb_timeOption=1;
        chooseSunHours.setText("");
        chooseSunMinutes.setText("");
        chooseSunBeforeAfter.setText("");
        chooseSunriseSunset.setText("");

        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                //------------------------------------------------------------
                // Android is bitching about doing the concat in the settext
                //------------------------------------------------------------
                String timeString="Start at : " + String.format("%02d",selectedHour) +":"+ String.format("%02d", selectedMinute);
                startTime.setText( timeString );
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Start Time");
        mTimePicker.show();
    }

    //=====================================================================
    // This is invoked by the Button on the bottom right on the xml layout
    //---------------------------------------------------------------------
    // NOTE: I set the glb_timeOption = 1| regular time   2| suntime
    // When passing to the web server:
    //  timeOption=
    //  -----------
    //  1 set time
    //  2 suntime
    //
    //
    //  dateOption=
    //  -------------
    //  3 - Specific days (day0=1,day1=1,day2=1)
    //  2 - Even days
    //      - evenDate=0   <-- need to add this extra crap
    //  1 - Odd days
    //      - oddDate=0    <-- need to add this extra crap
    //
    //
    //  regular start time (timeoption=1)
    //  -----------------------------------
    //  hour=xx
    //  minute=xx
    //  clock=0,1  (AM PM respectfully)
    //
    //suntime
    //--------
    //  sunhour=xx
    //  sunminutes=xx
    //  sunforaft =0,1 (before after)
    //  sunriseset=0,1 (sunrise, sunset)
    //==================================================================
    public void submitSchedules(View view) {

        Integer days[] = new Integer[7];           // Used this for sending to the DB method
        days[0] = 0;
        days[1] = 0;
        days[2] = 0;
        days[3] = 0;
        days[4] = 0;
        days[5] = 0;
        days[6] = 0;

        MyDatabase myDb   = new MyDatabase(this);
        MyHttpPost myHttp = new MyHttpPost();
        SharedPreferences prefs        = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        boolean dbRc=false;
        try {
            myDb.open();
            dbRc=myDb.getDexserver(prefs.getString("pref_controller", "1"), Url, port, key);
            dbRc = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        //---------------------------------------------
        // Specify dayx = when it's being specified
        //---------------------------------------------
        if(dbRc) {
            if(setSchedName.length() == 0) {
                Toast.makeText(getApplicationContext(), "Schedule name is blank", Toast.LENGTH_SHORT).show();
            }
            else if (setSeqId.length() == 0) {
                Toast.makeText(getApplicationContext(), "No sequence chosen", Toast.LENGTH_SHORT).show();
            }
            else {
                String webUrl = ("http://" + Url[0] + ":" + port[0] + "/cgi-bin/h2o/h2o_schedules.cgi") +
                        "?james=" + setSchedName.getText().toString().replaceAll(" ", "%20") +
                        "&seqId=" + setSeqId.getText();
                //--------------------------------------------------------
                // repeat from above
                // 1 = odd | 2 = Even | 3 = specific days
                //--------------------------------------------------------
                switch (glb_dateOption) {
                    case 1: webUrl = webUrl + "&dateOption=1&oddDate=0";
                       break;
                    case 2: webUrl = webUrl + "&dateOption=2&evenDate=0";
                        break;
                    case 0: webUrl=  webUrl + "&dateOption=3";
                            if( sunday.isChecked() )    { webUrl=webUrl+"&day0=1"; days[0] = 1;}
                            if( monday.isChecked() )    { webUrl=webUrl+"&day1=1"; days[1] = 1;}
                            if( tuesday.isChecked() )   { webUrl=webUrl+"&day2=1"; days[2] = 1;}
                            if( wednesday.isChecked() ) { webUrl=webUrl+"&day3=1"; days[3] = 1;}
                            if( thursday.isChecked() )  { webUrl=webUrl+"&day4=1"; days[4] = 1;}
                            if( friday.isChecked() )    { webUrl=webUrl+"&day5=1"; days[5] = 1;}
                            if( saturday.isChecked() )  { webUrl=webUrl+"&day6=1"; days[6] = 1;}
                        break;
                }

                switch (glb_timeOption) {
                    case 1:                                                                         // specific time
                        glb_sunType=0;
                        webUrl = webUrl + "&timeOption=1";
                        String tmpString=startTime.getText().toString().substring(11);
                        String[] timepiece=tmpString.split(":");
                        Integer hours=Integer.parseInt(timepiece[0]);
                        Integer minutes=Integer.parseInt(timepiece[1]);

                        if(hours > 12) {
                            webUrl = webUrl + "&timeStart=0&timeOption=1&hour=" + String.valueOf(hours-12)
                                    + "&minute=" + timepiece[1] + "&clock=1";
                        }
                        else {
                            webUrl= webUrl + "&timeStart=0&timeOption=1&hour=" + timepiece[0] +
                                    "&minute=" + timepiece[1] + "&clock=0";
                        }
                        glb_time=(hours*60*60) + (minutes*60);

                        break;
                    case 2:                                                                         // Sun time
                        Integer sHours=Integer.parseInt(chooseSunHours.getText().toString());
                        Integer sMins =Integer.parseInt(chooseSunMinutes.getText().toString());

                        webUrl = webUrl+  "&sunStart=0&timeOption=2" +
                                "&sunhour="    + String.format("%02d", sHours) +
                                "&sunminutes=" + String.format("%02d", sMins);
                        if( chooseSunBeforeAfter.getText().toString().equals("before") ) {
                            glb_sunType=0;
                            webUrl = webUrl + "&sunforaft=0";
                        }
                        else {
                            glb_sunType=2;
                            webUrl = webUrl + "&sunforaft=1";
                        }

                        if( chooseSunriseSunset.getText().toString().equals("sunrise") ) {
                            webUrl = webUrl + "&sunriseset=0";
                            glb_sunType++;
                        }
                        else {
                            glb_sunType+=2;
                            webUrl = webUrl + "&sunriseset=1";
                        }
                        glb_time=(sHours*60*60) + (sMins*60);

                        break;
                }
                webUrl = webUrl + "&event=432&app=1";


                //Log.d("fragment_5_detail", webUrl);

                String[] response = myHttp.callHome(webUrl).split("\\|");
                if (response[0].substring(0,3).equals("011")) {
                    // Log.d("FRAG5ADD ", "INSERTING INTO DB");
                    myDb.insertSchedules( Integer.parseInt(response[2]),  setSchedName.getText().toString(),
                            Integer.parseInt(setSeqId.getText().toString()), 1,  glb_time,
                            days[0], days[1], days[2], days[3], days[4], days[5], days[6],
                            (int) glb_dateOption,  (int) glb_sunType);

                    Toast.makeText(getApplicationContext(), "Schedule added to server", Toast.LENGTH_LONG).show();
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Server failed to add schedule", Toast.LENGTH_LONG).show();
                }
            }
            myDb.close();
        }
    }


    @Override
    public void onResume() {
        //This is called every time you swipe a tab. To force a refresh, you have to run the following
        //Log.d("frag1", "Refreshed");
        super.onResume();
    }
}