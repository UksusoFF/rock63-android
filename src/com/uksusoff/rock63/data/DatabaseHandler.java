package com.uksusoff.rock63.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.uksusoff.rock63.data.entities.Event;
import com.uksusoff.rock63.data.entities.NewsItem;
import com.uksusoff.rock63.data.entities.Place;
import com.uksusoff.rock63.utils.CommonUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
	
	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;
 
    // Database Name
    private static final String DATABASE_NAME = "rock63androidclient";
 
    // Table names
    private static final String TABLE_PLACES = "places";
    private static final String TABLE_NEWS = "news";
    private static final String TABLE_EVENTS = "events";
 
    // Place table field names
    private static final String TABLE_PLACES_KEY_ID = "id";
    private static final String TABLE_PLACES_KEY_NAME = "name";
    private static final String TABLE_PLACES_KEY_ADDRESS = "address";
    private static final String TABLE_PLACES_KEY_SITEURL = "site";
    private static final String TABLE_PLACES_KEY_PHONE = "phone";
    private static final String TABLE_PLACES_KEY_VKURL = "vkurl";
    private static final String TABLE_PLACES_KEY_MAP_IMAGE_URL = "mapimageurl";
    
    // News table field names
    private static final String TABLE_NEWS_KEY_ID = "id";
    private static final String TABLE_NEWS_KEY_TITLE = "title";
    private static final String TABLE_NEWS_KEY_BODY = "body";
    private static final String TABLE_NEWS_KEY_DATETIME = "datetime";
    private static final String TABLE_NEWS_KEY_SMALL_THUMB_URL = "smallthumburl";
    private static final String TABLE_NEWS_KEY_MEDIUM_THUMB_URL = "mediumthumburl";
    private static final String TABLE_NEWS_KEY_ISNEW = "isnew";
    
    // Events table field names
    private static final String TABLE_EVENTS_KEY_ID = "id";
    private static final String TABLE_EVENTS_KEY_TITLE = "title";
    private static final String TABLE_EVENTS_KEY_BODY = "body";
    private static final String TABLE_EVENTS_KEY_STARTDATETIME = "start";
    private static final String TABLE_EVENTS_KEY_ENDDATETIME = "end";
    private static final String TABLE_EVENTS_KEY_PLACEID = "placeid";
    private static final String TABLE_EVENTS_KEY_MEDIUM_THUMB_URL = "mediumthumburl";
    private static final String TABLE_EVENTS_KEY_URL = "url";
     
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
    	
        String CREATE_PLACES_TABLE = "CREATE TABLE " + TABLE_PLACES + "("
                + TABLE_PLACES_KEY_ID + " INTEGER PRIMARY KEY,"
        		+ TABLE_PLACES_KEY_NAME + " TEXT,"
        		+ TABLE_PLACES_KEY_ADDRESS + " TEXT,"
        		+ TABLE_PLACES_KEY_SITEURL + " TEXT,"
        		+ TABLE_PLACES_KEY_PHONE + " TEXT,"
        		+ TABLE_PLACES_KEY_VKURL + " TEXT,"
        		+ TABLE_PLACES_KEY_MAP_IMAGE_URL + " TEXT" + ")";
        
        db.execSQL(CREATE_PLACES_TABLE);
        
        String CREATE_NEWS_TABLE = "CREATE TABLE " + TABLE_NEWS + "("
                + TABLE_NEWS_KEY_ID + " INTEGER PRIMARY KEY,"
        		+ TABLE_NEWS_KEY_TITLE + " TEXT,"
        		+ TABLE_NEWS_KEY_BODY + " TEXT,"
        		+ TABLE_NEWS_KEY_DATETIME + " INTEGER,"
        		+ TABLE_NEWS_KEY_SMALL_THUMB_URL + " TEXT,"
                + TABLE_NEWS_KEY_MEDIUM_THUMB_URL + " TEXT,"
        		+ TABLE_NEWS_KEY_ISNEW + " INTEGER" + ")";
        
        db.execSQL(CREATE_NEWS_TABLE);
        
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
                + TABLE_EVENTS_KEY_ID + " INTEGER PRIMARY KEY,"
        		+ TABLE_EVENTS_KEY_TITLE + " TEXT,"
        		+ TABLE_EVENTS_KEY_BODY + " TEXT,"
        		+ TABLE_EVENTS_KEY_STARTDATETIME + " INTEGER,"
        		+ TABLE_EVENTS_KEY_ENDDATETIME + " INTEGER,"
                + TABLE_EVENTS_KEY_PLACEID + " INTEGER,"
        		+ TABLE_EVENTS_KEY_MEDIUM_THUMB_URL + " TEXT,"
                + TABLE_EVENTS_KEY_URL + " TEXT" + ")";
        
        db.execSQL(CREATE_EVENTS_TABLE);
        
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
 
        // Create tables again
        onCreate(db);
    }
    
    public void addNewsItem( NewsItem item ) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	 
        ContentValues values = new ContentValues();
        values.put(TABLE_EVENTS_KEY_ID, item.getId());
        values.put(TABLE_NEWS_KEY_TITLE, item.getTitle());
        values.put(TABLE_NEWS_KEY_BODY, item.getBody());
        values.put(TABLE_NEWS_KEY_DATETIME, CommonUtils.getTimestampFromDate(item.getDate()));
        values.put(TABLE_NEWS_KEY_SMALL_THUMB_URL, item.getSmallThumbUrl());
        values.put(TABLE_NEWS_KEY_MEDIUM_THUMB_URL, item.getMediumThumbUrl());
        values.put(TABLE_NEWS_KEY_ISNEW, item.isNew());
        
        // Inserting Row
        db.insert(TABLE_NEWS, null, values);
        db.close(); // Closing database connection
    }
    
    public List<Integer> getAllNewsItemsIds() {
        
        List<Integer> itemList = new ArrayList<Integer>();
        // Select All Query
        String selectQuery = "SELECT " + TABLE_NEWS_KEY_ID + " FROM " + TABLE_NEWS;
     
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        try {
            if (cursor.moveToFirst()) {
                do {
                    itemList.add(cursor.getInt(cursor.getColumnIndex(TABLE_NEWS_KEY_ID)));
                } while (cursor.moveToNext());
            }
        }
        finally {
            cursor.close();
        }
     
        // return contact list
        return itemList;
        
    }
    
    public List<NewsItem> getAllNewsItems() {
    	
    	List<NewsItem> newsItemList = new ArrayList<NewsItem>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NEWS + " ORDER BY " + TABLE_NEWS_KEY_DATETIME + " DESC";
     
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        try {
            if (cursor.moveToFirst()) {
                do {
                	NewsItem newsItem = new NewsItem();
                	newsItem.setId(cursor.getInt(cursor.getColumnIndex(TABLE_NEWS_KEY_ID)));
                	newsItem.setTitle(cursor.getString(cursor.getColumnIndex(TABLE_NEWS_KEY_TITLE)));
                	newsItem.setBody(cursor.getString(cursor.getColumnIndex(TABLE_NEWS_KEY_BODY)));
                	newsItem.setDate(CommonUtils.getDateFromTimestamp(cursor.getInt(cursor.getColumnIndex(TABLE_NEWS_KEY_DATETIME))));
                	newsItem.setSmallThumbUrl(cursor.getString(cursor.getColumnIndex(TABLE_NEWS_KEY_SMALL_THUMB_URL)));
                	newsItem.setMediumThumbUrl(cursor.getString(cursor.getColumnIndex(TABLE_NEWS_KEY_MEDIUM_THUMB_URL)));
                	newsItem.setNew(cursor.getInt(cursor.getColumnIndex(TABLE_NEWS_KEY_ISNEW))!=0);
                	
                    newsItemList.add(newsItem);
                } while (cursor.moveToNext());
            }
        }
        finally {
            cursor.close();
        }
     
        // return contact list
        return newsItemList;
        
    }
    
    public void deleteAllNewsItems() {
    	SQLiteDatabase db = this.getWritableDatabase();
    	
    	db.delete(TABLE_NEWS, null, null);
        
        db.close();
    }
    
    public void deleteAllOldNews( Date before )
    {
        SQLiteDatabase db = this.getWritableDatabase();
        
        db.delete(TABLE_NEWS, TABLE_NEWS_KEY_DATETIME + " <= " + Integer.toString(CommonUtils.getTimestampFromDate(before)), null);
        
        db.close();
    }
    
    public void addEvent( Event item ) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	 
        ContentValues values = new ContentValues();
        values.put(TABLE_EVENTS_KEY_TITLE, item.getTitle());
        values.put(TABLE_EVENTS_KEY_BODY, item.getBody());
        values.put(TABLE_EVENTS_KEY_STARTDATETIME, CommonUtils.getTimestampFromDate(item.getStart()));
        values.put(TABLE_EVENTS_KEY_ENDDATETIME, CommonUtils.getTimestampFromDate(item.getEnd()));
        values.put(TABLE_EVENTS_KEY_PLACEID, item.getPlaceId());
        values.put(TABLE_EVENTS_KEY_MEDIUM_THUMB_URL, item.getMediumThumbUrl());
        values.put(TABLE_EVENTS_KEY_URL, item.getUrl());
        
        // Inserting Row
        db.insert(TABLE_EVENTS, null, values);
        db.close(); // Closing database connection
    }
    
    public List<Event> getAllEvents() {
    	
    	List<Event> eventsList = new ArrayList<Event>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_EVENTS;
     
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	Event e = new Event();
            	e.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(TABLE_EVENTS_KEY_ID))));
            	e.setTitle(cursor.getString(cursor.getColumnIndex(TABLE_EVENTS_KEY_TITLE)));
            	e.setBody(cursor.getString(cursor.getColumnIndex(TABLE_EVENTS_KEY_BODY)));
            	e.setStart(CommonUtils.getDateFromTimestamp(cursor.getInt(cursor.getColumnIndex(TABLE_EVENTS_KEY_STARTDATETIME))));
            	e.setEnd(CommonUtils.getDateFromTimestamp(cursor.getInt(cursor.getColumnIndex(TABLE_EVENTS_KEY_ENDDATETIME))));
            	e.setPlaceId(cursor.getInt(cursor.getColumnIndex(TABLE_EVENTS_KEY_PLACEID)));
            	e.setMediumThumbUrl(cursor.getString(cursor.getColumnIndex(TABLE_EVENTS_KEY_MEDIUM_THUMB_URL)));
            	e.setUrl(cursor.getString(cursor.getColumnIndex(TABLE_EVENTS_KEY_URL)));
            	
            	eventsList.add(e);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
     
        // return contact list
        return eventsList;
        
    }
    
    public void deleteAllEvents() {
    	SQLiteDatabase db = this.getWritableDatabase();
    	
    	db.delete(TABLE_EVENTS, null, null);
        
        db.close();
    }
    
    public void addPlace( Place item ) {
    	SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TABLE_PLACES_KEY_ID, item.getId());
        values.put(TABLE_PLACES_KEY_NAME, item.getName());
        values.put(TABLE_PLACES_KEY_ADDRESS, item.getAddress());
        values.put(TABLE_PLACES_KEY_SITEURL, item.getUrl());
        values.put(TABLE_PLACES_KEY_PHONE, item.getPhone());
        values.put(TABLE_PLACES_KEY_MAP_IMAGE_URL, item.getMapImageUrl());
        values.put(TABLE_PLACES_KEY_VKURL, item.getVkUrl());
        
        // Inserting Row
        db.insert(TABLE_PLACES, null, values);
        db.close(); // Closing database connection
    }
    
    public List<Place> getAllPlaces() {
    	
    	List<Place> placeList = new ArrayList<Place>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_PLACES;
     
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	Place p = new Place();
            	p.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(TABLE_PLACES_KEY_ID))));
            	p.setName(cursor.getString(cursor.getColumnIndex(TABLE_PLACES_KEY_NAME)));
            	p.setAddress(cursor.getString(cursor.getColumnIndex(TABLE_PLACES_KEY_ADDRESS)));
            	p.setUrl(cursor.getString(cursor.getColumnIndex(TABLE_PLACES_KEY_SITEURL)));
            	p.setPhone(cursor.getString(cursor.getColumnIndex(TABLE_PLACES_KEY_PHONE)));
            	p.setMapImageUrl(cursor.getString(cursor.getColumnIndex(TABLE_PLACES_KEY_MAP_IMAGE_URL)));
            	p.setVkUrl(cursor.getString(cursor.getColumnIndex(TABLE_PLACES_KEY_VKURL)));
            	
            	placeList.add(p);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
     
        // return contact list
        return placeList;
        
    }
    
    public List<Integer> getAllPlaceIds() {
    	
    	String selectQuery = "SELECT id FROM " + TABLE_PLACES;
    	
    	SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
    	
        List<Integer> result = new ArrayList<Integer>();
        
        if (cursor.moveToFirst()) {
            do {
            	result.add(cursor.getColumnIndex(TABLE_PLACES_KEY_ID));
            } while (cursor.moveToNext());
        }
        
        cursor.close();
     
        // return contact list
        return result;
    }
    
    public Place getPlaceById(int id) {
    	
    	String selectQuery = "SELECT * FROM " + TABLE_PLACES + " WHERE " + TABLE_PLACES_KEY_ID + "=" + Integer.toString(id);
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        Place res = null;
        
        if (cursor.moveToFirst()) {
        	Place p = new Place();
        	p.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(TABLE_PLACES_KEY_ID))));
        	p.setName(cursor.getString(cursor.getColumnIndex(TABLE_PLACES_KEY_NAME)));
        	p.setAddress(cursor.getString(cursor.getColumnIndex(TABLE_PLACES_KEY_ADDRESS)));
        	p.setUrl(cursor.getString(cursor.getColumnIndex(TABLE_PLACES_KEY_SITEURL)));
        	p.setPhone(cursor.getString(cursor.getColumnIndex(TABLE_PLACES_KEY_PHONE)));
        	p.setMapImageUrl(cursor.getString(cursor.getColumnIndex(TABLE_PLACES_KEY_MAP_IMAGE_URL)));
        	p.setVkUrl(cursor.getString(cursor.getColumnIndex(TABLE_PLACES_KEY_VKURL)));
        	        	
        	res = p;
        }
        
        cursor.close();
        
        return res;
    }
    
    public void deleteAllPlaces() {
    	SQLiteDatabase db = this.getWritableDatabase();
    	
    	db.delete(TABLE_PLACES, null, null);
        
        db.close();
    }
    
}
