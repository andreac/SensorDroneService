package com.example.airqualitytest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventObject;
import java.util.Hashtable;


import android.R.array;
import android.R.bool;
import android.os.Bundle;
import android.os.SystemClock;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	// private DroneApplication droneApp;
	Context cxt;
	// A ConnectionBLinker from the SDHelper Library
	static String TAG = "droneTest";

	public int streamingRate;
	static LocalBroadcastManager broadcaster;

	public ArrayList<String> dataArray;
	private Button service;
	private Button disconnect;
	static final public String READ_PARAMETERS = "com.sensorDroneTest.READ_PARAMETERS";
	BroadcastReceiver receiver;
	IntentFilter uiFilter;
	
	private String MAC; // The MAC address we will connect to
	private BluetoothAdapter btAdapter;
	private BroadcastReceiver btReceiver;
	private String TAGC = "Connect";
	
	private IntentFilter btFilter;
	private final static String MY_PREFERENCES = "MyPref";
    // Costante relativa al nome della particolare preferenza
    private final static String TEXT_DATA_KEY = "MacAddress";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		cxt = this;

		disconnect = (Button) findViewById(R.id.btnDisconnect);
		disconnect.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// stopService(new Intent(getBaseContext(),
				// sensorService.class));

				// final AlarmManager alarm = (AlarmManager)
				// getSystemService(Context.ALARM_SERVICE);
				// final Calendar TIME = Calendar.getInstance();
				// TIME.set(Calendar.MINUTE, 0);
				// TIME.set(Calendar.SECOND, 0);
				// TIME.set(Calendar.MILLISECOND, 0);
				// final Intent intentRun = new Intent(cxt,
				// sensorService.class);
				// intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// PendingIntent pending = PendingIntent.getService(cxt, 0,
				// intentRun, 0);
				// pending.cancel();
				// alarm.cancel(pending);
				//
				// stopService(intentRun);
				
				
				
				 AlarmManager mgr=(AlarmManager)MainActivity.this.getSystemService(Context.ALARM_SERVICE);
				 Intent i=new Intent(MainActivity.this, OnAlarmReceiver.class);
				 PendingIntent pi=PendingIntent.getBroadcast(MainActivity.this, 0,i, 0);
				 mgr.cancel(pi);
				 
				 service.setVisibility(View.VISIBLE);
				 disconnect.setVisibility(View.GONE);

			}
		});
		//
		//
		service = (Button) findViewById(R.id.btnService);
		service.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
				String macAddress = prefs.getString(TEXT_DATA_KEY, "");
				if(macAddress==""){
					connect();
				}
				else
				{
					sendBroadcast(new Intent(MainActivity.this, OnBootReceiver.class));
					Log.d(TAG, "alarm attivo");
					Toast.makeText(MainActivity.this, "Alarms active!", Toast.LENGTH_LONG).show();
					   
					service.setVisibility(View.GONE);
					 disconnect.setVisibility(View.VISIBLE);
				}
			}
		});

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// String s = intent.getStringExtra(READ_PARAMETERS);
				// do something here.

				ArrayList<String> objSensor = (ArrayList<String>) intent.getSerializableExtra(READ_PARAMETERS);
				Log.d(TAG, "receive broadcast");
				Log.d(TAG, objSensor.toString());

				updateLabel(R.id.txtTmp, objSensor.get(0));
				updateLabel(R.id.txtHum, objSensor.get(1));
				updateLabel(R.id.txtPre, objSensor.get(2));
				updateLabel(R.id.txtIRTemp, objSensor.get(3));
				//updateLabel(R.id.txtRGBC, objSensor.get(4));
				updateLabel(R.id.txtGas, objSensor.get(5));
				updateLabel(R.id.txtADC, objSensor.get(6));
				//updateLabel(R.id.txtCap, objSensor.get(7));
				updateLabel(R.id.txtAlt, objSensor.get(8));
				updateLabel(R.id.txtRGas, objSensor.get(9));
				updateLabel(R.id.txtOGas, objSensor.get(10));
				updateLabel(R.id.txtUpdate, objSensor.get(11));

			}
		};

		uiFilter = new IntentFilter(READ_PARAMETERS);
		cxt.registerReceiver(receiver, uiFilter);
		
		
		
		Intent checkIntent = new Intent(MainActivity.this, OnBootReceiver.class);
		boolean alarmUp = (PendingIntent.getBroadcast(getBaseContext(), 0, checkIntent, PendingIntent.FLAG_NO_CREATE) != null);
		if(alarmUp)
		{
			Log.d(TAG, "service running");
			service.setVisibility(View.GONE);
			disconnect.setVisibility(View.VISIBLE);
			
			
		}
		 
		ArrayList<String> lastdata = loadArray(cxt);
		if(lastdata!= null)
		{
			
			updateLabel(R.id.txtTmp, lastdata.get(0));
			updateLabel(R.id.txtHum, lastdata.get(1));
			updateLabel(R.id.txtPre, lastdata.get(2));
			updateLabel(R.id.txtIRTemp, lastdata.get(3));
			//updateLabel(R.id.txtRGBC, objSensor.get(4));
			updateLabel(R.id.txtGas, lastdata.get(5));
			updateLabel(R.id.txtADC, lastdata.get(6));
			//updateLabel(R.id.txtCap, objSensor.get(7));
			updateLabel(R.id.txtAlt, lastdata.get(8));
			updateLabel(R.id.txtRGas, lastdata.get(9));
			updateLabel(R.id.txtOGas, lastdata.get(10));
			updateLabel(R.id.txtUpdate, lastdata.get(11));
		}
		
		
	}

	private void updateLabel(int id, String msg) {
		TextView tmp = (TextView) findViewById(id);
		tmp.setText(msg);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
				new IntentFilter(READ_PARAMETERS));

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	
	
	protected boolean connect() {
		
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager)cxt.getSystemService(Context.TELEPHONY_SERVICE);
		String myDeviceID = mTelephonyMgr.getDeviceId();
		
		final String macAddressDevice = getStringResourceByName("_" + myDeviceID);
		
		Log.d(TAG, macAddressDevice);
		
		broadcaster = LocalBroadcastManager.getInstance(this);
		

		// Set up our Bluetooth Adapter
		btAdapter = BluetoothAdapter.getDefaultAdapter();

		// Is Bluetooth on?
		boolean isOn = isBTEnabled(cxt, btAdapter);

		if (!isOn) {
			// Don't proceed until the user turns Bluetooth on.
			Log.d(TAGC, "bt is not enable");
			return false;
		}
		else{
			Log.d(TAGC, "bt is enable");
			
			
			SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			MAC = macAddressDevice;
			editor.putString(TEXT_DATA_KEY, MAC);
			editor.commit();
			Log.d(TAG, "pref salvate");
			   
			   
			TextView tmp = (TextView) MainActivity.this.findViewById(R.id.txtMAC);
			tmp.setText(MAC);
			service.setVisibility(View.GONE);
			disconnect.setVisibility(View.VISIBLE);
			sendBroadcast(new Intent(MainActivity.this, OnBootReceiver.class));
			Log.d(TAG, "alarm attivo");
			Toast.makeText(MainActivity.this, "Alarms active!", Toast.LENGTH_LONG).show();
			   
			
			
			
			
		}
		// Make sure MAC String is initialized and empty
