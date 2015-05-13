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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateEvent extends SimpleBaseActivity 
	implements GoogleApiClient.OnConnectionFailedListener {
	/* Place autocomplete variables. */
	protected GoogleApiClient mGoogleApiClient;
	 
	private static final LatLngBounds BOUNDS_GREATER_BUCHAREST = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
	
	private Button finish;
	private EditText locationField;
	private EditText nameField;
	private List<EditText> dates;
	private PlanEvent planEvent;
	private PlanEvent parentPlanEvent;	
	
	private int parentInt = -1;
	private int mode;
	private int ids[];
	private static final int LISTSIZE = 12;
	private static final int NAME_INDEX = 10;
	private static final int LOCATION_INDEX = 11;
	private static final String dateText[] = {"yyyy", "mm", "dd", "hh", "mm", 
		"yyyy", "mm", "dd", "hh", "mm", "Write name here", "Write location here"};
	
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
		
		AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.location);
		
		ArrayAdapter adapter = new GooglePlacesAutocompleteAdapter(this, R.layout.list_item,
							mGoogleApiClient, BOUNDS_GREATER_BUCHAREST, null);
		autoCompView.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_event, menu);
		
		finish = (Button) findViewById(R.id.finishEvent);
		dates = new ArrayList<EditText>();
		ids = new int[LISTSIZE];
		
		ids[0] = R.id.beginYear;
		ids[1] = R.id.beginMonth;
		ids[2] = R.id.beginDay;
		ids[3] = R.id.beginHour;
		ids[4] = R.id.beginMinute;

		ids[5] = R.id.endYear;
		ids[6] = R.id.endMonth;
		ids[7] = R.id.endDay;
		ids[8] = R.id.endHour;
		ids[9] = R.id.endMinute;
		
		ids[10] = R.id.eventName;
		ids[11] = R.id.location;
		
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
			if (ids[i] == id) {
				return i;
			}
		}
		return -1;
	}
	
	boolean verifyConstraints() {
		int values[] = new int[10];
		boolean fail = false;
		
		try {
			for (int i = 0; i < 10; i++) {
				values[i] = Integer.parseInt(dates.get(i).getText().toString());
				if (values[i] < 0) { 
					fail = true;
				}
				
			}
			if (values[1] > 12 || values[6] > 12 || 
				values[2] > 31 || values[6] > 31 ||
				values[3] > 24 || values[8] > 24 ||
				values[4] > 59 || values[9] > 59) {
				fail = true;
			}
			if ("".equals(dates.get(10).toString()) || "".equals(dates.get(11).toString())) {
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
		String beginDateString = String.valueOf(values[2]) + "/" +
						   String.valueOf(values[1]) + "/" + 
						   String.valueOf(values[0]) + " " + 
						   String.valueOf(values[3]) + ":" + 
						   String.valueOf(values[4]);
						   
		String endDateString = String.valueOf(values[7]) + "/" +
				   		 String.valueOf(values[6]) + "/" + 
				   		 String.valueOf(values[5]) + " " + 
				   		 String.valueOf(values[8]) + ":" + 
				   		 String.valueOf(values[9]);
		
		Date beginDate = DateFormater.formatStringToDate(beginDateString);
		Date endDate = DateFormater.formatStringToDate(endDateString);
		String locationText = dates.get(LOCATION_INDEX).getText().toString();
		String name = dates.get(NAME_INDEX).getText().toString();
		
		GooglePlace place = new GooglePlace();
		place.setName(locationText);
		
		planEvent = new PlanEvent(name, beginDate, endDate, place);
	}
	
	void populateView() {
		dates.get(11).setText(parentPlanEvent.getLocation().toString());
		dates.get(10).setText(parentPlanEvent.getName());
		Calendar cbegin = Calendar.getInstance();
		cbegin.setTime(parentPlanEvent.getBeginDate());
		Calendar cend = Calendar.getInstance();
		cend.setTime(parentPlanEvent.getEndDate());
		dates.get(0).setText(String.valueOf(cbegin.get(Calendar.YEAR)));
		dates.get(1).setText(String.valueOf(cbegin.get(Calendar.MONTH) + 1));
		dates.get(2).setText(String.valueOf(cbegin.get(Calendar.DAY_OF_MONTH)));
		dates.get(3).setText(String.valueOf(cbegin.get(Calendar.HOUR_OF_DAY)));
		dates.get(4).setText(String.valueOf(cbegin.get(Calendar.MINUTE)));
		
		dates.get(5).setText(String.valueOf(cend.get(Calendar.YEAR)));
		dates.get(6).setText(String.valueOf(cend.get(Calendar.MONTH) + 1));
		dates.get(7).setText(String.valueOf(cend.get(Calendar.DAY_OF_MONTH)));
		dates.get(8).setText(String.valueOf(cend.get(Calendar.HOUR_OF_DAY)));
		dates.get(9).setText(String.valueOf(cend.get(Calendar.MINUTE))); 
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
