package edu.pitt.cs.exposure;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import edu.pitt.cs.exposure.LocationService.ServiceBinder;

/**
 * provides a simple administrative interface to control the LocationTracker
 * service
 * 
 * @author ylegall
 */
public class MainActivity extends BaseActivity implements OnClickListener,
		OnSeekBarChangeListener, OnItemClickListener {

	private boolean isBound;
	private ServiceBinder binder;
	private ArrayAdapter<SimpleLocation> adapter;

	private Button startButton, stopButton;
	private SeekBar minuteSeek, hourSeek;
	private TextView statusText, providerText;
	private TextView minuteText, hourText;
	private ListView listView;
	
	private String username, password;

	/**
	 * called when the activity is create. get handles to widgets and setup
	 * event handlers.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "MainActivity: onCreate");
		
		setContentView(R.layout.main);

		startButton = (Button) findViewById(R.id.start_button);
		startButton.setOnClickListener(this);

		stopButton = (Button) findViewById(R.id.stop_button);
		stopButton.setOnClickListener(this);

		minuteSeek = (SeekBar) findViewById(R.id.seek_min);
		minuteSeek.setOnSeekBarChangeListener(this);

		hourSeek = (SeekBar) findViewById(R.id.seek_hour);
		hourSeek.setOnSeekBarChangeListener(this);

		statusText = (TextView) findViewById(R.id.status_value);
		providerText = (TextView) findViewById(R.id.provider_value);
		minuteText = (TextView) findViewById(R.id.seek_min_value);
		hourText = (TextView) findViewById(R.id.seek_hrs_value);

		adapter = new ArrayAdapter<SimpleLocation>(this, R.layout.list_item);
		listView = (ListView) findViewById(R.id.location_list);
		listView.setOnItemClickListener(this);
		listView.setAdapter(adapter);
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "MainActivity: onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		Log.i(TAG, "MainActivity: onStart");
		super.onStart();
		bindService(new Intent(this, LocationService.class), connection,
				BIND_AUTO_CREATE);
		loadSettings();
		updateStatus();
		updateLocations();
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "MainActivity: onStop");
		saveSettings();
		if (isBound) {
			unbindService(connection);
			isBound = false;
		}
		super.onStop();
	}

	/**
	 * use the sharedPreferences to load the state of the sliders.
	 */
	private void loadSettings() {
		Log.i(TAG, "MainActivity: loading settings");
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		int update_minutes = settings.getInt("update_minutes",
				DEFUALT_UPDATE_MIN);
		int update_hours = settings.getInt("update_hours", DEFUALT_UPDATE_HRS);
		minuteSeek.setProgress(update_minutes);
		hourSeek.setProgress(update_hours);
		
		// get the username and password
		String user = settings.getString("username", null);
		String pass = settings.getString("password",null);
		if (user == null || pass == null) {
			Log.i(TAG, "MainActivity: username and password are empty.");
			startActivityForResult(new Intent(this,LoginActivity.class), 0);
		} else {
			Log.i(TAG, "MainActivity: username & password acquired.");
		}
	}

	/**
	 * use the sharedPreferences to save the state of the sliders.
	 */
	private void saveSettings() {
		Log.i(TAG, "MainActivity: saving settings");
		SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putInt("update_minutes", minuteSeek.getProgress());
		editor.putInt("update_hours", hourSeek.getProgress());
		if (username != null) { editor.putString("username", username); }
		if (password != null) { editor.putString("password", password); }
		editor.commit();
	}

	/**
	 * the service calls this method to indicate that this activity should
	 * update its UI.
	 * 
	 * @param statusChange
	 *            the type of change of the service.
	 */
	void onServiceStatusChange(int statusChange) {
		switch (statusChange) {
			case LocationService.UPDATE_LOCATION:
				Log.i(TAG, "MainActivity: service location update");
				updateLocations();
				break;
			case LocationService.UPDATE_STATUS:
				Log.i(TAG, "MainActivity: service status update");
				updateStatus();
				break;
		}
	}

	/**
	 * updates the recent locations list.
	 */
	private void updateLocations() {
		Log.i(TAG, "MainActivity: updating location list");

		if (isBound) {
			adapter.clear();
			RingBuffer<SimpleLocation> buffer = binder.getRecentLocations();
			for (SimpleLocation loc : buffer) {
				adapter.add(loc);
			}
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * updates the buttons and text labels of this activity to reflect the
	 * recent status change in the LocationService.
	 */
	private void updateStatus() {
		Log.i(TAG, "MainActivity: checking service status");
		switch (LocationService.status) {

			case LocationService.STATUS_STARTED:
				statusText.setText(R.string.status_started_text);
				statusText.setTextColor(Color.CYAN);
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
				providerText.setText(LocationService.provider);
				break;

			// case LocationService.STATUS_PAUSED:
			// break;

			case LocationService.STATUS_STOPPED:
				statusText.setText(R.string.status_stopped_text);
				statusText.setTextColor(Color.RED);
				stopButton.setEnabled(false);
				startButton.setEnabled(true);
				providerText.setText("");
				break;
		}
	}

	/**
	 * callbacks for binding to the LocationService
	 */
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "MainActivity: service unbound");
			isBound = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "MainActivity: service bound");
			isBound = true;
			binder = (ServiceBinder) service;
			binder.registerActivity(MainActivity.this);
			updateStatus();
			updateLocations();
		}
	};

	/**
	 * communicate with Service
	 */
	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, LocationService.class);

		switch (v.getId()) {
			case R.id.start_button:
				Log.i(TAG, "MainActivity: start clicked");
				intent.putExtra("action", LocationService.ACTION_START);
				intent.putExtra("interval_minutes", minuteSeek.getProgress());
				intent.putExtra("interval_hours", hourSeek.getProgress());
				intent.putExtra("username", username);
				intent.putExtra("password", password);
				startService(intent);
				break;

			case R.id.stop_button:
				Log.i(TAG, "MainActivity: stop clicked");
				intent.putExtra("action", LocationService.ACTION_STOP);
				startService(intent);
				// stopService(intent); // the service can call stopSelf()
				break;
		}
	}

	/**
	 * update the service with the new settings
	 */
	@Override
	public void onProgressChanged(SeekBar seek, int prog, boolean fromUser) {
		switch (seek.getId()) {
			case R.id.seek_min:
				minuteText.setText(prog + "");
				break;
			case R.id.seek_hour:
				hourText.setText(prog + "");
				break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	/**
	 * called when the user stops sliding the bar. updates the interval of the
	 * service when the slider bars are modified.
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		switch (seekBar.getId()) {
			case R.id.seek_min:
				Log.i(TAG, "MainActivity: minute bar stopped at "
						+ seekBar.getProgress());
				break;
			case R.id.seek_hour:
				Log.i(TAG, "MainActivity: hour bar stopped at "
						+ seekBar.getProgress());
				break;
		}

		// if the service is running, then tell it to
		// change the interval at which it receives location updates
		if (LocationService.status == LocationService.STATUS_STARTED) {
			Intent intent = new Intent(this, LocationService.class);
			intent.putExtra("action", LocationService.ACTION_START);
			intent.putExtra("interval_minutes", minuteSeek.getProgress());
			intent.putExtra("interval_hours", hourSeek.getProgress());
			startService(intent);
		}
	}
	
	
	// called after the user enters a username and password
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// if the user aborts, then no data is returned, so quit
		if (data == null) {
			finish();
			return;
		}
		
		switch (requestCode) {
			case 0:
				username = data.getStringExtra("username");
				password = data.getStringExtra("password");
				saveSettings();
				if (LocationService.status == LocationService.STATUS_STARTED) {
					onClick(startButton);
				}
				break;
		}
	}
	
	/**
	 * callback for when one of the list items is clicked
	 * TODO: so something here?
	 */
	@Override
	public void onItemClick(AdapterView<?> av, View v, int i, long l) {
		Log.i(TAG, "MainActivity: list item clicked");
	}

}
