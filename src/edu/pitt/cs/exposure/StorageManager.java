package edu.pitt.cs.exposure;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.util.Log;

/**
 * Simple class to save and load past locations.
 * @author ylegall
 */
public class StorageManager {
	
	private Context context;
	private String filename;
	
	public StorageManager(Context context) {
		this.context = context;
		filename = context.getString(R.string.app_name);
	}
	
	/**
	 * TODO: change this to use some format like JSON
	 * @param index
	 * @param location
	 */
	void save(RingBuffer<Location> ringBuffer) {
		try {
			FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
			PrintWriter writer = new PrintWriter(fos);
			for (Location location : ringBuffer) {
				writer.println(
						location.getLatitude() + ',' +
						location.getLongitude() + ',' + 
						location.getTime());
			}
		} catch (FileNotFoundException e) {
			Log.e(BaseActivity.TAG, e.getMessage());
		}
	}
	
	/**
	 * TODO: change this to use some standard
	 * format like JSON
	 * @return
	 */
	List<Location> load() {
		List<Location> list = new ArrayList<Location>();
		
		try {
			FileInputStream fis = context.openFileInput(filename);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			while((line = reader.readLine()) != null) {
				Location loc = new Location("unknown");
				String[] tokens = line.split(",");
				loc.setLatitude(Double.parseDouble(tokens[0]));
				loc.setLongitude(Double.parseDouble(tokens[1]));
				loc.setTime(Long.parseLong(tokens[2]));
				list.add(loc);
			}
		} catch (IOException e) {
			Log.e(BaseActivity.TAG, e.getMessage());
		}
//		Collections.reverse(list);
		return list;
	}
}
