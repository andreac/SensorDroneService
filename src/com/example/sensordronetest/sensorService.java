package com.example.sensordronetest;


import java.util.ArrayList;
import java.util.EventObject;

import com.sensorcon.sdhelper.ConnectionBlinker;
import com.sensorcon.sdhelper.SDHelper;
import com.sensorcon.sdhelper.SDStreamer;
import com.sensorcon.sensordrone.Drone;
import com.sensorcon.sensordrone.Drone.DroneEventListener;
import com.sensorcon.sensordrone.Drone.DroneStatusListener;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;


public class sensorService extends Service {
	private String MAC; // The MAC address we will connect to
	private BluetoothAdapter btAdapter;
	private BroadcastReceiver btReceiver;
	private String TAGC = "Connect";
	private String TAG = "Sensor";
	private IntentFilter btFilter;
	public static Drone myDrone;
	private DroneEventListener deListener;
	private DroneStatusListener dsListener;
	private ConnectionBlinker myBlinker;
	private SDStreamer[] streamerArray;
	private int[] qsSensors;
	private Context cxt;
	private static Sensor sensorObj = null;
	static LocalBroadcastManager broadcaster;
	static final public String READ_PARAMETERS = "com.sensorDroneTest.READ_PARAMETERS";
	
	 @Override  
	public void onCreate() {
		 
		 broadcaster = LocalBroadcastManager.getInstance(this);
		 
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		buildUpdate();

		return super.onStartCommand(intent, flags, startId);
	}

	private void buildUpdate() {
		Log.d(TAGC, "start service");
		if(myDrone == null)
		{
			myDrone = new Drone();
			cxt = this;
			qsSensors = new int[] { myDrone.QS_TYPE_TEMPERATURE,
					myDrone.QS_TYPE_HUMIDITY,
					myDrone.QS_TYPE_PRESSURE,
					myDrone.QS_TYPE_IR_TEMPERATURE,
					myDrone.QS_TYPE_PRECISION_GAS,
					myDrone.QS_TYPE_ADC, 
					myDrone.QS_TYPE_ALTITUDE };
			
			streamerArray = new SDStreamer[qsSensors.length];
			
			//enable all sensor
			for (int i = 0; i < qsSensors.length; i++) {
				streamerArray[i] = new SDStreamer(myDrone, qsSensors[i]);
			}
			
			this.droidEventListener();
			
			myDrone.registerDroneEventListener(deListener);
			
			connect(myDrone, this);
		}
		else if (!myDrone.isConnected)
		{
			connect(myDrone, this);
		}
		else if(myDrone.isConnected)
		{
			
			
			if(sensorObj != null){
				sensorObj.buildArray();
				ArrayList<String> tmp = new ArrayList<String>(sensorObj.getDataArray());
				sendResult(tmp);
				if(sensorObj.saveData(cxt))
				{
					Log.d(TAG, "data saved");
				}
				else
				{
					this.alertMessage("Sensor connect but not working");
					this.doOnDisconnect();
				}
				
			}
			else{
				sensorObj = new Sensor();
			}
			
			Log.d(TAGC, "connesso");
			for (int i = 0; i < qsSensors.length; i++) {
				streamerArray[i].run();
			}
			
		}
//		else{	
//			
//			Log.d(TAG, "connect but obj null");
//			this.alertMessage("Sensor connect but not working");
//			this.doOnDisconnect();
//		}
	}
	
	public void sendResult(ArrayList<String> obj) {
		Log.d(TAG, "send broadcast");
	    Intent intent = new Intent(READ_PARAMETERS);
	    if(obj != null)
	        intent.putExtra(READ_PARAMETERS, obj);
	    broadcaster.sendBroadcast(intent);
	}
	
	
	
	
	
