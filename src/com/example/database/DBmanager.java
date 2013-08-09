package com.example.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBmanager {  

        SQLiteDatabase mDb;
        DbHelper mDbHelper;
        Context mContext;
        private static final String DB_NAME="datadb";//nome del db
        private static final int DB_VERSION=1; //numero di versione del nostro db
       
        public DBmanager(Context ctx){
                mContext=ctx;
                mDbHelper= new DbHelper(ctx, DB_NAME, null, DB_VERSION);   //quando istanziamo questa classe, istanziamo anche l'helper (vedi sotto)    
        }
       
        public void open(){  //il database su cui agiamo è leggibile/scrivibile
                mDb=mDbHelper.getWritableDatabase();
               
        }
       
        public void close(){ //chiudiamo il database su cui agiamo
                mDb.close();
        }
       
       
        //i seguenti 2 metodi servono per la lettura/scrittura del db. aggiungete e modificate a discrezione
       // consiglio:si potrebbe creare una classe Prodotto, i quali oggetti verrebbero passati come parametri dei seguenti metodi, rispettivamente ritornati. Lacio a voi il divertimento
       
       
        public void insertSensorData(ArrayList<String> sensorData){ //metodo per inserire i dati
                ContentValues cv=new ContentValues();
                cv.put(SensorMetaData.TEMPERATURE, sensorData.get(0));
                cv.put(SensorMetaData.HUMIDITY, sensorData.get(1));
                cv.put(SensorMetaData.PRESSURE, sensorData.get(2));
                cv.put(SensorMetaData.IR_TEMPERATURE, sensorData.get(3));
                cv.put(SensorMetaData.RGBC, sensorData.get(4));
                cv.put(SensorMetaData.PRECISION_GAS, sensorData.get(5));
                cv.put(SensorMetaData.CAPACITANCE, sensorData.get(6));
                cv.put(SensorMetaData.ADC, sensorData.get(7));
                cv.put(SensorMetaData.ALTITUDE, sensorData.get(8));
                cv.put(SensorMetaData.TIMESTAMP, sensorData.get(9));
                mDb.insert(SensorMetaData.TABLE, null, cv);
        }
       
        public Cursor fetchData(){ //metodo per fare la query di tutti i dati
                return mDb.query(SensorMetaData.TABLE, null,null,null,null,null,null);              
        }

        static class SensorMetaData {  // i metadati della tabella, accessibili ovunque
                static final String TABLE = "sensorsData";
                static final String ID = "_id";
                static final String TEMPERATURE = "temperature";
                static final String HUMIDITY = "humidity";
                static final String PRESSURE= "pressure";
                static final String IR_TEMPERATURE = "irTemperature";
                static final String RGBC = "rgbc";
                static final String PRECISION_GAS = "precisionGas";
                static final String CAPACITANCE = "capacitance";
                static final String ADC = "adc";
                static final String ALTITUDE = "altitude";
                static final String TIMESTAMP = "timestamp";
        }

        private static final String SENSOR_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "  //codice sql di creazione della tabella
        				+ SensorMetaData.TABLE + " (" 
                        + SensorMetaData.ID + " integer primary key autoincrement, "
                        + SensorMetaData.TEMPERATURE + " real, "
                        + SensorMetaData.HUMIDITY + " real, " 
                        + SensorMetaData.PRESSURE + " real, " 
                        + SensorMetaData.IR_TEMPERATURE + " real, " 
                        + SensorMetaData.RGBC + " real, " 
                        + SensorMetaData.PRECISION_GAS + " real, " 
                        + SensorMetaData.CAPACITANCE + " real, " 
                        + SensorMetaData.ADC + " real, " 
                        + SensorMetaData.ALTITUDE + " real, "
                        + SensorMetaData.TIMESTAMP + " string "
                        + ");";

        private class DbHelper extends SQLiteOpenHelper { //classe che ci aiuta nella creazione del db

                public DbHelper(Context context, String name, CursorFactory factory,int version) {
                        super(context, name, factory, version);
                }

                @Override
                public void onCreate(SQLiteDatabase _db) { //solo quando il db viene creato, creiamo la tabella
                        _db.execSQL(SENSOR_TABLE_CREATE);
                }

                @Override
                public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
                        //qui mettiamo eventuali modifiche al db, se nella nostra nuova versione della app, il db cambia numero di versione

                }

        }
               

}