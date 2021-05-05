package com.dextender.dextender_h2o;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

//------------------------------------------------------------------------------------
// Class : MyTools
// Author: Mike LiVolsi
//
// Purpose: A multi-purpose class to do mundane things, like calc differences in time
//          time offsets, etc..
//-----------------------------------------------------------------------------------
public class MyTools {

    // YOU pass the format
    public String epoch2FmtTime(long argEpoch, String argFormat) {
        Date date = new Date(argEpoch * 1000L);
        DateFormat format;
        if(argFormat == null)
            format = new SimpleDateFormat("HH:mm:ss");
        else
            format = new SimpleDateFormat(argFormat);
        format.setTimeZone(TimeZone.getDefault());
        return format.format(date);
    }

    public int getOffsetFromUtc() {
        TimeZone tz = TimeZone.getDefault();
        Date now = new Date();
        return tz.getOffset(now.getTime()) / 1000;
    }

    public String secondsToTimeStr(long argInSeconds) {
        long hours;
        long minutes;
        long seconds;
        String outString="";
        if (argInSeconds >= 60 ) {
            if(argInSeconds >= 3600) {
                hours=argInSeconds/3600;
                minutes=(argInSeconds-(hours*3600))/60;
                seconds=argInSeconds -(minutes*60) - (hours*3600);

                if (hours < 10) outString = "0";
                outString = outString + hours;

                if (minutes < 10 )   outString=outString + ":0";
                else                 outString=outString + ":";

                outString = outString + String.valueOf(minutes);

                if (seconds < 10 )   outString=outString + ":0";
                else                 outString=outString + ":";

                outString = outString + String.valueOf(seconds);
            }
            else {
                minutes=argInSeconds/60;
                seconds=argInSeconds - (minutes*60);
                if (minutes < 10 )   outString="00:0";
                else                 outString="00:";

                outString = outString + String.valueOf(minutes);

                if(seconds < 10)     outString = outString + ":0";
                else                 outString = outString + ":";

                outString = outString + String.valueOf(seconds);
            }
        }
        else {
            if(argInSeconds < 10)   outString="00:00:0" + String.valueOf(argInSeconds);
            else                    outString="00:00:"  + String.valueOf(argInSeconds);

        }
        return outString;
    }

    public String now() {
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();
        return format.format(now.getTime());
    }

    public int modulo(int m, int n) {
        int mod = m % n;
        return (mod < 0) ? mod + n : mod;
    }


/*
    public Date epoch2Date(long argEpoch) {

        Date date = new Date(argEpoch * 1000L);
        return date;
        // DateFormat format = new SimpleDateFormat("HH:mm:ss");
        // format.setTimeZone(TimeZone.getDefault());

    }
*/
/*
    public String fuzzyTimeDiff(long millis1, long millis2) {
        long diffInMillis = millis1 - millis2;

        String output;

        float floatTime = diffInMillis / 24;
        if (floatTime < 1) {
            floatTime = diffInMillis / 60;
            if (floatTime < 1) {
                floatTime = diffInMillis / 60;
                output = Float.toString(floatTime) + " seconds ago";
            } else {
                output = "More than " + Float.toString(floatTime) + " minutes ago";
            }
        } else {
            output = "More than " + Float.toString(floatTime) + " hours ago";

        }
        return output;
    }
*/

    public String threeDigitsToAscii(int inDigits) {

        //----------------------------------------------------------------------------
        // Do a little pre-formatting. I'm betting there's a function that does this..
        // but this works just the same
        //----------------------------------------------------------------------------
        String tmpString = Integer.toString(inDigits);
        if (inDigits < 100) {
            if (inDigits < 10) {
                tmpString = "00" + tmpString;
            } else {
                tmpString = "0" + tmpString;
            }
        }

        String buildString = "";
        char tmpChr;
        int x;
        for (int i = 0; i < tmpString.length(); i++) {
            x = Character.getNumericValue(tmpString.charAt(i));  // literal value of the string (if 000 and the first char is 0, then x = 0 )
            switch (i) {
                case 0:
                    x += 67;
                    tmpChr = (char) x;
                    buildString = buildString + tmpChr;
                    break;
                case 1:
                    x += 48;
                    buildString = buildString + Integer.toString(x);
                    break;
                case 2:
                    x += 100;
                    tmpChr = (char) x;
                    buildString = buildString + tmpChr;
            }
        }
        return buildString;
    }

