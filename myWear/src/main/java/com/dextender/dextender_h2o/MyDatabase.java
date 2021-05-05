package com.dextender.dextender_h2o;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.sql.SQLException;


//-----------------------------------------------------------------------------------------
// Created by livolsi on 9/16/2016.
// How to call from other methods:
//      MyDatabase dexDB = new dexDB(your_method.this);
//      boolean rc=true;
//      try {
//      dexDB.open();
//      dexDB.update(xxx);
//      dexDB.close();
//      } catch (Exception e){
//        rc=false;
//      }
//
//  The database is the bridge between the service and the UI
//
//-----------------------------------------------------------------------------------------
public class MyDatabase {

    private static final String DATABASE_NAME="dexDB";
    private static final String DATABASE_TABLE_BG_SUMMARY="bgSummary";
    private static final String DATABASE_TABLE_BG_DATA="bgData";
    private static final String DATABASE_TABLE_SLOT="slotTracker";
    private static final String DATABASE_TABLE_SETTINGS="settings";


    private static final int MAX_BG_SLOTS=36; // 3 hours
    private static final int DATABASE_VERSION=1;

    private final short META_RECORD_POSITION=17;

    // DB version 1 (wearble) - Cape May 3.1


    //-----------------------------------------------------------
    // DATA Columns
    //-----------------------------------------------------------
    private static final String   COL_BG_ID             = "id";                                     //  generic key
    private static final String   COL_BG_RECEIVER_DATE  = "receiver_time";
    private static final String   COL_BG_BGVAL          = "bg_value";
    private static final String   COL_BG_TREND          = "bg_trend";
    private static final String   COL_BG_HIGH03         = "bg_high03";
    private static final String   COL_BG_LOW03          = "bg_low03";
    private static final String   COL_BG_RECORD_UPDATED  = "record_update_time";

    private static final String   COL_BG_DATE           = "bg_time";                                 //  generic key
    private static final String   COL_BG_SLOT_ID        = "slot_id";                                 //  generic key


    //-----------------------------------------------------------
    // setting columns
    //-----------------------------------------------------------
    private static final String COL_SETTING_ID            = "id";
    private static final String COL_SETTING_BGHIGH        = "bg_high";
    private static final String COL_SETTING_BGLOW         = "bg_low";
    private static final String COL_SETTING_SYSMSG        = "sys_message";
    private static final String COL_SETTING_BACKGROUND    = "background";
    private static final String COL_SETTING_CANVAS_BKGRND = "canvas_background";
    private static final String COL_SETTING_VIBRATE       = "vibrate";
    private static final String COL_SETTING_SCREEN_ON     = "screen_on";
    private static final String COL_SETTING_CHART_LINES   = "chart_lines";  // Boolean - will chart lines and dates be displayed ?

    private DbHelper       ourHelper;
    private final Context  ourContext;
    private SQLiteDatabase ourDatabase;


    private static class DbHelper extends SQLiteOpenHelper {

