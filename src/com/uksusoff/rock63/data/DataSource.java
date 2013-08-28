package com.uksusoff.rock63.data;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.*;

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;
import com.uksusoff.rock63.data.entities.Event;
import com.uksusoff.rock63.data.entities.NewsItem;
import com.uksusoff.rock63.data.entities.Place;
import com.uksusoff.rock63.utils.CommonUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

@EBean(scope = com.googlecode.androidannotations.api.Scope.Singleton)
public class DataSource {
		
	private static final String NEWS_SOURCE_URL = "http://rock63.ru/export/api.php?type=news&from=0&to=15";
	private static final String NEWS_SOURCE_URL_FROM_DATE = "http://rock63.ru/export/api.php?type=news&date=%s";
	
	private static final String EVENTS_SOURCE_URL = "http://rock63.ru/export/api.php?type=afisha";
	
	private static final String PLACES_SOURCE_URL = "http://rock63.ru/export/api.php?type=venue";
	
	public static final int NO_PLACE_ID = -1;
	
	private static final long NEWS_LIFETIME_DAYS = 60;
	
	public static final String ROCK63_OPTIONS_STORE = "rock63_options_store";
	public static final String ROCK63_OPTION_LAST_NEWS_UPDATE = "rock63_options_last_news_update";
	
	private DatabaseHandler database;
	private INewsDataSourceListener newsListener;
	private IEventDataSourceListener eventListener;
	
	@RootContext
	Context context;
	
	@AfterInject
    public void init() {
	    database = new DatabaseHandler(context);
    }
	
	public INewsDataSourceListener getNewsListener() {
        return newsListener;
    }

    public void setNewsListener(INewsDataSourceListener newsListener) {
        this.newsListener = newsListener;
    }
    
    public IEventDataSourceListener getEventListener() {
        return eventListener;
    }

    public void setEventListener(IEventDataSourceListener eventListener) {
        this.eventListener = eventListener;
    }
	
	public List<NewsItem> getAllNews() {	    
		return database.getAllNewsItems();
	}
	
	private static String convertStreamToString(InputStream is) throws UnsupportedEncodingException {
		 
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
	
	public void saveImageCache( Bitmap bmp, String cacheName ) throws IOException {
	    String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/rock63/image_cache";
        File dir = new File(file_path);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir, cacheName + ".png");
        FileOutputStream fOut = new FileOutputStream(file);
        
        bmp.compress(Bitmap.CompressFormat.PNG, 85, fOut);
        fOut.flush();
        fOut.close();
	}
	
	public Bitmap loadImageCache( String cacheName ) {
	    String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/rock63/image_cache/" + cacheName + ".png";

	    Bitmap res = null;
	    
        try {
            File f = new File(file_path);
            FileInputStream s = new FileInputStream(f);
            res = BitmapFactory.decodeStream(s);
        } catch (FileNotFoundException e) {
        }
        
        return res;
	}
	
	public void clearOldNews() {
	    
	    Date before = new Date();
	    before.setTime( before.getTime() - NEWS_LIFETIME_DAYS*1000*60*60*24 );
	    
	    database.deleteAllOldNews(before);
	    
	}
		
	public void refreshNews() {
	    
	    (new AsyncTask<Void, Void, Integer>() {

	          @Override
	          protected Integer doInBackground(Void... v) {
	              
	              SharedPreferences optstore = context.getSharedPreferences(ROCK63_OPTIONS_STORE, 0);
	              int lastNewsUpdate = optstore.getInt(ROCK63_OPTION_LAST_NEWS_UPDATE, 0);
	              
	              String contents = "";
	              URLConnection conn;
	              
	              try {
	                  URL url = null;
	                  if (lastNewsUpdate!=0) {
	                      Date d = CommonUtils.getDateFromTimestamp(lastNewsUpdate);
	                      url = new URL(String.format(NEWS_SOURCE_URL_FROM_DATE, (new SimpleDateFormat("yyyyy/MM/dd", Locale.getDefault())).format(d)));
	                  } else
	                      url = new URL(NEWS_SOURCE_URL);
	                  conn = url.openConnection();
	                  
	                  InputStream in = conn.getInputStream();
	                  contents = convertStreamToString(in);
	                  
	                  
	              } catch (MalformedURLException e) {
	                  Log.e("Rock63", e.getMessage());
	              } catch (IOException e) {
	                  Log.e("Rock63", e.getMessage());
	              }
	              
	              if (contents!="") {
	                  
	                  try {
	                      JSONArray news = new JSONArray(contents);
	                  
	                      clearOldNews();
	                      
	                      //debug
	                      database.deleteAllNewsItems();
	                      
	                      List<Integer> ids = database.getAllNewsItemsIds();
	                  
	                      for (int i = 0; i<news.length(); i++) {
	                          JSONObject newsItemJson = news.getJSONObject(i);
	                          
	                          int id = newsItemJson.getInt("id");
	                          
	                          if (ids.contains(Integer.valueOf(id)))
	                              continue;
	                          
	                          NewsItem newsItem = new NewsItem();
	                          newsItem.setId(id);
	                          newsItem.setDate(CommonUtils.getDateFromTimestamp(newsItemJson.getInt("created")));
	                          if (newsItemJson.has("img")) {
	                              newsItem.setSmallThumbUrl(newsItemJson.getJSONArray("img").getJSONObject(0).getString("thumb_small"));
	                              newsItem.setMediumThumbUrl(newsItemJson.getJSONArray("img").getJSONObject(0).getString("thumb_medium"));
	                          }
	                          newsItem.setTitle(newsItemJson.getString("title"));
	                          newsItem.setBody(newsItemJson.getString("text"));
	                          newsItem.setNew(true);
	                          
	                          database.addNewsItem(newsItem);
	                      }
	                  
	                  } catch (JSONException e) {
	                      
	                  }
	                  
                      SharedPreferences.Editor editor = optstore.edit();
                      editor.putInt(ROCK63_OPTION_LAST_NEWS_UPDATE, lastNewsUpdate);
                      editor.commit();
	                  
	                  return 0;
	              } else
	                  return 1;
	          }

	          @Override
	          protected void onPostExecute(Integer result) {
	              if (newsListener!=null) {
    	              if (result==0) {
    	                  newsListener.newsRefreshed(DataSource.this);
    	              } else {
    	                  newsListener.newsRefreshFailed(DataSource.this);
    	              }
	              }
	          }
	          
	    }).execute();
		
	}
	