//		MAC = "";
//		//cxt.unregisterReceiver(btReceiver);
//		// What to do when we find a device
//		btReceiver = new BroadcastReceiver() {
//
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				String action = intent.getAction();
//				// When discovery finds a device
//				Log.d(TAGC, "discovered");
//				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//					// Get the BluetoothDevice object from the Intent
//					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//					// We only want Sensordrones
//					try {
//						if (device.getName().contains("drone")) {
//							// Add the Name and MAC
//							Log.d(TAGC,
//									device.getName() + "\n"
//											+ device.getAddress());
//							MAC = device.getAddress();
//							Log.d(TAGC, MAC);
//							if (!TextUtils.isEmpty(MAC)) {
//								// Display a message if the connect fails
//								Log.d(TAG, "trovato!");
//								
//								SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
//								SharedPreferences.Editor editor = prefs.edit();
//								if (MAC != null && MAC.equalsIgnoreCase(macAddressDevice)) {
//								   editor.putString(TEXT_DATA_KEY, MAC);
//								   editor.commit();
//								   Log.d(TAG, "pref salvate");
//								   
//								   
//								   TextView tmp = (TextView) MainActivity.this.findViewById(R.id.txtMAC);
//								   tmp.setText(MAC);
//								   service.setVisibility(View.GONE);
//								   disconnect.setVisibility(View.VISIBLE);
//								   sendBroadcast(new Intent(MainActivity.this, OnBootReceiver.class));
//								   Log.d(TAG, "alarm attivo");
//								   Toast.makeText(MainActivity.this, "Alarms active!", Toast.LENGTH_LONG).show();
//								   
//								   
//								}
//								
//							}
//						}
//					} catch (NullPointerException n) {
//						// Some times getName() will return null, which doesn't
//						// parse very well :-)
//						// Catch it here
//						n.printStackTrace();
//						//Log.d(TAGC, "eccezione " + n.printStackTrace(););
//						// You can still add it to the list if you want, it just
//						// might not
//						// be a Sensordrone...
//						// macAdapter.add("nullDevice" + "\n" +
//						// device.getAddress());
//					}
//				}
//				unregisterReceiver(this);
//			}
//		};
//		// Set up our IntentFilters
//		btFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//		cxt.registerReceiver(btReceiver, btFilter);
		// Don't forget to unregister when done!
		// Start scanning for Bluetooth devices
