package com.example.database;

import org.json.JSONObject;



import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class JsonManagerBroadcast {
	
	private static String urlDown;
	private static Context conxt;
	private ConnectionDetector cd;
	Boolean isInternetPresent = false;
	
	
	public JsonManagerBroadcast(String url, JSONObject parmas, Context cxt) {
		
		urlDown = url;
		conxt = cxt;
		
		cd = new ConnectionDetector(conxt);
		isInternetPresent = cd.isConnectingToInternet();
		if(isInternetPresent){
			Log.d("Server", "send data");
			new JsonDownloader().execute(new JSONObject[]{parmas});
		}else
			Toast.makeText(conxt, "Connessione Internet non disponibile", Toast.LENGTH_LONG).show();
	}



class JsonDownloader extends AsyncTask<JSONObject, Void, JSONObject>{

	@Override
	protected JSONObject doInBackground(JSONObject... params) {
		// TODO Auto-generated method stub
		JSONObject nameValuePairs = params[0];
		return new JSONParser().getJSONObjectFromUrl(urlDown, nameValuePairs);
	}
	@Override
	protected void onPostExecute(JSONObject result) {
		try {
			Log.d("Server", result.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}



}
