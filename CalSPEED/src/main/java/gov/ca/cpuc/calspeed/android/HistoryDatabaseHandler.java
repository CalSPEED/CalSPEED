/*
Copyright (c) 2020, California State University Monterey Bay (CSUMB).
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above
       copyright notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. Neither the name of the CPUC, CSU Monterey Bay, nor the names of
       its contributors may be used to endorse or promote products derived from
       this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package gov.ca.cpuc.calspeed.android;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class HistoryDatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "historyManager";
 
    // History table name
    private static final String TABLE_HISTORY = "results";
 
    // History Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "date";
    private static final String KEY_UPLOAD = "upload";
    private static final String KEY_DOWNLOAD = "download";
    private static final String KEY_LATENCY = "latency";
    private static final String KEY_JITTER = "jitter";
    private static final String KEY_NETWORKTYPE = "networktype";
    private static final String KEY_CONNECTIONTYPE = "connectiontype";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_MOS_VALUE = "mosvalue";
	private static final String KEY_VIDEO = "video";
    private static final String KEY_VIDEO_CONFERENCE = "videoconference";
    private static final String KEY_VOIP = "voip";
    private static final String[] UPDATED_COLUMNS = {
            KEY_MOS_VALUE, KEY_VIDEO, KEY_VIDEO_CONFERENCE, KEY_VOIP };


    public HistoryDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_DATE + " TEXT,"
                + KEY_UPLOAD + " REAL," + KEY_DOWNLOAD + " REAL,"
                + KEY_LATENCY + " REAL," + KEY_JITTER + " REAL," + KEY_NETWORKTYPE + " TEXT,"
                + KEY_CONNECTIONTYPE + " TEXT," + KEY_LATITUDE + " TEXT," + KEY_LONGITUDE + " TEXT," 
                + KEY_MOS_VALUE + " TEXT," + KEY_VIDEO + " TEXT" + KEY_VIDEO_CONFERENCE + " TEXT"
                + KEY_VOIP + " TEXT)";
        db.execSQL(CREATE_HISTORY_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v("Database", "Upgrading database");
        // Drop older table if exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
 
        updateTable();
        // Create tables again
        onCreate(db);
    }
    
    
    public void updateTable() {
        Log.v(this.getClass().getName(), "Updating table");
    	SQLiteDatabase db = this.getWritableDatabase();
        try {
        	Cursor mCursor = db.rawQuery("SELECT * FROM " + TABLE_HISTORY + " LIMIT 0", null);
            validateTable(db, mCursor);
        } catch (SQLException e) {
            Log.e("SQLException", "Unexpected SQLException: " + e.getMessage());
        }
    }

    private void validateTable(SQLiteDatabase db, Cursor mCursor) {
        for (String column : UPDATED_COLUMNS) {
            if (mCursor.getColumnIndex(column) == -1) {
                db.execSQL("ALTER TABLE " + TABLE_HISTORY + " ADD " + column + " TEXT");
            } else {
                Log.w("HistoryDatabaseHandler", "Column " + column + " doesn't exist");
            }
        }
    }
    
    // Adding new results history
	public void addHistory(History history) {
	    SQLiteDatabase db = this.getWritableDatabase();
	 
	    ContentValues values = new ContentValues();
	    values.put(KEY_DATE, history.getDate()); 
	    values.put(KEY_UPLOAD, history.getUploadAverage());
	    values.put(KEY_DOWNLOAD, history.getDownloadAverage()); 
	    values.put(KEY_LATENCY, history.getLatencyAverage()); 
	    values.put(KEY_JITTER, history.getJitterAverage()); 
	    values.put(KEY_NETWORKTYPE, history.getNetworkType());
	    values.put(KEY_CONNECTIONTYPE, history.getConnectionType());
	    values.put(KEY_LATITUDE, history.getLatitude());
	    values.put(KEY_LONGITUDE, history.getLongitude());
	    values.put(KEY_MOS_VALUE, history.getMosValue());
		values.put(KEY_VIDEO, history.getVideo());
		values.put(KEY_VIDEO_CONFERENCE, history.getVideoConference());
        values.put(KEY_VOIP, history.getVideoVoip());
	 
	    // Inserting Row
	    db.insert(TABLE_HISTORY, null, values);
	    db.close(); // Closing database connection
	}


	public History getHistory(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HISTORY, new String[] { KEY_ID,
                KEY_DATE, KEY_UPLOAD, KEY_DOWNLOAD, KEY_LATENCY, KEY_JITTER,
                KEY_NETWORKTYPE, KEY_CONNECTIONTYPE, KEY_LATITUDE, KEY_LONGITUDE, KEY_MOS_VALUE,
                KEY_VIDEO, KEY_VIDEO_CONFERENCE, KEY_VOIP}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        History history = new History(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4),
                cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8),
                cursor.getString(9),cursor.getString(10), cursor.getString(11), cursor.getString(12),
                cursor.getString(13));
        return history;
	}
	

	public List<History> getAllHistoryRecords() {
        List<History> historyList = new ArrayList<History>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_HISTORY + " ORDER BY "+ KEY_DATE + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and add to list
        if (cursor.moveToFirst()) {
            do {
                History history = new History();
                history.setID(Integer.parseInt(cursor.getString(0)));
                history.setDate(cursor.getString(1));
                history.setUploadAverage(cursor.getString(2));
                history.setDownloadAverage(cursor.getString(3));
                history.setLatencyAverage(cursor.getString(4));
                history.setJitterAverage(cursor.getString(5));
                history.setNetworkType(cursor.getString(6));
                history.setConnectionType(cursor.getString(7));
                history.setLatitude(cursor.getString(8));
                history.setLongitude(cursor.getString(9));
                history.setMosValue(cursor.getString(10));
                history.setVideo(cursor.getString(11));
                history.setVideoConference(cursor.getString(12));
                history.setVideoVoip(cursor.getString(13));
                historyList.add(history);
            } while (cursor.moveToNext());
        }
        return historyList;
	}
	
	// Getting Results History Count
	public Integer getHistoryCount() {
	    String countQuery = "SELECT  * FROM " + TABLE_HISTORY;
	    SQLiteDatabase db = this.getReadableDatabase();
	    Cursor cursor = db.rawQuery(countQuery, null);
	    Integer count=cursor.getCount();
	    cursor.close();
	    return count;
	}


	//Deleting single History entry
	public void deleteHistory(String idKey) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, KEY_ID + " = ?", new String[] { idKey });
        db.close();
	}

}
