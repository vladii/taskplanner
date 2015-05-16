package ro.pub.cs.taskplanner;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.maps.model.LatLng;

/* GoogleDistanceMatrix will compute for you the distances
 * (in kilometers and hours) for every pair of GooglePlace's in a list.
 * How to use it:
 * GoogleDistanceMatrix distanceMatrix =
				new GoogleDistanceMatrix(List<GooglePlace> list);
 * * If you don't set mode, it will be automatically set to "car".
 * List<List<GoogleDistance> > distances = distanceMatrix.getDistanceMatrix()
 * 
 * In "distances" list you will find information about distance between each pair
 * of places.
 * 
 * * Note: GooglePlace should be complete!!! That means they should have "coords"
 * field (that one with latittude and longitude) set!!!
 */
public class GoogleDistanceMatrix {
	public static final String API_KEY = "AIzaSyCBjG_0s0H3h9IsIrpLMEFHeOmEnY7TS9s";
	private String responseString;
	private List<GooglePlace> places;
	private String mode;
	
	public GoogleDistanceMatrix(List<GooglePlace> places, String mode) {
		this.places = places;
		this.mode = mode;
	}
	
	public GoogleDistanceMatrix(List<GooglePlace> places) {
		this(places, "car");
	}
	
	public List<List<GoogleDistance> > getDistanceMatrix() {
		String origins = "";
		String destinations = "";
		
		boolean first = true;
		for (GooglePlace place : places) {
			if (first) {
				first = false;
			} else {
				origins += URLEncoder.encode("|");
			}
			
			origins += place.getCoords().latitude + "," +
					   place.getCoords().longitude;
		}
		
		String url = "https://maps.googleapis.com/maps/api/distancematrix/json" +
					 "?origins=" + origins + "&destinations=" + origins +
					 "&mode=" + mode +
					 "&key=" + API_KEY;
		
		Log.d("TaskPlanner", url);
		
		GoogleNetworkRequest asyncReq = new GoogleNetworkRequest(url);
		asyncReq.execute();
		try {
			asyncReq.get();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		// The result will be stored in responseString.
		// Trim the whitespaces and parse the result.
		return this.parse(responseString.trim());
	}
	
	private class GoogleNetworkRequest extends AsyncTask {
		private String url;
		
		public GoogleNetworkRequest(String url) {
			super();
			
			this.url = url;
		}

		@Override
		protected Object doInBackground(Object... params) {
			// string buffers the url
			StringBuffer buffer_string = new StringBuffer(url);
			String replyString = "";

			// instanciate an HttpClient
			HttpClient httpclient = new DefaultHttpClient();
			// instanciate an HttpGet
			HttpGet httpget = new HttpGet(buffer_string.toString());

			try {
				// get the responce of the httpclient execution of the url
				HttpResponse response = httpclient.execute(httpget);
				InputStream is = response.getEntity().getContent();

				// buffer input stream the result
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayBuffer baf = new ByteArrayBuffer(20);
				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}
				
				// the result as a string is ready for parsing
				replyString = new String(baf.toByteArray());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			responseString = replyString;
			return replyString;
		}
		
	}
	
	private List<List<GoogleDistance> > parse(String response) {
		List<List<GoogleDistance> > temp =
				new ArrayList<List<GoogleDistance> >();
		
		try {
			// make a jsonObject in order to parse the response
			JSONObject jsonObject = new JSONObject(response);

			// make an jsonObject in order to parse the response
			if (jsonObject.has("rows")) {
				JSONArray jsonArray = jsonObject.getJSONArray("rows");

				for (int i = 0; i < jsonArray.length(); i++) {
					List<GoogleDistance> currList = new ArrayList<GoogleDistance>();

					if (jsonArray.getJSONObject(i).has("elements")) {
						JSONArray elements = jsonArray.getJSONObject(i).getJSONArray("elements");
						
						for (int j = 0; j < elements.length(); j++) {
							JSONObject currElement = elements.getJSONObject(j);
							String distanceText = null;
							double distanceValue = 0;
							String durationText = null;
							double durationValue = 0;
							
							if (currElement.has("distance")) {
								JSONObject distanceElement = currElement.getJSONObject("distance");
								
								distanceText = distanceElement.getString("text");
								distanceValue = distanceElement.getDouble("value");
							}
							
							if (currElement.has("duration")) {
								JSONObject durationElement = currElement.getJSONObject("duration");
								
								durationText = durationElement.getString("text");
								durationValue = durationElement.getDouble("value");
							}
							
							// Create object.
							GoogleDistance distanceObject = new GoogleDistance(distanceText, distanceValue,
																		durationText, durationValue);
							
							// Add it to the list.
							currList.add(distanceObject);
						}
					}
					
					temp.add(currList);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return temp;
	}
}

