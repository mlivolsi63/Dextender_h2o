package com.dextender.dextender_h2o;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


 class Tools {

    // YOU pass the format
     String epoch2FmtTime(long argEpoch, String argFormat) {
        Date date = new Date(argEpoch * 1000L);
        DateFormat format;
        if (argFormat == null)
            format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        else
            format = new SimpleDateFormat(argFormat, Locale.getDefault());
        format.setTimeZone(TimeZone.getDefault());
        return format.format(date);
    }


     String now() {
        DateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Date now = new Date();
        return format.format(now.getTime());
    }


     String fuzzyTimeDiff(long epochSeconds1, long epochSeconds2) {
        long diffInSeconds = epochSeconds1 - epochSeconds2;

        String output;

        float floatTime;

        if (diffInSeconds < 60) {
            output = "";
        }
        else {
            if (diffInSeconds < 3600) {
                floatTime = diffInSeconds / 60;
                 output =  Math.round(floatTime) + " minute(s) ago";
            } else {
                floatTime = diffInSeconds / 3600;
                output = Math.round(floatTime) + " hour(s) ago";
            }
        }

        return output;
    }

}
