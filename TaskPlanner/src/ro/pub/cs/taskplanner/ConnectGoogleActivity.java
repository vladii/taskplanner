package ro.pub.cs.taskplanner;

import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.plus.Plus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ConnectGoogleActivity extends Activity
	implements ConnectionCallbacks, OnConnectionFailedListener {
	
	private com.google.android.gms.common.SignInButton connectButton;
	private Button skipButton;
	
	/* Request code used to invoke sign in user interactions. */
	private static final int RC_SIGN_IN = 0;
	private static final int REQUEST_CODE_CREATOR = 3;

	/* Client used to interact with Google APIs. */
	private GoogleApiClient clientGoogleApi = null;
	
	/* A flag indicating that a PendingIntent is in progress and prevents
	 * us from starting further intents.
	 */
	private boolean mIntentInProgress;
	
	/* Click on Connect with Google. */
	private class ButtonConnectGoogle implements Button.OnClickListener {
		private Context father;
		
		public ButtonConnectGoogle(Context father) {
			this.father = father;
		}
		
		@Override
		public void onClick(View v) {
			if (clientGoogleApi == null || !clientGoogleApi.isConnected()) {
				clientGoogleApi = new GoogleApiClient.Builder(father)
			        .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) father)
			        .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) father)
			        .addApi(Drive.API)
			        .addScope(Drive.SCOPE_FILE)
			        .build();

				clientGoogleApi.connect();
			
			} else if (clientGoogleApi.isConnected()) {
				// Check if there exist any file.				
				checkTaskPlannerFile();
			}
		}
	}
	
	private class ButtonSkip implements Button.OnClickListener {
		private Context father;
		
		public ButtonSkip(Context father) {
			this.father = father;
		}
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent("ro.pub.cs.taskplanner.MainActivity");
			startActivityForResult(intent, 69);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect_google);
		
		if (GoogleApiClientSingleton.get() != null && GoogleApiClientSingleton.get().isConnected()) {
			Intent intent = new Intent("ro.pub.cs.taskplanner.MainActivity");
			startActivityForResult(intent, 69);
		}
		
		connectButton = (com.google.android.gms.common.SignInButton) findViewById(R.id.buttonConnect);
		connectButton.setOnClickListener(new ButtonConnectGoogle(this));
		
		skipButton = (Button) findViewById(R.id.buttonSkip);
		skipButton.setOnClickListener(new ButtonSkip(this));
	}
	
	protected void onActivityResult(int requestCode, int responseCode, Intent data) {
		if (requestCode == RC_SIGN_IN) {
		    mIntentInProgress = false;

		    if (!clientGoogleApi.isConnecting()) {
		    	clientGoogleApi.connect();
		    }
		
		} else if (requestCode == REQUEST_CODE_CREATOR) {
	    	DriveId driveId = (DriveId) data.getParcelableExtra(
                    OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
	    	
	    	// Set this driveId to MainActivity.
	    	MainActivity.fileId = driveId;
	    	
	    	// Start MainActivity.
	    	Intent intent = new Intent("ro.pub.cs.taskplanner.MainActivity");
    		startActivityForResult(intent, 69);
	    }
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!mIntentInProgress && result.hasResolution()) {
			System.out.println("Connection failed! " + result);
			
		    try {
		      mIntentInProgress = true;
		      startIntentSenderForResult(result.getResolution().getIntentSender(),
		          RC_SIGN_IN, null, 0, 0, 0);
		    } catch (SendIntentException e) {
		      // The intent was canceled before it was sent.  Return to the default
		      // state and attempt to connect to get an updated ConnectionResult.
		      mIntentInProgress = false;
		      clientGoogleApi.connect();
		    }
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		GoogleApiClientSingleton.set(clientGoogleApi);
		
		checkTaskPlannerFile();
	}
	
	public void checkTaskPlannerFile() {
		// Check if our file exists on Google Drive.
		// If no, create it.
		PendingResult<Status> asd = Drive.DriveApi.requestSync(GoogleApiClientSingleton.get());
		asd.setResultCallback(new ResultCallback<Status>() {

			@Override
			public void onResult(Status result) {
				System.out.println(result);
				
				Query query = new Query.Builder()
			 		.addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
			 		.addFilter(Filters.eq(SearchableField.TITLE, MainActivity.SAVE_DATA_FILE))
			 		.addFilter(Filters.eq(SearchableField.TRASHED, false))
			 		.build();
					 
				Drive.DriveApi.query(clientGoogleApi, query)
					.setResultCallback(metadataCallback);
			}
			
		});
	}
	
	final private ResultCallback<MetadataBufferResult> metadataCallback = new
            ResultCallback<MetadataBufferResult>() {
        @Override
        public void onResult(MetadataBufferResult result) {
            if (!result.getStatus().isSuccess()) {
                System.out.println("Problem while retrieving results");
                return;
            }
            
            boolean found = false;
            DriveId fileId = null;
            
            // Search for our file.
            int count = result.getMetadataBuffer().getCount();
            for (int i = 0; i < count; i++) {
            	if (result.getMetadataBuffer().get(i).getTitle()
            			.equals(MainActivity.SAVE_DATA_FILE)) {
            		found = true;
            		fileId = result.getMetadataBuffer().get(i).getDriveId();
            	
            		break;
            	}
            }
            
            result.getMetadataBuffer().release();
            
            if (found) {
            	// Save this device ID and open the main page.
            	MainActivity.fileId = fileId;
            	
            	Intent intent = new Intent("ro.pub.cs.taskplanner.MainActivity");
        		startActivityForResult(intent, 69);
            
            } else {
            	// File doesn't exist. Create it.
            	Drive.DriveApi.newDriveContents(clientGoogleApi)
            		.setResultCallback(driveContentsCallback);
            }
        }
    };
    
    final ResultCallback<DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveContentsResult>() {
        @Override
        public void onResult(DriveContentsResult result) {
        	// Compose metadata.
            MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                    .setTitle(MainActivity.SAVE_DATA_FILE)
                    .setMimeType("text/plain").build();
            
            // Create an empty file.
            IntentSender intentSender = Drive.DriveApi
                    .newCreateFileActivityBuilder()
                    .setInitialMetadata(metadataChangeSet)
                    .setInitialDriveContents(null)
                    .build(clientGoogleApi);
            
            try {
                startIntentSenderForResult(
                        intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                
            } catch (SendIntentException e) {
                Log.w("TaskPlanner", "Unable to send intent", e);
            }
        }
    };

	@Override
	public void onConnectionSuspended(int cause) {
		clientGoogleApi.connect();
	}
}
