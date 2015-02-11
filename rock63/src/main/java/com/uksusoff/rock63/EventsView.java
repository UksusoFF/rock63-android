package com.uksusoff.rock63;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import android.support.v4.app.ListFragment;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import com.uksusoff.rock63.data.DataSource;
import com.uksusoff.rock63.data.IEventDataSourceListener;
import com.uksusoff.rock63.data.entities.Event;
import com.uksusoff.rock63.utils.AdvSimpleAdapter;

@EFragment
public class EventsView extends ListFragment implements IEventDataSourceListener, IRefreshableFragment, ISearchableFragment {

    @Bean
    DataSource source;
    
    private View progressWrap;
    private ProgressBar progress;
    //private List<Event> mEvents;
        
    @AfterViews
    void init() {
        source.setEventListener(this);

        LayoutInflater inflater = (LayoutInflater)this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        progressWrap = inflater.inflate(R.layout.listview_header_progress, null);
        progress = (ProgressBar)progressWrap.findViewById(R.id.header_progress_bar);
        progress.setVisibility(View.GONE);
        
        setEmptyText(getActivity().getText(R.string.events_no_item_text));

        // We have a menu item to show in action bar.
        setHasOptionsMenu(false);
        
        if (getListView().getAdapter()==null)
            getListView().addHeaderView(progressWrap);
        
        if (!loadEventsFromDataSource(source))
            refreshEvents();
    }
    
    @Override
    public void onDestroy() {
        
        source.setEventListener(null);
        
        super.onDestroy();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id); 
        Map<String, Object> dataItem = (Map<String, Object>) l.getAdapter().getItem(position);
        if (dataItem!=null) {
            Event eventItem = (Event)dataItem.get("obj");
            EventDetailActivity_.intent(getActivity()).eventId(eventItem.getId()).start();
        }
    }
    
    @Override
    public void eventsRefreshed(DataSource lSource) {
        
        loadEventsFromDataSource(lSource);
        
        if (getView()!=null) {
            this.setListShown(true);
            progress.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onRefresh() {
        // TODO Auto-generated method stub
        refreshEvents();
    }

    @Override
    public void eventsRefreshFailed(DataSource source) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onSearch(String query) {
        // TODO Auto-generated method stub
        /*int p = searchItemPositionByQuery(query);
        if (p!=-1) {
            //getListView().smoothScrollToPosition(p);
            getListView().setSelection(p);
        }*/
        
        if (getListView().getAdapter()!=null) {
            ((AdvSimpleAdapter)getListAdapter()).getFilter().filter(query);
        }
    }
    
    public void onFilter(String query) {
        if (getListAdapter()!=null) {
            ((AdvSimpleAdapter)getListAdapter()).getFilter().filter(query);
            //((SimpleAdapter)getListAdapter()).
        }
    }
    
    public void onClearFilter() {
        if (getListAdapter()!=null) {
            ((AdvSimpleAdapter)getListAdapter()).getFilter().filter("");
        }
    }
    
    public void refreshEvents() {
        
        // Fake empty container layout
        /*RelativeLayout lContainerLayout = new RelativeLayout(this);
        lContainerLayout.setLayoutParams(new RelativeLayout.LayoutParams( LayoutParams.FILL_PARENT , LayoutParams.FILL_PARENT ));

        
        this.getActivity().addContentView( lContainerLayout, new LayoutParams( LayoutParams.FILL_PARENT , LayoutParams.FILL_PARENT ) )
        */
        
        getListView().smoothScrollToPosition(0);
        //getListView().scrollTo(getListView().getScrollX(), 0);
        progress.setVisibility(View.VISIBLE);
                
        source.refreshEvents();
    }
    
    public boolean loadEventsFromDataSource(DataSource lSource) {
        
        List<Event> events = null;
        try {
            events = lSource.getAllEvents();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        if (events.size()==0)
            return false;
        
        //mEvents = events;
        
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        for (int i = 0; i<events.size(); i++) {
            Event item = events.get(i);
            Map<String, Object> datum = new HashMap<String, Object>(3);
            datum.put("title", item.getTitle());
            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            datum.put("date", f.format(item.getStart()));
            if (item.getPlace()!=null)
                datum.put("place", item.getPlace().getName());
            else
                datum.put("place", getActivity().getText(R.string.events_no_place_text));
            datum.put("obj", item);
            
            data.add(datum);
        }

        AdvSimpleAdapter adapter = new AdvSimpleAdapter(this.getActivity(), data,
                                                  com.uksusoff.rock63.R.layout.event_item_view,
                                                  new String[] {"title", "date", "place"},
                                                  new int[] { com.uksusoff.rock63.R.id.event_item_title,
                                                              com.uksusoff.rock63.R.id.event_item_date,
                                                              com.uksusoff.rock63.R.id.event_item_place });
                
        this.setListAdapter(adapter);
        
        return true;
    }
    
    public int searchItemPositionByQuery( String query ) {
                
        try {
            for (int i = 0; i<getListView().getAdapter().getCount(); i++) {
                
                if (getListView().getAdapter().getItem(i)==null)
                    continue;
                
                @SuppressWarnings("unchecked")
                Event e = (Event)((Map<String, Object>)getListView().getAdapter().getItem(i)).get("obj");
                
                if (e.getTitle().toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault())))
                    return i;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return -1;
        
    }
    
}
