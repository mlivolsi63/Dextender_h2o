package com.dextender.dextender_h2o;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MyWidget extends AppWidgetProvider {

    private static final String ACTION_CLICK = "ACTION_CLICK";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        //----------------------------------------------
        // Call the classes
        //----------------------------------------------
        MyTools    myTools  = new MyTools();
        MyDatabase myDb    = new MyDatabase(context);                                              // Call the database class
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        int displayUnits = Integer.parseInt(prefs.getString("listBgDisplayUnits","0"));             // mmol or mg/dl from preferences

        String dbBgVal="";                                                                          // display of bg
        int dbBgTrend=0;                                                                            // display trend
        String bgRecords="";                                                                        // the line from the database

        //---------------------------------------------------------
        // Get the real data from the database
        //---------------------------------------------------------
        boolean dbRc=false;
        try {
            myDb.open();                                                                           // open the database
            Long sequenceId = (long)0;                                        // get the offset from table 'offset'
            bgRecords = "---";
            dbRc=true;
        }
        catch (Exception e) {
            e.printStackTrace();
            myDb.close();
        }

        if(dbRc) {
            //---------------------------------------------------------------------------------------
            // Start processing the record. First, we'll split it up since it's delimited by pipes
            //---------------------------------------------------------------------------------------
            if (bgRecords != null) {

                String recordPiece[] = bgRecords.split("\\|");                                      // Split the record into it's respective parts
                if (displayUnits == 1)
                    dbBgVal = recordPiece[1] + " " + "mmol/l";                           // if preference array = 1, that's mmol
                else
                    dbBgVal = recordPiece[1] + " " + "mg/dl";                           // otherwise, it's going to be 0, so it's mg/dl (default value)

                //-------------------------------------------------------
                // Format the display date from epoch to readable time
                //-------------------------------------------------------
                dbBgTrend = Integer.parseInt(recordPiece[2]);
            }

            //-----------------------------------------------------------------------------------
            // This I'm not so sure fragment_about. We need to talk to ALL widgets that might be running
            // Get all ids
            //-----------------------------------------------------------------------------------
            ComponentName thisWidget = new ComponentName(context, MyWidget.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            for (int widgetId : allWidgetIds) {

                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

                // Set the text

                if (bgRecords != null) {
                    //remoteViews.setTextViewText(R.id.widget_sysTime, stringSysDate);
                    remoteViews.setTextViewText(R.id.widget_bg, dbBgVal);


                    // set the trend (do it as an image)

                } else {
                    long epochAsLong = 0;
                    if(epochAsLong != 0) {
                        remoteViews.setTextViewText(R.id.widget_usbTime, myTools.now());
                    }
                    else {
                        remoteViews.setTextViewText(R.id.widget_usbTime, "--:--:--");
                    }
                    remoteViews.setTextViewText(R.id.widget_bg, "");

                }


                // Register an onClickListener
                Intent intent = new Intent(context, MyWidget.class);

                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget_bg, pendingIntent);
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Toast.makeText(context, "Widget removed", Toast.LENGTH_SHORT).show();

    }
}