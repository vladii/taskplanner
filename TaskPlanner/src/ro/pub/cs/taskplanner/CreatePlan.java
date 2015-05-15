package ro.pub.cs.taskplanner;

import java.io.Serializable;
import java.util.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import ro.pub.cs.taskplanner.*;
import android.app.Activity;
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

public class CreatePlan extends SimpleBaseActivity
	implements GoogleApiClient.OnConnectionFailedListener {

	private static final long serialVersionUID = 1L;
	Button finish;
	Button createEvent;
	Button scheduleButton;
	LinearLayout eventsLayout;
	
	protected GoogleApiClient mGoogleApiClient;

	static final int EDIT_PLAN_EVENT = 0;
	static final int NEW_PLAN_EVENT = 1;
	private int idCounter = 1;
	private int mode = -1;
	private int parentInt = -1;
	private Plan parentPlan;
	private Plan plan;
	private EditText nameText;
	private static final String INITIAL_NAME = "Write name here";
	
	public List<PlanEvent> events;
	
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
			System.out.println(events);
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
	
		createEvent = (Button) findViewById(R.id.newEvent);
		finish = (Button) findViewById(R.id.finishPlan);
		scheduleButton = (Button) findViewById(R.id.scheduleButton);
		eventsLayout = (LinearLayout) findViewById(R.id.eventsLayout);
		
		events = new ArrayList<PlanEvent>();
		nameText = (EditText) findViewById(R.id.planName);
		nameText.setText(INITIAL_NAME);
		nameText.setOnFocusChangeListener(new EditTextListener());
		
		createEvent.setOnClickListener(new ButtonCreateEvent());
		finish.setOnClickListener(new ButtonFinish());
		scheduleButton.setOnClickListener(new ButtonSchedule());
	
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
	
	void addView(PlanEvent planEvent) {
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    	            LinearLayout.LayoutParams.MATCH_PARENT,
    	            LinearLayout.LayoutParams.WRAP_CONTENT);
    	Button button = new Button(this);
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
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
				+ connectionResult.getErrorCode());

		// TODO(Developer): Check error code and notify the user of error state and resolution.
		Toast.makeText(this,
				"Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
				Toast.LENGTH_SHORT).show();
		
	}
}
