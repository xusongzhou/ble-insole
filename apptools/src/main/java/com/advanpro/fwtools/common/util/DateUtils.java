package com.advanpro.fwtools.common.util;


import com.advanpro.fwtools.entity.ByteDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

	public static final int SECONDS_IN_DAY = 60 * 60 * 24;
	public static final long MILLIS_IN_DAY = 1000L * SECONDS_IN_DAY;
	
	public enum Week {
		Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday,
		星期天, 星期一, 星期二, 星期三, 星期四, 星期五, 星期六
	}
	
	public enum WeekFormat {
		EN, CN
	}

	/**
	 * 判断是否同一天
	 */
	public static boolean isSameDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isSameDay(cal1, cal2);
	}

	/**
	 * 判断是否同一天
	 */
	public static boolean isSameDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
				cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
				cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}
	
	/**
	 * 获取基于当前日期偏移后的日期
	 * @param date 当前日期
	 * @param offset 偏移的天数，负数向前偏移，正数向后偏移
	 */
	public static Date getDay(Date date, int offset) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, offset);
		return calendar.getTime();
	}
	
	/**
	 * 获取前一天日期
	 */
	public static Date getPreviousDay(Date date) {
		return getDay(date, -1);
	}
	
	/**
	 * 获取后一天日期
	 */
	public static Date getNextDay(Date date) {
		return getDay(date, 1);
	}

	/**
	 * 返回指定格式日期
	 */
	public static String formatDate(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}
	
	/**
	 * 获取当前日期对应是星期几
	 * @param date 当前日期
	 * @return 与Calendar的星期字段相同，1表示星期天，以此类推
	 */
	public static int getDayOfWeek(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_WEEK);
	}
	
	public static String getDayOfWeek(Date date, WeekFormat format) {
		int num = getDayOfWeek(date) - 1;
		if (format == WeekFormat.CN) {
			num += 7;
		} 
		return Week.values()[num].toString();
	}

	/**
	 * 返回英文格式字符串
	 */
	public static String getDayOfWeekEn(Date date) {
		return getDayOfWeek(date, WeekFormat.EN);
	}

	/**
	 * 返回中格式字符串
	 */
	public static String getDayOfWeekCn(Date date) {
		return getDayOfWeek(date, WeekFormat.CN);
	}

	/**
	 * 获取指定日期的当天毫秒数
	 */
	public static long getMillisInDay(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.HOUR_OF_DAY) * 3600000 + c.get(Calendar.MINUTE) * 60000 +
				c.get(Calendar.SECOND) * 1000 + c.get(Calendar.MILLISECOND);
	}

	/**
	 * 将字符串日期解析成Date对象
	 */
	public static Date parseStringDate(String date, String pattern) {
		try {
			return new SimpleDateFormat(pattern).parse(date);
		} catch (ParseException e) {
			throw new IllegalArgumentException("日期格式不对");
		}
	}

	/**
	 * 两个日期相差的天数(B-A)
	 */
	public static int daysBetween(Date dateA, Date dateB) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateA);
		int day1 = calendar.get(Calendar.DAY_OF_YEAR);
		calendar.setTime(dateB);
		int day2 = calendar.get(Calendar.DAY_OF_YEAR);
		return day2 - day1;

	}

	/**
	 * 将此日期时间设置成0点整
	 */
	public static Calendar getStartOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}

	/**
	 * 将时间转换成字节型
	 */
	public static ByteDate getByteDate(Calendar c) {
		if (c == null) c = Calendar.getInstance();
		ByteDate date = new ByteDate();
		date.year = (byte) (c.get(Calendar.YEAR) % 100);
		date.month = (byte) (c.get(Calendar.MONTH) + 1);
		date.date = (byte) c.get(Calendar.DATE);
		date.hour = (byte) c.get(Calendar.HOUR_OF_DAY);
		date.minute = (byte) c.get(Calendar.MINUTE);
		date.second = (byte) c.get(Calendar.SECOND);
		return date;
	}


	/**
	 * 获取每月的总天数
	 * @param year  年份
	 * @param month  月份
	 * */
	public static int getEveryMonthCountTime(String year,String month){
		Calendar calendar=Calendar.getInstance();
		calendar.set(Calendar.YEAR,Integer.parseInt(year));
		calendar.set(Calendar.MONTH,Integer.parseInt(month)-1);
		calendar.set(Calendar.DATE, 1);
		calendar.roll(Calendar.DATE,-1);
		return calendar.get(Calendar.DATE);
	}


	/**
	 * 获取月份第一天
	 * @param year 年份
	 * @param month 月份
	 * @return
	 * */
	public static Date getEveryMonthFistDay(String year,String month){
		Calendar calendar=Calendar.getInstance();
		try{
		calendar.set(Calendar.YEAR,Integer.parseInt(year));
		calendar.set(Calendar.MONTH,Integer.parseInt(month)-1);
		calendar.set(Calendar.DAY_OF_MONTH,1);
		}catch (NumberFormatException e){
			throw new IllegalArgumentException("日期格式不正确！");
		}
		return calendar.getTime();
	}


	/**
	 * 获取月份最后一天
	 * @param year 年份
	 * @param month 月份
	 * @return
	 * */
	public static Date getEveryMonthLastDay(String year,String month){
		Calendar calendar=Calendar.getInstance();
		try{
			calendar.set(Calendar.YEAR,Integer.parseInt(year));
			calendar.set(Calendar.MONTH,Integer.parseInt(month)-1);
			calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		}catch (NumberFormatException e){
			throw new IllegalArgumentException("日期格式不正确！");
		}
		return calendar.getTime();
	}


    /**
     * 获取指定日期所在星期的第一天日期
     * @param date 日期
     * @return 第一天日期
     */
    public static Date getFirstDayOfWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, 1);
        return c.getTime();
    }

	public static Date getLastDayOfWeek(Date date){
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_WEEK,-1);
		return calendar.getTime();
	}

	/**
	 * 初始化
	 * */
	public static Date initDate(){
		Calendar calendar=Calendar.getInstance();
		calendar.set(Calendar.YEAR,Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date())));
		calendar.set(Calendar.MONTH,0);
		calendar.set(Calendar.DATE,1);
		return calendar.getTime();
	}

    /**
     * 两个日期所在的周相差多少周
     */
    public static int weeksBetween(Date dateA, Date dateB) {
        return daysBetween(getFirstDayOfWeek(dateA), getFirstDayOfWeek(dateB)) / 7;
    }
}
