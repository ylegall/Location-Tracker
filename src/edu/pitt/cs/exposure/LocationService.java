package edu.pitt.cs.exposure;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * this service runs in the background and receives location updates
 * periodically. It is designed to only be accessed by the "MainActivity"
 * component, which runs in the same application/process.
 * 
 * getting username
 * http://stackoverflow.com/questions/2727029/how-can-i-get-the-
 * google-username-on-android
 * 
 * @author ylegall
 */
public class LocationService extends Service implements LocationListener {

	private static final String TAG = BaseActivity.TAG;

	public static final int STATUS_STOPPED = 0;
	public static final int STATUS_STARTED = 1;
	public static final int STATUS_PAUSED = 2;

	public static final int ACTION_STOP = 3;
	public static final int ACTION_START = 4;
	public static final int ACTION_PAUSE = 5;

	public static final int UPDATE_STATUS = 6;
	public static final int UPDATE_LOCATION = 7;

	static int status;
	static int count;

	private LocationManager locationManager;
	private String provider;
	private long updateInterval; // milliseconds
	private MainActivity activity;
	private RingBuffer<SimpleLocation> ringBuffer;

	private ServiceBinder binder;

	/**
	 * when an activity binds to this service, it receives an instance of this
	 * inner class, which provides access to the following interface:
	 */
	class ServiceBinder extends Binder {

		/**
		 * allows the activity to register itself with this service for
		 * service-to-activity communication
		 * 
		 * @param activity
		 */
		void registerActivity(MainActivity ma) {
			activity = ma;
		}

		void unregisterActivity() {
			activity = null;
		}

		RingBuffer<SimpleLocation> getRecentLocations() {
			return ringBuffer;
		}
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "LocationService: onCreate");
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		ringBuffer = new RingBuffer<SimpleLocation>(
				BaseActivity.DEFUALT_HISTORY_SIZE);
		updateInterval = getMilliseconds(BaseActivity.DEFUALT_UPDATE_MIN,
				BaseActivity.DEFUALT_UPDATE_HRS);

		binder = new ServiceBinder();
	}

	private static long getMilliseconds(int minutes, int hours) {
		return minutes * 60000 + hours * 3600000;
	}

	/**
	 * this is invoked by the system whenever startService() is called with an
	 * Intent targeted for this service. This provides an easy way for an
	 * activity to communicate with or control this service.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "LocationService: onStartCommand");

		int action = intent.getIntExtra("action", ACTION_START);

		switch (action) {
			case ACTION_START:
				int minutes = intent.getIntExtra("interval_minutes",
						BaseActivity.DEFUALT_UPDATE_MIN);
				int hours = intent.getIntExtra("interval_hours",
						BaseActivity.DEFUALT_UPDATE_HRS);
				updateInterval = getMilliseconds(minutes, hours);
				provider = findProvider();
				if (status == STATUS_STARTED) {
					locationManager.removeUpdates(this);
				}
				listenForLocation();
				break;

			case ACTION_STOP:
				stopListening();
				status = STATUS_STOPPED;
				stopSelf();
				break;

			case ACTION_PAUSE:
				// TODO:
				break;
		}

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	/**
	 * recomputes the best location provider. the criteria for selecting a
	 * provider can be configured.
	 */
	private String findProvider() {
		Criteria c = new Criteria();
		// c.setPowerRequirement(Criteria.POWER_LOW);
		c.setCostAllowed(false);
		c.setAccuracy(Criteria.ACCURACY_FINE);
		return locationManager.getBestProvider(c, true);
	}

	/**
	 * Subscribe for location updates
	 */
	private void listenForLocation() {
		Log.i(TAG, "LocationService: listening for location. interval = "
				+ updateInterval);
		Log.i(TAG, "LocationService: provider = " + provider);
		locationManager.requestLocationUpdates(this.provider, updateInterval,
				0, this);
		status = STATUS_STARTED;
		notifyActivity(UPDATE_STATUS);
		Toast.makeText(this, "service started", Toast.LENGTH_SHORT).show();
	}

	/**
	 * unsubscribe from location updates
	 */
	private void stopListening() {
		Log.i(TAG, "LocationService: NOT listening for location!");
		locationManager.removeUpdates(this);
		status = STATUS_STOPPED;
		notifyActivity(UPDATE_STATUS);
		Toast.makeText(this, "service stopped", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 
	 * @param statusChange
	 */
	private void notifyActivity(int statusChange) {
		if (activity != null) {
			activity.onServiceStatusChange(statusChange);
		}
	}

	/**
	 * called when this service component is being destroyed.
	 */
	@Override
	public void onDestroy() {
		Log.d(TAG, "LocationService: onDestroy");
		binder.unregisterActivity();
		stopListening();
	}

	/**
	 * called when a new location fix is acquired. TODO transmit location to
	 * remote database
	 */
	@Override
	public void onLocationChanged(Location location) {
		Log.i(TAG, "LocationService: location updated");
		ringBuffer.add(new SimpleLocation(getApplicationContext(), location));
		count++;
		notifyActivity(UPDATE_LOCATION);
		Toast.makeText(this, "location updated", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "location = (" + location.getLatitude() + ","
				+ location.getLongitude() + ")");
	}

	@Override
	public void onProviderDisabled(String arg0) {
		Log.i(TAG, "LocationService: provider disabled: " + arg0);
	}

	@Override
	public void onProviderEnabled(String arg0) {
		Log.i(TAG, "LocationService: provider enabled: " + arg0);
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		Log.d(TAG, "LocationService: status changed: " + arg0);
		switch (arg1) {
			case LocationProvider.AVAILABLE:
				Log.d(TAG, "\t available");
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Log.d(TAG, "\t temporarily unavailable");
				break;
			case LocationProvider.OUT_OF_SERVICE:
				Log.d(TAG, "\t out of service");
				break;
		}
	}
}
