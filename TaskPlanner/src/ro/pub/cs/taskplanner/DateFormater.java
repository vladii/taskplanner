package ro.pub.cs.taskplanner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormater {

	public static final String DATE_FORMAT = "dd/MM/yyyy hh:mm";
	
	
	public static String formateDateToString(Date date) {
		return new SimpleDateFormat(DATE_FORMAT, Locale.UK).format(date);
	}
	
 	public static Date formatStringToDate(String date) {
		try {
			return new SimpleDateFormat(DATE_FORMAT, Locale.UK).parse(date);
		} catch (Exception e) {
			return null;		
		}
	}
}
