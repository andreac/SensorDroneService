package com.example.sensordronetest;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.util.Log;

import com.example.database.DBmanager;

public class Sensor implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String Temperature = null;
	String irTemperature = null;
	String Humidity = null;
	String Pressure = null;
	String RGBC = null;
	String Precision_GAS = null;
	String Capacitance = null;
	String Altitude = null;
	String ADC = null;
	private ArrayList<String> dataArray;
	
	
	public ArrayList<String> getDataArray() {
		return dataArray;
	}

	private final String TAG = "database";
	
	public Sensor(){
		inizialize();
	}
	
	private void inizialize(){
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
	}
	
	public void buildArray(){
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
		
		
		//Log.d(TAG, "save data local db");
		String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
		dataArray.add(timeStamp);
	}
	
	public Boolean saveData(Context cxt){
		
		if(this.getHumidity() != null && this.getTemperature() != null )
		{
			DBmanager db=new DBmanager(cxt);
	        db.open();  //apriamo il db
	        
	        db.insertSensorData(dataArray);
	        db.close();
	        Log.d(TAG, "saved on db");
	        dataArray.clear();
	        inizialize();
	        return true;
		}
		else
		{
			return false;
		}
		
		
		
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
		
		String para = "temp: "+ this.Temperature + " hum: " + this.Humidity;
		
		return para;
	}
	
	
}
