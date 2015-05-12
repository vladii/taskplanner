package com.example.gogutest;

import java.io.BufferedInputStream;
import java.io.InputStream;
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

import com.google.android.gms.common.api.Result;
import com.google.android.gms.maps.model.LatLng;

public class GoogleNearbyLocations {
	public static final int RADIUS = 2000;
	public static final String API_KEY = "AIzaSyCBjG_0s0H3h9IsIrpLMEFHeOmEnY7TS9s";

	private String queryString;
	private String responseString;
	private double lattitude;
	private double longitude;
	
	public GoogleNearbyLocations(String what, double lat, double lung) {
		this.lattitude = lat;
		this.longitude = lung;
		this.queryString = what.replace(' ', '+');
	}
	
	public List<GooglePlace> getNearbyLocations() {
		String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/" +
					 "json?location=" + lattitude + "," + longitude +
					 "&radius=" + RADIUS + "&keyword=" + queryString +
					 "&key=" + API_KEY;
		
		
		
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
	
	private List<GooglePlace> parse(String response) {
		List<GooglePlace> temp = new ArrayList<GooglePlace>();
		
		try {
			// make a jsonObject in order to parse the response
			JSONObject jsonObject = new JSONObject(response);

			// make an jsonObject in order to parse the response
			if (jsonObject.has("results")) {
				JSONArray jsonArray = jsonObject.getJSONArray("results");

				for (int i = 0; i < jsonArray.length(); i++) {
					GooglePlace poi = new GooglePlace();

					if (jsonArray.getJSONObject(i).has("name")) {
						// Check name.
						poi.setName(jsonArray.getJSONObject(i).optString("name"));
						
						// Check if is open.
						if (jsonArray.getJSONObject(i).has("opening_hours")) {
							if (jsonArray.getJSONObject(i).getJSONObject("opening_hours").has("open_now")) {
								if (jsonArray.getJSONObject(i).getJSONObject("opening_hours").getString("open_now").equals("true")) {
									poi.setOpenNow(true);
								} else {
									poi.setOpenNow(false);
								}
							}
						}
						
						// Check id.
						if (jsonArray.getJSONObject(i).has("id")) {
							poi.setId(jsonArray.getJSONObject(i).optString("id"));
						}
						
						// Check place id.
						if (jsonArray.getJSONObject(i).has("place_id")) {
							poi.setId(jsonArray.getJSONObject(i).optString("place_id"));
						}
						
						// Check Address.
						if (jsonArray.getJSONObject(i).has("vicinity")) {
							poi.setAddress(jsonArray.getJSONObject(i).optString("vicinity"));
						}
						
						// Check lattitude and longitude.
						if (jsonArray.getJSONObject(i).has("geometry") &&
								jsonArray.getJSONObject(i).getJSONObject("geometry").has("location")) {
							double lat = jsonArray.getJSONObject(i).getJSONObject("geometry")
										 .getJSONObject("location").optDouble("lat");
							double lng = jsonArray.getJSONObject(i).getJSONObject("geometry")
									 .getJSONObject("location").optDouble("lng");
							
							poi.setCoords(new LatLng(lat, lng));
						}
					}
					
					temp.add(poi);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return temp;
	}
}
