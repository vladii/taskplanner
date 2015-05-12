package com.example.gogutest;

import com.google.android.gms.maps.model.LatLng;

public class GooglePlace {
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
		return "Nume: " + name + ", Adresa: " + address +
			   "Coordonate: " + coords.latitude + "/" + coords.longitude;
	}
}
