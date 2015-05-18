package ro.pub.cs.taskplanner;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;

public class ViewSchedule extends Activity {

	Plan plan = null;
	TableLayout table;
	int max_width = 480;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_schedule);
	
		table = (TableLayout) findViewById(R.id.table);
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		max_width= size.x - 30;
		
		Intent intent = getIntent();
		if (intent != null) {
			try {
				plan = (Plan) intent.getParcelableExtra("SCHEDULE_VIEW");
				System.out.println("PLAN RECEIVED");
			} catch(Exception e) {
				System.out.println(e.getMessage());
			}
			if ( plan != null) {
				System.out.println("PLAN RECEIVED IS : \n" + plan.toString());
				populateTable();
			} else {
				System.out.println("PLAN IS NULLL");
			}
		}
	}
	
	private void populateTable() {
		List<PlanEvent> events = plan.getPlansEvents();

		for (int i = 0; i < events.size(); i++) {
			PlanEvent event = events.get(i);
			TableRow row = new TableRow(this);
			
			row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
									LayoutParams.WRAP_CONTENT));
			
			row.setPadding(0, 0, 7, 7);
			
			TextView text = new TextView(this);
			text.setBackgroundColor(Color.parseColor("#D2D2D2"));
			String formatDate = "";
			Date bDate = event.getBeginDate();
			Date eDate = event.getEndDate();
			String min1, min2;
			if (bDate.getMinutes() < 10) 
				min1 = "0" + bDate.getMinutes();
			else 
				min1 = "" + bDate.getMinutes();
			if (eDate.getMinutes() < 10) 
				min2 = "0" + eDate.getMinutes();
			else 
				min2 = "" + eDate.getMinutes();
			
			formatDate = "Event time : " + bDate.getHours() + ":" + min1 
					+ " - " + eDate.getHours() + ":" + min2;
			text.setWidth(max_width);
			text.setText(event.getName() + "\n" + event.getLocation().toString() + "\n" 
					+ formatDate);	
			row.addView(text);
			table.addView(row, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_schedule, menu);
			
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
