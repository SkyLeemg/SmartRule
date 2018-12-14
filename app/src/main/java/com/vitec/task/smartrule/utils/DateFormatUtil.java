package com.vitec.task.smartrule.utils;

import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatUtil {



    /**
     * 获取当前日期
     * @return
     */
    public static String getDate(String format){
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        String  str=sdf.format(new Date());
        return str;
    }

    public static int getDate() {
        String date = getDate("yyyy-MM-dd");
        int create_date = transForMilliSecondByTim(date, "yyyy-MM-dd");
        return create_date;
    }

    public static String stampToDateString(int time) {
        String date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        long lt = (long) time * 1000;
        Date date1 = new Date(lt);
        date = simpleDateFormat.format(date1);
        return date;
    }

    public static String stampToDateString(int time,String format) {
        String date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        long lt = (long) time * 1000;
        Date date1 = new Date(lt);
        date = simpleDateFormat.format(date1);
        return date;
    }


    /**
     * 获取晚上9点半的时间戳
     *
     * @return
     */
    public static int getTimes(int day, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) (cal.getTimeInMillis() / 1000);
    }

    /**
     * 获取当前时间往上的整点时间
     *
     * @return
     */
    public static int getIntegralTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) (cal.getTimeInMillis() / 1000);
    }

    public static int getIntegralTimeEnd() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) (cal.getTimeInMillis() / 1000);
    }


    /**
     * 日期转时间戳
     * @param date
     * @return
     */
    public static Integer transForMilliSecond(Date date){
        if(date==null) return null;
        return (int)(date.getTime()/1000);
    }

    /**
     * 获取当前时间戳
     * @return
     */
    public static Integer currentTimeStamp(){
        return (int)(System.currentTimeMillis());
    }

    /**
     * 日期字符串转时间戳
     * @param dateStr
     * @return
     */
    public static Integer transForMilliSecond(String dateStr){
        Date date = DateFormatUtil.formatDate(dateStr);
        return date == null ? null : DateFormatUtil.transForMilliSecond(date);
    }
    /**
     * 日期字符串转时间戳
     * @param dateStr
     * @return
     */
    public static Integer transForMilliSecond(String dateStr,String format){
        Date date = DateFormatUtil.formatDate(dateStr,format);
        return date == null ? null : DateFormatUtil.transForMilliSecond(date);
    }
    /**
     * 日期字符串转时间戳
     * @param dateStr
     * @param tim 如"yyyy-mm-dd"
     * @return
     */
    public static Integer transForMilliSecondByTim(String dateStr,String tim){
        SimpleDateFormat sdf=new SimpleDateFormat(tim);
        Date date =null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date == null ? null : DateFormatUtil.transForMilliSecond(date);
    }
    /**
     * 字符串转日期，格式为："yyyy-MM-dd HH:mm:ss"
     * @param dateStr
     * @return
     */
    public static Date formatDate(String dateStr){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date result=null;
        try {
            result = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * 字符串转日期，格式为："yyyy-MM-dd HH:mm:ss"
     * @param dateStr
     * @return
     */
    public static Date formatDate(String dateStr,String format){
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        Date result=null;
        try {
            result = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * 日期转字符串
     * @param date
     * @return
     */
    public static String formatDate(Date date){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String result=null;
        result = sdf.format(date);
        return result;
    }
    /**
     * 日期转字符串
     * @param date
     * @return
     */
    public static String formatDate(Date date,String format){
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        String result=null;
        result = sdf.format(date);
        return result;
    }

}
