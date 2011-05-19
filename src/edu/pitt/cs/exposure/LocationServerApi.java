package edu.pitt.cs.exposure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.util.Log;

/**
 * A class to access the exposure server API at a given server.
 * 
 * @author Roman Schlegel
 */
public class LocationServerApi {

	private String _username;
	private String _password;
	// double _argLat;
	// double _argLon;
	// String _argAddress;

	/**
	 * The URL for the server, should be the complete except the last part,
	 * e.g.: http://saarinen.soic.indiana.edu/exposure/
	 */
	private String _serverURL;
	static final String DEFAULT_SERVER_URL = "http://saarinen.soic.indiana.edu/exposure/";

	private HttpClient _httpClient;

	/**
	 * Connection manager for HTTPS connection
	 */
	// private ThreadSafeClientConnManager _cm;
	/**
	 * Scheme registry needed for HTTPS connection.
	 */
	// private SchemeRegistry _schemeRegistry;
	/**
	 * HTTP parameters needed for HTTPS connection.
	 */
	// private BasicHttpParams _httpParams;

	/**
	 * Initialises a new server API using the default URL.
	 * 
	 * @param user The user used for authentication.
	 * @param pass The password for the the user.
	 */
	public LocationServerApi(String user, String pass) {
		this(user, pass, DEFAULT_SERVER_URL);
	}

	/**
	 * Initialises a new server API.
	 * 
	 * @param user The user used for authentication.
	 * @param pass The password for the user.
	 * @param url The base url for the API.
	 */
	public LocationServerApi(String user, String pass, String url) {
		_username = user;
		_password = pass;
		_serverURL = url;
		if (!_serverURL.endsWith("/")) {
			_serverURL = _serverURL + "/";
		}

		// initialise SSL
		SchemeRegistry reg = new SchemeRegistry();
		reg.register(new Scheme("https", new EasySSLSocketFactory(), 443));

		BasicHttpParams params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
				new ConnPerRouteBean(30));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		_httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(
				params, reg), params);
	}

	// /**
	// * This method is called by a new thread whenever a new request is made.
	// */
	// public void run() {
	// // double argLat = 0.0;
	// // double argLon = 0.0;
	// // String argAddress = null;
	//
	// synchronized (this) {
	// // copy arguments
	// // push location
	// // argLat = _argLat;
	// // argLon = _argLon;
	// // argAddress = _argAddress;
	//
	// // release thread
	// this.notify();
	// }
	//
	// // push location
	// try {
	// _pushLocation(_argLat, _argLon, _argAddress);
	// } catch (Exception e) {
	// }
	// }

	// /**
	// * This method pushes a location with the already given credentials to the
	// * server.
	// *
	// * @param lat The latitude of the location to push.
	// * @param lon The longitude of the location to push.
	// * @param address The address of the location to push. Optional, can be
	// null.
	// */
	// public void pushLocation(double lat, double lon, String address) {
	// // execute operation
	// synchronized (this) {
	// _argLat = lat;
	// _argLon = lon;
	// _argAddress = address;
	//
	// new Thread(this).start();
	// try {
	// this.wait();
	// } catch (InterruptedException ie) {
	// }
	// }
	// }

	/**
	 * @param lat
	 * @param lon
	 * @param address
	 */
	public void pushLocation(final double lat, final double lon,
			final String address) {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				_pushLocation(lat, lon, address);
			}
		}).start();
	}

	private void _pushLocation(double lat, double lon, String address) {

		try {
			// HttpClient hc = new DefaultHttpClient(_cm, _httpParams);

			// create post request
			HttpPost post = new HttpPost(_serverURL + "push_location");

			// set parameters
			List<NameValuePair> parameters = new ArrayList<NameValuePair>(5);
			parameters.add(new BasicNameValuePair("user", this._username));
			parameters.add(new BasicNameValuePair("pw", this._password));
			parameters.add(new BasicNameValuePair("lat", "" + lat));
			parameters.add(new BasicNameValuePair("lon", "" + lon));
			if (address != null) {
				parameters.add(new BasicNameValuePair("address", address));
			}
			post.setEntity(new UrlEncodedFormEntity(parameters));

			// execute request
			HttpResponse response = _httpClient.execute(post);
			Log.i(BaseActivity.TAG, "pushLocation: HTTP post: "
					+ response.getStatusLine().getStatusCode());

			// read response
			InputStream is = response.getEntity().getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			// read first line
			String line = br.readLine();
			if (line == null) {
				Log.e(BaseActivity.TAG, "Response from server is empty.");
			} else if (!line.startsWith("ok:")) {
				Log.e(BaseActivity.TAG, "Pushing location failed.");
			}
		} catch (UnsupportedEncodingException e) {
			Log.e(BaseActivity.TAG, "push location: " + e);
		} catch (ClientProtocolException e) {
			Log.e(BaseActivity.TAG, "push location: " + e);
		} catch (IllegalStateException e) {
			Log.e(BaseActivity.TAG, "push location: " + e);
		} catch (IOException e) {
			Log.e(BaseActivity.TAG, "push location: " + e);
		}
	}

}