        private DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {


            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_BG_SUMMARY +
                    " (id                integer  primary key, " +
                    " receiver_time      string   not null, "   +
                    " bg_value           integer  not null, "   +
                    " bg_trend           integer  not null, "   +
                    " bg_high03          integer  not null, "   +
                    " bg_low03           integer  not null, "   +
                    " record_update_time integer  not null); ");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_BG_DATA +
                    " (slot_id           integer  primary key, " +
                    " bg_time            integer  not null, "   +
                    " bg_value           integer  not null  "   +
                    " );" );


            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_SETTINGS +
                    " (id               integer  primary key, " +
                    " bg_high           integer  not null, "    +
                    " bg_low            integer  not null, "    +
                    " sys_message       string   string, "      +
                    " background        integer  not null, "    +
                    " canvas_background string   not null, "    +
                    " vibrate           integer  not null, "    +
                    " screen_on         integer  not null, "    +
                    " chart_lines       integer  not null);" );


            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_SLOT +
                    " (table_name varchar not null, " +
                    " slot_id integer not null, " +
                    " seq_id integer not null," +
                    " created_date integer not null); ");


            db.execSQL("insert into " + DATABASE_TABLE_BG_SUMMARY +
                       " (id, receiver_time, bg_value, bg_trend, bg_high03, bg_low03, record_update_time) " +
                       " values (1,  1473961886, 100, 90, 180, 60, 0);" );

            for(int i=0; i < MAX_BG_SLOTS; i++) {
                db.execSQL("insert into " + DATABASE_TABLE_BG_DATA + " (slot_id, bg_time, bg_value) " +
                           " values (" + i + ", 0, 0);" );
            }

            db.execSQL("insert into " + DATABASE_TABLE_SLOT +
                       " (table_name, slot_id, seq_id, created_date) values ('" + DATABASE_TABLE_BG_DATA + "',0, 0, 0)");


            db.execSQL("insert into " + DATABASE_TABLE_SETTINGS +
                       " (id, bg_high, bg_low, sys_message, background, canvas_background, vibrate, screen_on, chart_lines) " +
                       " values (1,  180, 60, NULL, 1, '0', 0, 0, 0);" );
        }

        //====================================================================================
        // REMEMBER - Each version is an aggregation of all the previous versions
        // so if in version A - you added column A
        // and you plan to add a column in VERSION B, then VERSION B is version A + VERSION B
        // it may be possible to do a fall through in the switch statement
        // Also, remember to reflect all your changes in the initial database creation
        //====================================================================================
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("Database", "New Version is : " + newVersion);
            switch (newVersion) {

                default:
                    Log.i("Database", "Dropping existing tables");
                    break;
            }
        }
    }

    //----------------------------------------------------------------------
    // Constructor for this class
    //----------------------------------------------------------------------
     MyDatabase(Context c) {
        //c = ge
        ourContext = c;                                                        // Private
    }

    public MyDatabase open() throws SQLException {
        ourHelper = new DbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

     boolean close() {
        ourHelper.close();
        return true;
    }


     void getBgSummary(long inCurrentTime, String[] inbgTime, String[] inbgVal, String[] inbgTrend, String[] inbgHigh03,
                       String[] inbgLow03, long inRecordUpdated[]) {
         String[] columns = new String[]{COL_BG_RECEIVER_DATE, COL_BG_BGVAL, COL_BG_TREND, COL_BG_HIGH03, COL_BG_LOW03, COL_BG_RECORD_UPDATED};
         String predicate = COL_BG_ID + " = 1 AND " + COL_BG_RECORD_UPDATED + " > " + String.valueOf(inCurrentTime - 900);


         Cursor c1 = ourDatabase.query(DATABASE_TABLE_BG_SUMMARY, columns, predicate, null, null, null, null, null);

         int irow01 = c1.getColumnIndex(COL_BG_RECEIVER_DATE);
         int irow02 = c1.getColumnIndex(COL_BG_BGVAL);
         int irow03 = c1.getColumnIndex(COL_BG_TREND);
         int irow04 = c1.getColumnIndex(COL_BG_HIGH03);
         int irow05 = c1.getColumnIndex(COL_BG_LOW03);
         int irow06 = c1.getColumnIndex(COL_BG_RECORD_UPDATED);

         inbgTime[0] = "0";
         inbgVal[0] = "0";
         inbgTrend[0] = "0";
         inbgHigh03[0] = "0";
         inbgLow03[0] = "0";
         inRecordUpdated[0] = 0;


         for (c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()) {
             inbgTime[0] = c1.getString(irow01);
             inbgVal[0] = c1.getString(irow02);
             inbgTrend[0] = c1.getString(irow03);
             inbgHigh03[0] = c1.getString(irow04);
             inbgLow03[0] = c1.getString(irow05);
             inRecordUpdated[0] = c1.getLong(irow06);
         }

         c1.close();
     }



     int getBgDataAsArray(long[] inX, int[] inY){
        String[]  columns = new String[] {COL_BG_DATE, COL_BG_BGVAL};
        String orderBy = COL_BG_DATE + " ASC ";


        Cursor c1 = ourDatabase.query(DATABASE_TABLE_BG_DATA, columns, null, null, null, null, orderBy, null);

        int irow1     = c1.getColumnIndex(COL_BG_DATE);
        int irow2     = c1.getColumnIndex(COL_BG_BGVAL);


        int i=0;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            inX[i] = Integer.parseInt(c1.getString(irow1));
            //Log.d("Database(wear)", "Fetched for date (aka inX) value (should be epoc number) -->" + inX[i]);
            inY[i] = Integer.parseInt(c1.getString(irow2));
            //Log.d("Database(wear)", "Fetched for bg value (aka inY) value (should be bg number) -->" + inY[i]);

            i++;
        }

        c1.close();
        return i;
    }


     int getBackground(){
        String[]  columns = new String[] {COL_SETTING_BACKGROUND};
        String predicate = COL_SETTING_ID + "=1";


        Cursor c1 = ourDatabase.query(DATABASE_TABLE_SETTINGS, columns, predicate, null, null, null, null, null);

        int irow01     = c1.getColumnIndex(COL_SETTING_BACKGROUND);

        int color=0;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            color  = Integer.parseInt(c1.getString(irow01));
        }

        c1.close();
        return color;
    }

     String getCanvasBackground(){
        String[]  columns = new String[] {COL_SETTING_CANVAS_BKGRND};
        String predicate = COL_SETTING_ID + "=1";


        Cursor c1 = ourDatabase.query(DATABASE_TABLE_SETTINGS, columns, predicate, null, null, null, null, null);

        int irow01     = c1.getColumnIndex(COL_SETTING_CANVAS_BKGRND);

        String color="0";
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            color  =  c1.getString(irow01);
        }

        c1.close();
        return color;
    }

     int getVibrate(){
        String[]  columns = new String[] {COL_SETTING_VIBRATE};
        String predicate = COL_SETTING_ID + "=1";


        Cursor c1 = ourDatabase.query(DATABASE_TABLE_SETTINGS, columns, predicate, null, null, null, null, null);

        int irow01     = c1.getColumnIndex(COL_SETTING_VIBRATE);

        int vibrate=0;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            vibrate  = Integer.parseInt(c1.getString(irow01));
        }

        c1.close();
        return vibrate;
    }

     boolean getChartLines() {
        String[]  columns = new String[] {COL_SETTING_CHART_LINES};
        String predicate = COL_SETTING_ID + "=1";


        Cursor c1 = ourDatabase.query(DATABASE_TABLE_SETTINGS, columns, predicate, null, null, null, null, null);

        int irow01     = c1.getColumnIndex(COL_SETTING_CHART_LINES);

        int vibrate=0;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            vibrate  = Integer.parseInt(c1.getString(irow01));
        }

        c1.close();
        return (vibrate==1);

    }


     int getLowLimit(){
        String[]  columns = new String[] {COL_SETTING_BGLOW};
        String predicate = COL_SETTING_ID + "=1";

        Cursor c1 = ourDatabase.query(DATABASE_TABLE_SETTINGS, columns, predicate, null, null, null, null, null);
        int irow01     = c1.getColumnIndex(COL_SETTING_BGLOW);

        int setting=0;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            setting  = Integer.parseInt(c1.getString(irow01));
        }
        c1.close();
        return setting;
    }

     int getHighLimit(){
        String[]  columns = new String[] {COL_SETTING_BGHIGH};
        String predicate = COL_SETTING_ID + "=1";

        Cursor c1 = ourDatabase.query(DATABASE_TABLE_SETTINGS, columns, predicate, null, null, null, null, null);
        int irow01     = c1.getColumnIndex(COL_SETTING_BGHIGH);

        int setting=0;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            setting  = Integer.parseInt(c1.getString(irow01));
        }
        c1.close();
        return setting;
    }


    //-------------------------------------------------------------------------
    // Since we are only going to have 1 record, just update the 1
    // I'm on the fence regarding doing this the right way, in that
    // if we only received BG values from the mobile, then I could receive
    // a much smaller set of data.. but how much are we really talking about
    // going across the bluetooth network
    //-------------------------------------------------------------------------
     boolean receiveInComingRecord(String inRecord) {

        //Log.d("MyDatabase (wear)" , "Record->" + inRecord);
        updateBgData(inRecord);
        updateBgSummary(inRecord);
        updateSettings(inRecord);
        return true;
    }


    private boolean updateBgData(String inRecord) {

        //Log.d("database(wear)", inRecord);

        String[] recordPiece = inRecord.split("\\|");                                      // Split the record into it's respective parts
        Integer inNumberofRecs = Integer.parseInt(recordPiece[META_RECORD_POSITION]);

        //high|lo|sysmessage |                |bgtime    |bg |tnd|hig|lo|  |  |  |  |  |# |  |
        //   0| 1|          2|3|   4|   5|   6|         7|  8|  9| 10|11|12|13|14|15|16|17|  |
        //"170|65|sys message|0|    |    |    |1473961886|101|180|203|52| 0|  |  |  |  | 2|t1|120
        // 0 high target
        // 1 low target
        // 2 system message
        // 3 is background
        // 4 Chart background
        // 5 vibrate
        // 6 screen behavior
        //12 number of BG records


            int j=META_RECORD_POSITION+1;

            for(int i=0; i< inNumberofRecs; i++) {

                ContentValues cv = new ContentValues();
                cv.put(COL_BG_DATE,  recordPiece[j]);
                cv.put(COL_BG_BGVAL, recordPiece[j+1]);
                j+=2;

                String predicate = COL_BG_SLOT_ID + "= " +  i + ";";
                ourDatabase.update(DATABASE_TABLE_BG_DATA, cv, predicate, null);
                //Log.d("Database (wear)", "Slot set to -->" + glb_slotId);
            }

            return true;

    }

    private boolean updateBgSummary(String inRecord) {

        //   0| 1|          2|3|   4|   5|   6|         7|  8|  9| 10|11|12|13|14|15|16|17|18| 19|20|21|22
        //"170|65|sys message|0|tbd1|tbd2|tbd3|1473961886|101|180|203|52|  |  |  |  |  | 2|30|120|90|25|110|90"
        String[] rec = inRecord.split("\\|");

        ContentValues cv = new ContentValues();
        cv.put(COL_BG_RECEIVER_DATE, rec[12]);
        cv.put(COL_BG_BGVAL,         rec[13]);
        cv.put(COL_BG_TREND,         rec[14]);
        cv.put(COL_BG_HIGH03,        rec[15]);
        cv.put(COL_BG_LOW03,         rec[16]);

        cv.put(COL_BG_RECORD_UPDATED, (System.currentTimeMillis() / 1000));

        String predicate = COL_BG_ID + "= 1";

        ourDatabase.update(DATABASE_TABLE_BG_SUMMARY, cv, predicate, null);

        return true;

    }


    private boolean updateSettings(String inRecord) {

        //Log.d("Wear (DB/Settings)", inRecord);
        //   0| 1|          2|3|   4|   5|   6|7     | | | | |       12| 13| 14| 15|16|17|   18| 19|   20|21
        //"170|65|sys message|0|bkgd|vibr|scrn|clines| | | | |473961886|101|180|203|52| 2|TIME1|120|TIME2|90"
        String[] rec = inRecord.split("\\|");

        ContentValues cv = new ContentValues();
        cv.put(COL_SETTING_BGHIGH,        rec[0]);
        cv.put(COL_SETTING_BGLOW,         rec[1]);
        cv.put(COL_SETTING_SYSMSG,        rec[2]);
        cv.put(COL_SETTING_BACKGROUND,    rec[3]);
        cv.put(COL_SETTING_CANVAS_BKGRND, rec[4]);
        cv.put(COL_SETTING_VIBRATE,       rec[5]);
        cv.put(COL_SETTING_SCREEN_ON,     rec[6]);
        cv.put(COL_SETTING_CHART_LINES,   rec[7]);

        String predicate = COL_SETTING_ID + "= 1";
        ourDatabase.update(DATABASE_TABLE_SETTINGS, cv, predicate, null);

        return true;

    }

}
