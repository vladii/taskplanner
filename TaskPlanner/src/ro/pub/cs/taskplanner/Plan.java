package ro.pub.cs.taskplanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Plan implements Parcelable ,Serializable{
	private static final long serialVersionUID = 8713028020657729069L;
	private String name;
	private int eventsSize;
	private List<PlanEvent> events;
	private int id;
	
	public Plan(String name) {
		this(name, 0);
	}
	
	public Plan(String name, int id) {
		this.id = id;
		this.name = name;
		events = new ArrayList<PlanEvent>();
		eventsSize = 0;
	}
	
	public Plan(String name, int id, List<PlanEvent> events) {
		this.id = id;
		this.name = name;
		this.events = new ArrayList<PlanEvent>();
		this.events.addAll(events);
		eventsSize = events.size();
	}
	
	private Plan(Parcel in) {
		name = in.readString();
		eventsSize = in.readInt();
		events = new ArrayList<PlanEvent>();
		
		for (int i = 0; i < eventsSize; i++) {
			PlanEvent event = in.readParcelable(PlanEvent.class.getClassLoader());
			events.add(event);
		}
    }
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeInt(eventsSize);
		
		for (int i = 0; i < eventsSize; i++) {
			dest.writeParcelable(events.get(i), 1);
		}
	}
	
	public static final Parcelable.Creator<Plan> CREATOR
    	= new Parcelable.Creator<Plan>() {
			public Plan createFromParcel(Parcel in) {
				return new Plan(in);
			}

			public Plan[] newArray(int size) {
				return new Plan[size];
			}
	};
	
	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}
	
	public List<PlanEvent> getPlansEvents() {
		return events;
	}
	
	public String toString() {
		return name + " " + String.valueOf(id) + " " + events.toString();
	}
}
