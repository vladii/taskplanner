package ro.pub.cs.taskplanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Pair;

public class ScheduleAlgorithm {
	private List<PlanEvent> completeEvents = new ArrayList<PlanEvent>();
	private List<PlanEvent> noTimeEvents = new ArrayList<PlanEvent>();
	private List<PlanEvent> noPlaceEvents = new ArrayList<PlanEvent>();
	private List<PlanEvent> freeEvents = new ArrayList<PlanEvent>();
	private List<PlanEvent> allEvents;
	private GoogleDistanceMatrix googleDistanceMatrix;
	private Map<Pair<GooglePlace, GooglePlace>, GoogleDistance> distances =
			new HashMap<Pair<GooglePlace, GooglePlace>, GoogleDistance>();
	
	int debug = 0;

	/* 30 minutes */
	private static final int MINIMAL_TIME_BETWEEN_MINS = 30; 
	private static final int SAFE_ERROR_MINS = 5;
	
	public ScheduleAlgorithm(List<PlanEvent> events) {
		this.allEvents = events;
	}
	
	public List<PlanEvent> schedulePlan() {
		fillLists();
		
		Collections.sort(completeEvents);
		Collections.sort(noPlaceEvents);
		
		setCurrentLocationFirstEvent();
		
		mergeCompleteAndNoPlaceEvents();
		
		computeDistances(completeEvents, completeEvents);
		computeDistances(completeEvents, noTimeEvents);
		computeDistances(noTimeEvents, completeEvents);
		computeDistances(noTimeEvents, noTimeEvents);
		
		mergeCompleteAndNoTimeEvents();
		
		mergeCompleteAndFreeEvents();
		
		return completeEvents;
	}
	
	private void mergeCompleteAndFreeEvents() {
		if (freeEvents.size() == 0) {
			return;
		}
		
		for (PlanEvent event : freeEvents) {
			int pos = -1;
			int maxim = 0;
			
			for (int i = 0; i < completeEvents.size() - 1; i++) {
				int dist = completeEvents.get(i + 1).getBeginDateMinutes() 
							- completeEvents.get(i).getEndDateMinutes(); 
				if (dist > maxim) {
					maxim = dist;
					pos = i;
				}
			}
			
			if (pos == -1 || maxim < MINIMAL_TIME_BETWEEN_MINS) {
				pos = completeEvents.size() - 1;
			}
			
			int pos2 = pos + 1;
			if ( pos2 == completeEvents.size()) {
				pos2 = pos;
			}
			List<GooglePlace> nearLocations = computeNearLocations(event, pos, pos2);
			List<GooglePlace> origins = new ArrayList<GooglePlace>();
			origins.add(completeEvents.get(pos).getLocation());
			if (pos != completeEvents.size() - 1) {
				origins.add(completeEvents.get(pos + 1).getLocation());
			}
			
			googleDistanceMatrix = new GoogleDistanceMatrix(origins, nearLocations);
			List<List<GoogleDistance> > distanceMatrix = googleDistanceMatrix.getDistanceMatrix();
			int nearPos = getNearestPosition(origins, nearLocations, distanceMatrix);
			
			boolean constraints = false;
			double dist = distanceMatrix.get(0).get(nearPos).durationValue / 60; // minutes
			int dist1 = (int) dist;
			if (origins.size() > 1) {
				dist += (distanceMatrix.get(1).get(nearPos).durationValue / 60);
				if (dist + completeEvents.get(pos).getEndDateMinutes() + event.getDurationMinutes()
						+ SAFE_ERROR_MINS < completeEvents.get(pos + 1).getBeginDateMinutes()) {
					constraints = true;
				}
			} else {
				constraints = true;
			}
			if (constraints) {
				dist1 += SAFE_ERROR_MINS;
				event.setLocation(nearLocations.get(nearPos));
				Date date = new Date();
				Date date1 = completeEvents.get(pos).getEndDate();
				date.setHours(date1.getHours() + dist1 / 60 + (date1.getMinutes() + dist1 % 60) / 60);
				date.setMinutes((date1.getMinutes() + dist1) % 60);
				event.setBeginDate(date);
				event.setExactBeginDate(1);
				event.setExactLocation(1);
				completeEvents.add(pos + 1, event);
			} else {
				addEventAtTheEnd(event);
			}
		}
	}
	
	private void addEventAtTheEnd(PlanEvent event) {
		int pos = completeEvents.size() - 1;
		List<GooglePlace> nearLocations = computeNearLocations(event, pos, -1);
		List<GooglePlace> origins = new ArrayList<GooglePlace>();
		origins.add(completeEvents.get(pos).getLocation());
		
		googleDistanceMatrix = new GoogleDistanceMatrix(origins, nearLocations);
		List<List<GoogleDistance> > distanceMatrix = googleDistanceMatrix.getDistanceMatrix();
		int nearPos = getNearestPosition(origins, nearLocations, distanceMatrix);
		int dist = (int) (distanceMatrix.get(0).get(nearPos).durationValue / 60) + SAFE_ERROR_MINS;
				
		event.setLocation(nearLocations.get(nearPos));
		event.setExactLocation(1);
		
		Date date = new Date();
		Date date1 = completeEvents.get(pos).getEndDate();
				
		date.setHours(date1.getHours() + dist / 60 + (date1.getMinutes() + dist % 60) / 60);
		date.setMinutes((date1.getMinutes() + dist) % 60);
		
		event.setBeginDate(date);
		event.setExactBeginDate(1);
		completeEvents.add(pos + 1, event);
	}
	
