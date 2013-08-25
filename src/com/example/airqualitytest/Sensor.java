package com.example.airqualitytest;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.example.airqualitytest.database.DBmanager;
import com.example.airqualitytest.database.JsonManagerBroadcast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;





public class Sensor implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String url = "http://collector-svil.mobileterritoriallab.eu/sensordrone/ws/saveSensorData/";
	String Temperature = null;
	String irTemperature = null;
	String Humidity = null;
	String Pressure = null;
	String RGBC = null;
	String Precision_GAS = null;
	String Capacitance = null;
	String Altitude = null;
	String ADC = null;
	String oxidizingGas = null;
	String reducingGas = null;
	private ArrayList<String> dataArray;
	static LocalBroadcastManager broadcaster;
	static final public String READ_PARAMETERS = "com.sensorDroneTest.READ_PARAMETERS";

	private final static String MY_PREFERENCES = "MyPref";
    // Costante relativa al nome della particolare preferenza
    private final static String TEXT_DATA_KEY = "LastUpdate";
	
	
	
	public String getOxidizingGas() {
		return oxidizingGas;
	}

	public void setOxidizingGas(String oxidizingGas) {
		this.oxidizingGas = oxidizingGas;
	}

	public String getReducingGas() {
		return reducingGas;
	}

	public void setReducingGas(String reducingGas) {
		this.reducingGas = reducingGas;
	}

	
	public ArrayList<String> getDataArray() {
		return dataArray;
	}

	private final String TAG = "database";

	public Sensor() {
		inizialize();
	}

	private void inizialize() {
		Log.d(TAG, "clear obj");
		Temperature = null;
		irTemperature = null;
		Humidity = null;
		Pressure = null;
		RGBC = null;
		Precision_GAS = null;
		Capacitance = null;
		Altitude = null;
		ADC = null;
		oxidizingGas = null;
		reducingGas = null;
	}

	public void buildArray() {
		dataArray = new ArrayList<String>();
		dataArray.add(getTemperature());
		dataArray.add(getHumidity());
		dataArray.add(getPressure());
		dataArray.add(getIrTemperature());
		dataArray.add(getRGBC());
		dataArray.add(getPrecision_GAS());
		dataArray.add(getADC());
		dataArray.add(getCapacitance());
		dataArray.add(getAltitude());
		dataArray.add(getReducingGas());
		dataArray.add(getOxidizingGas());
		// Log.d(TAG, "save data local db");
		String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
				.format(Calendar.getInstance().getTime());
		dataArray.add(timeStamp);
	}

	public Boolean saveData(Context cxt) {

		if (this.getHumidity() != null && this.getTemperature() != null && this.getPrecision_GAS() != null && this.getPressure() != null
				&& this.getAltitude() != null && this.getADC() != null && this.getOxidizingGas() != null && this.getReducingGas() != null) {
			this.buildArray();

			JSONObject listFinal = new JSONObject();

			try {
				TelephonyManager mTelephonyMgr;
				mTelephonyMgr = (TelephonyManager)cxt.getSystemService(Context.TELEPHONY_SERVICE);
				
				
				JSONObject list = new JSONObject();
				list.put("id", mTelephonyMgr.getDeviceId());
				list.put("temp", getTemperature());
				list.put("irtemp", getIrTemperature());
				list.put("humidity", getHumidity());
				list.put("pressure", getPressure());
				list.put("gas", getPrecision_GAS());
				list.put("lux", getRGBC());
				list.put("capacitance", getCapacitance());
				list.put("volts", getADC());
				list.put("altitude", getAltitude());
				list.put("oxidizing", getOxidizingGas());
				list.put("reducing", getReducingGas());

				JSONArray arrayEl = new JSONArray();
				arrayEl.put(list);

				listFinal.put("total", arrayEl.length());
				listFinal.put("array", arrayEl);

			} catch (Exception ex) {
				Log.d(TAG, ex.toString());
			}
			
			Log.d(TAG, listFinal.toString());
			JsonManagerBroadcast jmB = new JsonManagerBroadcast(url, listFinal, cxt);

			broadcaster = LocalBroadcastManager.getInstance(cxt);
			Log.d(TAG, broadcaster.toString());
			Intent intent = new Intent(READ_PARAMETERS);
			if (dataArray != null && dataArray.size()>=12)
				intent.putExtra(READ_PARAMETERS, dataArray);
			broadcaster.sendBroadcast(intent);
			
			
			saveArray(cxt, dataArray);

			DBmanager db = new DBmanager(cxt);
			db.open(); // apriamo il db

			db.insertSensorData(dataArray);
			db.close();
			Log.d(TAG, "saved on db");
			dataArray.clear();
			inizialize();

			return true;
		} else {
			return false;
		}

	}
	
	
	public static boolean saveArray(Context cxt, ArrayList<String> data)
	{
		SharedPreferences sp = cxt.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor mEdit1= sp.edit();
	         mEdit1.putInt("Status_size",data.size()); /*sKey is an array*/ 

	    for(int i=0;i<data.size();i++)  
	    {

	        mEdit1.remove("Status_" + i);
	        mEdit1.putString("Status_" + i, data.get(i));  
	    }

	    return mEdit1.commit();     
	}
	
	
	

	public String getTemperature() {
		return Temperature;
	}

	public void setTemperature(String temperature) {
		Temperature = temperature;
	}

	public String getIrTemperature() {
		return irTemperature;
	}

	public void setIrTemperature(String irTemperature) {
		this.irTemperature = irTemperature;
	}

	public String getHumidity() {
		return Humidity;
	}

	public void setHumidity(String humidity) {
		Humidity = humidity;
	}

	public String getPressure() {
		return Pressure;
	}

	public void setPressure(String pressure) {
		Pressure = pressure;
	}

	public String getRGBC() {
		return RGBC;
	}

	public void setRGBC(String rGBC) {
		RGBC = rGBC;
	}

	public String getPrecision_GAS() {
		return Precision_GAS;
	}

	public void setPrecision_GAS(String precision_GAS) {
		Precision_GAS = precision_GAS;
	}

	public String getCapacitance() {
		return Capacitance;
	}

	public void setCapacitance(String capacitance) {
		Capacitance = capacitance;
	}

	public String getAltitude() {
		return Altitude;
	}

	public void setAltitude(String altitude) {
		Altitude = altitude;
	}

	public String getADC() {
		return ADC;
	}

	public void setADC(String aDC) {
		ADC = aDC;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub

		String para = "temp: " + this.Temperature + " hum: " + this.Humidity;

		return para;
	}

}