	public List<Event> getAllEvents() {
	    
	    List<Event> res = database.getAllEvents();
	    
	    for (int i = 0; i<res.size(); i++)
	        if (res.get(i).getPlaceId()!=NO_PLACE_ID)
	            res.get(i).setPlace(database.getPlaceById(res.get(i).getPlaceId()));
	    
	    return res;
	    
	}
	
	public void refreshEvents() {
		
				
		(new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... v) {
            
                String contents = "";
                URLConnection conn;
                
                try {
                    conn = new URL(EVENTS_SOURCE_URL).openConnection();
                    
                    InputStream in = conn.getInputStream();
                    contents = convertStreamToString(in);
                    
                } catch (MalformedURLException e) {
                    
                } catch (IOException e) {
                    
                }
                
                if (contents!="") {
                    
                    List<Integer> locations = database.getAllPlaceIds();
                    boolean updatePlaces = false;
                                
                    database.deleteAllEvents();
                                
                    try {
                        JSONArray events = new JSONArray(contents);
                        
                        for (int i = 0; i<events.length(); i++) {
                            JSONObject eventJson = events.getJSONObject(i);
                            
                            Event e = new Event();
                            e.setId(eventJson.getInt("id"));
                            e.setTitle(eventJson.getString("title"));
                            e.setBody(eventJson.getString("datdescription"));
                            e.setStart(CommonUtils.getDateFromTimestamp(eventJson.getInt("start_time")));
                            if (eventJson.has("img")) {
                                e.setMediumThumbUrl(eventJson.getJSONArray("img").getJSONObject(0).getString("thumb_medium"));
                            }
                            if (eventJson.has("end_time"))
                                e.setEnd(CommonUtils.getDateFromTimestamp(eventJson.getInt("end_time")));
                            if (eventJson.has("locid")) {
                                e.setPlaceId(eventJson.getInt("locid"));
                                
                                if (!updatePlaces && !locations.contains(e.getPlaceId())) {
                                    updatePlaces = true;
                                }
                            } else {
                                e.setPlaceId(NO_PLACE_ID);
                            }
                            
                            database.addEvent(e);
                        }
                        
                        if (updatePlaces) {
                            if (!refreshPlacesSync()) {
                                return 1;
                            }
                        }
                    
                    } catch (JSONException e) {
                        
                    } catch (MalformedURLException e) {
                        
                    } catch (IOException e) {
                        
                    }
                    
                    return 0;
                } else
                    return 1;
                
            }
            
    		@Override
            protected void onPostExecute(Integer result) { 
    		    if (eventListener!=null) {
                    if (result==0) {         
                        eventListener.eventsRefreshed(DataSource.this);
                    } else {
                        eventListener.eventsRefreshFailed(DataSource.this);
                    }
    		    }
            }
        
	    }).execute();
	}
	
	public boolean refreshPlacesSync() throws MalformedURLException, IOException {
		
		String contents = "";
		URLConnection conn;
		
		try {
			conn = new URL(PLACES_SOURCE_URL).openConnection();
			
			InputStream in = conn.getInputStream();
	        contents = convertStreamToString(in);
	        
		} catch (MalformedURLException e) {
			
		} catch (IOException e) {
			
		}
		
		if (contents!="") {
			
			try {
				JSONArray places = new JSONArray(contents);
			
			//obj.getJSONArray("")
			
				for (int i = 0; i<places.length(); i++) {
					JSONObject placeJson = places.getJSONObject(i);
					
					Place place = new Place();
					place.setId(placeJson.getInt("id"));
					place.setName(placeJson.getString("venue"));
					place.setAddress(placeJson.getString("city") + " " + placeJson.getString("street"));
					place.setUrl(placeJson.getString("url"));
					place.setPhone(placeJson.getString("state"));
					place.setVkUrl(placeJson.getString("plz"));
					place.setMapImageUrl(placeJson.getString("locimage"));
										
					database.addPlace(place);
				}
			
			} catch (JSONException e) {
				
			}
			
			return true;
		} else
			return false;
	}
	
}