	private void mergeCompleteAndNoTimeEvents() {
		if (noTimeEvents.size() == 0) {
			return;
		}
		
		for (int i = 0; i < noTimeEvents.size(); i++) {
			PlanEvent event = noTimeEvents.get(i);
			int pos = -1;
			double minim = 24 * 3600;
			for (int j = 0; j < completeEvents.size(); j++) {
				Pair p = new Pair(completeEvents.get(j).getLocation(), event.getLocation());
				double dist = distances.get(p).durationValue;
				double distAdded = dist * 2;
				if (j < completeEvents.size() - 1) {
					dist += distances.get(new Pair(event.getLocation(), completeEvents.get(j + 1).getLocation())).durationValue;
					distAdded = dist - distances.get(new Pair(completeEvents.get(j).getLocation(), 
														completeEvents.get(j + 1).getLocation())).durationValue;
				}
				
				boolean constraints = false;
				int endTime = completeEvents.get(j).getEndDateMinutes() + (int) (dist / 60) 
						+ event.getDurationMinutes();
				if (j < completeEvents.size() - 1 
						&& endTime + SAFE_ERROR_MINS < completeEvents.get(j + 1).getBeginDateMinutes()) {
					constraints = true;
				}
				if (j == completeEvents.size() - 1) {
					constraints = true;
				}
				if ((pos == -1 || minim > distAdded) && constraints) {
					pos = j;
					minim = distAdded;
				}
			}
			
			if (pos != -1) {
				Date prevEventDate = completeEvents.get(pos).getEndDate();
				Date date = new Date();
				int dist = (int) (distances.get(
						new Pair(completeEvents.get(pos).getLocation(), 
								event.getLocation())).durationValue / 60) + SAFE_ERROR_MINS;
				date.setHours(dist / 60 + prevEventDate.getHours()
						+ (dist % 60 + prevEventDate.getMinutes()) / 60);
				date.setMinutes((prevEventDate.getMinutes() + dist) % 60);
				event.setBeginDate(date);
				event.setExactBeginDate(1);
				completeEvents.add(pos + 1, event);
			} 
		}
	}
		
	private void mergeCompleteAndNoPlaceEvents() {
		
		for (PlanEvent event : noPlaceEvents) {
			int pos;
			for (pos = 0; pos < completeEvents.size(); pos++) {
				if (completeEvents.get(pos).getEndDateMinutes() > event.getBeginDateMinutes()) {
					break;
				}
			}
			int pos2 = pos - 1;
			if (pos == completeEvents.size()) {
				pos = -1;
			}
			List<GooglePlace> nearLocations = computeNearLocations(event, pos, pos2);			
			List<GooglePlace> origins = new ArrayList<GooglePlace>();
			
			if (pos > 0) {
				origins.add(completeEvents.get(pos - 1).getLocation());
			}
			if (pos < completeEvents.size()) {
				origins.add(completeEvents.get(pos).getLocation());
			}
			
			googleDistanceMatrix = new GoogleDistanceMatrix(origins, nearLocations);
			List<List<GoogleDistance>> distanceMatrix = googleDistanceMatrix.getDistanceMatrix();
			
			int nearPos = getNearestPosition(origins, nearLocations, distanceMatrix);
						
			event.setLocation(nearLocations.get(nearPos));
			event.setExactLocation(1);
			completeEvents.add(pos, event);
		}
	}
	
	private int getNearestPosition(List<GooglePlace> origins, 
								List<GooglePlace> nearLocations,
								List<List<GoogleDistance> > distanceMatrix) {
		double minim = 24 * 3600;
		int nearPos = -1;
		
		for (int i = 0; i < nearLocations.size(); i++) {
			double dist = distanceMatrix.get(0).get(i).durationValue;
			if (origins.size() > 1) {
				dist += distanceMatrix.get(1).get(i).durationValue;
			}
			if (nearPos == -1 || minim > dist) {
				minim = dist;
				nearPos = i;
			}
		}
		return nearPos;
	}
	
