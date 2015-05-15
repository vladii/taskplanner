package ro.pub.cs.taskplanner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class CreateEvent extends SimpleBaseActivity 
	implements GoogleApiClient.OnConnectionFailedListener {
	
	/* Place autocomplete variables. */
	protected GoogleApiClient mGoogleApiClient;
	 
	private static final LatLngBounds BOUNDS_GREATER_BUCHAREST = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
	
	AutoCompleteTextView autoCompView;
	private Button finish;
	private Button remove;
	private List<EditText> dates;
	private CheckBox locationCheckbox;
	private CheckBox dateCheckbox;
	private PlanEvent planEvent;
	private PlanEvent parentPlanEvent;	
	private GooglePlace placeSelected = null;
	
	private int parentInt = -1;
	private int mode;
	private boolean removeEvent;
	
	private static final int LISTSIZE = 6;
	private static final int INTEGERS = 4;
	private static final int NAME_INDEX = 4;
	private static final String dateText[] = { "hh", "mm", "hh", "mm", "Write name here", "Write location here"};
	private static final String DEFAULT_DATE = "1/1/1991 11:11";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_event);
		
		/* Create Google autocomplete. */
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this, 0 /* clientId */, this)
				.addApi(Places.GEO_DATA_API)
				.addApi(Places.PLACE_DETECTION_API)
				.build();
		
		autoCompView = (AutoCompleteTextView) findViewById(R.id.location);
		
		ArrayAdapter<GooglePlace> adapter =
					new GooglePlacesAutocompleteAdapter(this, R.layout.list_item,
					mGoogleApiClient, BOUNDS_GREATER_BUCHAREST, null);
		
		autoCompView.setAdapter(adapter);
		
		autoCompView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				placeSelected = (GooglePlace) parent.getItemAtPosition(position);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_event, menu);
		
		finish = (Button) findViewById(R.id.finishEvent);
		remove = (Button) findViewById(R.id.removeEvent);
		finish.setOnClickListener(new ButtonFinish());
		remove.setOnClickListener(new ButtonFinish());
		
		dates = new ArrayList<EditText>();
		int ids[] = {R.id.beginHour, R.id.beginMinute, R.id.hoursDuration,
				R.id.minutesDuration, R.id.eventName, R.id.location};
				
		for (int i = 0; i < LISTSIZE; i ++) {
			EditText et = (EditText) findViewById(ids[i]);
			et.setText(dateText[i]);
			et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			et.setOnFocusChangeListener(new EditTextListener());
			et.setTypeface(null, Typeface.ITALIC);
			dates.add(et);
		}
		locationCheckbox = (CheckBox) findViewById(R.id.checkBoxLocation);
		dateCheckbox = (CheckBox) findViewById(R.id.checkBoxDate);
		
		Intent intent = getIntent();
		
		if (intent != null) {
			mode = intent.getIntExtra("MODE", -1);
			if (mode == 1) {
				remove.setText("Remove event");
				parentInt = intent.getIntExtra("EDIT_EVENT_INDEX", -1);
				parentPlanEvent = (PlanEvent) intent.getParcelableExtra("EDIT_EVENT");
				populateView();
			}
		} 
			
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
	
	private class ButtonFinish implements Button.OnClickListener {		
		@Override
		public void onClick(View v) {
			int result = 1;
			if (v.getId() == remove.getId()) {
				removeAlertDialog();
				return;
			}
			if (verifyConstraints()) {
				Intent resultIntent = new Intent();
				if (mode == 1) {
					if (parentInt != -1 && (!planEvent.toString().equals(parentPlanEvent.toString()))) {
						resultIntent.putExtra("EDIT_EVENT_INDEX", parentInt);
						resultIntent.putExtra("EDIT_EVENT", planEvent);
					} else {
						result = 0;
					}
				}
				
				if (mode == 0) {
					resultIntent.putExtra("PLAN_EVENT", planEvent);
				}
				
				setResult(result, resultIntent);
				finish();
			}
		}
	}
	
	void removeAlertDialog() {
		new AlertDialog.Builder(this)
		.setTitle("Remove event")
		.setMessage("Are you sure you want to remove the event ?")
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
				Intent resultIntent = new Intent();
				resultIntent.putExtra("EDIT_EVENT_INDEX", parentInt);
				setResult(2, resultIntent); // remove event
				finish();
			}
		})
		.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
			}
		})
		.setIcon(android.R.drawable.ic_dialog_alert)
		.show();
	}
	
	private class EditTextListener implements OnFocusChangeListener {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			int id = v.getId();
			int index = findId(id);
			EditText et = dates.get(index);
			
			if (hasFocus) {
				if (!textChanged(et.getText().toString())) {
					et.setText("");
					et.setTypeface(null, Typeface.NORMAL);
				}
			}
			
			if (!hasFocus) {
				if ("".equals(et.getText().toString())) {
					et.setText(dateText[index]);
					et.setTypeface(null, Typeface.ITALIC);
				}
			}
		}
	}
	
	private boolean textChanged(String text) {
		for (int i = 0; i < LISTSIZE; i++) {
			if (dateText[i].equals(text)) {
				return false;
			}
		}
		
		return true;
	}
	
	private int findId(int id) {
		for (int i = 0; i < LISTSIZE; i++) {
			if (dates.get(i).getId() == id) {
				return i;
			}
		}
		return -1;
	}
	
	boolean verifyConstraints() {
		int values[] = new int[10];
		boolean fail = false;
		int rangeStart = 0, rangeEnd = INTEGERS;
		if (!dateCheckbox.isChecked()) {
			rangeStart = 2;
		}
		try {
			for (int i = rangeStart; i < rangeEnd; i += 2) {
				values[i] = Integer.parseInt(dates.get(i).getText().toString());
				values[i + 1] = Integer.parseInt(dates.get(i + 1)
						.getText().toString());
				
				if (values[i] < 0 || values[i] > 24 || values[i + 1] < 0 
						|| values[i + 1] > 59) { 
					fail = true;
				}	
			}
		} catch (Exception e) {
			fail = true;
		}
		if (fail) {
			new AlertDialog.Builder(this)
	    		.setTitle("Format error")
	    		.setMessage("One of the fields are not properly completed!")
	    		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int which) { 
	    			}
	    		})
	    		.setIcon(android.R.drawable.ic_dialog_alert)
	    		.show();
			return false;
		} else {
			createEventObject(values);
		}
		return true;
	}
	
	void createEventObject(int values[]) {
		String durationString = String.valueOf(1) + "/" +
			   	 String.valueOf(1) + "/" + 
			   	 String.valueOf(2015) + " " + 
			   	 String.valueOf(values[2]) + ":" + 
			     String.valueOf(values[3]);

		String beginDateString = DEFAULT_DATE;				   
		if (dateCheckbox.isChecked()) {
			beginDateString = String.valueOf(1) + "/" +
				   		 String.valueOf(1) + "/" + 
				   		 String.valueOf(2015) + " " + 
				   		 String.valueOf(values[0]) + ":" + 
				   		 String.valueOf(values[1]);
		}
		
		Date beginDate = DateFormater.formatStringToDate(beginDateString);
		Date endDate = DateFormater.formatStringToDate(durationString);
		String name = dates.get(NAME_INDEX).getText().toString();
		GooglePlace place;
		
		if (placeSelected != null) {
			place = placeSelected;
		} else {
			place = new GooglePlace();
			place.setName(autoCompView.getText().toString());
		}
		
		// Create new event.
		planEvent = new PlanEvent(name, beginDate, endDate, place);
		
		// Fill exactLocation and exactBeginDate fields.
		if (locationCheckbox.isChecked()) {
			planEvent.setExactLocation(1);
		} else {
			planEvent.setExactLocation(0);
		}
		
		if (dateCheckbox.isChecked()) {
			planEvent.setExactBeginDate(1);
		} else {
			planEvent.setExactBeginDate(0);
		}
		System.out.println(planEvent.toString());
	}
	
	void populateView() {
		System.out.println(parentPlanEvent.toString());
		dates.get(NAME_INDEX).setText(parentPlanEvent.getName());
	
		Calendar cbegin = Calendar.getInstance();
		cbegin.setTime(parentPlanEvent.getBeginDate());
		Calendar cend = Calendar.getInstance();
		cend.setTime(parentPlanEvent.getEndDate());
		
		placeSelected = parentPlanEvent.getLocation();
		System.out.println(placeSelected.toString() + " location ");
		try {
			autoCompView.setText(placeSelected.getAddress().toString());
		} catch (Exception e) {
			autoCompView.setText("Write location here");
		}
		if (parentPlanEvent.getExactBeginDate() == 1) { 
			dates.get(0).setText(String.valueOf(cbegin.get(Calendar.HOUR_OF_DAY)));
			dates.get(1).setText(String.valueOf(cbegin.get(Calendar.MINUTE)));
		}
		
		dates.get(2).setText(String.valueOf(cend.get(Calendar.HOUR_OF_DAY)));
		dates.get(3).setText(String.valueOf(cend.get(Calendar.MINUTE))); 
		
		locationCheckbox.setChecked(parentPlanEvent.getExactLocation() == 1);
		dateCheckbox.setChecked(parentPlanEvent.getExactBeginDate() == 1);
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
