package net.cmikavac.autowol.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;

public class TimeUtil {
    public static Long getTimeInMilliseconds(int hour, int minute) {
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minute);
        return time.getTimeInMillis(); 
    }

    public static String getFormatedTime(Long milliSeconds, Context context) {
        DateFormat formatter = getFormatter(context);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(milliSeconds);
        return formatter.format(time.getTime());
    }

    private static DateFormat getFormatter(Context context) {
        String format = android.text.format.DateFormat.is24HourFormat(context) ? "HH:mm" : "hh:mm aa";
        return new SimpleDateFormat(format, Locale.getDefault());
    }

    public static int getHourFromMilliseconds(Long milliSeconds) {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(milliSeconds);
        return time.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinuteFromMilliseconds(Long milliSeconds) {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(milliSeconds);
        return time.get(Calendar.MINUTE);
    }

    public static Boolean isNowBetweenQuietHours(Long quietFrom, Long quietTo) {
        Calendar timeNow = Calendar.getInstance();
        android.util.Log.d("Test", "1");
        Calendar timeFrom = Calendar.getInstance();
        android.util.Log.d("Test", Long.toString(quietFrom) + " " + Long.toString(quietTo));
        timeFrom.set(Calendar.HOUR_OF_DAY, getHourFromMilliseconds(quietFrom));
        timeFrom.set(Calendar.MINUTE, getMinuteFromMilliseconds(quietFrom));
        android.util.Log.d("Test", "2");
        Calendar timeTo = Calendar.getInstance();
        timeTo.set(Calendar.HOUR_OF_DAY, getHourFromMilliseconds(quietTo));
        timeTo.set(Calendar.MINUTE, getMinuteFromMilliseconds(quietTo));
        android.util.Log.d("Test", "3");
        if (timeTo.before(timeFrom)) {
            timeTo.add(Calendar.DATE, 1);
        }
        android.util.Log.d("Test", "4");
        return timeNow.after(timeFrom) && timeNow.before(timeTo) ? true : false;
    }
    
    public static Boolean hasIdleTimePassed(Integer idleTime, Long lastDisconnected) {
        if (lastDisconnected.equals(null))
            return true;

        Calendar timeIdle = Calendar.getInstance();
        timeIdle.add(Calendar.MINUTE, -1 * idleTime);

        Calendar timeDisconnected = Calendar.getInstance();
        timeDisconnected.setTimeInMillis(lastDisconnected);

        return timeDisconnected.before(timeIdle);
    }
}