	private List<GooglePlace> computeNearLocations(PlanEvent event, int pos1, int pos2) {
		List<GooglePlace> nearLocations = new ArrayList<GooglePlace>();
		GoogleNearbyLocations googleNearbyLocations; 
		GooglePlace place1 = null, place2 = null;
		if (pos1 != -1) {
			place1 = completeEvents.get(pos1).getLocation();
			
			
			googleNearbyLocations = new GoogleNearbyLocations(event.getLocation().getName(),
					place1.getCoords().latitude, place1.getCoords().longitude);
			
			nearLocations.addAll(googleNearbyLocations.getNearbyLocations());
						
			for (; nearLocations.size() > 3;) {
				nearLocations.remove(nearLocations.size() - 1);
			}
			
		}
		if (pos2 != -1) {
			place2 = completeEvents.get(pos2).getLocation();
			googleNearbyLocations = new GoogleNearbyLocations(event.getLocation().getName(),
					place2.getCoords().latitude, place2.getCoords().longitude);
			nearLocations.addAll(googleNearbyLocations.getNearbyLocations());
			
			for (; 5 < nearLocations.size(); ) {
				nearLocations.remove(nearLocations.size() - 1);
			}
		}
		
		return nearLocations;
	}
	
	private void computeDistances(List<PlanEvent> origins, List<PlanEvent> destinations) {
		if (origins.size() == 0 || destinations.size() == 0) {
			return;
		}
		System.out.println("origins : " + getGooglePlaces(origins) + "dest : " + getGooglePlaces(destinations));
		googleDistanceMatrix = new GoogleDistanceMatrix(
				getGooglePlaces(origins), getGooglePlaces(destinations));
		List<List<GoogleDistance>> distanceMatrix = googleDistanceMatrix.getDistanceMatrix();
		
		for (int i = 0; i < origins.size(); i++) {
			for (int j = 0; j < destinations.size(); j++) {
				distances.put(new Pair(origins.get(i).getLocation(), 
									   destinations.get(j).getLocation()),
						distanceMatrix.get(i).get(j));
			}
		}
	}
		
	private void fillLists() {
		for (PlanEvent event : allEvents) {
			if (event.getExactBeginDate() == 1) {
				if (event.getExactLocation() == 1) {
					completeEvents.add(event);
				} else {
					noPlaceEvents.add(event);
				}
			} else if (event.getExactLocation() == 1) {
				noTimeEvents.add(event);
			} else {
				freeEvents.add(event);
			}
		}
	}
	
	private List<GooglePlace> getGooglePlaces (List<PlanEvent> events) {
		List <GooglePlace> places = new ArrayList<GooglePlace>();
		
		for (PlanEvent event : events) {
			places.add(event.getLocation());
		}
		
		return places;
	}

	private void setCurrentLocationFirstEvent() {
		PlanEvent event = null;
		for (int i = 0; i < noTimeEvents.size(); i++) {
			if ("Current location".equals(noTimeEvents.get(i).getName())) {
				event = noTimeEvents.get(i);
				noTimeEvents.remove(i);
				break;
			}
		}
		if (event == null) {
			return;
		}
		addCurrentLocationToComplete(event);
	}
	
	void addCurrentLocationToComplete(PlanEvent event) {
		PlanEvent minEvent;
		int isComplete = 1;
		if (completeEvents.size () > 0) {
			minEvent = completeEvents.get(0);
		} else if (noPlaceEvents.size() > 0) {
			minEvent = noPlaceEvents.get(0);
			isComplete = 0;
		} else {
			// No time set for any event
			event.getBeginDate().setHours(9);
			event.getBeginDate().setMinutes(0);
			event.setExactBeginDate(1);
			completeEvents.add(event);
			return;
		}
		if (completeEvents.size() > 0 && noPlaceEvents.size() > 0) {
			if (minEvent.getBeginDateMinutes() > noPlaceEvents.get(0).getBeginDateMinutes()) {
				minEvent = noPlaceEvents.get(0);
				isComplete = 0;
			}
		}
		completeEvents.add(0, event);
		
		List<GooglePlace> origins = new ArrayList<GooglePlace>();
		List<GooglePlace> dest = new ArrayList<GooglePlace>();
		origins.add(event.getLocation());
		debug = 1;
		if (isComplete == 1) {
			dest.add(minEvent.getLocation());
		} else {
			dest.addAll(computeNearLocations(minEvent, 0, -1));
		}
		googleDistanceMatrix = new GoogleDistanceMatrix(origins, dest);
		List<List<GoogleDistance> > distanceMatrix = googleDistanceMatrix.getDistanceMatrix();
		
		int pos = getNearestPosition(origins, dest, distanceMatrix);
		if (isComplete == 0) {
			noPlaceEvents.get(0).setLocation(dest.get(pos));
			noPlaceEvents.get(0).setExactLocation(1);
			completeEvents.add(1, noPlaceEvents.get(0));
			noPlaceEvents.remove(0);
		}

		int dist = (int) distanceMatrix.get(0).get(pos).durationValue / 60;
		int mins = completeEvents.get(1).getBeginDateMinutes();
		mins = mins - dist - event.getDurationMinutes() - SAFE_ERROR_MINS;
		event.getBeginDate().setMinutes(mins % 60);
		event.getBeginDate().setHours(mins / 60);
		event.setExactBeginDate(1);
		System.out.println("debug : " + completeEvents + "\n" + noPlaceEvents + "\n");
	}
}
