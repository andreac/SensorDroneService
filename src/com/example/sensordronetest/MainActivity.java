package com.example.sensordronetest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventObject;
import java.util.Hashtable;

import com.sensorcon.sdhelper.ConnectionBlinker;
import com.sensorcon.sdhelper.SDHelper;
import com.sensorcon.sdhelper.SDStreamer;
import com.sensorcon.sensordrone.Drone;
import com.sensorcon.sensordrone.Drone.DroneEventListener;
import com.sensorcon.sensordrone.Drone.DroneStatusListener;
import com.example.database.DBmanager;
import com.example.sensordronetest.DroneApplication;

import android.R.bool;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	//private DroneApplication droneApp;
	Context cxt;
	// A ConnectionBLinker from the SDHelper Library
	private ConnectionBlinker myBlinker;

	// Our Listeners
	private DroneEventListener deListener;
	private DroneStatusListener dsListener;
	
	static String TAG = "droneTest";
	

	public int streamingRate;
	
	public static Drone myDrone;
	
	private int[] qsSensors;
	private SDStreamer[] streamerArray;
	public ArrayList<String> dataArray;
	private Button service;
	private long INTERVAL = 1000 * 30;
	static final public String READ_PARAMETERS = "com.sensorDroneTest.READ_PARAMETERS";
	BroadcastReceiver receiver;
	IntentFilter uiFilter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		cxt = this;
		
		
		Button disconnect = (Button)findViewById(R.id.btnDisconnect);
		disconnect.setOnClickListener(new OnClickListener() {
		    public void onClick(View v)
		    {
		    	//stopService(new Intent(getBaseContext(), sensorService.class));
		    	
		    	final AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);   
				final Calendar TIME = Calendar.getInstance();
				TIME.set(Calendar.MINUTE, 0);
				TIME.set(Calendar.SECOND, 0);
				TIME.set(Calendar.MILLISECOND, 0);
		    	final Intent intentRun = new Intent(cxt, sensorService.class);
				intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				PendingIntent pending = PendingIntent.getService(cxt, 0, intentRun, 0);
				pending.cancel();
				alarm.cancel(pending);
				
				stopService(intentRun);
		    	
		    	
		    }
		});
//		
//		
		service = (Button)findViewById(R.id.btnService);
		service.setOnClickListener(new OnClickListener() {
		    public void onClick(View v)
		    {
				final AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);  
				  
				final Calendar TIME = Calendar.getInstance();
				TIME.set(Calendar.MINUTE, 0);
				TIME.set(Calendar.SECOND, 0);
				TIME.set(Calendar.MILLISECOND, 0);

				final Intent intentRun = new Intent(cxt, sensorService.class);
				intentRun.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				PendingIntent pending = PendingIntent.getService(cxt, 0, intentRun, 0);
				alarm.cancel(pending);
				
				alarm.setRepeating(AlarmManager.ELAPSED_REALTIME,SystemClock.elapsedRealtime(), INTERVAL, pending);
				
		    } 
		});
		
		
		 receiver = new BroadcastReceiver() {
		        @Override
		        public void onReceive(Context context, Intent intent) {
		            //String s = intent.getStringExtra(READ_PARAMETERS);
		            // do something here.
		        	
		        	ArrayList<String> objSensor = (ArrayList<String>) intent.getSerializableExtra(READ_PARAMETERS);
		        	
		        	Log.d(TAG, objSensor.toString());
		        	
		        	updateLabel(R.id.txtTmp, objSensor.get(0));
		        	updateLabel(R.id.txtHum, objSensor.get(1));
		        	updateLabel(R.id.txtPre, objSensor.get(2));
		        	updateLabel(R.id.txtIRTemp, objSensor.get(3));
		        	updateLabel(R.id.txtRGBC, objSensor.get(4));
		        	updateLabel(R.id.txtGas, objSensor.get(5));
		        	updateLabel(R.id.txtADC, objSensor.get(6));
		        	updateLabel(R.id.txtCap, objSensor.get(7));
		        	updateLabel(R.id.txtAlt, objSensor.get(8));
		        	updateLabel(R.id.txtUpdate, objSensor.get(9));
		        	
		        }
		    };
		    
		    
		    uiFilter = new IntentFilter(READ_PARAMETERS);
		    cxt.registerReceiver(receiver, uiFilter);
	}
	
	private void updateLabel(int id, String msg){
		TextView tmp = (TextView)findViewById(id);
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
		
		LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(READ_PARAMETERS));
		
		
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
}
