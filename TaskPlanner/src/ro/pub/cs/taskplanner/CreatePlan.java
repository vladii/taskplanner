package ro.pub.cs.taskplanner;

import java.io.Serializable;
import java.util.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import ro.pub.cs.taskplanner.*;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

public class CreatePlan extends SimpleBaseActivity
	implements GoogleApiClient.OnConnectionFailedListener {

	public List<PlanEvent> events;
	public List<EventNotificationManager> notifications;
	
	protected GoogleApiClient mGoogleApiClient;

	private static final long serialVersionUID = 1L;
	
	private static final int EDIT_PLAN_EVENT = 0;
	private static final int NEW_PLAN_EVENT = 1;
	private static final int SCHEDULE_VIEW_EVENT = 1001;
	private static final String INITIAL_NAME = "Write name here";
	
	private EditText nameText;
	private Button finish;
	private Button createEvent;
	private Button scheduleButton;
	private Button notifyButton;
	private Button viewSchedule;
	private LinearLayout eventsLayout;
	
	private int idCounter = 1;
	private int mode = -1;
	private int parentInt = -1;
	private Plan parentPlan;
	private Plan plan;
	
	private class EditTextListener implements OnFocusChangeListener  {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {	
			if (hasFocus) {
				if (INITIAL_NAME.equals(nameText.getText().toString())) {
					nameText.setText("");
					nameText.setTypeface(null, Typeface.NORMAL);
				}
			}
			if (!hasFocus) {
				if ("".equals(nameText.getText().toString())) {
					nameText.setText(INITIAL_NAME);
					nameText.setTypeface(null, Typeface.ITALIC);
				}
			}
		}
	}

	private class ButtonSchedule implements Button.OnClickListener {

		@Override
		public void onClick(View v) {
			ScheduleAlgorithm schedule = new ScheduleAlgorithm(events);
			events = schedule.schedulePlan();
			Toast.makeText(getApplicationContext(), "Scheduling finished!",
					Toast.LENGTH_SHORT).show();
			populateView();
		}
	}
	
	private class ButtonViewSchedule implements Button.OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent("ro.pub.cs.taskplanner.ViewSchedule");
			createPlanObject();
			if (plan == null) {
				System.out.println("plan still null");
			}
			System.out.println(plan.toString());
			intent.putExtra("SCHEDULE_VIEW", (Parcelable)plan);
			startActivityForResult(intent, SCHEDULE_VIEW_EVENT);		
		}
	}

	
	private class ButtonFinish implements Button.OnClickListener {
		@Override
		public void onClick(View v) {
			int result = 1;
			Intent resultIntent = new Intent();
			createPlanObject();
			if (mode == 1) {
				if (parentInt != -1 && (!plan.toString().equals(parentPlan.toString()))) {
					resultIntent.putExtra("EDIT_PLAN_INDEX", parentInt);
					resultIntent.putExtra("EDIT_PLAN", (Parcelable)plan);
					System.out.println("CREATE PLAN return edited plan : " + plan.toString());
				} else {
					result = 0;
				}
			}

			if (mode == 0) {
				resultIntent.putExtra("CREATE_PLAN", (Parcelable)plan);
				System.out.println("CREATE PLAN return new plan : " + plan.toString());
			}
			setResult(result, resultIntent);
			finish();	
		}
	}

	private class ButtonCreateEvent implements Button.OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent("ro.pub.cs.taskplanner.CreateEvent");
			intent.putExtra("MODE", 0);
			startActivityForResult(intent, NEW_PLAN_EVENT);		
		}
	}
	
	private class ButtonNotify implements Button.OnClickListener {
		@Override
		public void onClick(View v) {
			/* Do a start notification. */
			EventNotificationManager notificationManagerStart =
					new EventNotificationManager(getApplicationContext(), null, null);
			notificationManagerStart.schedule();
			
			/* Start notifications for all events. */
			PlanEvent prevEvent = null;
			for (PlanEvent event : events) {
				EventNotificationManager notificationManager =
						new EventNotificationManager(getApplicationContext(), prevEvent, event);

				notificationManager.schedule();
				
				notifications.add(notificationManager);
				
				prevEvent = event;
			}
			
			/* Show a message :-). */
			Toast.makeText(getApplicationContext(), "You will receive notifications before each event!",
							Toast.LENGTH_SHORT).show();
		}
	}
	
	private class EditEventListener implements Button.OnClickListener {
		@Override
		public void onClick(View v) {
			int index = -1;
			for (int i = 0; i < events.size(); i++) {
				if (v.getId() == eventsLayout.getChildAt(i).getId()) {
					index = i;
				}
			}
			if (index == -1) { return ;}
			Intent intent = new Intent("ro.pub.cs.taskplanner.CreateEvent");
			intent.putExtra("MODE", 1);
			intent.putExtra("EDIT_EVENT_INDEX", index);
			intent.putExtra("EDIT_EVENT", (Parcelable)events.get(index));
			startActivityForResult(intent, EDIT_PLAN_EVENT);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_plan);
		
		mGoogleApiClient = new GoogleApiClient.Builder(this)
					.enableAutoManage(this, 0 /* clientId */, this)
					.addApi(Places.GEO_DATA_API)
					.addApi(Places.PLACE_DETECTION_API)
					.build();
	
		notifications = new ArrayList<EventNotificationManager>();
		
		createEvent = (Button) findViewById(R.id.newEvent);
		finish = (Button) findViewById(R.id.finishPlan);
		scheduleButton = (Button) findViewById(R.id.scheduleButton);
		notifyButton = (Button) findViewById(R.id.notificationButton);
		eventsLayout = (LinearLayout) findViewById(R.id.eventsLayout);
		viewSchedule = (Button) findViewById(R.id.viewSchedule);
		
		events = new ArrayList<PlanEvent>();
		nameText = (EditText) findViewById(R.id.planName);
		nameText.setText(INITIAL_NAME);
		nameText.setOnFocusChangeListener(new EditTextListener());
		
		createEvent.setOnClickListener(new ButtonCreateEvent());
		finish.setOnClickListener(new ButtonFinish());
		scheduleButton.setOnClickListener(new ButtonSchedule());
		notifyButton.setOnClickListener(new ButtonNotify());
		viewSchedule.setOnClickListener(new ButtonViewSchedule());
		
		Intent intent = getIntent();
		if (intent != null) {
			mode = intent.getIntExtra("MODE", -1);
			if (mode == 1) { // edit plan
				parentInt = intent.getIntExtra("EDIT_PLAN_INDEX", -1);
				parentPlan = (Plan) intent.getParcelableExtra("EDIT_PLAN");
				events.addAll(parentPlan.getPlansEvents());
				System.out.println("CREATE PLAN received plan to edit : " + parentPlan.toString());
				populateView();
			} else { // create plan
				/* Add the current location in the events list. */
				GoogleCurrentLocation currLocEvent = new GoogleCurrentLocation(mGoogleApiClient);
				currLocEvent.setCurrentLocation(this);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_plan, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if (resultCode == 0) {
			return;
		}
		
		if (requestCode == NEW_PLAN_EVENT) {
	    	if (resultCode == 2) { // cancel
	    		return;
	    	}
			PlanEvent planEvent = (PlanEvent) data.getParcelableExtra("PLAN_EVENT");
	    	events.add(planEvent);
	    	addView(planEvent);
	      
	    } else if (requestCode == EDIT_PLAN_EVENT) {
	    	int index = data.getIntExtra("EDIT_EVENT_INDEX", -1);
	    	if (index == -1) {
	    		return;
	    	}
	    	eventsLayout.removeViewAt(index);
	    	events.remove(index);
	    	if (resultCode == 1) { // edit event
	    		PlanEvent planEvent = (PlanEvent) data.getParcelableExtra("EDIT_EVENT");
	    		events.add(planEvent);
	    		addView(planEvent);
	    	}
	    }
	}
	
	public Drawable scaleImage (Drawable image, float scaleFactor) {

	    if ((image == null) || !(image instanceof BitmapDrawable)) {
	        return image;
	    }

	    Bitmap b = ((BitmapDrawable)image).getBitmap();

	    int sizeX = Math.round(image.getIntrinsicWidth() * scaleFactor);
	    int sizeY = Math.round(image.getIntrinsicHeight() * scaleFactor);

	    Bitmap bitmapResized = Bitmap.createScaledBitmap(b, sizeX, sizeY, false);

	    image = new BitmapDrawable(getResources(), bitmapResized);

	    return image;

	}
	
	void addView(PlanEvent planEvent) {
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    	            LinearLayout.LayoutParams.MATCH_PARENT,
    	            LinearLayout.LayoutParams.WRAP_CONTENT);
    	Button button = new Button(this);
    	
    	Drawable icon = getBaseContext().getResources().getDrawable( R.drawable.bullet);
    	button.setCompoundDrawablesWithIntrinsicBounds(this.scaleImage(icon, (float) 0.1), null, null, null );
    	
    	button.setText(planEvent.getName());
    	button.setId(idCounter ++);
    	eventsLayout.addView(button , params);
    	button.setOnClickListener(new EditEventListener());
	}
	
	void populateView() {
		if (parentPlan != null) {
			nameText.setText(parentPlan.getName());
		}
		
    	eventsLayout.removeAllViews();
		for (PlanEvent event : events) {
			addView(event);
		}
    	
	}
	
	private void createPlanObject() {
		String name = nameText.getText().toString();
		int id = 1;
		if (mode == 1) {
			// todo : remove later
			id = parentPlan.getId();
		}
		plan = new Plan(name, id, events);
	}
	
	 @Override
	 protected void onStop() {
		 // When our activity is stopped ensure we also stop the connection to the service
		 // this stops us leaking our activity into the system *bad*
	    for (EventNotificationManager notification : notifications) {
	    	if (notification != null)
	    		notification.stop();
	    }
	    
	    super.onStop();
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
				+ connectionResult.getErrorCode());

		// TODO(Developer): Check error code and notify the user of error state and resolution.
		Toast.makeText(this,
				"Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
				Toast.LENGTH_SHORT).show();
		
	}
}
