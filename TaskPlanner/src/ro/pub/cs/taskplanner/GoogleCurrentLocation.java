package ro.pub.cs.taskplanner;

import java.util.Date;
import java.util.List;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

public class GoogleCurrentLocation extends SimpleBaseActivity 
		implements GoogleApiClient.OnConnectionFailedListener {

	protected GoogleApiClient mGoogleApiClient;
	
	public GoogleCurrentLocation(GoogleApiClient mGoogleApiClient) {
		this.mGoogleApiClient = mGoogleApiClient;
	}
	
	private class CallbackHandler implements ResultCallback<PlaceLikelihoodBuffer> {

		private CreatePlan plan;
		
		public CallbackHandler(CreatePlan plan) {
			this.plan = plan;
		}
		
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
			
			/* Compose dates. */
			String startDateString = String.valueOf(1) + "/" +
								String.valueOf(1) + "/" + 
								String.valueOf(2015) + " " + 
								String.valueOf(0) + ":" + 
								String.valueOf(0);
			
			String durationString = String.valueOf(1) + "/" +
						String.valueOf(1) + "/" + 
						String.valueOf(2015) + " " + 
						String.valueOf(0) + ":" + 
						String.valueOf(1);
			
			/* Add current place in arr. */
			PlanEvent event = new PlanEvent("Current location",
					DateFormater.formatStringToDate(startDateString),
					DateFormater.formatStringToDate(durationString),
					place);
			event.setExactLocation(1);
			event.setExactBeginDate(0);
			
			Log.d(TAG, "Added current location!");
			
			plan.events.add(event);
			plan.populateView();
		}
		
	}
	
	public void setCurrentLocation(CreatePlan plan) {
		/* Google Current Location. */
		PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
			    .getCurrentPlace(mGoogleApiClient, null);
	
		result.setResultCallback(new CallbackHandler(plan));
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
