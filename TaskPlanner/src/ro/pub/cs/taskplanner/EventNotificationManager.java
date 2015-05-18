package ro.pub.cs.taskplanner;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/* Class for setting a notification based on a PlanEvent object.
 * It supposes that PlanEvent contains a VALID START TIME and EXACT LOCATION!
 */
public class EventNotificationManager {
	private PlanEvent event;
	private PlanEvent prevEvent;
	private Context context;
	private NotificationScheduleClient scheduleClient = null;
	
	public EventNotificationManager(Context context, PlanEvent prevEvent,
									PlanEvent event) {
		this.context = context;
		this.prevEvent = prevEvent;
		this.event = event;
		
		this.scheduleClient = new NotificationScheduleClient(context, prevEvent, event, getWhen());
	}
	
	public void schedule() {
		if (scheduleClient != null)
			scheduleClient.doBindService();
	}
	
	public void stop() {
		if(scheduleClient != null)
    		scheduleClient.doUnbindService();
	}
	
	/* Function which decides when to wake up the notification. */
	private Calendar getWhen() {
		if (event == null) {
			Calendar calendarNow = Calendar.getInstance();
			calendarNow.add(Calendar.SECOND, 7);
			
			return calendarNow;
		}
		
		Calendar calendarToday = Calendar.getInstance();
		Calendar calendarEvent = Calendar.getInstance();
		
		calendarEvent.set(Calendar.HOUR_OF_DAY, event.getBeginDate().getHours());
		calendarEvent.set(Calendar.MINUTE, event.getBeginDate().getMinutes());
		calendarEvent.set(Calendar.SECOND, event.getBeginDate().getSeconds());
		
		// Subtract 30 minutes.
		long timeEvent = calendarEvent.getTimeInMillis() - 30 * 60 * 1000;
		calendarEvent.setTimeInMillis(timeEvent);
		
		System.out.println("Today: " + calendarToday);
		System.out.println("Event: " + calendarEvent);
		
		if (calendarEvent.before(calendarToday)) {
			calendarEvent = calendarToday;
		}
		
		// Add 10 seconds for safety.
		calendarEvent.add(Calendar.SECOND, 10);
		
		return calendarEvent;
	}
}