    //-----------------------------------------------------------------------
    // Method  : processWebResponse
    // Used by : MyService
    // Slot  0 : 0 service not available
    //           1 service is available
    // slot  1 : 0 failed on insert
    //           1 success
    //           2 account not recognized
    //           3 account suspended
    //-----------------------------------------------------------------------
    public boolean processCloudResponse(String inString, String outErrorMsg[], String inType) {

        String tmpString;

        if (inType.equals("serverStatus")) {
            if ( (inString == null) || (inString.length()==0) ) return false;
            tmpString = inString.substring(0, 1);
            if (!tmpString.equals("1")) {
                int foo = Integer.parseInt(tmpString);
                int loc=inString.indexOf('|');
                if(loc > 1) outErrorMsg[0] = inString.substring(loc+1);
                switch(foo) {
                    case 0:
                        if(outErrorMsg[0] == null) {
                            outErrorMsg[0] = "Cloud connected, but a server component is down";
                        }
                        break;
                    case 3:
                        if(outErrorMsg[0] == null) {
                            outErrorMsg[0] = "Cannot connect to the cloud";
                        }
                        break;
                }
                return false;
            }
            else {
                return true;
            }

        }

        if (inType.equals("userStatus")) {
            tmpString = inString.substring(1, 2);
            if (!tmpString.equals("1")) {
                int foo = Integer.parseInt(tmpString);
                int loc=inString.indexOf('|');
                if(loc > 1) outErrorMsg[0] = inString.substring(loc+1);
                switch (foo) {
                    case 0:
                        if(outErrorMsg[0] == null) {
                            outErrorMsg[0] = "Server RDBMS failure - Contact server admin";
                        }
                        break;
                    case 2:
                        if(outErrorMsg[0] == null) {
                            outErrorMsg[0] = "Your account was not recognized by the server - Please register";
                        }
                        break;
                    case 3:
                        if(outErrorMsg[0] == null) {
                            outErrorMsg[0] = "Your account has been suspended";
                        }
                }
                return false;
            }
            return true;
        }


        //------------------------------------------------------------------------------
        // Right now, if the cloud responds in slot 6 (array position 5) that there's an
        // issue, most likely it's because there's an error on the USB on the master
        // side.
        //------------------------------------------------------------------------------
        if (inType.equals("masterServiceStatus")) {
            if ( (inString == null) || (inString.length()==0) ) return true;
            int srvstat = Integer.parseInt(inString.substring(5, 6));

            switch(srvstat) {
                case 0:                                                                             // all components are 'ok'                                        return true;
                    if (outErrorMsg[0] == null) {
                        outErrorMsg[0] = "The main device is having issues connecting to the USB";
                    }
                    return false;

                case 1:

                    return true;

            }
        }

        return false;
    }

    //-----------------------------------------------------------------------
    // Method  : processCloudOptions
    // Used by : MyService
    // Slot  2 : 0 service not available
    //           1 service is available
    // slot  3 : 0 failed on insert
    //           1 success
    //           2 account not recognized
    //           3 account suspended
    //-----------------------------------------------------------------------
    public boolean processCloudOptions(String inString, String inType, int inOption[], String outMsg[]) {

        String tmpString;

        if (inType.equals("sysmsg")) {
            tmpString = inString.substring(2, 3);                                                   // has the system message changed
            int foo=Integer.parseInt(tmpString);
            int loc;
            switch(foo) {
                case 1:
                    loc = inString.indexOf('|');                                                    // any message will come after the instructions
                    if(loc > 0) {
                        outMsg[0] = inString.substring(loc + 1);
                        loc = outMsg[0].indexOf('|');                                               // look for a '|' at the end
                        if (loc > 0) {                                                              // it exists
                            outMsg[0] = outMsg[0].substring(0, loc);                                // grab what's in-between the '|'
                        }
                        return true;
                    }
                    else {
                        return false;
                    }                                                                               // there was a change
                case 2:
                    outMsg[0] = null;
                    return true;                                                                    // there was a change
            }
            return false;                                                                           // nothing to change
        }
        else {
            if(inType.equals("smartBand")) {
                tmpString = inString.substring(3,4);                                                // smart band option changed
                int foo=Integer.parseInt(tmpString);

                switch(foo) {
                    case 0: return false;                                                           // No change
                    case 1:
                        inOption[0] = 1;
                        return true;                                                                // there was a change - no smartband
                    case 2:
                        inOption[0] = 2;
                        return true;                                                                // there was a change - smartband
                }
                return false;
            }
        }
        return true;
    }


    //----------------------------------------------------------------------------------------------
    // Method: processCloudBgRecords
    // Author: MLV
    // Purpose: To take the string coming in from the server with client records and parse it
    //          and populate into the associated arrays
    // Arguments: inString
    //
    //----------------------------------------------------------------------------------------------
    public int processCloudBgRecords(String inString, String[] outTime, String[] outBg,  String[] outSeq){

        String tmpString;

        tmpString=inString.substring(4,5);                                                          // no records were processed
        if( tmpString.equals("0")) {
            return 0;
        }


        int startPos;
        tmpString=inString.substring(2,3);                                                          // There's a system message
        if( tmpString.equals("1")) {
            startPos = inString.indexOf('|',1);                                                     // first pipe
            startPos = inString.indexOf('|', (startPos+1));                                         // second pipe after the system message
        }
        else {
            startPos = inString.indexOf('|',1)+1;                                                   // The very first 'pipe'
        }


        int idx=0;
        int loc;
        for(int i=startPos; i < inString.length(); i++) {
            loc=inString.indexOf('|',i+1);
            outTime[idx]=inString.substring(i,i+10);
            outBg[idx]=inString.substring(i+10,i+13);
            if(loc > 1) {
                outSeq[idx]=inString.substring(i+16, loc);
                i=loc;
            }
            else {                                                                                  // we are at the last record in the string
                outSeq[idx]=inString.substring(i+16);
                i+= (16+ outSeq[idx].length());
            }
            idx++;
        }

        return idx;
    }
}
