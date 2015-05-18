package ro.pub.cs.taskplanner;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.plus.Plus;

import ro.pub.cs.taskplanner.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
	private LinearLayout plansLayout;
	private Button createPlan;
	private Button savePlansButton;
	private Button syncLocallyButton;
	private Button syncGoogleDriveButton;
	private List<Plan> plans;
	private int idCounter = 1;
	private final static int EDIT_PLAN = 1;
	private final static int CREATE_PLAN = 0;
	protected static final int REQUEST_CODE_CREATOR = 3;
	
	public final static String SAVE_DATA_FILE = "taskPlannerData.txt";
	public static DriveId fileId = null;	// File Id on Google Drive
	
	private GoogleApiClient clientGoogleApi = null;
	
	private class ButtonCreatePlan implements Button.OnClickListener {
		@Override
		public void onClick(View v) {	
			Intent intent = new Intent("ro.pub.cs.taskplanner.CreatePlan");
			intent.putExtra("MODE", CREATE_PLAN);
			startActivityForResult(intent, CREATE_PLAN);
		}
	}
	
	private class ButtonSavePlan implements Button.OnClickListener {
		@Override
		public void onClick(View v) {	
			// Write all plans to file.
			writePlansToFile();
		}
	}
	
	private class ButtonSyncLocally implements Button.OnClickListener {
		@Override
		public void onClick(View v) {	
			// Read plans from local file.
			readPlansFromFile();
		}
	}
	
	private class ButtonSyncGoogleDrive implements Button.OnClickListener {
		@Override
		public void onClick(View v) {	
			// Read plans from Google Drive.
			readPlansFromGoogleDrive();
		}
	}
	
	private class ButtonEditActivity implements Button.OnClickListener {
		@Override
		public void onClick(View v) {
			int index = -1;
			for (int i = 0; i < plans.size() && i < plansLayout.getChildCount() ; i++) {
				int idv = v.getId();
				View lv = plansLayout.getChildAt(i);
				int idl = lv.getId();
				if (idv == idl) {
					index = i;
				}
			}
			if (index < 0) { return; }
			Intent intent = new Intent("ro.pub.cs.taskplanner.CreatePlan");
			intent.putExtra("MODE", EDIT_PLAN);
			intent.putExtra("EDIT_PLAN_INDEX", index);
			intent.putExtra("EDIT_PLAN", (Parcelable)plans.get(index));
			startActivityForResult(intent, EDIT_PLAN);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Get Google API.
		clientGoogleApi = GoogleApiClientSingleton.get();

		// Add listeners.
		createPlan = (Button) findViewById(R.id.create_new_plan_button);
		savePlansButton = (Button) findViewById(R.id.savePlansButton);
		syncLocallyButton = (Button) findViewById(R.id.syncLocallyButton);
		syncGoogleDriveButton = (Button) findViewById(R.id.syncGoogleDriveButton);
		
		plansLayout = (LinearLayout) findViewById(R.id.linearLayout);
		plans = new ArrayList<Plan>();
		int layoutSize = plansLayout.getChildCount();
		
		for (int i = 0 ; i < layoutSize; i++) {
			Button activity = (Button) plansLayout.getChildAt(i);
			activity.setOnClickListener(new ButtonEditActivity());
		}
		
		createPlan.setOnClickListener(new ButtonCreatePlan());
		savePlansButton.setOnClickListener(new ButtonSavePlan());
		syncLocallyButton.setOnClickListener(new ButtonSyncLocally());
		syncGoogleDriveButton.setOnClickListener(new ButtonSyncGoogleDrive());
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if (resultCode == 0) {
			System.out.println("Result code 0. Exit");
			return;
		}
		
		if (requestCode == CREATE_PLAN) {
	    	Plan plan = (Plan) data.getParcelableExtra("CREATE_PLAN");
	    	plans.add(plan);
	    	addView(plan);
	
	    } else if (requestCode == EDIT_PLAN) {
	    	int index = data.getIntExtra("EDIT_PLAN_INDEX", -1);
	    	if (index == -1) {
	    		System.out.println("plan not changed. exiting");
	    		return;
	    	}
	    	plansLayout.removeViewAt(index);
	    	plans.remove(index);
	    	Plan plan = (Plan) data.getParcelableExtra("EDIT_PLAN");
	    	plans.add(plan);
	        addView(plan);
	    
	    }
	}
	
	public Drawable scaleImage (Drawable image, float scaleFactor) {

	    if ((image == null) || !(image instanceof BitmapDrawable)) {
	        return image;
	    }

	    Bitmap b = ((BitmapDrawable)image).getBitmap();

	    int sizeX = Math.round(image.getIntrinsicWidth() * scaleFactor);
	    int sizeY = Math.round(image.getIntrinsicHeight() * scaleFactor);

	    Bitmap bitmapResized = Bitmap.createScaledBitmap(b, sizeX, sizeY, false);

	    image = new BitmapDrawable(getResources(), bitmapResized);

	    return image;

	}
	
	private void addView(Plan plan) {
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    	            LinearLayout.LayoutParams.MATCH_PARENT,
    	            LinearLayout.LayoutParams.WRAP_CONTENT);
    	Button button = new Button(this);
    	
    	Drawable icon = getBaseContext().getResources().getDrawable( R.drawable.bullet);
    	button.setCompoundDrawablesWithIntrinsicBounds(this.scaleImage(icon, (float) 0.1), null, null, null );
    	
    	button.setText(plan.getName());
    	button.setId(idCounter ++);
    	plansLayout.addView(button , params);
    	button.setOnClickListener(new ButtonEditActivity());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

	public void writePlansToFile(){
		// Write on drive.
		if (fileId != null)
			writePlansToDrive(fileId);
		
		// Write locally.
        FileOutputStream fos;
        ObjectOutputStream oos = null;
        
        try{
            fos = getApplicationContext().openFileOutput(SAVE_DATA_FILE, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(plans);
            oos.flush();
            oos.close();
            
            Toast.makeText(this,
    				"Plans were successfully saved on your disk!",
    				Toast.LENGTH_SHORT).show();
            
        } catch(Exception e) {
        	System.out.println("Error writing data " + e.getMessage());
        	
        	Toast.makeText(this,
    				"Oops, error while saving your plans! Try again!",
    				Toast.LENGTH_SHORT).show();
        }
        finally{
            if(oos!=null) {
                try {
                    oos.close();
                }catch(Exception e){
                	System.out.println("Error closing file" + e.getMessage());
                }
            }
        }
	}
	
	/* Write file to drive when we know its file ID. */
	private void writePlansToDrive(DriveId fileID) {
		clientGoogleApi = GoogleApiClientSingleton.get();
		
        if (clientGoogleApi == null || !clientGoogleApi.isConnected()) {
        	Toast.makeText(this,
    				"You are not logged in with Google!",
    				Toast.LENGTH_SHORT).show();
        	
        	return;
        }
        
        final Context mainContext = this;
        DriveFile file = Drive.DriveApi.getFile(clientGoogleApi, fileID);
        
        // Write on drive.
        file.open(clientGoogleApi, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallback<DriveContentsResult>() {
            @Override
            public void onResult(DriveContentsResult result) {
                if (!result.getStatus().isSuccess()) {
                    // Handle error.
                    return;
                }
                
                DriveContents contents = result.getDriveContents();
                
                try {
                    ParcelFileDescriptor parcelFileDescriptor = contents.getParcelFileDescriptor();
                    
                    // Append to the file.
                    FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor
                        .getFileDescriptor());
                    
                    ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
                    oos.writeObject(plans);
                    
                    contents.commit(clientGoogleApi, null).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status result) {
                            // Wonderful! File saved.
                        	System.out.println("Commit results: " + result);
                        	
                        	Toast.makeText(mainContext,
                    				"Plans saved on your Google Drive!",
                    				Toast.LENGTH_SHORT).show();
                        }
                    });
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
	}
	
	final private class RetrieveDriveFileContentsAsyncTask
    	extends ApiClientAsyncTask<DriveId, Boolean, String> {

		public RetrieveDriveFileContentsAsyncTask(Context context) {
			super(context);
		}

		@Override
		protected String doInBackgroundConnected(DriveId... params) {
			String contents = null;
			DriveFile file = Drive.DriveApi.getFile(GoogleApiClientSingleton.get(), params[0]);
			DriveContentsResult driveContentsResult =
					file.open(GoogleApiClientSingleton.get(), DriveFile.MODE_READ_ONLY, null).await();
			
			System.out.println(driveContentsResult.getStatus());
			
			if (!driveContentsResult.getStatus().isSuccess()) {
				return null;
			}
			
			DriveContents driveContents = driveContentsResult.getDriveContents();
			ObjectInputStream reader = null;
			
			try {
				reader = new ObjectInputStream(driveContents.getInputStream());
				
				plans.clear();
		        plans = (ArrayList<Plan>) reader.readObject();
		        
		        reader.close();
				
			} catch (Exception e) {
				return null;
			}
			
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			driveContents.discard(GoogleApiClientSingleton.get());
			return "ok";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			if (result != null) {
				updateView(true);
				
			} else {
				updateView(false);
			}
		}
	}
	
	final private ResultCallback<DriveIdResult> idCallback = new ResultCallback<DriveIdResult>() {
        @Override
        public void onResult(DriveIdResult result) {
            new RetrieveDriveFileContentsAsyncTask(MainActivity.this).execute(result.getDriveId());
        }
    };
	
	private void readPlansFromGoogleDrive() {
		if (fileId == null || GoogleApiClientSingleton.get() == null ||
			!GoogleApiClientSingleton.get().isConnected()) {
			Toast.makeText(this,
    			"Error when loading plans from your Google Drive! Check your connection!",
    			Toast.LENGTH_SHORT).show();
			
			return;
		}
		
		// Read file.
		Drive.DriveApi.fetchDriveId(GoogleApiClientSingleton.get(), fileId.getResourceId())
        	.setResultCallback(idCallback);
	}

	private boolean readPlansFromFile() {
        FileInputStream fin;
        ObjectInputStream ois = null;
        
        try{
            fin = getApplicationContext().openFileInput(SAVE_DATA_FILE);
            ois = new ObjectInputStream(fin);   
            
            plans.clear();
            idCounter = 1;
            plansLayout.removeAllViews();
            
            plans = (ArrayList<Plan>) ois.readObject();
            for (int i = 0; i < plans.size(); i++) {
            	addView(plans.get(i));
            }
            ois.close();
            
            Toast.makeText(this,
    				"Plans were successfully loaded from local drive!",
    				Toast.LENGTH_SHORT).show();
            
            return true;
            
            } catch(Exception e) {
                System.out.println("Error reading from file" + e.getMessage());
                
                Toast.makeText(this,
        				"Error when loading plans from local drive!",
        				Toast.LENGTH_SHORT).show();
                
                return false;
            }
        finally {
        	if(ois!=null) {
        		try {
        			ois.close();
        		} catch(Exception e) {
        			System.out.println("Error closing the file" + e.getMessage());
        		}
        	}
        }
    }
	
	/* Function called by Google Drive request file content handler. */
	public void updateView(Boolean reallyUpdated) {
		if (reallyUpdated) {
			idCounter = 1;
			plansLayout.removeAllViews();

			for (int i = 0; i < plans.size(); i++) {
				addView(plans.get(i));
			}
			
			Toast.makeText(this,
		        	"Plans were successfully loaded from your Google Drive!",
		    		Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this,
		        	"Error when retrieving your plans from Google Drive!",
		    		Toast.LENGTH_SHORT).show();
		}
	}
}
