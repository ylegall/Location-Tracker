package edu.pitt.cs.exposure;

import android.location.Location;

/**
 * This class just overrides the default
 * toString method to make the recent locations list
 * look better.
 * 
 * @author ylegall
 *
 */
public class SimpleLocation extends Location {
	
	public SimpleLocation(Location l) {super(l);}
	public SimpleLocation(String provider) {super(provider);}

	@Override
	public String toString() {
		return new StringBuilder('(')
		.append(getLatitude())
		.append(", ")
		.append(getLongitude())
		.append(')')
		.toString();
	}
}
