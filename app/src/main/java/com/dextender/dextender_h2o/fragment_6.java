package com.dextender.dextender_h2o;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;


//===============================================================================================
// Created by livolsi on 9/24/2014.
//===============================================================================================
public class fragment_6 extends Fragment {

    Integer MAX_LOG_RECORDS=50;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_6, container, false);       // Inflate this fragment

        //-------------------------------------------------------------------
        // Only going to display 40 items. That "40" really should be a var
        // Keep this initialization.. this was making me nuts !!! If one of the values
        // in the array was null, then I was getting a nullpointerexception.. on the
        // superGetView.. so
        //-------------------------------------------------------------------
        int i;
        final String[] logRec = new String[MAX_LOG_RECORDS];                                        // array to hold messages
        for (i=0; i < MAX_LOG_RECORDS; i++) {
            logRec[i] = " ";
        }
        //-------------------------------------------------------------------
        // DATABASE !!! - Fetch records from the database
        //-------------------------------------------------------------------
        MyDatabase myDb = new MyDatabase(getActivity());                                           // open database class
        int dbRecords=0;
        try {
            myDb.open();
            dbRecords=myDb.getLogData(logRec);                                                  // fill the array with messages that we have in table "log"
        } catch (Exception e) {
            e.printStackTrace();
        }
        myDb.close();

        int allocate=1;
        if(dbRecords > 0) {
            // NEW
            allocate=dbRecords;
        }

        final MyRowStructureForLogs[] rowStruct = new MyRowStructureForLogs[allocate];
        //----------------------------------------------------------------------
        // If there's no record in the database, instead of a null record
        // put something in slot 0 (1st record)
        //----------------------------------------------------------------------
        // Commented out because this sections core dumps if it's  a new system.
        if ( dbRecords == 0 ) {
            rowStruct[0] = new MyRowStructureForLogs();
            rowStruct[0].thisRow(R.mipmap.info, "---", "Begin Log");     // NOTE 3 Arguments - Expected in 'MyRowStructure.java'
        }
        else {
            for(i=0; i < dbRecords; i++) {
                if (logRec[i] != null) {
                    rowStruct[i] = new MyRowStructureForLogs();                                     // prelimary step. You need to do this before using the struct
                    String rec[] = logRec[i].split("\\|");
                    // Log.d("FRAG6", "Rec -->" + rec[1]);
                    switch (Integer.parseInt(rec[1])) {
                        case 1:
                            rowStruct[i].thisRow(R.mipmap.ierr, rec[3], rec[2]);
                            break;
                        case 2:
                            rowStruct[i].thisRow(R.mipmap.warn, rec[3], rec[2]);
                            break;
                        case 3:
                            rowStruct[i].thisRow(R.mipmap.info, rec[3], rec[2]);
                            break;
                        default:
                            rowStruct[i].thisRow(R.mipmap.info, rec[3], rec[2]);
                            break;
                    }
                }
            }

        }

        //--------------------------------------------------------------------
        // Find the listview
        // An Adapter is a bridge between the view (listview) and the data
        // we bind the adapter to the view via the setadapter method.
        // NOTE: listview6 is defined in fragment_6.xml
        //--------------------------------------------------------------------
        ListAdapter listAdapter = new MyCustomAdapterForLogs(getActivity(), rowStruct );
        ListView lv = (ListView) v.findViewById(R.id.listview6);                            // Name of the view in fragment_5.xml
        lv.setAdapter(listAdapter);
        lv.setFastScrollEnabled(true);

        //-----------------------------------------------------------------------
        // here, I'm setting my adapter as an array adapter.
        // TEST ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, serviceMsg);
        // TEST lv.setAdapter(arrayAdapter);
        //
        //-----------------------------------------------------------------------
    //    lv.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, logRec) {

            //-----------------------------------------------------------------------------------
            // Need to override the 'getview' method to set the color
            // here the adapter is calling the getview method (standard way of doing things)
            // The layout format and the corresponding data within the adapter view are set in
            // the getview method
            //-----------------------------------------------------------------------------------
      //      @Override
      //      public View getView(int position, View convertView, ViewGroup parent) {
      //          View view = super.getView(position, convertView, parent);
      //          TextView text = (TextView) view.findViewById(android.R.id.text1);
      //          text.setTextSize(11);
      //          text.setTextColor(Color.WHITE);
      //          return view;
      //      }
      //  });

        return v;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_CANCELED) {
            Toast toast = Toast.makeText(getActivity(), "Bluetooth must be enabled", Toast.LENGTH_SHORT);
            toast.show();
        }
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(r,
                new IntentFilter("TAG_REFRESH"));
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    }
}
