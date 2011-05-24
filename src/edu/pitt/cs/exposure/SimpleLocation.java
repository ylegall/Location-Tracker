package edu.pitt.cs.exposure;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

/**
 * This class just overrides the default toString method to make the recent
 * locations list look better.
 * 
 * @author ylegall
 */
public class SimpleLocation extends Location {

	private Context context;

	public SimpleLocation(Context context, Location l) {
		super(l);
		this.context = context;
	}

	public SimpleLocation(Context context, String provider) {
		super(provider);
		this.context = context;
	}

	public String getAddressString() {
//		if (Geocoder.isPresent()) {		// API level 9
			Geocoder coder = new Geocoder(context, Locale.getDefault());
			try {
				List<Address> addresses = coder.getFromLocation(getLatitude(),
						getLongitude(), 1);
				if (addresses != null && !addresses.isEmpty()) {
					Address a = addresses.get(0);
					StringBuilder sb = new StringBuilder();
					for (int i=0; i < a.getMaxAddressLineIndex(); i++) {
						sb.append(a.getAddressLine(i)).append('\n');
					}
					
//					sb.append(a.getCountryName()).append(',');
//					sb.append(a.getLocality()).append(',');
//					sb.append(a.get)
//					sb.append(a.getPostalCode());
					
					return sb.toString();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
//		} else {
//			Log.d(BaseActivity.TAG, "geocoder not present.");
//		}

		Log.d(BaseActivity.TAG, "failed to get address from geocoder.");
		return getCoordinateString();
	}

	public String getCoordinateString() {
		return new StringBuilder().append('(').append(getLatitude()).append(
				", ").append(getLongitude()).append(')').toString();
	}

	@Override
	public String toString() {
//		return getAddressString();
		 return getCoordinateString();
	}
}
