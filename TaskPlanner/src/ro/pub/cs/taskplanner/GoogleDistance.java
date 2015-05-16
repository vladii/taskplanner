package ro.pub.cs.taskplanner;

/* Holder for information retrieved by GoogleDistanceMatrix. */
/* Constains information about distance, measured in hours and meters. */
public class GoogleDistance {
	public String distanceText;
	public double distanceValue;
	public String durationText;
	public double durationValue;
	
	public GoogleDistance(String distanceText, double distanceValue,
						  String durationText, double durationValue) {
		this.distanceText = distanceText;
		this.distanceValue = distanceValue;
		this.durationText = durationText;
		this.durationValue = durationValue;
	}
	
	@Override
	public String toString() {
		return "Distance: " + distanceText + "/" + distanceValue + ", " +
			   "Duration: " + durationText + "/" + durationValue;
	}
}
