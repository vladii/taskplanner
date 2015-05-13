package ro.pub.cs.taskplanner;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * Base launcher activity, to handle most of the common plumbing for samples.
 */
public class SimpleBaseActivity extends FragmentActivity {

    public static final String TAG = "TaskPlanner";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected  void onStart() {
        super.onStart();
        initializeLogging();
    }

    /** Set up targets to receive log data */
    public void initializeLogging() {
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        // Wraps Android's native log framework;
        Log.i(TAG, "Ready");
    }
}

