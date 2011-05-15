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

public class MainActivity extends BaseActivity implements OnClickListener,
		OnSeekBarChangeListener, OnItemClickListener {

	private boolean isBound;
	private ServiceBinder binder;
	private ArrayAdapter<SimpleLocation> adapter;
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "MainActivity: onCreate");

		setContentView(R.layout.main);

		Button button = (Button) findViewById(R.id.start_button);
		button.setOnClickListener(this);

		button = (Button) findViewById(R.id.stop_button);
		button.setOnClickListener(this);

		SeekBar seek = (SeekBar) findViewById(R.id.seek_min);
		seek.setOnSeekBarChangeListener(this);

		seek = (SeekBar) findViewById(R.id.seek_hour);
		seek.setOnSeekBarChangeListener(this);
		
		// TODO: allow listView to be clicked
		adapter = new ArrayAdapter<SimpleLocation>(this,R.layout.list_item);
		ListView lv = (ListView)findViewById(R.id.location_list);
		lv.setOnItemClickListener(this);
		lv.setAdapter(adapter);
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "MainActivity: onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		Log.i(TAG, "MainActivity: onStart");
		super.onStart();
		bindService(new Intent(this, LocationService.class), connection, BIND_AUTO_CREATE);
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
	
	
//	@Override
//	protected void onPause() {
//		Log.i(TAG, "MainActivity: onPause");
//		unregisterReceiver(receiver);
//		super.onPause();
//	}
//
//	@Override
//	protected void onResume() {
//		Log.i(TAG, "MainActivity: onResume");
//		super.onResume();
//		IntentFilter filter = new IntentFilter(TAG);
//		registerReceiver(receiver, filter);
//	}

	/**
	 * 
	 */
	private void loadSettings() {
		
		// Restore preferences
		Log.i(TAG, "MainActivity: loading settings");
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		int update_minutes = settings.getInt("update_minutes", DEFUALT_UPDATE_MIN);
		int update_hours = settings.getInt("update_hours", DEFUALT_UPDATE_HRS);
		
		SeekBar seek = (SeekBar) findViewById(R.id.seek_min);
		seek.setProgress(update_minutes);
		
		seek = (SeekBar) findViewById(R.id.seek_hour);
		seek.setProgress(update_hours);
	}

	/**
	 * save the value of the settings
	 */
	private void saveSettings() {
		Log.i(TAG, "MainActivity: saving settings");
		SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
		
		SeekBar seek = (SeekBar) findViewById(R.id.seek_min);
		editor.putInt("update_minutes", seek.getProgress());
		
		seek = (SeekBar) findViewById(R.id.seek_hour);
		editor.putInt("update_hours", seek.getProgress());
		editor.commit();
	}
	
	/**
	 * the service calls this method to indicate
	 * that this activity should update its UI.
	 * @param statusChange
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
	 * updates the recent locations list
	 */
	private void updateLocations() {
		Log.i(TAG, "MainActivity: updating location list");
		TextView tv = (TextView)findViewById(R.id.count_value);
		tv.setText(LocationService.count + "");
		
		if (isBound) {
			adapter.clear();
			ListView lv = (ListView)findViewById(R.id.location_list);
			RingBuffer<SimpleLocation> buffer = binder.getRecentLocations();
			for (SimpleLocation loc: buffer) {
				adapter.add(loc);
			}
			adapter.notifyDataSetChanged();
			lv.invalidate();
		}
	}
	
	/**
	 * updates the buttons and text labels of this
	 * activity to reflect the recent status change in
	 * the LocationService
	 */
	private void updateStatus() {
		Button button;
		TextView tv;
		
		Log.i(TAG, "MainActivity: checking service status");
		switch (LocationService.status) {
			
			case LocationService.STATUS_STARTED:
				tv = (TextView)findViewById(R.id.status_value);
				tv.setText(R.string.status_started_text);
				tv.setTextColor(Color.CYAN);
				button = (Button)findViewById(R.id.start_button);
				button.setEnabled(false);
				button = (Button)findViewById(R.id.stop_button);
				button.setEnabled(true);
				break;
	
			case LocationService.STATUS_PAUSED:
				break;
	
			case LocationService.STATUS_STOPPED:
				tv = (TextView)findViewById(R.id.status_value);
				tv.setText(R.string.status_stopped_text);
				tv.setTextColor(Color.RED);
				button = (Button)findViewById(R.id.stop_button);
				button.setEnabled(false);
				button = (Button)findViewById(R.id.start_button);
				button.setEnabled(true);
				break;
		}
	}
	
	private int getProgress(int id) {
		return ((SeekBar)findViewById(id)).getProgress();
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
			binder = (ServiceBinder)service;
			binder.registerActivity(MainActivity.this);
			updateStatus();
			updateLocations();
		}
	};
	
	/**
	 * TODO: communicate with Service
	 */
	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, LocationService.class);
		
		switch (v.getId()) {
			case R.id.start_button:
				Log.i(TAG, "MainActivity: start clicked");
				intent.putExtra("action", LocationService.ACTION_START);
				intent.putExtra("interval_minutes", getProgress(R.id.seek_min));
				intent.putExtra("interval_hours", getProgress(R.id.seek_hour));
				startService(intent);
				break;
	
			case R.id.stop_button:
				Log.i(TAG, "MainActivity: stop clicked");
				intent.putExtra("action", LocationService.ACTION_STOP);
				startService(intent);
	//			stopService(intent);
				break;
				
			// TODO: pause button
		}
	}

	/**
	 * TODO: update the service with the new settings
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		TextView tv;
		switch (seekBar.getId()) {
			case R.id.seek_min:
				tv = (TextView) findViewById(R.id.seek_min_value);
				tv.setText(progress + "");
				break;
			case R.id.seek_hour:
				tv = (TextView) findViewById(R.id.seek_hrs_value);
				tv.setText(progress + "");
				break;
		}
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	/**
	 * called when the user stops sliding the bar.
	 * updates the interval of the service when
	 * the slider bars are modified.
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		switch (seekBar.getId()) {
			case R.id.seek_min:
				Log.i(TAG, "MainActivity: minute bar stopped at " + seekBar.getProgress());
				break;
			case R.id.seek_hour:
				Log.i(TAG, "MainActivity: hour bar stopped at " + seekBar.getProgress());
				break;
		}
		
		// if the service is running, then tell it to
		// change the interval at which it receives location updates
		if (LocationService.status == LocationService.STATUS_STARTED) {
			Intent intent = new Intent(this, LocationService.class);
			intent.putExtra("action", LocationService.ACTION_START);
			intent.putExtra("interval_minutes", getProgress(R.id.seek_min));
			intent.putExtra("interval_hours", getProgress(R.id.seek_hour));
			startService(intent);
		}
	}

	/**
	 * callback for when one of the list items is clicked
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Log.i(TAG, "MainActivity: list item clicked");
	}

}
