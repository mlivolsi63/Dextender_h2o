package com.dextender.dextender_h2o;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import java.sql.SQLException;
import java.util.ArrayList;

//-----------------------------------------------------------------------------------------
// Created by livolsi on 10/2/2014.
// How to call from other methods:
//      MyDatabase dexDB = new dexDB(your_method.this);
//      boolean rc=true;
//      try {
//      dexDB.open();
//      dexDB.insert(xxx);
//      dexDB.close();
//      } catch (Exception e){
//        rc=false;
//      }
//
//-----------------------------------------------------------------------------------------
public class MyDatabase {


    public static Integer MAX_LOG_DISPLAY_ENTRIES=20;

    public static final String DATABASE_NAME="h2o";
    public static final String DATABASE_TABLE_SOLAR="solar";
    public static final String DATABASE_TABLE_STATUS="status";
    public static final String DATABASE_TABLE_DEXSERVER="dexserver";
    public static final String DATABASE_TABLE_LOG="log";
    public static final String DATABASE_TABLE_ALARM="alarm";
    public static final String DATABASE_TABLE_ZONES="zones";
    public static final String DATABASE_TABLE_SCHEDULES="schedules";
    public static final String DATABASE_TABLE_JOB="job";
    public static final String DATABASE_TABLE_JOB_GROUP="job_group";
    public static final String DATABASE_TABLE_SEQUENCES="sequences";
    public static final String DATABASE_TABLE_SEQUENCE_ZONES="sequence_zones";
    public static final String DATABASE_TABLE_SERVICES="services";
    public static final String DATABASE_TABLE_SYS_TIMESTAMP="sys_timestamp";
    public static final int    DATABASE_VERSION=5;

    MyTools tools = new MyTools();                                                  // Call the httpd class

    //-----------------------------------------------------------
    // Solar Columns
    //-----------------------------------------------------------
    public static final String COL_SOLAR_SEQ           = "seq_id";
    public static final String COL_SOLAR_SUNRISE       = "sunrise";
    public static final String COL_SOLAR_SUNSET        = "sunset";
    public static final String COL_SOLAR_CREATED_DATE  = "created_date";

    //-----------------------------------------------------------
    // status columns
    //-----------------------------------------------------------
    public static final String COL_STATUS_ID            = "status_id";
    public static final String COL_STATUS_NAME          = "status_name";
    public static final String COL_STATUS_STATUS        = "status";
    public static final String COL_STATUS_PAUSE_TIME    = "pause_time";
    public static final String COL_STATUS_PAUSE_DATE    = "pause_date";
    public static final String COL_STATUS_CURRENT_ZONE  = "current_zone";
    public static final String COL_STATUS_LAST_ZONE     = "last_zone";
    public static final String COL_STATUS_LAST_RUN_TIME = "last_run_time";
    public static final String COL_STATUS_CONTROLLER    = "controller";


    //-----------------------------------------------------------
    // Status of the different systems and controllers
    // server_id of 0 is the web site (aka operator)
    //-----------------------------------------------------------

    public static final String COL_DEXSERVER_ID        = "server_id";
    public static final String COL_DEXSERVER_URL       = "url";
    public static final String COL_DEXSERVER_PORT      = "http_port";
    public static final String COL_DEXSERVER_KEY       = "access_key";
    public static final String COL_DEXSERVER_STATUS    = "status";


    //-----------------------------------------------------------
    // Log columns
    //-----------------------------------------------------------
    public static final String COL_LOG_ID              = "log_id";
    public static final String COL_LOG_LEVEL           = "log_level";
    public static final String COL_LOG_MESSAGE         = "message";
    public static final String COL_LOG_CREATED         = "created_date";

    //-----------------------------------------------------------
    // Zones
    //-----------------------------------------------------------
    public static final String COL_ZONE_ID            = "zone_id";
    public static final String COL_ZONE_DESCRIPTION   = "description";
    public static final String COL_ZONE_STATUS        = "status";
    public static final String COL_ZONE_LAST_TIME     = "last_time";

    //-----------------------------------------------------------
    // Services
    //-----------------------------------------------------------
    public static final String COL_SERVICE_ID         = "service_id";
    public static final String COL_SERVICE_NAME       = "service_name";
    public static final String COL_SERVICE_STATUS     = "status";
    //public static final String COL_SERVICE_MODIFIED   = "modified_time";

    //-----------------------------------------------------------
    // Alarm columns
    //-----------------------------------------------------------
    public static final String COL_ALARM_ID           = "alarm_id";
    public static final String COL_ALARM_COUNT        = "alarm_count";
    public static final String COL_ALARM_TIME         = "last_update";

    //-----------------------------------------------------------
    // Sequences
    //-----------------------------------------------------------
    public static final String COL_SEQ_ID             = "sequence_id";
    public static final String COL_SEQ_STATUS         = "status";
    public static final String COL_SEQ_NAME           = "sequence_name";
    public static final String COL_SEQ_LAST_TIME      = "last_time";

    //-----------------------------------------------------------
    // Sequences Zones columns
    //-----------------------------------------------------------
    public static final String COL_SEQZ_ID             = "sequence_id";
    public static final String COL_SEQZ_ZONE_ID        = "zone_id";
    public static final String COL_SEQZ_ZONE_POSITION  = "zone_position";
    public static final String COL_SEQZ_STATUS         = "status";
    public static final String COL_SEQZ_RUNTIME        = "runtime";
    public static final String COL_SEQZ_SUBTIME        = "submit_time";


    //-----------------------------------------------------------
    // Job columns
    //-----------------------------------------------------------
    public static final String COL_JOB_ID             = "job_id";
    public static final String COL_JOB_GROUP          = "job_group";
    //public static final String COL_JOB_SEQ_ID         = "sequence_id";
    public static final String COL_JOB_TYPE           = "job_type";
    public static final String COL_JOB_ZONE_ID        = "zone_id";
    public static final String COL_JOB_SLOT           = "slot";
    public static final String COL_JOB_STATUS         = "status";
    public static final String COL_JOB_RUNTIME        = "runtime";
    public static final String COL_JOB_SUBMIT_TIME    = "submit_time";

    //-----------------------------------------------------------
    // Job group columns
    //-----------------------------------------------------------
    //public static final String COL_JOB_GROUP_ID       = "job_id";
    //public static final String COL_JOB_GROUP_CREATED  = "job_group";
    //public static final String COL_JOB_GROUP_STATUS   = "sequence_id";

    //-----------------------------------------------------------
    // schedules columns
    //-----------------------------------------------------------
    public static final String COL_SCHED_ID           = "schedule_id";
    public static final String COL_SCHED_NAME         = "schedule_name";
    //public static final String COL_SCHED_SEQ_ID       = "sequence_id";
    public static final String COL_SCHED_STATUS       = "status";
    public static final String COL_SCHED_START_TIME   = "start_time";
    public static final String COL_SCHED_SUNDAY       = "sunday";
    public static final String COL_SCHED_MONDAY       = "monday";
    public static final String COL_SCHED_TUESDAY      = "tuesday";
    public static final String COL_SCHED_WEDNESDAY    = "wednesday";
    public static final String COL_SCHED_THURSDAY     = "thursday";
    public static final String COL_SCHED_FRIDAY       = "friday";
    public static final String COL_SCHED_SATURDAY     = "saturday";
    public static final String COL_SCHED_EVENODD      = "evenodd";
    public static final String COL_SCHED_SUNTYPE      = "suntype";

