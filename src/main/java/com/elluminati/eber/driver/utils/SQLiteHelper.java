package com.elluminati.eber.driver.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;

import java.util.Calendar;

/**
 * Created by elluminati on 15-Nov-17.
 */

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "eberProvider.db";

    private static final String TABLE_LOCATION = "providerLocation";

    private static final String KEY_ID = "id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_TIME = "time";
    private static final String LOCATION_UNIQUE_ID = "location_unique_id";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_LOCATION_TABLE = "CREATE TABLE " + TABLE_LOCATION + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT," + KEY_TIME + " TEXT," + LOCATION_UNIQUE_ID + " TEXT" +
                ")";
        sqLiteDatabase.execSQL(CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);

        // Create tables again
        onCreate(sqLiteDatabase);
    }


    // Getting Location JSONArray
    public JSONArray getAllDBLocations() {
        JSONArray locationJSONArray = new JSONArray();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_LOCATION + " ORDER BY " + KEY_TIME + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                JSONArray location = new JSONArray();
                location.put(cursor.getString(cursor.getColumnIndex(KEY_LATITUDE)));
                location.put(cursor.getString(cursor.getColumnIndex(KEY_LONGITUDE)));
                location.put(cursor.getString(cursor.getColumnIndex(KEY_TIME)));
                // Adding location to JSONArray
                locationJSONArray.put(location);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return location JSONArray
        AppLog.Log("getAllDBLocations", locationJSONArray
                .toString() + "");
        return locationJSONArray;
    }

    public JSONArray getDBLocationsFromId(int uniqueId) {
        JSONArray locationJSONArray = new JSONArray();
        String selectQuery = "SELECT * FROM " + TABLE_LOCATION + " WHERE " + LOCATION_UNIQUE_ID +
                " " +
                " = " + uniqueId;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                JSONArray location = new JSONArray();
                location.put(cursor.getString(cursor.getColumnIndex(KEY_LATITUDE)));
                location.put(cursor.getString(cursor.getColumnIndex(KEY_LONGITUDE)));
                location.put(cursor.getString(cursor.getColumnIndex(KEY_TIME)));
                // Adding location to JSONArray
                locationJSONArray.put(location);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return location JSONArray
        AppLog.Log(this.getClass().getSimpleName(), locationJSONArray.toString() + "");
        return locationJSONArray;
    }


    // Adding location
    public void addLocation(String latitude, String longitude, int uniqueId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);
        values.put(KEY_TIME, String.valueOf(Calendar.getInstance().getTimeInMillis()));
        values.put(LOCATION_UNIQUE_ID, String.valueOf(uniqueId));
        // Inserting Row
        db.insert(TABLE_LOCATION, null, values);
        db.close(); // Closing database connection
        AppLog.Log(" AddLocationDB",
                "latitude= " + latitude + "  longitude= " + longitude + " uniqueId= " + uniqueId);
    }

    public void clearLocationTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATION, null, null);
        db.close();
    }

    public void deleteLocation(int uniqueId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATION, LOCATION_UNIQUE_ID + "=" + uniqueId, null);
        db.close();
    }
}