	private void connect(final Drone myDrone, Context cxt){
		// Set up our Bluetooth Adapter
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// Is Bluetooth on?
		boolean isOn = isBTEnabled(cxt, btAdapter);

		if (!isOn) {
			// Don't proceed until the user turns Bluetooth on.
			Log.d(TAGC, "bt is not enable");
			return;
		}

		// Make sure MAC String is initialized and empty
		MAC = "";
		
		// What to do when we find a device
		btReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				// When discovery finds a device
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					// We only want Sensordrones
					try {
						if (device.getName().contains("drone")) {
							// Add the Name and MAC
							Log.d(TAGC,device.getName() + "\n"+ device.getAddress());
							MAC = device.getAddress();
							Log.d(TAGC, MAC);
							if (!TextUtils.isEmpty(MAC)) {
								// Display a message if the connect fails
								if (!myDrone.btConnect(MAC)) {
									Log.d(TAGC, "not connected");
									alertMessage("Connection lost! try to reconnect!");
								}
								else
								{
									Log.d(TAGC, "device connect");
									
								}
							}
						}
					} catch (NullPointerException n) {
						// Some times getName() will return null, which doesn't
						// parse very well :-)
						// Catch it here
						Log.d(TAGC, "eccezione " + n.toString());
						// You can still add it to the list if you want, it just
						// might not
						// be a Sensordrone...
						// macAdapter.add("nullDevice" + "\n" +
						// device.getAddress());
					}
				}
			}
		};
		// Set up our IntentFilters
		btFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		cxt.registerReceiver(btReceiver, btFilter);
		// Don't forget to unregister when done!
		// Start scanning for Bluetooth devices
		btAdapter.startDiscovery();
		
		
	}
	
	private boolean isBTEnabled(Context context, BluetoothAdapter btAdapter) {
		if (!btAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(enableBtIntent);
			return false;
		} else {
			return true;
		}
	}

	private void droidEventListener(){
		deListener = new DroneEventListener() {

			@Override
			public void adcMeasured(EventObject arg0) {
				// This is triggered the the external ADC pin is measured

				
				float adcVolts = myDrone.externalADC_Volts;
				//saveData(adcVolts,7);
				Log.d(TAG, "volts: " + adcVolts);
				sensorObj.setADC(Float.toString(adcVolts));
			}

			@Override
			public void altitudeMeasured(EventObject arg0) {
				
				float altitude = myDrone.altitude_Meters;
				//saveData(altitude, 8);
				Log.d(TAG, "altitudine: " + altitude);
				sensorObj.setAltitude(Float.toString(altitude));
				//streamerArray[8].streamHandler.postDelayed(streamerArray[8],streamingRate);

			}

			@Override
			public void capacitanceMeasured(EventObject arg0) {
				
				float fmto = myDrone.capacitance_femtoFarad;
				//saveData(fmto, 6);
				Log.d(TAG, "capacitance: " + fmto);
				sensorObj.setCapacitance(Float.toString(fmto));
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
				alertMessage("Sensor Connected!!");
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
				
				
				btAdapter.cancelDiscovery();
				cxt.unregisterReceiver(btReceiver);
				
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
				// Try to reconnect once, automatically
				/*if (myDrone.btConnect(myDrone.lastMAC)) {
					// A brief pause
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					connectionLostReconnect();
				} else {
					quickMessage("Re-connect failed");
					doOnDisconnect();
				}*/
			}

			@Override
			public void customEvent(EventObject arg0) {

			}

			@Override
			public void disconnectEvent(EventObject arg0) {
				// notify the user
				//quickMessage("Disconnected!");
				Log.d(TAG,"Disconnected");
				alertMessage("Sensor Disconnected!!");
				doOnDisconnect();
			}

			@Override
			public void oxidizingGasMeasured(EventObject arg0) {

			}

			@Override
			public void reducingGasMeasured(EventObject arg0) {

			}

			@Override
			public void humidityMeasured(EventObject arg0) {
				float humidity = myDrone.humidity_Percent;
				//saveData(humidity,1);
				Log.d(TAG, "humidity: " + humidity);
				sensorObj.setHumidity(Float.toString(humidity));
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
				sensorObj.setIrTemperature(Float.toString(temp));
				//streamerArray[3].streamHandler.postDelayed(streamerArray[3],streamingRate);

			}

			@Override
			public void precisionGasMeasured(EventObject arg0) {
				float ppmgas = myDrone.precisionGas_ppmCarbonMonoxide;
				//saveData(ppmgas,5);
				Log.d(TAG, "gas: " + ppmgas);
				sensorObj.setPrecision_GAS(Float.toString(ppmgas));
				//streamerArray[5].streamHandler.postDelayed(streamerArray[5],streamingRate);

			}

			@Override
			public void pressureMeasured(EventObject arg0) {
				
				float pressure = myDrone.pressure_Atmospheres;
				//saveData(pressure,2);
				Log.d(TAG, "pressure: " + pressure);
				sensorObj.setPressure(Float.toString(pressure));
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
				sensorObj.setRGBC(Float.toString(myDrone.rgbcLux));
				//saveData(myDrone.rgbcLux,4);
				//streamerArray[4].streamHandler.postDelayed(streamerArray[4],streamingRate);

			}

			@Override
			public void temperatureMeasured(EventObject arg0) {
				float temp = myDrone.temperature_Celcius;
				//saveData(temp,6);
				Log.d(TAG, "obj temp: " + temp);
				sensorObj.setTemperature(Float.toString(temp));
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
	
	
	private void droidStatusListener(){
		dsListener = new DroneStatusListener() {

			@Override
			public void adcStatus(EventObject arg0) {
				// This is triggered when the status of the external ADC has
				// been
				// enable, disabled, or checked.

				// If status has been triggered to true (on)
				if (myDrone.adcStatus) {
					// then start the streaming by taking the first
					// measurement
					streamerArray[7].run();
				}
				// Don't do anything if false (off)
			}

			@Override
			public void altitudeStatus(EventObject arg0) {
				if (myDrone.altitudeStatus) {
					streamerArray[8].run();
				}

			}

			@Override
			public void batteryVoltageStatus(EventObject arg0) {
				// This is triggered when the battery voltage has been
				// measured.
				
				float volts = myDrone.batteryVoltage_Volts;
				Log.d(TAG, "Volts: " + volts);
			}

			@Override
			public void capacitanceStatus(EventObject arg0) {
				if (myDrone.capacitanceStatus) {
					streamerArray[6].run();
				}
			}

			@Override
			public void chargingStatus(EventObject arg0) {

			}

			@Override
			public void customStatus(EventObject arg0) {

			}

			@Override
			public void humidityStatus(EventObject arg0) {
				if (myDrone.humidityStatus) {
					streamerArray[1].run();
				}

			}

			@Override
			public void irStatus(EventObject arg0) {
				if (myDrone.irTemperatureStatus) {
					streamerArray[3].run();
				}

			}

			@Override
			public void lowBatteryStatus(EventObject arg0) {
				// If we get a low battery, notify the user
				// and disconnect

				// This might trigger a lot (making a call the the LEDS will
				// trigger it,
				// so the myBlinker will trigger this once a second.
				// calling myBlinker.disable() even sets LEDS off, which
				// will trigger it...
				// Set true again in connectEvent
					myBlinker.disable();
					doOnDisconnect(); // run our disconnect routine
					// Notify the user
					Log.d(TAG, "low battery");
				

			}

			@Override
			public void oxidizingGasStatus(EventObject arg0) {

			}

			@Override
			public void precisionGasStatus(EventObject arg0) {
				if (myDrone.precisionGasStatus) {
					streamerArray[5].run();
				}

			}

			@Override
			public void pressureStatus(EventObject arg0) {
				if (myDrone.pressureStatus) {
					streamerArray[2].run();
				}

			}

			@Override
			public void reducingGasStatus(EventObject arg0) {

			}

			@Override
			public void rgbcStatus(EventObject arg0) {
				if (myDrone.rgbcStatus) {
					streamerArray[4].run();
				}

			}

			@Override
			public void temperatureStatus(EventObject arg0) {
				if (myDrone.temperatureStatus) {
					streamerArray[0].run();
				}

			}

			@Override
			public void unknownStatus(EventObject arg0) {

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
	
	
	private void doOnDisconnect() {
		// Shut off any sensors that are on
		myBlinker.disable();
		Log.d(TAG, "disconnect");
		// Make sure the LEDs go off
		if (myDrone.isConnected) {
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
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
    public void onDestroy() {
        // Cancel the persistent notification.
		doOnDisconnect();
		//unregisterReceiver(btReceiver);
		super.onDestroy();  
    }
	
	
	

}
