package ro.pub.cs.taskplanner;

import java.util.ArrayList;
import java.util.List;

import ro.pub.cs.taskplanner.*;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	private LinearLayout plansLayout;
	private Button createPlan;
	private List<Plan> plans;
	private int idCounter = 1;
	private final static int EDIT_PLAN = 1;
	private final static int CREATE_PLAN = 0;
	
	
	private class ButtonCreatePlan implements Button.OnClickListener {
		
		@Override
		public void onClick(View v) {	
			Intent intent = new Intent("ro.pub.cs.taskplanner.CreatePlan");
			intent.putExtra("MODE", CREATE_PLAN);
			startActivityForResult(intent, CREATE_PLAN);
		}
	}
	
	private class ButtonEditActivity implements Button.OnClickListener {
		@Override
		public void onClick(View v) {
			int index = -1;
			
			for (int i = 0; i < plans.size() ; i++) {
				if (v.getId() == plansLayout.getChildAt(i).getId()) {
					index = i;
				}
			}
			if (index < 0) { return; }
			
			Intent intent = new Intent("ro.pub.cs.taskplanner.CreatePlan");
			intent.putExtra("MODE", EDIT_PLAN);
			intent.putExtra("EDIT_PLAN_INDEX", index);
			intent.putExtra("EDIT_PLAN", plans.get(index));
			startActivityForResult(intent, EDIT_PLAN);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		createPlan = (Button) findViewById(R.id.create_new_plan_button);
		plansLayout = (LinearLayout) findViewById(R.id.linearLayout);
		plans = new ArrayList<Plan>();
		int layoutSize = plansLayout.getChildCount();
		
		for (int i = 0 ; i < layoutSize; i++) {
			Button activity = (Button) plansLayout.getChildAt(i);
			activity.setOnClickListener(new ButtonEditActivity());
		}
		
		createPlan.setOnClickListener(new ButtonCreatePlan());
		
		if (savedInstanceState != null) {
			// TODO
		}
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if (resultCode == 0) {
			return;
		}
		
		if (requestCode == CREATE_PLAN) {
	    	Plan plan = (Plan) data.getParcelableExtra("CREATE_PLAN");
	    	plans.add(plan);
	    	addButton(plan);
	      
	    } else if (requestCode == EDIT_PLAN) {
	    	int index = data.getIntExtra("EDIT_PLAN_INDEX", -1);
	    	if (index == -1) {
	    		return;
	    	}
	    	plansLayout.removeViewAt(index);
	    	plans.remove(index);
	    	Plan plan = (Plan) data.getParcelableExtra("EDIT_PLAN");
	        plans.add(plan);
	        addButton(plan);
	    }
	}	
	
	private void addButton(Plan plan) {
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    	            LinearLayout.LayoutParams.MATCH_PARENT,
    	            LinearLayout.LayoutParams.WRAP_CONTENT);
    	Button button = new Button(this);
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
}
