package ro.pub.cs.taskplanner;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

public class GoogleApiClientSingleton {
	public static volatile GoogleApiClient client;
	
	public synchronized static void set(GoogleApiClient mClient) {
		client = mClient;
	}
	
	public synchronized static GoogleApiClient get() {
		return client;
	}
}
