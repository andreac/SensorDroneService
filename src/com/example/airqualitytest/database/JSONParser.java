package com.example.airqualitytest.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {

	public final static int GET_MODE = 0;

	public final static int POST_MODE = 1;

	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";

	// constructor
	public JSONParser() {

	}

	public String getJSONStringFromUrlHttpGetMode(String url_select) {

		try {

			HttpClient client = new DefaultHttpClient();

			URI getURL = new URI(url_select);

			// Log.i("QUERY",getURL.getQuery());

			HttpGet get = new HttpGet(getURL);

			get.setHeader("Content-Type", "application/x-zip");
			get.setHeader("accept", "text/plain");

			HttpResponse responseGet = client.execute(get);

			HttpEntity resEntityGet = responseGet.getEntity();

			if (resEntityGet != null) {

				// Log.i("GET RESPONSE",EntityUtils.toString(resEntityGet));

				return EntityUtils.toString(resEntityGet);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}
		return null;
	}

	public String getJSONStringFromUrl(String url, JSONObject params) {

		// Making HTTP request
		try {
			// defaultHttpClient
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is
			// established.
			// The default value is zero, that means the timeout is not used.
			int timeoutConnection = 15000;
			HttpConnectionParams.setConnectionTimeout(httpParameters,
					timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT)
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 20000;

			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpPost httpPost = new HttpPost(url);
			
			if(params != null){
				StringEntity se = new StringEntity(params.toString());
				httpPost.setEntity(se);
			}
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			return sb.toString();
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}
		return null;
	}

	public JSONArray getJSONArrayFromUrl(String url) {
		JSONArray JArr = null;
		try {
			String jsonString = getJSONStringFromUrl(url,null);
			if (jsonString != null)
				JArr = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return JArr;
	}

	public JSONArray getJSONArrayFromUrl(String url, JSONObject params) {
		JSONArray JArr = null;
		try {
			String jsonString = getJSONStringFromUrl(url,params);
			if (jsonString != null)
				JArr = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return JArr;
	}
	
	public JSONArray getJSONArrayFromUrlHttpGetMode(String url) {
		JSONArray JArr = null;
		try {
			String jsonString = getJSONStringFromUrlHttpGetMode(url);
			if (jsonString != null)
				JArr = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return JArr;
	}

	public JSONObject getJSONObjectFromUrl(String url) {

		jObj = null;
		// try parse the string to a JSON object
		try {
			String jsonString = getJSONStringFromUrl(url,null);
			if (jsonString != null)
				jObj = new JSONObject(jsonString);
		} catch (Exception e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return jObj;

	}
	
	
	public JSONObject getJSONObjectFromUrl(String url, JSONObject params) {

		jObj = null;
		// try parse the string to a JSON object
		try {
			String jsonString = getJSONStringFromUrl(url,params);
			if (jsonString != null)
				jObj = new JSONObject(jsonString);
		} catch (Exception e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return jObj;

	}
	
	

	public JSONObject getJSONObjectFromUrlHttpGetMode(String url) {

		jObj = null;
		// try parse the string to a JSON object
		try {
			String jsonString = getJSONStringFromUrlHttpGetMode(url);
			if (jsonString != null)
				jObj = new JSONObject(jsonString);
		} catch (Exception e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return jObj;

	}

	public JSONArray getJSONArrayFromUrl(String url, final int method) {
	
		switch (method) {
		case GET_MODE:
			return getJSONArrayFromUrlHttpGetMode(url);
		case POST_MODE:
			return getJSONArrayFromUrl(url);
		default:
			break;
		}
		
		return null;
	}
	
	
	public JSONArray getJSONArrayFromUrl(String url, final int method, JSONObject params) {
		
		switch (method) {
		case GET_MODE:
			return getJSONArrayFromUrlHttpGetMode(url);
		case POST_MODE:
			return getJSONArrayFromUrl(url, params);
		default:
			break;
		}
		
		return null;
	}
	

	public JSONObject getJSONObjectFromUrl(String url, final int method) {
	
		switch (method) {
		case GET_MODE:
			return getJSONObjectFromUrlHttpGetMode(url);
		case POST_MODE:
			return getJSONObjectFromUrl(url);
		default:
			break;
		}
		
		return null;
	}
	
	public JSONObject getJSONObjectFromUrl(String url, final int method, JSONObject params) {
		
		switch (method) {
		case GET_MODE:
			return getJSONObjectFromUrlHttpGetMode(url);
		case POST_MODE:
			return getJSONObjectFromUrl(url,params);
		default:
			break;
		}
		
		return null;
	}
	
	private JSONObject parseList(JSONObject params){
		
		
		
		return null;
	}
}