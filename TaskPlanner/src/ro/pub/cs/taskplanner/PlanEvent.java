package ro.pub.cs.taskplanner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

public class PlanEvent implements Parcelable {
	private String name;
	private Date beginDate;
	private Date endDate;
	private GooglePlace location;
	
	public PlanEvent (String name, Date bDate, Date eDate, GooglePlace location) {
		this.name = name;
		this.beginDate = bDate;
		this.endDate = eDate;
		this.location = location;
	}
	
	private PlanEvent (Parcel in) {
		this.name = in.readString();
		this.beginDate = DateFormater.formatStringToDate(in.readString());
		this.endDate = DateFormater.formatStringToDate(in.readString());
		this.location = in.readParcelable(GooglePlace.class.getClassLoader());
	}

	
	public String getName() {
		return name;
	}
	
	public Date getBeginDate() {
		return beginDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public GooglePlace getLocation() {
		return location;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(DateFormater.formateDateToString(beginDate));
		dest.writeString(DateFormater.formateDateToString(endDate));
		dest.writeParcelable(location, flags);
	}
	
	public static final Parcelable.Creator<PlanEvent> CREATOR
    	= new Parcelable.Creator<PlanEvent>() {
			public PlanEvent createFromParcel(Parcel in) {
				return new PlanEvent(in);
			}

			public PlanEvent[] newArray(int size) {
				return new PlanEvent[size];
			}
	};
	
	public String toString() {
		return "printing event : " + name + "\n" + beginDate.toString() + "\n" + endDate.toString() + "\n" + location;
	}
}
