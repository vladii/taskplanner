package ro.pub.cs.taskplanner;

import java.util.Calendar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/* Class used for notification system. */
public class NotificationScheduleClient {
	// The hook into our service
	private NotificationScheduleService mBoundService;
	
	// The context to start the service in
	private Context mContext;
	
	// A flag if we are connected to the service or not
	private boolean mIsBound;
	
	// The event to be notified.
	private PlanEvent mEvent;
	
	// Previous event.
	private PlanEvent mPrevEvent;
	
	// When to schedule notification.
	private Calendar mC;
	
	/**
	 * When you attempt to connect to the service, this connection will be called with the result.
	 * If we have successfully connected we instantiate our service object so that we can call methods on it.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with our service has been established, 
			// giving us the service object we can use to interact with our service.
			mBoundService = ((NotificationScheduleService.ServiceBinder) service).getService();
		
			mBoundService.setAlarm(mPrevEvent, mEvent, mC);
		}

		public void onServiceDisconnected(ComponentName className) {
			mBoundService = null;
		}
	};

	public NotificationScheduleClient(Context context, PlanEvent prevEvent,
									PlanEvent event, Calendar c) {
		mContext = context;
		mPrevEvent = prevEvent;
		mEvent = event;
		mC = c;
	}
	
	/**
	 * Call this to connect your activity to your service
	 */
	public void doBindService() {
		// Establish a connection with our service
		mContext.bindService(new Intent(mContext, NotificationScheduleService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}
	
	/**
	 * When you have finished with the service call this method to stop it 
	 * releasing your connection and resources
	 */
	public void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			mContext.unbindService(mConnection);
			mIsBound = false;
		}
	}
}
