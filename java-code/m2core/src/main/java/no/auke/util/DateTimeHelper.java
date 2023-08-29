/*
 * This file is part of Smooby project,  
 * 
 * Copyright (c) 2011-2011 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */


package no.auke.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeHelper {

    public static final String DATE_FORMAT = "dd-MM-yyyy";

    public static Date Now() {
        Calendar c = Calendar.getInstance();
        return c.getTime();
    }

    public static String ToString(String format) {
        return ToString(Now(), format);

    }

    public static String ToString(Date date, String format) {
        if (format==null || format.equals("")){
            format = DATE_FORMAT;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);

    }

    private static Date Add(Date date, int type, int num) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(date);
        c1.add(type, num);
        return c1.getTime();
    }

    public static Date Roll(Date date, int type) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(date);
        c1.roll(type, false);
        return c1.getTime();
    }

    public static Date AddMonth(Date date, int months) {
        return Add(date, Calendar.MONTH, months);
    }

    public static Date AddDay(Date date, int days) {
        return Add(date, Calendar.DAY_OF_MONTH, days);
    }

    public static Date AddYear(Date date, int years) {
        return Add(date, Calendar.YEAR, years);
    }

    public static Date AddHour(Date date, int hour) {
        return Add(date, Calendar.HOUR, hour);
    }

    public static Date AddMinute(Date date, int minute) {
        return Add(date, Calendar.MINUTE, minute);
    }

    public static Date AddSecond(Date date, int seconds) {
        return Add(date, Calendar.SECOND, seconds);
    }

    public static int DateDiff(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (24 * 3600 * 1000));
    }

    @SuppressWarnings("deprecation")
    public static int DayInMonths(Date date) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(date);
        int[] daysInMonths = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        daysInMonths[1] += IsLeapYear(date.getYear()) ? 1 : 0;
        return daysInMonths[c1.get(Calendar.MONTH)];
    }

    public static boolean IsLeapYear(int year) {

        if ((year % 100 != 0) || (year % 400 == 0)) {
            return true;
        }
        return false;
    }

    public static Date Parse(String dateString, String dateformat) {
        if (dateformat==null || dateformat.equals(""))
            dateformat = DATE_FORMAT;
        Date dt = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
            sdf.setLenient(false);
            dt = sdf.parse(dateString);
            return dt;
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid date");
        }
        return dt;
    }

    public static String GetDayNameofTheDate(Date d) {
        String day = null;
        DateFormat f = new SimpleDateFormat("EEEE");
        try {
            day = f.format(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return day;
    }


}
