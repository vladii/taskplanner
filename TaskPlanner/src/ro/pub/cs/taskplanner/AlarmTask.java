package ro.pub.cs.taskplanner;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

/* Class used for notifications system. */
public class AlarmTask implements Runnable {
		// The date selected for the alarm
		private final Calendar date;
		
		// The android system alarm manager
		private final AlarmManager am;
		
		// Your context to retrieve the alarm manager from
		private final Context context;
		
		// Event to notify.
		private final PlanEvent event;
		
		// Prev event.
		private final PlanEvent prevEvent;

		public AlarmTask(Context context, PlanEvent prevEvent, PlanEvent event, Calendar date) {
			this.context = context;
			this.prevEvent = prevEvent;
			this.event = event;
			this.am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			this.date = date;
		}
		
		@Override
		public void run() {
			// Request to start are service when the alarm date is upon us
			// We don't start an activity as we just want to pop up a notification into the system bar not a full activity
			Intent intent = new Intent(context, NotifyService.class);
			intent.putExtra(NotifyService.INTENT_NOTIFY, true);
			intent.putExtra("Event", (Parcelable) event);
			intent.putExtra("PrevEvent", (Parcelable) prevEvent);
			
			int __id = (int) System.currentTimeMillis();
			PendingIntent pendingIntent = PendingIntent.getService(context, __id, intent, 0);
			
			System.out.println("LALA: " + __id);
			
			// Sets an alarm - note this alarm will be lost if the phone is turned off and on again.
			am.set(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), pendingIntent);
		}	
}
