package ro.pub.cs.taskplanner;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class GooglePlace implements Parcelable {
	private String id;
	private String placeId;
	private String name;
	private String address;
	private Boolean open;
	private LatLng coords;

	public GooglePlace() {
		this.name = null;
		this.id = null;
		this.placeId = null;
		this.address = null;
		this.coords = null;
		this.open = false;
	}
	
	public GooglePlace(Parcel in) {
		this.id = in.readString();
		this.placeId = in.readString();
		this.name = in.readString();
		this.address = in.readString();
		this.open = ((in.readInt() == 1) ? true : false);
		this.coords = in.readParcelable(LatLng.class.getClassLoader());
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setOpenNow(Boolean open) {
		this.open = open;
	}

	public Boolean getOpenNow() {
		return open;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}
	
	public String getPlaceId() {
		return placeId;
	}
	
	public void setCoords(LatLng coords) {
		this.coords = coords;
	}
	
	public LatLng getCoords() {
		return coords;
	}
	
	public String toString() {
		String ret = "";
		
		if (name != null) {
			ret += "Nume: " + name + ", ";
		}
		
		if (address != null) {
			ret += "Adresa: " + address + ", ";
		}
		
		if (coords != null) {
			ret += "Coord: " + coords.latitude + "/" + coords.longitude;
		}
		
		return ret;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(placeId);
		dest.writeString(name);
		dest.writeString(address);
		dest.writeInt(open ? 1 : 0);
		dest.writeParcelable(coords, flags);
	}
	
	public static final Parcelable.Creator<GooglePlace> CREATOR
	= new Parcelable.Creator<GooglePlace>() {
		public GooglePlace createFromParcel(Parcel in) {
			return new GooglePlace(in);
		}

		public GooglePlace[] newArray(int size) {
			return new GooglePlace[size];
		}
	};
}

