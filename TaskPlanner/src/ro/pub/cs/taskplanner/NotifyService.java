package ro.pub.cs.taskplanner;

import java.util.Calendar;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

/* Class used for notifications system. */
public class NotifyService extends Service {

	/**
	 * Class for clients to access
	 */
	public class ServiceBinder extends Binder {
		NotifyService getService() {
			return NotifyService.this;
		}
	}

	// Unique id to identify the notification.
	private static final int NOTIFICATION = 123;
	
	// Name of an intent extra we can use to identify if this service was started to create a notification	
	public static final String INTENT_NOTIFY = "X0X";
	
	// The system notification manager
	private NotificationManager mNM;
	
	// Events.
	private PlanEvent prevEvent = null;
	private PlanEvent event = null;

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// If this service was started by out AlarmTask intent then we want to show our notification
		if(intent.getBooleanExtra(INTENT_NOTIFY, false)) {
			this.prevEvent = (PlanEvent) intent.getParcelableExtra("PrevEvent");
			this.event = (PlanEvent) intent.getParcelableExtra("Event");
			showNotification();
		}
		
		// We don't care if this service is stopped as we have already delivered our notification
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients
	private final IBinder mBinder = new ServiceBinder();

	/**
	 * Creates a notification and shows it in the OS drag-down status bar
	 */
	private synchronized void showNotification() {
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.common_ic_googleplayservices)
		        .setContentTitle("Task Planner - Reminder")
		        .setContentText("Information about your next destination.");
		
		NotificationCompat.InboxStyle inboxStyle =
		        new NotificationCompat.InboxStyle();
		inboxStyle.setBigContentTitle("Details");
		
		// Add information about locations.
		if (prevEvent != null && prevEvent.getLocation() != null) {
			inboxStyle.addLine("Current location: " + prevEvent.getLocation().getAddress());
		}
		
		if (event != null && event.getLocation() != null) {
			inboxStyle.addLine("Next location: " + event.getLocation().getAddress());
		}
		
		if (event != null && event.getBeginDate() != null) {
			// Compute time.
			long tm = event.getBeginDate().getTime() - Calendar.getInstance().getTimeInMillis();
			Date xtm = new Date(tm);
			inboxStyle.addLine("Time until next event: " + xtm.getMinutes() + ":" + xtm.getMinutes());
			
		} else {
			// Initial event.
			mBuilder = mBuilder.setContentText("You will receive reminders before start times of your events!");
			inboxStyle.addLine("You will receive reminders before start times of your events!");
		}
		
		
		// Set style.
		mBuilder.setStyle(inboxStyle);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		// mId allows you to update the notification later on.
		int __id = (int) System.currentTimeMillis();
		mNotificationManager.notify(__id, mBuilder.build());
		
		// Stop the service when we are finished
		stopSelf();
	}
	
	private String getNotificationText() {
		String ret = "";
		
		if (prevEvent != null) {
			ret += "Locatia curenta: " + prevEvent.getLocation() + ".";
		}
		
		if (event != null) {
			ret += "Locatia urmatoare: " + event.getLocation() + ".";
		}
		
		return ret;
	}
}
