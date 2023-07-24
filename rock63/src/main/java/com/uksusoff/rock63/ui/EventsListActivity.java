package com.uksusoff.rock63.ui;

import android.view.Menu;
import android.widget.ListAdapter;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;

import com.uksusoff.rock63.R;
import com.uksusoff.rock63.data.DataSource;
import com.uksusoff.rock63.data.entities.Event;
import com.uksusoff.rock63.ui.adapters.AdvSimpleAdapter;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsMenu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@EActivity(R.layout.a_list)
@OptionsMenu(R.menu.menu_search_list)
public class EventsListActivity extends ItemListActivity {

    @Bean
    DataSource source;

    private static boolean isRefreshing = false;
    private static EventsListActivity activeActivity;

    @Override
    protected boolean isRefreshing() {
        return isRefreshing;
    }

    @Override
    protected void setRefreshing(boolean refreshing) {
        isRefreshing = refreshing;
    }

    @Override
    protected ItemListActivity getActiveActivity() {
        return activeActivity;
    }

    @Override
    protected void setActiveActivity(ItemListActivity activity) {
        activeActivity = (EventsListActivity) activity;
    }

    @Override
    protected int getEmptyListTextResId() {
        return R.string.events_no_item_text;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(
                menu.findItem(R.id.menu_search)
        );
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                FilterList(newText);
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                FilterList("");
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void FilterList(String query) {
        ((AdvSimpleAdapter) list.getAdapter()).getFilter().filter(query);
    }

    protected ListAdapter createAdapterFromStorageItems() {

        List<Event> events = source.getAllEvents();

        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            Event item = events.get(i);
            Map<String, Object> datum = new HashMap<>(3);
            datum.put("title", item.getTitle());
            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            datum.put("date", f.format(item.getStart()));
            if (item.getPlace() != null)
                datum.put("place", item.getPlace().getName());
            else
                datum.put("place", getText(R.string.events_no_place_text));
            datum.put("obj", item);

            data.add(datum);
        }

        return new AdvSimpleAdapter(this, data,
                com.uksusoff.rock63.R.layout.i_event_item,
                new String[]{"title", "date", "place"},
                new int[]{com.uksusoff.rock63.R.id.event_item_title,
                        com.uksusoff.rock63.R.id.event_item_date,
                        com.uksusoff.rock63.R.id.event_item_place});
    }

    @Override
    protected void refreshItemStorage() throws DataSource.NoInternetException {
        source.refreshEvents();
    }

    @ItemClick(R.id.list)
    public void eventItemClicked(Map<String, Object> item) {
        EventDetailActivity_.intent(this).eventId(((Event) item.get("obj")).getId()).start();
    }

}
