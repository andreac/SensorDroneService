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

package com.example.airqualitytest;

import android.support.v4.app.NotificationCompat;
import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;

import android.util.Log;


import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.sensorcon.sensordrone.DroneEventHandler;
import com.sensorcon.sensordrone.DroneEventObject;
import com.sensorcon.sensordrone.android.*;
import com.sensorcon.sensordrone.android.tools.DroneConnectionHelper;
import com.sensorcon.sensordrone.android.tools.DroneQSStreamer;

public class AppService extends WakefulIntentService {
	
	public static Drone myDrone;
	//private ConnectionBlinker myBlinker;
	//private SDStreamer[] streamerArray;
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
	
    
    public DroneQSStreamer []streamerList;
    public boolean lowbatNotify;
    public DroneConnectionHelper myHelper;
    public DroneEventHandler droneHandler;
    
    Object lock;
	
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
				myDrone.QS_TYPE_HUMIDITY,
				myDrone.QS_TYPE_PRESSURE,
				myDrone.QS_TYPE_IR_TEMPERATURE,
				myDrone.QS_TYPE_PRECISION_GAS,
				myDrone.QS_TYPE_ALTITUDE,
				};
		
		myHelper = new DroneConnectionHelper();
		
		streamerList = new DroneQSStreamer[qsSensors.length];
		
		for (int i = 0; i < qsSensors.length; i++) {
			streamerList[i] = new DroneQSStreamer(myDrone, qsSensors[i]);
		}
		
		if(sensorObj == null){
			sensorObj = new Sensor();
			
			
		}
		
		
		
        droneHandler = new DroneEventHandler() {
            @Override
            public void parseEvent(DroneEventObject droneEventObject) {
            	Log.d(TAG, droneEventObject.toString());
                if (droneEventObject.matches(DroneEventObject.droneEventType.CONNECTED)) {
                    
                    // Things to do when we connect to a Sensordrone
                    Log.d(TAG, "connesso");
                   
                    // Flash teh LEDs green
                    myDrone.setLEDs(0,0,126);
                    // People don't need to know how to connect if they are already connected
                    
                    // Notify if there is a low battery
                    lowbatNotify = true;


                    for (int i = 0; i < qsSensors.length; i++) {
                    	Log.d(TAG, "enable Sensors");
                	   
                    	myDrone.quickEnable(qsSensors[i]);
                    	streamerList[i].enable();
                	   
                    }
                   
                } else if (droneEventObject.matches(DroneEventObject.droneEventType.CONNECTION_LOST)) {
                    // If in the middle of a countdown, stop it
                	alertMessage("Connection Lost");
                    // notify the user
                   

                } else if (droneEventObject.matches(DroneEventObject.droneEventType.DISCONNECTED)) {
                    // notify the user
                	Log.d(TAG, "disconesso");
                } else if (droneEventObject.matches(DroneEventObject.droneEventType.LOW_BATTERY)) {
                   
                    if (lowbatNotify && myDrone.batteryVoltage_Volts < 3.1) {
                    	alertMessage("Low Battery");
                        lowbatNotify = false; // Set true again in connectEvent
                        doOnDisconnect(); // run our disconnect routine
                        // Notify the user
                       
                    }
                } else if (droneEventObject.matches(DroneEventObject.droneEventType.HUMIDITY_ENABLED)) {
                    streamerList[0].run();
                    Log.d(TAG, "enable humidity");
                }else if (droneEventObject.matches(DroneEventObject.droneEventType.HUMIDITY_MEASURED)) {
                    float humidity = myDrone.humidity_Percent ;
                    Log.d(TAG, humidity + " humidity");
                    saveData(0, Float.toString(humidity));
                    
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.PRESSURE_ENABLED)) {
                    streamerList[1].run();
                }else if (droneEventObject.matches(DroneEventObject.droneEventType.PRESSURE_MEASURED)) {
                    float pressure = myDrone.pressure_Atmospheres ;
                    Log.d(TAG, pressure + " pressure");
                    saveData(1, Float.toString(pressure));
                    
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.IR_TEMPERATURE_ENABLED)) {
                    streamerList[2].run();
                }else if (droneEventObject.matches(DroneEventObject.droneEventType.IR_TEMPERATURE_MEASURED)) {
                    float temp = myDrone.irTemperature_Celsius ;
                    Log.d(TAG, temp + " irTemp");
                    saveData(2, Float.toString(temp));
                    
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.PRECISION_GAS_ENABLED)) {
                    streamerList[3].run();
                }else if (droneEventObject.matches(DroneEventObject.droneEventType.PRECISION_GAS_MEASURED)) {
                    float gas = myDrone.precisionGas_ppmCarbonMonoxide ;
                    Log.d(TAG, gas + " gas");
                    saveData(3, Float.toString(gas));
                    
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.ALTITUDE_ENABLED)) {
                	Log.d(TAG, "enable altitude");
                    streamerList[4].run();
                }else if (droneEventObject.matches(DroneEventObject.droneEventType.ALTITUDE_MEASURED)) {
                    float altitude = myDrone.altitude_Meters ;
                    Log.d(TAG, altitude + " altitude");
                    saveData(4, Float.toString(altitude));
                    
                }
            }// parseEvent
        };


        myDrone.registerDroneListener(droneHandler);
	
		
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
				
				Log.d(TAG, "giˆ connesso");
				//readData();
				
			}
		}
		lock = new Object();

        synchronized (lock) {
            // Give 10 seconds to complete task, then give up.
            // To take less time, call a sync'ed lock.notify();
            // when your are done in droneEventHandler.
            try {
                lock.wait(10000);
                
                //doOnDisconnect();
            } catch (InterruptedException e) {
                Log.d(TAG,"I took longer than 10s; giving up!");
            }
        }
		
	}

	
	private boolean saveData(int param, String Value){
		Log.d(TAG, "save data: " + param + " value: " + Value);
		switch (param) {
		case 7:
			sensorObj.setADC(Value);
			break;
		case 4:
			sensorObj.setAltitude(Value);
			break;
		case 6:
			sensorObj.setCapacitance(Value);
			break;
		case 0:
			sensorObj.setHumidity(Value);
			break;
		case 1:
			sensorObj.setPressure(Value);
			break;
		case 3:
			sensorObj.setPrecision_GAS(Value);
			break;
		case 2:
			sensorObj.setIrTemperature(Value);
			break;
		case 8:
			sensorObj.setRGBC(Value);
			break;
		case 5:
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
			//lock.notifyAll();
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
		//myBlinker.disable();
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
	
	
	
	
	
	private void alertMessage(String message){
		
		Notification not = new NotificationCompat.Builder(cxt)
				.setContentTitle("Sensor Drone")
				.setContentText(message)
				.setSmallIcon(R.drawable.ic_launcher).build();

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Hide the notification after its selected
		not.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(0, not);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		//cxt.unregisterReceiver(btReceiver);
		myDrone.unregisterDroneListener(droneHandler);
		super.onDestroy();
	}
}
