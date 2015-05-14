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
	
	private Button finish;
	private EditText nameField;
	AutoCompleteTextView autoCompView;
	private List<EditText> dates;
	private PlanEvent planEvent;
	private PlanEvent parentPlanEvent;	
	private GooglePlace placeSelected = null;
	
	private int parentInt = -1;
	private int mode;
	private static final int LISTSIZE = 5;
	private static final int INTEGERS = 4;
	private static final int NAME_INDEX = 4;
	private static final String dateText[] = { "hh", "mm", "hh", "mm", "Write name here", "Write location here"};
	
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
		dates = new ArrayList<EditText>();
		int ids[] = {R.id.beginHour, R.id.beginMinute, R.id.hoursDuration,
				R.id.minutesDuration, R.id.eventName};
				
		for (int i = 0; i < LISTSIZE; i ++) {
			EditText et = (EditText) findViewById(ids[i]);
			et.setText(dateText[i]);
			et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			et.setOnFocusChangeListener(new EditTextListener());
			et.setTypeface(null, Typeface.ITALIC);
			dates.add(et);
		}
		
		Intent intent = getIntent();
		
		if (intent != null) {
			mode = intent.getIntExtra("MODE", -1);
			if (mode == 1) {
				parentInt = intent.getIntExtra("EDIT_EVENT_INDEX", -1);
				parentPlanEvent = (PlanEvent) intent.getParcelableExtra("EDIT_EVENT");
				populateView();
			}
		} 
		
		finish.setOnClickListener(new ButtonFinish());
			
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
		
		try {
			for (int i = 0; i < INTEGERS; i++) {
				values[i] = Integer.parseInt(dates.get(i).getText().toString());
				if (values[i] < 0) { 
					fail = true;
				}
				
			}
			if (values[0] > 24 || values[2] > 24 ||
				values[1] > 59 || values[3] > 59) {
				fail = true;
			}
			if ("".equals(dates.get(NAME_INDEX).toString())) {
				fail = true;
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
		String beginDateString = String.valueOf(1) + "/" +
						   String.valueOf(1) + "/" + 
						   String.valueOf(2015) + " " + 
						   String.valueOf(values[0]) + ":" + 
						   String.valueOf(values[1]);
						   
		String endDateString = String.valueOf(1) + "/" +
				   		 String.valueOf(1) + "/" + 
				   		 String.valueOf(2015) + " " + 
				   		 String.valueOf(values[2]) + ":" + 
				   		 String.valueOf(values[3]);
		
		Date beginDate = DateFormater.formatStringToDate(beginDateString);
		Date endDate = DateFormater.formatStringToDate(endDateString);
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
		CheckBox locationCheckbox = (CheckBox) findViewById(R.id.checkBoxLocation);
		if (locationCheckbox.isChecked()) {
			planEvent.setExactLocation(1);
		} else {
			planEvent.setExactLocation(0);
		}
		
		CheckBox dateCheckbox = (CheckBox) findViewById(R.id.checkBoxDate);
		if (dateCheckbox.isChecked()) {
			planEvent.setExactBeginDate(1);
		} else {
			planEvent.setExactBeginDate(0);
		}
	}
	
	void populateView() {
		dates.get(NAME_INDEX).setText(parentPlanEvent.getName());
	
		Calendar cbegin = Calendar.getInstance();
		cbegin.setTime(parentPlanEvent.getBeginDate());
		Calendar cend = Calendar.getInstance();
		cend.setTime(parentPlanEvent.getEndDate());
		
		placeSelected = parentPlanEvent.getLocation();
		autoCompView.setText(placeSelected.toString());

		dates.get(0).setText(String.valueOf(cbegin.get(Calendar.HOUR_OF_DAY)));
		dates.get(1).setText(String.valueOf(cbegin.get(Calendar.MINUTE)));
		
		dates.get(2).setText(String.valueOf(cend.get(Calendar.HOUR_OF_DAY)));
		dates.get(3).setText(String.valueOf(cend.get(Calendar.MINUTE))); 
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
