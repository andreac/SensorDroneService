/***
  Copyright (c) 2009-11 CommonsWare, LLC
  
  Licensed under the Apache License, Version 2.0 (the "License"); you may
  not use this file except in compliance with the License. You may obtain
  a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.example.sensordronetest;

import java.util.ArrayList;
import java.util.EventObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.sensorcon.sdhelper.ConnectionBlinker;
import com.sensorcon.sdhelper.SDStreamer;
import com.sensorcon.sensordrone.Drone;
import com.sensorcon.sensordrone.Drone.DroneEventListener;
import com.sensorcon.sensordrone.Drone.DroneStatusListener;

public class AppService extends WakefulIntentService {
	
	public static Drone myDrone;
	private DroneEventListener deListener;
	private DroneStatusListener dsListener;
	private ConnectionBlinker myBlinker;
	private SDStreamer[] streamerArray;
	private int[] qsSensors;
	private Context cxt;
	private static Sensor sensorObj = null;
	static String TAG = "droneTest";
	static String TAGC = "Connect";
	static final public String READ_PARAMETERS = "com.sensorDroneTest.READ_PARAMETERS";
	boolean isConnect = false;
	boolean connect = false;
	Intent mainIntent;
	private final static String MY_PREFERENCES = "MyPref";
    // Costante relativa al nome della particolare preferenza
    private final static String TEXT_DATA_KEY = "MacAddress";
	
	
	public AppService() {
		super("AppService");
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		Log.i("AppService", "I'm awake!");
		mainIntent = intent;
		if(myDrone == null){
			myDrone = new Drone();
		}
		cxt = this;
		qsSensors = new int[] { 
				myDrone.QS_TYPE_TEMPERATURE,
				myDrone.QS_TYPE_HUMIDITY,
				myDrone.QS_TYPE_PRESSURE,
				myDrone.QS_TYPE_IR_TEMPERATURE,
				myDrone.QS_TYPE_PRECISION_GAS,
				myDrone.QS_TYPE_ADC, 
				myDrone.QS_TYPE_ALTITUDE,
				myDrone.QS_TYPE_OXIDIZING_GAS,
				myDrone.QS_TYPE_REDUCING_GAS};
		
		streamerArray = new SDStreamer[qsSensors.length];
		
		//enable all sensor
		for (int i = 0; i < qsSensors.length; i++) {
			streamerArray[i] = new SDStreamer(myDrone, qsSensors[i]);
		}
		
		this.droidEventListener();
		
		myDrone.registerDroneEventListener(deListener);
		
		if(sensorObj == null){
			sensorObj = new Sensor();
			
			
		}
//		sensorObj.buildArray();
//		ArrayList<String> tmp = new ArrayList<String>(sensorObj.getDataArray());
//		sendResult(tmp);
//		if(sensorObj.saveData(cxt))
//		{
//			Log.d(TAG, "data saved");
//		}
//		else
//		{
//			this.alertMessage("Sensor connect but not working");
//			this.doOnDisconnect();
//		}
		//connect = this.connect();
		
		SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
		String macAddress = prefs.getString(TEXT_DATA_KEY, "");
		if(macAddress!=""){
			sensorObj = new Sensor();
			if(!myDrone.isConnected){
				Log.d(TAG, "connetto");
				if(!myDrone.btConnect(macAddress)){
					alertMessage("Impossibile Connettersi");
				}
			}
			else
			{
				for (int i = 0; i < qsSensors.length; i++) {
					//streamerArray[i] = new SDStreamer(myDrone, qsSensors[i]);
					streamerArray[i].enable();
					// Enable the sensor					
				}
				Log.d(TAG, "giˆ connesso");
				readData();
			}
		}
		
		
		
	}
	private void readData(){
		
			Log.d(TAGC, "connesso");
			for (int i = 0; i < qsSensors.length; i++) {
				streamerArray[i].run();
			}
	}
	
	private boolean saveData(int param, String Value){
		
		switch (param) {
		case 7:
			sensorObj.setADC(Value);
			break;
		case 8:
			sensorObj.setAltitude(Value);
			break;
		case 6:
			sensorObj.setCapacitance(Value);
			break;
		case 1:
			sensorObj.setHumidity(Value);
			break;
		case 2:
			sensorObj.setPressure(Value);
			break;
		case 5:
			sensorObj.setPrecision_GAS(Value);
			break;
		case 3:
			sensorObj.setIrTemperature(Value);
			break;
		case 4:
			sensorObj.setRGBC(Value);
			break;
		case 0:
			sensorObj.setTemperature(Value);
			break;
		case 9:
			sensorObj.setOxidizingGas(Value);
			break;
		case 10:
			sensorObj.setReducingGas(Value);
			break;
		default:
			break;
		}
		if(sensorObj.saveData(cxt)){
			doOnDisconnect();
		}
		
		return true;
	}
	

	

	private boolean isBTEnabled(Context context, BluetoothAdapter btAdapter) {
		if (!btAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(enableBtIntent);
			//cxt.unregisterReceiver(btReceiver);
			return false;
		} else {
			return true;
		}
	}
	
	
	
	private void doOnDisconnect() {
		// Shut off any sensors that are on
		myBlinker.disable();
		Log.d(TAG, "disconnect");
		// Make sure the LEDs go off
		if (myDrone.isConnected) {
			Log.d(TAG, "led off");
			myDrone.setLEDs(0, 0, 0);
		}
		
		// Only try and disconnect if already connected
		if (myDrone.isConnected) {
			myDrone.disconnect();
		}
		
		//myDrone = null;
		//cxt.unregisterReceiver(btReceiver);
		//getApplicationContext().unregisterReceiver(btReceiver);
		
	}
	
	
	
	private void droidEventListener(){
		deListener = new DroneEventListener() {

			@Override
			public void adcMeasured(EventObject arg0) {
				// This is triggered the the external ADC pin is measured

				
				float adcVolts = myDrone.externalADC_Volts;
				//saveData(adcVolts,7);
				Log.d(TAG, "volts: " + adcVolts);
				saveData(7, Float.toString(adcVolts));
				//
			}

			@Override
			public void altitudeMeasured(EventObject arg0) {
				
				float altitude = myDrone.altitude_Meters;
				//saveData(altitude, 8);
				Log.d(TAG, "altitudine: " + altitude);
				
				saveData(8, Float.toString(altitude));
				
				
				//streamerArray[8].streamHandler.postDelayed(streamerArray[8],streamingRate);

			}

			@Override
			public void capacitanceMeasured(EventObject arg0) {
				
				float fmto = myDrone.capacitance_femtoFarad;
				//saveData(fmto, 6);
				Log.d(TAG, "capacitance: " + fmto);
				//sensorObj.setCapacitance(Float.toString(fmto));
				saveData(6, Float.toString(fmto));
				//streamerArray[6].streamHandler.postDelayed(streamerArray[6],streamingRate);

			}

			@Override
			public void connectEvent(EventObject arg0) {

				// Since we are adding SharedPreferences to store unit
				// preferences,
				// we might as well store the last MAC there. Now we can
				// press re-connect
				// to always try and connect to the last Drone (not just the
				// last one per
				// app instance)
				//alertMessage("Sensor Connected!!");
				myBlinker = new ConnectionBlinker(myDrone, 1000, 0, 255, 0);
				//if (myDrone.isConnected) {
				Log.d(TAG, "connected- blink");
					// Turn on our blinker
				myBlinker.enable();
				myBlinker.run();
				
				
				for (int i = 0; i < qsSensors.length; i++) {
					//streamerArray[i] = new SDStreamer(myDrone, qsSensors[i]);
					streamerArray[i].enable();
					// Enable the sensor					
				}
				
				readData();
				//service.setVisibility(View.VISIBLE);
				
				
			}

			@Override
			public void connectionLostEvent(EventObject arg0) {

				// Things to do if we think the connection has been lost.

				// Turn off the blinker
				myBlinker.disable();

				// notify the user
				
				Log.d(TAG, "Connection lost! Trying to re-connect!");
				alertMessage("Connection Lost!!");
				doOnDisconnect();
				
			}

			@Override
			public void customEvent(EventObject arg0) {

			}

			@Override
			public void disconnectEvent(EventObject arg0) {
				// notify the user
				//quickMessage("Disconnected!");
				Log.d(TAG,"Disconnected");
				//alertMessage("Sensor Disconnected!!");
				//doOnDisconnect();
			}

			@Override
			public void oxidizingGasMeasured(EventObject arg0) {
				float oxidizing = myDrone.oxidizingGas_Ohm;
				//saveData(humidity,1);
				Log.d(TAG, "oxidizing: " + oxidizing);
				
				saveData(9, Float.toString(oxidizing));
			}

			@Override
			public void reducingGasMeasured(EventObject arg0) {
				float redicing = myDrone.reducingGas_Ohm;
				//saveData(humidity,1);
				Log.d(TAG, "redicing: " + redicing);
				
				saveData(10, Float.toString(redicing));
			}

			@Override
			public void humidityMeasured(EventObject arg0) {
				float humidity = myDrone.humidity_Percent;
				//saveData(humidity,1);
				Log.d(TAG, "humidity: " + humidity);
				
				saveData(1, Float.toString(humidity));
				//streamerArray[1].streamHandler.postDelayed(streamerArray[1],streamingRate);

			}

			@Override
			public void i2cRead(EventObject arg0) {

			}

			@Override
			public void irTemperatureMeasured(EventObject arg0) {
				
				float temp = myDrone.irTemperature_Celcius;
				//saveData(temp,3);
				Log.d(TAG, "irTemp " +temp);
				saveData(3, Float.toString(temp));
				
				//streamerArray[3].streamHandler.postDelayed(streamerArray[3],streamingRate);

			}

			@Override
			public void precisionGasMeasured(EventObject arg0) {
				float ppmgas = myDrone.precisionGas_ppmCarbonMonoxide;
				//saveData(ppmgas,5);
				Log.d(TAG, "gas: " + ppmgas);
				saveData(5, Float.toString(ppmgas));
				
				//streamerArray[5].streamHandler.postDelayed(streamerArray[5],streamingRate);

			}

			@Override
			public void pressureMeasured(EventObject arg0) {
				
				float pressure = myDrone.pressure_Atmospheres;
				//saveData(pressure,2);
				Log.d(TAG, "pressure: " + pressure);
				
				saveData(2, Float.toString(pressure));
				//streamerArray[2].streamHandler.postDelayed(streamerArray[2],streamingRate);

			}

			@Override
			public void rgbcMeasured(EventObject arg0) {
				// The Lux value is calibrated for a (mostly) broadband
				// light source.
				// Pointing it at a narrow band light source (like and LED)
				// will bias the color channels, and provide a "wonky"
				// number.
				// Just for a nice look, we won't show a negative number.
				String msg = "";
				if (myDrone.rgbcLux >= 0) {
					msg = String.format("%.0f", myDrone.rgbcLux)
							+ " Lux";
				} else {
					msg = String.format("%.0f", 0.0) + " Lux";
				}
				Log.d(TAG, "lux: "+ msg);
				
				saveData(4, Float.toString(myDrone.rgbcLux));
				
				//saveData(myDrone.rgbcLux,4);
				//streamerArray[4].streamHandler.postDelayed(streamerArray[4],streamingRate);

			}

			@Override
			public void temperatureMeasured(EventObject arg0) {
				float temp = myDrone.temperature_Celcius;
				//saveData(temp,6);
				Log.d(TAG, "obj temp: " + temp);
				saveData(0, Float.toString(temp));
				
				//streamerArray[0].streamHandler.postDelayed(streamerArray[0],streamingRate);

			}

			@Override
			public void uartRead(EventObject arg0) {

			}

			@Override
			public void unknown(EventObject arg0) {

			}

			@Override
			public void usbUartRead(EventObject arg0) {

			}
		};
	}
	
	@SuppressLint("NewApi")
	private void alertMessage(String message){
		Notification noti = new Notification.Builder(cxt)
				.setContentTitle("Sensor Drone")
				.setContentText(message)
				.setSmallIcon(R.drawable.ic_launcher).build();

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(0, noti);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		//cxt.unregisterReceiver(btReceiver);
		super.onDestroy();
	}
}