//		btAdapter.startDiscovery();
		return false;
	}
	
	public static ArrayList<String> loadArray(Context mContext) {  
		
		try {
			SharedPreferences sp = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
		    ArrayList<String> sKey = new ArrayList<String>();
		    int size = sp.getInt("Status_size", 0);  
		    if(size == 0) return null;
		    for(int i=0;i<size;i++) 
		    {
		        sKey.add(sp.getString("Status_" + i, null));  

		    }
		    return sKey;
		} catch (Exception e) {
			return null;
		}
		
	}
	
	
	private String getStringResourceByName(String aString) {
	      String packageName = MainActivity.this.getPackageName();
	      int resId = getResources().getIdentifier(aString, "string", packageName);
	      return getString(resId);
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
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.d(TAG, "on resume app");
		
		Intent checkIntent = new Intent(MainActivity.this, OnBootReceiver.class);
		boolean alarmUp = (PendingIntent.getBroadcast(getBaseContext(), 0, checkIntent, PendingIntent.FLAG_NO_CREATE) != null);
		if(alarmUp)
		{
			Log.d(TAG, "service running");
			service.setVisibility(View.GONE);
			disconnect.setVisibility(View.VISIBLE);
			
			
		}
		 
		ArrayList<String> lastdata = loadArray(cxt);
		if(lastdata!= null)
		{
			
			updateLabel(R.id.txtTmp, lastdata.get(0));
			updateLabel(R.id.txtHum, lastdata.get(1));
			updateLabel(R.id.txtPre, lastdata.get(2));
			updateLabel(R.id.txtIRTemp, lastdata.get(3));
			//updateLabel(R.id.txtRGBC, objSensor.get(4));
			updateLabel(R.id.txtGas, lastdata.get(5));
			updateLabel(R.id.txtADC, lastdata.get(6));
			//updateLabel(R.id.txtCap, objSensor.get(7));
			updateLabel(R.id.txtAlt, lastdata.get(8));
			updateLabel(R.id.txtRGas, lastdata.get(9));
			updateLabel(R.id.txtOGas, lastdata.get(10));
			updateLabel(R.id.txtUpdate, lastdata.get(11));
		}
		
		
		
		super.onResume();
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
}
