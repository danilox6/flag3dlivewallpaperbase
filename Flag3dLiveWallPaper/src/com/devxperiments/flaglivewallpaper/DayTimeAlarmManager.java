package com.devxperiments.flaglivewallpaper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DayTimeAlarmManager extends BroadcastReceiver{

	private static PendingIntent pendingIntent = null;
	private static Calendar current, sunrise, day, sunset, night;
	private static AlarmManager alarmManager;
	private static boolean isRunning;
	private static final List<Calendar> calendars = new ArrayList<Calendar>(); 
	
	public static final int SUNRISE = 0, DAY = 1, SUNSET = 2, NIGHT = 3;

	@Override
	public void onReceive(Context context, Intent intent) {
		FlagRenderer.updateDayTimeBackground();
		Log.e("ALARM", "alarm");
		setNextAlarm();
	}
	
	public static void start(Context context){
		if(pendingIntent==null)
			inizialize(context);
		setNextAlarm();
	}

	private static void inizialize(Context context){
		isRunning = false;
		Intent intent = new Intent(context, DayTimeAlarmManager.class);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	}

	private static void setNextAlarm(){

		int nextAlarmIndex = (getCurrentCalendarIndex()+1)%5;

		long nextAlarm = calendars.get(nextAlarmIndex).getTimeInMillis();// - current.getTimeInMillis();

		Log.e("TIME", nextAlarm+"");
		
		alarmManager.set(AlarmManager.RTC, nextAlarm, pendingIntent);
		isRunning = true;
	}

	public static void stop(){
		if(alarmManager!=null)
			alarmManager.cancel(pendingIntent);
		isRunning = false;
	}

	public static boolean isRunning(){
		return isRunning;
	}
	
	private static int getCurrentCalendarIndex(){
		current = Calendar.getInstance();

		sunrise = Calendar.getInstance();
		sunrise.set(Calendar.HOUR_OF_DAY, 6);
		sunrise.set(Calendar.MINUTE, 00);

		day = Calendar.getInstance();
		day.set(Calendar.HOUR_OF_DAY, 9);
		day.set(Calendar.MINUTE, 00);

		sunset = Calendar.getInstance();
		sunset.set(Calendar.HOUR_OF_DAY, 18);
		sunset.set(Calendar.MINUTE, 00);

		night = Calendar.getInstance();
		night.set(Calendar.HOUR_OF_DAY, 20);
		night.set(Calendar.MINUTE, 0);

		calendars.clear();
		calendars.add(current);
		calendars.add(sunrise);
		calendars.add(day);
		calendars.add(sunset);
		calendars.add(night);
		
		Collections.sort(calendars);
		
		Log.i("DAYTIME",dump(calendars));
		
		for(int currentIndex = 0; currentIndex<5; currentIndex++)
			if(calendars.get(currentIndex).equals(current))
				return currentIndex;
		
		return -1;
	}
	
	public static int getAttualDayTime(){
		int currentDayTimeIndex = getCurrentCalendarIndex()-1;
		if(currentDayTimeIndex==-1)
			currentDayTimeIndex = 5;
		if(calendars.get(currentDayTimeIndex)==sunrise)
			return SUNRISE;
		if(calendars.get(currentDayTimeIndex)==day)
			return DAY;
		if(calendars.get(currentDayTimeIndex)==sunset)
			return SUNSET;
		if(calendars.get(currentDayTimeIndex)==night)
			return NIGHT;
		return -1;
	}

	public static String getAttualDayTimeString(){
		int currentDayTimeIndex = getCurrentCalendarIndex()-1;
		if(currentDayTimeIndex==-1)
			currentDayTimeIndex = 5;
		String current = null;
		if(calendars.get(currentDayTimeIndex)==sunrise)
			current = "sky_sunrise";
		else if(calendars.get(currentDayTimeIndex)==day)
			current = "sky_day";
		else if(calendars.get(currentDayTimeIndex)==sunset)
			current = "sky_sunset";
		else if(calendars.get(currentDayTimeIndex)==night)
			current = "sky_night";
		Log.i("DAYTIME", current);
		return current;
	}
	
	private static String dump(List<Calendar> calendars){
		String log = "";
		for(Calendar c: calendars){
			log += c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+"\n";
		}
		return log;
	}
	
}
