package com.example.gogutest;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.android.gms.common.api.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

public class GoguMainActivity extends SimpleBaseActivity 
	implements GoogleApiClient.OnConnectionFailedListener{
	
	/* Place picker variables. */
	public int PLACE_PICKER_REQUEST = 1;
	public PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
	
	/* Place autocomplete variables. */
	protected GoogleApiClient mGoogleApiClient;
	 
	private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gogu_main);
		
		/* Google main API. */
		mGoogleApiClient = new GoogleApiClient.Builder(this)
			.enableAutoManage(this, 0 /* clientId */, this)
			.addApi(Places.GEO_DATA_API)
			.addApi(Places.PLACE_DETECTION_API)
			.build();
		
		double lat = 44.433159;
		double lng = 26.0368336;
		
		/* Google distance matrix. */
		List<GooglePlace> places = new ArrayList<GooglePlace>();
		
		GooglePlace place1 = new GooglePlace();
		place1.setCoords(new LatLng(lat, lng));
		places.add(place1);
		
		GooglePlace place2 = new GooglePlace();
		place2.setCoords(new LatLng(lat + 0.5, lng));
		places.add(place2);
		
		GooglePlace place3 = new GooglePlace();
		place3.setCoords(new LatLng(lat, lng + 0.5));
		places.add(place3);
		
		GoogleDistanceMatrix distanceMatrix =
				new GoogleDistanceMatrix(places);
		
		Log.d("TaskPlanner", (distanceMatrix.getDistanceMatrix()).toString());
		
		/* Google nearby locations. */
		GoogleNearbyLocations nearbyLoc =
				new GoogleNearbyLocations("banca transilvania", lat, lng);
		
		// Log.d("TaskPlanner", (nearbyLoc.getNearbyLocations()).toString());
		
		/* Google Current Location. */
		PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
			    .getCurrentPlace(mGoogleApiClient, null);
		result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
			@Override
			public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
				float maxLike = (float) -1.0;
				GooglePlace place = new GooglePlace();
				
				String currLocationName = "";
				LatLng currLocationCoord = null;

				for (PlaceLikelihood placeLikelihood : likelyPlaces) {
					if (placeLikelihood.getLikelihood() > maxLike) {
						currLocationCoord = placeLikelihood.getPlace().getLatLng();
						maxLike = placeLikelihood.getLikelihood();
						currLocationName = placeLikelihood.getPlace().getName().toString();
						
						place.setAddress(placeLikelihood.getPlace().getAddress().toString());
						place.setId(placeLikelihood.getPlace().getId());
						place.setName(currLocationName);
						place.setCoords(currLocationCoord);
						place.setOpenNow(true);
					}
				}
				
				likelyPlaces.release();
				
				TextView locationTextView = (TextView) findViewById(R.id.textView1);
				locationTextView.setText(place.getName() + ": " + place.getCoords());
			}
		});
		
		/* Google Place Autocomplete. */
		AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.locationText);
		
		ArrayAdapter adapter = new GooglePlacesAutocompleteAdapter(this, R.layout.list_item,
							mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);
		autoCompView.setAdapter(adapter);
		
		/* Google Place Picker. */
		Button button = (Button) findViewById(R.id.button1);
		
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Context context = getApplicationContext();
				try {
					startActivityForResult(builder.build(context), PLACE_PICKER_REQUEST);
				
				} catch (GooglePlayServicesRepairableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (GooglePlayServicesNotAvailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PLACE_PICKER_REQUEST) {
			if (resultCode == RESULT_OK) {
		        Place place = PlacePicker.getPlace(data, this);
		        String toastMsg = String.format("Place: %s", place.getName());
		        Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
		    }
			
		}
	}
	
	/**
	 * Called when the Activity could not connect to Google Play services and the auto manager
	 * could resolve the error automatically.
	 * In this case the API is not available and notify the user.
	 *
	 * @param connectionResult can be inspected to determine the cause of the failure
	 */
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
