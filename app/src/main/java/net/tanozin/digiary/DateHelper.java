package net.tanozin.digiary;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateHelper {
    private ArrayList<String> dateStrings;
    private static String format;

    public DateHelper(ArrayList<String> dateStrings, String format) {
        this.dateStrings = dateStrings;
        DateHelper.format = format;
    }

    public ArrayList<Date> sort() {
        int i = 0, k = 0;
        ArrayList<Date> dates = new ArrayList<>();
        while (i < dateStrings.size()) {
            String str = dateStrings.get(i);
            DateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
            try {
                dates.add(formatter.parse(str));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            i = i + 1;
        }

        i = 0;
        while (i < dates.size())

        {
            while (k < ((dates.size() - 1) - i)) {
                //Store the date values to be swapped
                Date l = dates.get(k), h = dates.get(k + 1);
                //make calendars for easy comparison
                Calendar current = new GregorianCalendar();
                Calendar next = new GregorianCalendar();
                //set the calendars to respective dates
                current.setTime(l);
                next.setTime(h);
                //swap the dates, let the future come first, position 0 being the farthest future
                if (next.after(current)) {
                    dates.set(k, h);
                    dates.set(k + 1, l);
                }
                k = k + 1;
            }
            k = 0;
            i = i + 1;
        }

        return dates;
    }

    public Date nextEvent() {
        Date returnValue = null;
        ArrayList<Date> dates = sort();
        Date now = new Date(System.currentTimeMillis());
        Calendar current = new GregorianCalendar();
        Calendar next = new GregorianCalendar();
        current.setTime(now);
        for (Date i : dates) {
            next.setTime(i);
            if (next.after(current))
                returnValue = i;
        }
        return returnValue;
    }

    public Date nextEvent(String eventDate) {
        DateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        Date returnValue = null;
        ArrayList<Date> dates = sort();
        Date now = null;
        try {
            now = formatter.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar current = new GregorianCalendar();
        Calendar next = new GregorianCalendar();
        current.setTime(now);
        for (Date i : dates) {
            next.setTime(i);
            if (next.after(current))
                returnValue = i;
        }
        return returnValue;
    }

    public static String convertDateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }

    public static String convertDateToString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(date);
    }

    public static String beautifulDateToString(Date date, String format) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        Calendar now = new GregorianCalendar();
        now.setTime(new Date(System.currentTimeMillis()));
        if (cal.get(Calendar.YEAR) == (now.get(Calendar.YEAR))) {
            format = "MMM dd";//make this a preference
            if (cal.get(Calendar.DAY_OF_YEAR) == (now.get(Calendar.DAY_OF_YEAR))) {
                format = "hh:mm aa";
                if (cal.get(Calendar.HOUR_OF_DAY) == (now.get(Calendar.HOUR_OF_DAY))) {
                    int min = (now.get(Calendar.MINUTE)) - cal.get(Calendar.MINUTE);
                    if (min > 1)
                        return Integer.toString(min) + " minutes ago";
                    else return "moments ago";
                }
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(date);
    }

    public static String getTime(Date date, String format) {
        if (format == null)
            format = "HH:mm";
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        Calendar now = new GregorianCalendar();
        now.setTime(new Date(System.currentTimeMillis()));
        if (cal.get(Calendar.YEAR) == (now.get(Calendar.YEAR))) {
            if (cal.get(Calendar.DAY_OF_YEAR) == (now.get(Calendar.DAY_OF_YEAR))) {
                if (cal.get(Calendar.HOUR_OF_DAY) == (now.get(Calendar.HOUR_OF_DAY))) {
                    int min = (now.get(Calendar.MINUTE)) - cal.get(Calendar.MINUTE);
                    if (min > 1)
                        return Integer.toString(min) + " minutes ago";
                    else return "moments ago";
                }
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(date);
    }


    public static Date convertStringToDate(String dateString, String format) {
        DateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        try {
            return formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(0L);
    }

    public static String getDate(Date date, String format) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        Calendar now = new GregorianCalendar();
        now.setTime(new Date(System.currentTimeMillis()));
        if (cal.get(Calendar.YEAR) == (now.get(Calendar.YEAR))) {
            format = "MMM dd";//make this a preference
        } else format = "yyyy, MMM dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(date);
    }

    public static Long getTime() {
        Calendar now = new GregorianCalendar();
        now.setTime(new Date(System.currentTimeMillis()));
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int min = (now.get(Calendar.MINUTE));
        int secs = now.get(Calendar.SECOND);
        int ms = now.get(Calendar.MINUTE);
        return (Long) (long) ((60 * 60 * 1000 * hour) + (60 * 1000 * min) + (1000 * secs) + ms);
    }
}