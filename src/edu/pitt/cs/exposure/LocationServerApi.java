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
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.util.Log;

/**
 * handles pushing Location data to the server.
 */
public class LocationServerApi {

	/**
	 * The URL for the server: http://saarinen.soic.indiana.edu/exposure/
	 */
	private String _serverURL;
	static final String DEFAULT_SERVER_URL = "http://saarinen.soic.indiana.edu/exposure/";

	private ClientConnectionManager _manager;
	private BasicHttpParams _params;
	private NameValuePair _username;
	private NameValuePair _password;
	
	/**
	 * Initializes a new server API using the default URL.
	 * @param user The user used for authentication.
	 * @param pass The password for the the user.
	 */
	public LocationServerApi(String user, String pass) {
		this(user, pass, DEFAULT_SERVER_URL);
	}

	/**
	 * Initializes a new server API.
	 * @param user The user used for authentication.
	 * @param pass The password for the user.
	 * @param url The base URL for the API.
	 */
	public LocationServerApi(String user, String pass, String url) {

		_username = new BasicNameValuePair("user", user);
		_password = new BasicNameValuePair("pw", pass);
		_serverURL = url;
		if (!_serverURL.endsWith("/")) {
			_serverURL += "/";
		}

		// initialize SSL
		SchemeRegistry reg = new SchemeRegistry();
		reg.register(new Scheme("https", new EasySSLSocketFactory(), 443));

		// BasicHttpParams params = new BasicHttpParams();
		_params = new BasicHttpParams();
		// _params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		// _params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
		// new ConnPerRouteBean(30));
		_params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(_params, HttpVersion.HTTP_1_1);

//		_manager = new ThreadSafeClientConnManager(_params, reg);
		_manager = new SingleClientConnManager(_params, reg);
	}

	/**
	 * pushes the location data to the server
	 * @param lat
	 * @param lon
	 * @param address
	 */
	public void pushLocation(final double lat, final double lon,
			final String address) {

//		_pushLocation(lat, lon, address);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				_pushLocation(lat, lon, address);
			}
		}).start();
	}

	/**
	 * 
	 * @param lat
	 * @param lon
	 * @param address
	 */
	private void _pushLocation(double lat, double lon, String address) {

		HttpClient client = new DefaultHttpClient(_manager, _params);

		try {

			// create post request
			HttpPost post = new HttpPost(_serverURL + "push_location");

			// set parameters
			List<NameValuePair> parameters = new ArrayList<NameValuePair>(5);
			parameters.add(_username);
			parameters.add(_password);
			parameters.add(new BasicNameValuePair("lat", "" + lat));
			parameters.add(new BasicNameValuePair("lon", "" + lon));
			if (address != null) {
				parameters.add(new BasicNameValuePair("address", address));
			}
			post.setEntity(new UrlEncodedFormEntity(parameters));

			// execute request
			HttpResponse response = client.execute(post);
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

			br.close();

		} catch (UnsupportedEncodingException e) {
			Log.e(BaseActivity.TAG, "push location: " + e);
		} catch (ClientProtocolException e) {
			Log.e(BaseActivity.TAG, "push location: " + e);
		} catch (IllegalStateException e) {
			Log.e(BaseActivity.TAG, "push location: " + e);
		} catch (IOException e) {
			Log.e(BaseActivity.TAG, "push location: " + e);
		}
		
//		finally {
//			client.getConnectionManager().shutdown();
//		}
	}
	
	/**
	 * shut down the connection manager
	 */
	public void close() {
		if (_manager != null) {
			_manager.shutdown();
		}
	}

}