    //-----------------------------------------------------------
    // System Time Stamp
    //-----------------------------------------------------------
    public static final String COL_SYS_ID             = "system_id";
    public static final String COL_SYS_TIMESTAMP      = "last_timestamp";

    private DbHelper       ourHelper;
    private final Context  ourContext;
    private SQLiteDatabase ourDatabase;


    private static class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {

            //--------------------------------------------------------------------------------------
            // Table solar
            // Purpose
            //
            //--------------------------------------------------------------------------------------
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_SOLAR +
                    " ( " + COL_SOLAR_SEQ + " integer not null primary key, " +
                    " " + COL_SOLAR_SUNRISE + " integer not null, " +
                    " " + COL_SOLAR_SUNSET + " integer not null, " +
                    " " + COL_SOLAR_CREATED_DATE + " integer not null); ");

            //--------------------------------------------------------------------------------------
            // status
            //--------------------------------------------------------------------------------------
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_STATUS +
                    " (status_id      integer not null, " +
                    "  status_name    varchar not null, " +
                    "  status         integer not null," +
                    "  pause_time     integer not null, " +
                    "  pause_date     integer not null, " +
                    "  current_zone   integer not null, " +
                    "  last_zone      integer not null," +
                    "  last_run_time  integer not null," +
                    "  controller     varchar not null," +
                    "  primary key (status_id));");

            //--------------------------------------------------------------------------------------
            // Dex Server | Does not include operator but controller with ID of 1
            //--------------------------------------------------------------------------------------
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_DEXSERVER +
                    " (server_id      integer not null," +
                    "  url            varchar not null," +
                    "  http_port      integer not null," +
                    "  access_key     integer, " +
                    "  status         integer not null," +
                    "  primary key (server_id) );");


            //--------------------------------------------------------------------------------------
            // log
            // log_id       - Sequence of this entry
            // comment      - Message/comment to display
            // create_date  - when this record was created (stored as epoch)
            //
            // NOTE: log is a circular queue of 100 entries
            //--------------------------------------------------------------------------------------
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_LOG +
                    " (log_id        integer   not null," +
                    "  log_level     integer   not null," +
                    "  message       varchar   not null," +
                    "  created_date  integer   not null," +
                    "  primary key (log_id));");

            //--------------------------------------------------------------------------------------
            // Alarm
            // table_name varchar
            //--------------------------------------------------------------------------------------

            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_ALARM +
                    " (alarm_id   integer not null, " +
                    " alarm_count integer not null, " +
                    " last_update integer not null," +
                    " primary key (alarm_id) );");

            //--------------------------------------------------------------------------------------
            // Options
            //--------------------------------------------------------------------------------------
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_ZONES +
                    " (zone_id     integer not null, " +
                    "  description varchar not null, " +
                    "  status      integer not null, " +
                    "  last_time   integer not null, " +
                    "  primary key (zone_id) ); ");

            //--------------------------------------------------------------------------------------
            // Services
            // Keep track of the status of the services, such as cloud post, usb and general
            // services
            //  -1 = unknown
            //   0 = off
            //   1 = on
            //   2 = error
            //   x = unknown
            //--------------------------------------------------------------------------------------

            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_SERVICES +
                    " (service_id    integer not null," +
                    "  service_name  varchar not null," +
                    "  status        integer not null," +
                    "  modified_time integer not null," +
                    "  primary key (service_id) );");

            //--------------------------------------------------------------------------------------
            // messages
            //--------------------------------------------------------------------------------------
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_SCHEDULES +
                    " (schedule_id    integer not null," +
                    "  schedule_name  varchar not null," +
                    "  sequence_id    integer not null," +
                    "  status         integer not null," +
                    "  start_time     integer not null," +
                    "  sunday         integer not null," +
                    "  monday         integer not null," +
                    "  tuesday        integer not null," +
                    "  wednesday      integer not null," +
                    "  thursday       integer not null," +
                    "  friday         integer not null," +
                    "  saturday       integer not null," +
                    "  evenodd        integer not null," +
                    "  suntype        integer not null," +
                    "  primary key (schedule_id) );");

            //--------------------------------------------------------------------------------------
            // Job
            //--------------------------------------------------------------------------------------
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_JOB +
                    " (job_id        integer not null, " +
                    "  job_group     integer not null," +
                    "  sequence_id   integer not null," +
                    "  job_type      integer not null," +
                    "  zone_id       integer not null," +
                    "  slot          integer not null," +
                    "  status        integer not null," +
                    "  runtime       integer not null," +
                    "  submit_time   integer not null," +
                    "  primary key (job_id) );");

            //--------------------------------------------------------------------------------------
            // Job group
            //--------------------------------------------------------------------------------------
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_JOB_GROUP +
                    " (job_group_id  integer not null, " +
                    "  created       integer not null," +
                    "  status        integer not null," +
                    "  primary key (job_group_id) );");


            //--------------------------------------------------------------------------------------
            // sequences
            //--------------------------------------------------------------------------------------
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_SEQUENCES +
                    " (sequence_id   integer not null," +
                    "  status        integer not null," +
                    "  sequence_name varchar not null," +
                    "  last_time     integer not null," +
                    "  primary key (sequence_id) );");

            //--------------------------------------------------------------------------------------
            // sequence_zones
            //--------------------------------------------------------------------------------------
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_SEQUENCE_ZONES +
                    " (sequence_id   integer not null," +
                    "  zone_id       integer not null," +
                    "  zone_position integer not null," +
                    "  status        integer not null," +
                    "  runtime       integer not null," +
                    "  submit_time   integer not null," +
                    "  primary key (sequence_id, zone_position) );");

            //--------------------------------------------------------------------------------------
            // System timestamp
            //--------------------------------------------------------------------------------------
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_SYS_TIMESTAMP +
                    " (" + COL_SYS_ID + " integer not null, " +
                    COL_SYS_TIMESTAMP + " integer not null, " +
                    "  primary key (" + COL_SYS_ID + ") );");


            //===========================
            // SEED DATA
            //===========================
            // This android service
            db.execSQL("insert into " + DATABASE_TABLE_SERVICES + "(service_id, service_name, status, modified_time) values (0, 'Android service', 1, " + (System.currentTimeMillis() / 1000) + ");");
            db.execSQL("insert into " + DATABASE_TABLE_SERVICES + "(service_id, service_name, status, modified_time) values (1, 'Communications',  -1, " + (System.currentTimeMillis() / 1000) + ");");
            db.execSQL("insert into " + DATABASE_TABLE_SERVICES + "(service_id, service_name, status, modified_time) values (2, 'Controller switch state', -1, " + (System.currentTimeMillis() / 1000) + ");");
            db.execSQL("insert into " + DATABASE_TABLE_SERVICES + "(service_id, service_name, status, modified_time) values (3, 'Controller server status', -1, " + (System.currentTimeMillis() / 1000) + ");");
            db.execSQL("insert into " + DATABASE_TABLE_SERVICES + "(service_id, service_name, status, modified_time) values (4, 'Controller scheduler status', -1, " + (System.currentTimeMillis() / 1000) + ");");


            //----------------------------------------
            // Seed data for dexserver (controller)
            // Controller status of -1 means disabled
            //----------------------------------------
            db.execSQL("insert into " + DATABASE_TABLE_DEXSERVER + " (server_id, url, http_port, status) " + "values (0, 'http://www.dextender.com', 80, 1);");
            db.execSQL("insert into " + DATABASE_TABLE_DEXSERVER + " (server_id, url, http_port, status) " + "values (1, '-', 80,  1);");
            db.execSQL("insert into " + DATABASE_TABLE_DEXSERVER + " (server_id, url, http_port, status) " + "values (2, '-', 80, -1);");
            db.execSQL("insert into " + DATABASE_TABLE_DEXSERVER + " (server_id, url, http_port, status) " + "values (3, '-', 80, -1);");
            db.execSQL("insert into " + DATABASE_TABLE_DEXSERVER + " (server_id, url, http_port, status) " + "values (4, '-', 80, -1);");
            db.execSQL("insert into " + DATABASE_TABLE_DEXSERVER + " (server_id, url, http_port, status) " + "values (5, '-', 80, -1);");


            //-----------------------------
            // Seed data for logs
            //-----------------------------
            db.execSQL("insert into " + DATABASE_TABLE_LOG + " (log_id, log_level, message, created_date) " +
                    "values (0, 1, '--- Begin of local log ---', 0);");

            //--------------------------------------
            // alarming codes - Service error
            //--------------------------------------
            db.execSQL("insert into " + DATABASE_TABLE_ALARM + " (alarm_id, alarm_count, last_update) values (0,0,0);");

            //--------------------------------------
            db.execSQL("insert into " + DATABASE_TABLE_SYS_TIMESTAMP + " (" +
                    COL_SYS_ID + "," +
                    COL_SYS_TIMESTAMP +
                    ") values (1,0);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SOLAR);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_STATUS);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_DEXSERVER);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_LOG);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ALARM);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ZONES);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SCHEDULES);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_JOB);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_JOB_GROUP);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SEQUENCES);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SEQUENCE_ZONES);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SERVICES);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SYS_TIMESTAMP);

            onCreate(db);                                                         // call method
        }
    }
    //----------------------------------------------------------------------
    // Constructor for this class
    //----------------------------------------------------------------------
    public MyDatabase(Context c) {
        //c = ge
        ourContext = c;                                                        // Private
    }

    public MyDatabase open() throws SQLException{
        ourHelper = new DbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public boolean close() {
        ourHelper.close();
        return true;
    }

    //-----------------------------------------------------------------------------------
    // Method : Insert log
    // Pass the comment, and time (as epoch value)
    // The sequence ID will autoincrement
    // I'm wondering how SQLite binds this SQL (is it the same as Oracle or MySQL ?)
    //-----------------------------------------------------------------------------------
    public boolean logIt(Integer inalertLevel, String argComment) {
        return logIt(inalertLevel, argComment, System.currentTimeMillis()/1000);
    }


    public boolean logIt(Integer inalertLevel, String argComment, Long argTime) {

        if(argTime == 0) {
            argTime = (System.currentTimeMillis() / 1000);
        }
        ourDatabase.execSQL("insert into " + DATABASE_TABLE_LOG + " (log_level, message, created_date) values (" +
                inalertLevel + ", '" +
                argComment + "'," +
                argTime + ")");

        return true;
    }

    public boolean insertSolar(Integer argSunrise, Integer argSunset) {
        ourDatabase.execSQL("insert into " + DATABASE_TABLE_SOLAR + " (sunrise, sunset, created_date) values (" +
                argSunrise + "," +
                argSunset  + "," +
                (System.currentTimeMillis() / 1000)+ ")");

        return true;
    }

    public boolean insertStatus(Integer argStatusId, String argStatusName, Integer argStatus, Integer argPauseTime, Long argPauseDate, Integer argCurrentZone, Integer argLastZone, Long argLastRunTime, String argController) {
        ourDatabase.execSQL("insert into " + DATABASE_TABLE_STATUS +
                " (status_id, status_name, status, pause_time, pause_date, current_zone, last_zone, last_run_time, controller) values (" +
                argStatusId         + ","  +
                "'" + argStatusName + "'," +
                argStatus           + ","  +
                argPauseTime        + ","  +
                argPauseDate        + ","  +
                argCurrentZone      + ","  +
                argLastZone         + ","  +
                argLastRunTime      + ","  +
                "'" + argController + "');");

        return true;
    }


    public boolean insertZones(Integer argZoneId, String argDescription, Integer argStatus, Integer argLastTime) {
        ourDatabase.execSQL("insert into " + DATABASE_TABLE_ZONES +
                "( zone_id, description, status, last_time ) values (" +
                argZoneId            + ","  +
                "'" + argDescription + "'," +
                argStatus            + ","  +
                argLastTime          + ");");

        return true;
    }

    public boolean insertSchedules(Integer argScheduleId, String argScheduleName, Integer argSequence, Integer argStatus, Integer argStartTime,
                                   Integer argSunday, Integer argMonday, Integer argTueday, Integer argWednesday,
                                   Integer argThursday, Integer argFriday, Integer argSaturday, Integer argEvenOdd, Integer argSunType) {


        ourDatabase.execSQL("insert into  " + DATABASE_TABLE_SCHEDULES +
                " (schedule_id, schedule_name, sequence_id, status, start_time, sunday, monday, tuesday, wednesday, thursday, friday, saturday, evenodd, suntype) values (" +
                argScheduleId + "," +
                "'" + argScheduleName + "'," +
                argSequence + "," +
                argStatus + "," +
                argStartTime + "," +
                argSunday + "," +
                argMonday + "," +
                argTueday + "," +
                argWednesday + "," +
                argThursday + "," +
                argFriday + "," +
                argSaturday + "," +
                argEvenOdd + "," +
                argSunType + ");");

        return true;
    }


    //-----------------------------------------------------------------------------------------
    // Only used by local call.. not when refreshing from the master - see fragment_4_detail
    // job_id is auto-incremented
    //
    //String result="";
    //String[]  columns = new String[] {COL_SOLAR_SUNRISE, COL_SOLAR_SUNSET};

    //Cursor c1 = ourDatabase.query(DATABASE_TABLE_SOLAR, columns, null, null, null, null, null, null);

    //int irowSolarSunrise = c1.getColumnIndex(COL_SOLAR_SUNRISE);                                  // X-axis value
    //int irowSolarSunset   = c1.getColumnIndex(COL_SOLAR_SUNSET);                                 // Y-axis Value

    //boolean recordsReadFlag=false;

    //for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
    //    result = c1.getString(irowSolarSunrise) + "|" + c1.getString(irowSolarSunset);
    //    recordsReadFlag=true;
    //}
    //c1.close();
    //-----------------------------------------------------------------------------------------

    public boolean insertJobForSequences(String argSeqId) {

        String[] columns = new String[] {COL_SEQZ_ID, COL_SEQZ_ZONE_ID, COL_SEQZ_ZONE_POSITION, COL_SEQZ_STATUS, COL_SEQZ_RUNTIME, COL_SEQZ_SUBTIME};
        String predicate = COL_SEQZ_ID + " = " + argSeqId;

        Cursor c1 = ourDatabase.query(DATABASE_TABLE_SEQUENCE_ZONES, columns, predicate, null, null, null, null, null);


        int irow01 = c1.getColumnIndex(COL_SEQZ_ID);
        int irow02 = c1.getColumnIndex(COL_SEQZ_ZONE_ID);
        int irow03 = c1.getColumnIndex(COL_SEQZ_ZONE_POSITION);
        int irow04 = c1.getColumnIndex(COL_SEQZ_STATUS);
        int irow05 = c1.getColumnIndex(COL_SEQZ_RUNTIME);


        String dml_string;
        boolean dbRc=false;
        Integer timeBucket=0;

        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){

            dml_string = "insert into " + DATABASE_TABLE_JOB + " (job_group, sequence_id, job_type, zone_id, slot, status, runtime, submit_time) values (" +
                    " 0, " + c1.getString(irow01) + ", 1, " + c1.getString(irow02) + "," +  c1.getString(irow03) + ", " + c1.getString(irow04) + "," + c1.getString(irow05) + ",";

            timeBucket+=5;
            dml_string = dml_string  + String.valueOf(System.currentTimeMillis()/1000 + timeBucket) + ");";
            try {
                ourDatabase.execSQL(dml_string);
                dbRc = true;
            }
            catch (Exception e){
                e.printStackTrace();
                dbRc=false;
            }
            timeBucket+=Integer.parseInt(c1.getString(irow05));
        }
        c1.close();
        return dbRc;
    }



    public boolean insertJob(Long argJobId, Integer argJobGroup, Integer argSeqId, Integer argJobType,
                             Integer argZoneId, Integer argSlot, Integer argStatus, Integer argRunTime, Long argSubmitted) {



        argSubmitted+=5;                                                                            // There's a 5 second initial sleep time on the controller

        ourDatabase.execSQL("insert into  " + DATABASE_TABLE_JOB +
                " (job_id, job_group, sequence_id, job_type, zone_id, slot, status, runtime, submit_time) values (" +
                argJobId     + "," +
                argJobGroup  + "," +
                argSeqId     + "," +
                argJobType   + "," +
                argZoneId    + "," +
                argSlot      + "," +
                argStatus    + "," +
                argRunTime   + "," +
                argSubmitted + ");");

        return true;
    }

    public boolean insertJobGroup(Integer argJobGroupId, Integer argCreated, Integer argStatus) {
        ourDatabase.execSQL("insert into  " + DATABASE_TABLE_JOB_GROUP +
                " (job_group_id, created, status ) values (" +
                argJobGroupId + "," +
                argCreated    + "," +
                argStatus     + ");");

        return true;
    }

    public boolean insertSequences(Integer argSequenceId, Integer argStatus, String argName, Integer argLastTime) {
        ourDatabase.execSQL("insert into  " + DATABASE_TABLE_SEQUENCES +
                " (sequence_id,  status, sequence_name, last_time) values (" +
                argSequenceId   + "," +
                argStatus       + "," +
                "'" + argName   + "'," +
                argLastTime     + ");");

        return true;
    }

    public boolean insertSequenceZones(Integer argSequenceId, Integer argZoneId, Integer argZonePosition, Integer argStatus,
                                       Integer argRunTime, Long argSubmitTime) {

        String dml_string = "insert into  " + DATABASE_TABLE_SEQUENCE_ZONES +
                            " (sequence_id,  zone_id, zone_position, status, runtime, submit_time) values (" +
                argSequenceId   + "," +
                argZoneId       + "," +
                argZonePosition + "," +
                argStatus       + "," +
                argRunTime      + "," +
                argSubmitTime   + ");";

        try {
            ourDatabase.execSQL(dml_string);
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        return true;
    }
    //------------------------------------------------------------------------------------
    // This method was originally commented out as I no longer needed the max.
    // Just like in other RDBMS...
    // .... the "AS MAX" reference below
    // The "AS MAX" is an alias to the column, so when we index the column, we
    // reference it by the alias, and not the "max (column_name) or "original column_name"
    //-------------------------------------------------------------------------------------
    //public long getLastRunTime(){
    //    String[]  columns = new String[] {" max(" + COL_BG_CREATED_DATE + ") AS max" };
    //    Cursor c1 = ourDatabase.query(DATABASE_TABLE_BG, columns, null, null, null, null, null);
    //    long result = 0;
    //    int irowDate = c1.getColumnIndex("max");
    //
    //    for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
    //        result = c1.getLong(irowDate);
    //    }
    //    return(result);
    //}

    //------------------------------------------------------------------------------------------
    // Used by the GUI and in the services (only get what we need)
    // ie. if we recorded the value from the dex, and we scan the dex again, only get what we
    // need
    // NOTE !!! - For graphing, this routine is flawed.
    //            The result will return the last row that we retrieved.
    //------------------------------------------------------------------------------------------
    public String getStatusData(long argStatusId){
        String[]  columns = new String[] {COL_STATUS_NAME, COL_STATUS_STATUS, COL_STATUS_PAUSE_TIME, COL_STATUS_PAUSE_DATE, COL_STATUS_CURRENT_ZONE, COL_STATUS_LAST_ZONE, COL_STATUS_LAST_RUN_TIME, COL_STATUS_CONTROLLER};
        String predicate;
        String orderBy = COL_STATUS_ID + " ASC ";
        predicate = COL_STATUS_ID + " = " + argStatusId;

        Cursor c1 = ourDatabase.query(DATABASE_TABLE_STATUS, columns, predicate, null, null, null, orderBy, null);

        String result="";
        int irowStatusName         = c1.getColumnIndex(COL_STATUS_NAME);
        int irowStatusStatus       = c1.getColumnIndex(COL_STATUS_STATUS);
        int irowStatusPauseTime    = c1.getColumnIndex(COL_STATUS_PAUSE_TIME);
        int irow04                 = c1.getColumnIndex(COL_STATUS_PAUSE_DATE);
        int irowsStatusCurrentZone = c1.getColumnIndex(COL_STATUS_CURRENT_ZONE);
        int irowStatusLastZone     = c1.getColumnIndex(COL_STATUS_LAST_ZONE);
        int irowStatusLastRundate  = c1.getColumnIndex(COL_STATUS_LAST_RUN_TIME);
        int irowStatusController   = c1.getColumnIndex(COL_STATUS_CONTROLLER);

        boolean recordsReadFlag=false;

        //------- this will give us the max..note. we aren't adding to the result, but replacing
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            result = c1.getString(irowStatusName)         + "|" + c1.getString(irowStatusStatus)    + "|" +
                     c1.getString(irowStatusPauseTime)    + "|" + c1.getString(irow04)              + "|" +
                     c1.getString(irowsStatusCurrentZone) + "|" + c1.getString(irowStatusLastZone)  + "|" +
                     c1.getString(irowStatusLastRundate)  + "|" + c1.getString(irowStatusController);
            recordsReadFlag=true;
        }

        c1.close();
        // returns : 1|123|180|1413400234   - seq|bg|trend|dexdate
        if(!recordsReadFlag) return null;
        else                 return result;
    }

    public String getSolar(){

        String result="";
        String[]  columns = new String[] {COL_SOLAR_SUNRISE, COL_SOLAR_SUNSET};

        Cursor c1 = ourDatabase.query(DATABASE_TABLE_SOLAR, columns, null, null, null, null, null, null);

        int irowSolarSunrise = c1.getColumnIndex(COL_SOLAR_SUNRISE);                                       // X-axis value
        int irowSolarSunset      = c1.getColumnIndex(COL_SOLAR_SUNSET);                                          // Y-axis Value

        boolean recordsReadFlag=false;

        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            result = c1.getString(irowSolarSunrise) + "|" + c1.getString(irowSolarSunset);
            recordsReadFlag=true;
        }
        c1.close();

        if(!recordsReadFlag) return null;
        else                 return result;


    }

    //=============================================================================================================
    // Get Jobs that are running or in the queue to run
    //=============================================================================================================
    public Integer getJobs(String[] outRecord, Integer inZoneId){

        String[]  columns = new String[] {COL_JOB_ID, COL_JOB_GROUP, COL_JOB_TYPE, COL_JOB_ZONE_ID, COL_JOB_SLOT, COL_JOB_STATUS, COL_JOB_RUNTIME, COL_JOB_SUBMIT_TIME};
        String predicate=null;
        String orderBy = COL_JOB_ID + " ASC, " + COL_JOB_SLOT + " ASC";

        if(inZoneId > 0) {
            predicate = COL_JOB_ID + " = " + inZoneId;
        }

        Cursor c1 = ourDatabase.query(DATABASE_TABLE_JOB, columns, predicate, null, null, null, orderBy, null);

        int i1 = c1.getColumnIndex(COL_JOB_ID);
        int i2 = c1.getColumnIndex(COL_JOB_GROUP);
        int i3 = c1.getColumnIndex(COL_JOB_TYPE);
        int i4 = c1.getColumnIndex(COL_JOB_ZONE_ID);
        int i5 = c1.getColumnIndex(COL_JOB_SLOT);
        int i6 = c1.getColumnIndex(COL_JOB_STATUS);
        int i7 = c1.getColumnIndex(COL_JOB_RUNTIME);
        int i8 = c1.getColumnIndex(COL_JOB_SUBMIT_TIME);


        int i=0;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            outRecord[i] = c1.getString(i1) + "|" + c1.getString(i2) + "|" + c1.getString(i3) + "|" + c1.getString(i4) + "|" +
                           c1.getString(i5) + "|" + c1.getString(i6) + "|" + c1.getString(i7) + "|" + c1.getString(i8);

            i=i+1;
        }
        c1.close();


        return i;
    }

    //=============================================================================================================
    // Get Jobs that are running or in the queue to run
    //=============================================================================================================
    public Boolean getJobCurrentZone(String[] outZoneId){

        String[]  columns = new String[] {COL_JOB_ZONE_ID, COL_JOB_RUNTIME, COL_JOB_SUBMIT_TIME};
        String predicate=COL_JOB_STATUS + " = 1 OR " + COL_JOB_STATUS + " = 2  ";
        String orderBy = COL_JOB_SUBMIT_TIME + " ASC";


        Cursor c1 = ourDatabase.query(DATABASE_TABLE_JOB, columns, predicate, null, null, null, orderBy, null);

        int i1 = c1.getColumnIndex(COL_JOB_ZONE_ID);
        int i2 = c1.getColumnIndex(COL_JOB_RUNTIME);
        int i3 = c1.getColumnIndex(COL_JOB_SUBMIT_TIME);


        Long longTimeLeft;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()) {

            longTimeLeft = (c1.getInt(i2) + c1.getLong(i3)) - (System.currentTimeMillis() / 1000); // time is already passed
            if (longTimeLeft > 0) {
                outZoneId[0] = c1.getString(i1);
                c1.close();
                return true;
            }
        }
        c1.close();
        return false;
    }

    //=============================================================================================================
    // Get server ID - ID 0 should be dextender.com
    //=============================================================================================================
     boolean getDexserver(String inControllerId, String[] outURL, String[] outPort, String[] outKey) {

         String[] columns = new String[]{COL_DEXSERVER_URL, COL_DEXSERVER_PORT, COL_DEXSERVER_KEY};
         String predicate = COL_DEXSERVER_STATUS + " = 1";
         String orderBy = COL_DEXSERVER_ID + " ASC ";

         if (!inControllerId.equals("0") ) {
             predicate = predicate + " AND " + COL_DEXSERVER_ID + " = " + inControllerId;
         }

         Cursor c1 = ourDatabase.query(DATABASE_TABLE_DEXSERVER, columns, predicate, null, null, null, orderBy, null);

         int col1 = c1.getColumnIndex(COL_DEXSERVER_URL);
         int col2 = c1.getColumnIndex(COL_DEXSERVER_PORT);
         int col3 = c1.getColumnIndex(COL_DEXSERVER_KEY);

         int i = 0;
         for (c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()) {
             outURL[i]  = c1.getString(col1);
             outPort[i] = c1.getString(col2);
             outKey[i]  = c1.getString(col3);

             i = i + 1;
         }

         c1.close();

         return i > 0;

     }


    //=============================================================================================================
    // Get zone information - this is a little backward in that we specify the zoneid as the inarg as the last arg
    //=============================================================================================================
    public Integer getZones(String[] outRecord, Integer inZoneId){

        String[]  columns = new String[] {COL_ZONE_ID, COL_ZONE_DESCRIPTION, COL_ZONE_STATUS, COL_ZONE_LAST_TIME};
        String predicate=null;
        String orderBy = COL_ZONE_ID + " ASC ";

        if(inZoneId > 0) {
            predicate = COL_ZONE_ID + " = " + inZoneId;
        }

        Cursor c1 = ourDatabase.query(DATABASE_TABLE_ZONES, columns, predicate, null, null, null, orderBy, null);

        int col1 = c1.getColumnIndex(COL_ZONE_ID);
        int col2 = c1.getColumnIndex(COL_ZONE_DESCRIPTION);
        int col3 = c1.getColumnIndex(COL_ZONE_STATUS);
        int col4 = c1.getColumnIndex(COL_ZONE_LAST_TIME);


        int i=0;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            outRecord[i] = c1.getString(col1) + "|" + c1.getString(col2) + "|" + c1.getString(col3) + "|" + c1.getString(col4);
            i++;
        }

        c1.close();


        return i;
    }

    //=============================================================================================================
    // Return the list of sequences
    //=============================================================================================================
    public Integer getZonesArray(ArrayList<String> outZoneName, ArrayList<Integer> outZoneId){

        String[]  columns = new String[] {COL_ZONE_ID, COL_ZONE_DESCRIPTION};
        String orderBy = COL_ZONE_ID + " ASC ";


        Cursor c1 = ourDatabase.query(DATABASE_TABLE_ZONES, columns, null, null, null, null, orderBy, null);

        int irow01  = c1.getColumnIndex(COL_ZONE_ID);
        int irow02  = c1.getColumnIndex(COL_ZONE_DESCRIPTION);

        int i=0;
        String tempString;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()) {
            tempString=c1.getString(irow01) + " - " + c1.getString(irow02);
            outZoneId.add(c1.getInt(irow01));
            outZoneName.add(tempString);
            i=i+1;
        }
        c1.close();

        return i;
    }

    //=============================================================================================================
    // Return the list of sequences
    //=============================================================================================================
    public Integer getSequences(String[] outRecord, Integer inSeqId){

        String[]  columns = new String[] {COL_SEQ_ID, COL_SEQ_NAME, COL_SEQ_STATUS, COL_SEQ_LAST_TIME};
        String predicate=null;
        String orderBy = COL_SEQ_ID + " ASC ";

        if(inSeqId > 0) {
            predicate = COL_SEQ_ID + " = " + inSeqId;
        }

        Cursor c1 = ourDatabase.query(DATABASE_TABLE_SEQUENCES, columns, predicate, null, null, null, orderBy, null);

        int irowSeqId       = c1.getColumnIndex(COL_SEQ_ID);
        int irowSeqDesc     = c1.getColumnIndex(COL_SEQ_NAME);
        int irowSeqStatus   = c1.getColumnIndex(COL_SEQ_STATUS);
        int irowSeqLastTime = c1.getColumnIndex(COL_SEQ_LAST_TIME);

        int i=0;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            outRecord[i] = c1.getString(irowSeqId) + "|" + c1.getString(irowSeqDesc) + "|" + c1.getString(irowSeqStatus) + "|" + c1.getString(irowSeqLastTime);
            i=i+1;
        }
        c1.close();

        return i;
    }

    //=============================================================================================================
    // Return the list of sequences
    //=============================================================================================================
    public Integer getSequencesArray(ArrayList<String> outSeqName, ArrayList<Integer> outSeqId){

        String[]  columns = new String[] {COL_SEQ_ID, COL_SEQ_NAME};
        String orderBy = COL_SEQ_ID + " ASC ";


        Cursor c1 = ourDatabase.query(DATABASE_TABLE_SEQUENCES, columns, null, null, null, null, orderBy, null);

        int irow01  = c1.getColumnIndex(COL_SEQ_ID);
        int irow02  = c1.getColumnIndex(COL_SEQ_NAME);

        int i=0;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()) {
            outSeqId.add(c1.getInt(irow01));
            outSeqName.add(c1.getString(irow02));
            i=i+1;
        }
        c1.close();

        return i;
    }

    //=============================================================================================================
    // Return the list of zones in a sequence
    //=============================================================================================================
    public Integer getSequenceZoneList(String inSequenceId, String[] outZones){


        Cursor c1 = ourDatabase.rawQuery("SELECT zones.zone_id, zones.description, sequence_zones.runtime " +
                " FROM zones, sequence_zones " +
                " WHERE zones.zone_id = sequence_zones.zone_id " +
                " and  sequence_zones.sequence_id = " + inSequenceId +
                " order by zones.zone_id asc ", null);

        int irow01       = c1.getColumnIndex(COL_ZONE_ID);
        int irow02       = c1.getColumnIndex(COL_ZONE_DESCRIPTION);
        int irow03       = c1.getColumnIndex(COL_SEQZ_RUNTIME);

        int i=0;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            outZones[i] = c1.getString(irow01) + "|" + c1.getString(irow02) + "|" + c1.getInt(irow03)/60;
            i=i+1;
        }
        c1.close();

        return i;
    }
    //=============================================================================================================
    // Return the list of schedules
    //=============================================================================================================
    public Integer getSchedules(String[] outRecord){

        String[]  columns = new String[] {
                COL_SCHED_ID,
                COL_SCHED_NAME,
                COL_SCHED_STATUS,
                COL_SCHED_START_TIME,
                COL_SCHED_SUNDAY,
                COL_SCHED_MONDAY,
                COL_SCHED_TUESDAY,
                COL_SCHED_WEDNESDAY,
                COL_SCHED_THURSDAY,
                COL_SCHED_FRIDAY,
                COL_SCHED_SATURDAY,
                COL_SCHED_EVENODD,
                COL_SCHED_SUNTYPE};


        String orderBy = COL_SCHED_ID + " ASC ";
        //predicate = COL_BG_SEQ + " > " + offsetValue;


        //Cursor c1 = ourDatabase.query(DATABASE_TABLE_BG, columns, predicate, null, null, null, orderBy, null);
        Cursor c1 = ourDatabase.query(DATABASE_TABLE_SCHEDULES, columns, null, null, null, null, orderBy, null);

        int col1  = c1.getColumnIndex(COL_SCHED_ID);
        int col2  = c1.getColumnIndex(COL_SCHED_NAME);
        int col3  = c1.getColumnIndex(COL_SCHED_STATUS);
        int col4  = c1.getColumnIndex(COL_SCHED_START_TIME);
        int col5  = c1.getColumnIndex(COL_SCHED_SUNDAY);
        int col6  = c1.getColumnIndex(COL_SCHED_MONDAY);
        int col7  = c1.getColumnIndex(COL_SCHED_TUESDAY);
        int col8  = c1.getColumnIndex(COL_SCHED_WEDNESDAY);
        int col9  = c1.getColumnIndex(COL_SCHED_THURSDAY);
        int col10 = c1.getColumnIndex(COL_SCHED_FRIDAY);
        int col11 = c1.getColumnIndex(COL_SCHED_SATURDAY);
        int col12 = c1.getColumnIndex(COL_SCHED_EVENODD);
        int col13 = c1.getColumnIndex(COL_SCHED_SUNTYPE);


        int i=0;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            outRecord[i] =
            c1.getString(col1) + "|" +
            c1.getString(col2) + "|" +
            c1.getString(col3) + "|" +
            c1.getString(col4) + "|" +
            c1.getString(col5) + "|" +
            c1.getString(col6) + "|" +
            c1.getString(col7) + "|" +
            c1.getString(col8) + "|" +
            c1.getString(col9) + "|" +
            c1.getString(col10)+ "|" +
            c1.getString(col11)+ "|" +
            c1.getString(col12)+ "|" +
            c1.getString(col13);
            i++;
        }

        c1.close();


        // returns the row count, with the array 'outrecord' containing all the records
        return i;

    }

    public Integer getLogData(String[] outRecord) {
        String[]  columns = new String[] {COL_LOG_ID, COL_LOG_LEVEL, COL_LOG_MESSAGE, COL_LOG_CREATED};

        String limit="50";
        String orderBy = COL_LOG_ID + " asc ";

        Cursor c1 = ourDatabase.query(DATABASE_TABLE_LOG, columns, null, null, null, null, orderBy, limit);

        int irow01 = c1.getColumnIndex(COL_LOG_ID);
        int irow02 = c1.getColumnIndex(COL_LOG_LEVEL);
        int irow03 = c1.getColumnIndex(COL_LOG_MESSAGE);
        int irow04 = c1.getColumnIndex(COL_LOG_CREATED);

        Integer i=0;
        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            outRecord[i] =  c1.getString(irow01) + "|" +
                            c1.getString(irow02) + "|" +
                            c1.getString(irow03) + "|" +
                            tools.epoch2FmtTime(c1.getLong(irow04), "MMMM d HH:mm:ss");
            i++;
            if (i >= MAX_LOG_DISPLAY_ENTRIES) break;
        }

        c1.close();

        return i;
    }


    //==============================================================================================
    // SERVICES
    // -1 - service is unknown
    //  0 - service is off
    //  1 - service is on
    //  2 - service is paused
    //==============================================================================================
    public Integer getServiceStatus(String argServiceName){
        String[]  columns = new String[] {COL_SERVICE_STATUS};
        String predicate = COL_SERVICE_NAME + "= '" + argServiceName + "'";

        Cursor c1 = ourDatabase.query(DATABASE_TABLE_SERVICES, columns, predicate, null, null, null, null);
        Integer result=-1;
        int col1     = c1.getColumnIndex(COL_SERVICE_STATUS);

        for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()){
            result = c1.getInt(col1);
        }
        c1.close();

        return result;
    }

    //----------------------------------------------------------
    // if any of the web related services are down, then return
    // false.
    //---------------------------------------------------------
    public boolean getAllWebServicesStatus() {
        String predicate = COL_SERVICE_ID + " > 0";
        String[]  columns = new String[] {COL_SERVICE_NAME, COL_SERVICE_STATUS};

        Cursor c1 = ourDatabase.query(DATABASE_TABLE_SERVICES, columns, predicate, null, null, null, null);
        int col2 = c1.getColumnIndex(COL_SERVICE_STATUS);

        for (c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()) {
            if (c1.getInt(col2) <= 0) {
                c1.close();
                return false;
            }
        }
        c1.close();

        return true;
    }

    //---------------------------------------------------------------------------------
    // Get the local 'last' system time information
    // This will be compared against the master and see if we need to complete refresh
    //----------------------------------------------------------------------------------
    public Long getLocalSystemTimestamp() {
        String predicate = COL_SYS_ID + " = 1 ";
        String[]  columns = new String[] {COL_SYS_TIMESTAMP};

        Cursor c1 = ourDatabase.query(DATABASE_TABLE_SYS_TIMESTAMP, columns, predicate, null, null, null, null);
        int col1 = c1.getColumnIndex(COL_SYS_TIMESTAMP);

        Long returnTimestamp=(long) 0;

        for (c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()) {
            returnTimestamp =  Long.parseLong(c1.getString(col1));
        }
        c1.close();

        return returnTimestamp;
    }

    //-------------------------------------------------------------------------
    // Method : getAlarm
    // Author : MLV
    // Purpose: To get the time the alarm was rung.
    //          Since int, long aren't passed by reference, but strings are,
    //          we will pass the time as a string and let the calling function
    //          deal with changing it to a long (c, c++ so much better)
    //-------------------------------------------------------------------------
    public int getPauseTime() {

        String[] columns = new String[]{COL_STATUS_PAUSE_TIME};
        String predicate = COL_STATUS_ID + " = 1";

        Cursor c1 = ourDatabase.query(DATABASE_TABLE_STATUS, columns, predicate, null, null, null, null);

        int irow01  = c1.getColumnIndex(COL_STATUS_PAUSE_TIME);
        int outPauseTime=0;

        for (c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()) {
            outPauseTime = c1.getInt(irow01);
        }
        c1.close();
        return outPauseTime;
    }

    public int  getAlarmCount(int argAlarmId ) {

        int outAlarmCount=0;
        String[] columns = new String[]{COL_ALARM_COUNT};
        String predicate = COL_ALARM_ID + "= " + argAlarmId;

        Cursor c1 = ourDatabase.query(DATABASE_TABLE_ALARM, columns, predicate, null, null, null, null);

        int irowAlarmCount = c1.getColumnIndex(COL_ALARM_COUNT);

        for (c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext()) {
            outAlarmCount = c1.getInt(irowAlarmCount);
        }

        c1.close();
        return outAlarmCount;
    }
    //-------------------------------------------------------------------------
    // Update status
    //-------------------------------------------------------------------------
    public int updateSysStatusCurrentZone(String inZone){

        ContentValues cv = new ContentValues();

        cv.put(COL_STATUS_LAST_ZONE, COL_STATUS_CURRENT_ZONE);                                      // set last zone to current zone
        String predicate = COL_STATUS_ID + "= 1;";
        ourDatabase.update(DATABASE_TABLE_STATUS, cv, predicate, null);

        cv.put(COL_STATUS_CURRENT_ZONE, "0");
        predicate = COL_STATUS_ID + " = " + inZone + ";";
        return ourDatabase.update(DATABASE_TABLE_STATUS, cv, predicate, null);
    }

    //-------------------------------------------------------------------------
    // Update the services with the latest status
    //-------------------------------------------------------------------------
    public int updateServiceStatus(String argServiceName, int argStatus){

        ContentValues cv = new ContentValues();
        cv.put(COL_SERVICE_STATUS, argStatus);

        String predicate = COL_SERVICE_NAME + "= '" + argServiceName + "';";

        return ourDatabase.update(DATABASE_TABLE_SERVICES, cv, predicate, null);
    }


    //-------------------------------------------------------------------------
    // Update Pause time
    //-------------------------------------------------------------------------
    public int updatePauseTime(int pauseLength, long pauseTime){

        ContentValues cv = new ContentValues();
        cv.put(COL_STATUS_PAUSE_TIME, pauseLength);
        cv.put(COL_STATUS_PAUSE_DATE, pauseTime);

        String predicate = COL_STATUS_ID + " = 1";

        return ourDatabase.update(DATABASE_TABLE_STATUS, cv, predicate, null);
    }



    //-------------------------------------------------------------------------
    // Update the dexserver
    // "00100000|http://www.foo.com|80|1|0000-0000-0000";
    //-------------------------------------------------------------------------
    public int updateDexserver(String inControllerId, String argUrl, String argPort, String argAccessKey) {

        ContentValues cv = new ContentValues();
        cv.put(COL_DEXSERVER_URL,  argUrl);
        cv.put(COL_DEXSERVER_PORT, argPort);
        cv.put(COL_DEXSERVER_KEY,  argAccessKey);

        String predicate = COL_DEXSERVER_ID + "= '" + inControllerId + "';";

        return ourDatabase.update(DATABASE_TABLE_DEXSERVER, cv, predicate, null);
    }

    //-------------------------------------------------------------------------
    // Update the services with the latest status
    //-------------------------------------------------------------------------
    public int updateSequenceStatus(Integer inSequenceId, Long inEpochTime, Integer inStatus){
        ContentValues cv = new ContentValues();
        cv.put(COL_SEQ_LAST_TIME, inEpochTime);
        cv.put(COL_SEQ_STATUS, inStatus);

        String predicate = COL_SEQ_ID + "= " + inSequenceId + ";";

        return ourDatabase.update(DATABASE_TABLE_SEQUENCES, cv, predicate, null);
    }

    //-------------------------------------------------------------------------
    // Update the services with the latest status
    //-------------------------------------------------------------------------
    public int updateSequenceInfo(Integer inSequenceId, Long inEpochTime){
        ContentValues cv = new ContentValues();
        cv.put(COL_SEQ_LAST_TIME, inEpochTime);

        String predicate = COL_SEQ_ID + "= " + inSequenceId + ";";

        return ourDatabase.update(DATABASE_TABLE_SEQUENCES, cv, predicate, null);
    }

    //-------------------------------------------------------------------------
    // Update the schedule with the latest status
    //-------------------------------------------------------------------------
    public int updateScheduleStatus(Integer inScheduleId, Integer inStatus){
        ContentValues cv = new ContentValues();
        cv.put(COL_SCHED_STATUS, inStatus);

        String predicate = COL_SCHED_ID + "= " + inScheduleId + ";";

        return ourDatabase.update(DATABASE_TABLE_SCHEDULES, cv, predicate, null);
    }


    //-------------------------------------------------------------------------
    // Update the zone with the latest status
    //-------------------------------------------------------------------------
    public int updateZoneStatus(Integer inZoneId, Long inEpochTime, Integer inStatus){
        ContentValues cv = new ContentValues();
        cv.put(COL_ZONE_LAST_TIME, inEpochTime);
        cv.put(COL_ZONE_STATUS, inStatus);

        String predicate = COL_ZONE_ID + "= " + inZoneId + ";";

        return ourDatabase.update(DATABASE_TABLE_ZONES, cv, predicate, null);
    }

    //-------------------------------------------------------------------------
    // Update the zone with the latest status
    //-------------------------------------------------------------------------
    public int updateZoneName(Integer inZoneId, Long inEpochTime, String inNewName){
        ContentValues cv = new ContentValues();
        cv.put(COL_ZONE_LAST_TIME, inEpochTime);
        cv.put(COL_ZONE_DESCRIPTION, inNewName);

        String predicate = COL_ZONE_ID + "= " + inZoneId + ";";

        return ourDatabase.update(DATABASE_TABLE_ZONES, cv, predicate, null);
    }

    //-------------------------------------------------------------------------
    // Update the services with the latest status
    //-------------------------------------------------------------------------
    public int updateZoneInfo(Integer inZoneId, Long inEpochTime){
        ContentValues cv = new ContentValues();
        cv.put(COL_ZONE_LAST_TIME, inEpochTime);

        String predicate = COL_ZONE_ID + "= " + inZoneId + ";";

        return ourDatabase.update(DATABASE_TABLE_ZONES, cv, predicate, null);
    }

    //-------------------------------------------------------------------------
    // Update the system timestamp with the latest timecheck from the master server
    //-------------------------------------------------------------------------
    public int updateSystemTimestamp(Integer inSysId, Long inEpochTime){
        ContentValues cv = new ContentValues();
        cv.put(COL_SYS_TIMESTAMP, inEpochTime);

        String predicate = COL_SYS_ID + "= " + inSysId + ";";

        return ourDatabase.update(DATABASE_TABLE_SYS_TIMESTAMP, cv, predicate, null);

    }


    public int deleteSequence(String inSequenceId) {
        String predicate = COL_SEQ_ID + "= " + inSequenceId + ";";
        ourDatabase.delete(DATABASE_TABLE_SEQUENCES, predicate, null);

        predicate = COL_SEQZ_ID + "= " + inSequenceId + ";";
        ourDatabase.delete(DATABASE_TABLE_SEQUENCE_ZONES, predicate, null);

        return 0;
    }


    public int deleteSchedule(String inScheduleId) {
        String predicate = COL_SCHED_ID + "= " + inScheduleId + ";";
        ourDatabase.delete(DATABASE_TABLE_SCHEDULES, predicate, null);

        return 0;
    }
    //==============================================================================================
    // ALARM
    //==============================================================================================
    //-------------------------------------------------------------------------
    // Method : updateAlarm
    // Author : MLV
    // Purpose: To update the alarm table with the current time. Used for
    //          Snoozing
    //-------------------------------------------------------------------------
    public void updateAlarm(int argAlarmId){
        ContentValues cv = new ContentValues();
        cv.put(COL_ALARM_TIME, (System.currentTimeMillis()/1000));
        cv.put(COL_ALARM_COUNT, 1);

        String predicate = COL_ALARM_ID + "= " + argAlarmId + ";";

        ourDatabase.update(DATABASE_TABLE_ALARM, cv, predicate, null);
    }

    //-------------------------------------------------------------------------
    // Method : clearAlarm
    // Author : MLV
    // Purpose: If last alarm time is 0, then this is a fresh alarm and
    //          usually sound right away
    //-------------------------------------------------------------------------

    public void clearAlarm(int argAlarmId){
        ContentValues cv = new ContentValues();
        cv.put(COL_ALARM_COUNT, 0);
        cv.put(COL_ALARM_TIME, 0);
        String predicate = COL_ALARM_ID + "= " + argAlarmId + ";";

        ourDatabase.update(DATABASE_TABLE_ALARM, cv, predicate, null);
    }

    public void deleteJobGroups()     { ourDatabase.delete(DATABASE_TABLE_JOB_GROUP, null, null);  }
    public void deleteJobs()          { ourDatabase.delete(DATABASE_TABLE_JOB,            null, null);  }
    public void deleteLog()           { ourDatabase.delete(DATABASE_TABLE_LOG,            null, null);  }
    public void deleteSchedules()     { ourDatabase.delete(DATABASE_TABLE_SCHEDULES,      null, null);  }
    public void deleteSequences()     { ourDatabase.delete(DATABASE_TABLE_SEQUENCES,      null, null);  }
    public void deleteSequenceZones() { ourDatabase.delete(DATABASE_TABLE_SEQUENCE_ZONES, null, null);  }
    public void deleteSolar()         { ourDatabase.delete(DATABASE_TABLE_SOLAR,          null, null);  }
    public void deleteStatus()        { ourDatabase.delete(DATABASE_TABLE_STATUS,         null, null);  }
    public void deleteZones()         { ourDatabase.delete(DATABASE_TABLE_ZONES,          null, null);  }





}
