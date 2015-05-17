package ro.pub.cs.taskplanner;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

public class PlanEvent implements Parcelable, Serializable, Comparable{

	private static final long serialVersionUID = 6980514441891816832L;

	private String name;
	private Date beginDate;
	private Date duration;
	private Integer exactLocation;
	private Integer exactBeginDate;
	private GooglePlace location;

	public PlanEvent (String name, Date bDate, Date eDate, GooglePlace location) {
		this.name = name;
		this.beginDate = bDate;
		this.duration = eDate;
		this.location = location;
		
		this.exactLocation = 1;
		this.exactBeginDate = 1;
	}
	
	private PlanEvent (Parcel in) {
		this.name = in.readString();
		this.beginDate = DateFormater.formatStringToDate(in.readString());
		this.duration = DateFormater.formatStringToDate(in.readString());
		this.exactLocation = in.readInt();
		this.exactBeginDate = in.readInt();
		this.location = in.readParcelable(GooglePlace.class.getClassLoader());
	}

	public Integer getExactLocation() {
		return this.exactLocation;
	}
		
	public Integer getExactBeginDate() {
		return this.exactBeginDate;
	}
	
	public String getName() {
		return name;
	}
	
	public Date getBeginDate() {
		return beginDate;
	}
	
	public Date getDuration() {
		return duration;
	}
	
	public GooglePlace getLocation() {
		return location;
	}
	
	public Date getEndDate() {
		Date endDate = new Date();
		endDate.setHours(beginDate.getHours() + duration.getHours() 
				+ (duration.getMinutes() + beginDate.getMinutes()) / 60);
		endDate.setMinutes((duration.getMinutes() + beginDate.getMinutes()) % 60);
		return endDate;
	}
	
	public int getDurationMinutes() {
		return duration.getHours() * 60 + duration.getMinutes();
	}
	
	public int getBeginDateMinutes() {
		return beginDate.getHours() * 60 + beginDate.getMinutes();
	}
	
	public int getEndDateMinutes() {
		return beginDate.getHours() * 60 + beginDate.getMinutes()
				+ duration.getHours() * 60 + duration.getMinutes();
	}
	
	public void setExactLocation(Integer exactLocation) {
		this.exactLocation = exactLocation;
	}
	
	public void setLocation(GooglePlace location) {
		this.location = location;
	}
	
	public void setBeginDate(Date bDate) {
		this.beginDate = bDate;
	}
	
	
	public void setExactBeginDate(Integer exactBeginDate) {
		this.exactBeginDate = exactBeginDate;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(DateFormater.formateDateToString(beginDate));
		dest.writeString(DateFormater.formateDateToString(duration));
		dest.writeInt(exactLocation);
		dest.writeInt(exactBeginDate);
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
		String ret = "";
		
		if (name != null) {
			ret += "Name: " + name;
		}
		
		ret += ", Exact location: " + exactLocation;
		ret += ", Exact begin date: " + exactBeginDate;
		
		if (beginDate != null) {
			ret += ", Begin date: " + beginDate;
		}
		
		if (duration != null) {
			ret += ", End date: " + duration;
		}
		
		if (location != null) {
			ret += ", Location: " + location;
		}
		
		return ret;
	}
	
	@Override
	public int compareTo(Object another) {
		return this.getBeginDateMinutes() - ((PlanEvent) another).getBeginDateMinutes();
	}
}
