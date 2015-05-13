package ro.pub.cs.taskplanner;

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

/* Given a placeID, return its lattitude and longitude. */
public class GoogleGeodecodingAPI {
	public static final String API_KEY = "AIzaSyCBjG_0s0H3h9IsIrpLMEFHeOmEnY7TS9s";

	private String placeID;
	private String responseString;
	
	public GoogleGeodecodingAPI(String placeID) {
		this.placeID = placeID;
	}
	
	public LatLng getCoords() {
		String url = "https://maps.googleapis.com/maps/api/geocode/json?place_id=" +
					 placeID + "&key=" + API_KEY;
		
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
	
	private LatLng parse(String response) {
		LatLng temp = null;
		
		try {
			// make a jsonObject in order to parse the response
			JSONObject jsonObject = new JSONObject(response);

			// make an jsonObject in order to parse the response
			if (jsonObject.has("results")) {
				JSONArray jsonArray = jsonObject.getJSONArray("results");

				for (int i = 0; i < jsonArray.length(); i++) {
					GooglePlace poi = new GooglePlace();

					if (jsonArray.getJSONObject(i).has("geometry")) {
						JSONObject jsonGeometry =
								jsonArray.getJSONObject(i)
									.getJSONObject("geometry")
									.getJSONObject("location");
						
						double lat = jsonGeometry.getDouble("lat");
						double lng = jsonGeometry.getDouble("lng");
						
						return new LatLng(lat, lng);
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return temp;
	}
}

