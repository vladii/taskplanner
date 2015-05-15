package ro.pub.cs.taskplanner;

import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/* Class used for notifications system. */
public class NotificationScheduleService extends Service {

	/**
	 * Class for clients to access
	 */
	public class ServiceBinder extends Binder {
		NotificationScheduleService getService() {
			return NotificationScheduleService.this;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("ScheduleService", "Received start id " + startId + ": " + intent);
		
		// We want this service to continue running until it is explicitly stopped, so return sticky.
		return START_STICKY;
	}

	// This is the object that receives interactions from clients. See
	private final IBinder mBinder = new ServiceBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/**
	 * Show an alarm for a certain date when the alarm is called it will pop up a notification
	 */
	public void setAlarm(PlanEvent prevEvent, PlanEvent event, Calendar c) {
		// This starts a new thread to set the alarm
		// You want to push off your tasks onto a new thread to free up the UI to carry on responding
		new AlarmTask(this, prevEvent, event, c).run();
	}
}
