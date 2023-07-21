package com.uksusoff.rock63.ui

import androidx.core.view.MenuItemCompat
import android.view.Menu
import android.widget.ListAdapter
import androidx.appcompat.widget.SearchView
import com.uksusoff.rock63.R
import com.uksusoff.rock63.data.DataSource
import com.uksusoff.rock63.data.DataSource.NoInternetException
import com.uksusoff.rock63.data.entities.Event
import com.uksusoff.rock63.ui.adapters.AdvSimpleAdapter
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EActivity
import org.androidannotations.annotations.ItemClick
import org.androidannotations.annotations.OptionsMenu
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by User on 13.05.2016.
 */
@EActivity(R.layout.a_list)
@OptionsMenu(R.menu.menu_search_list)
open class EventsListActivity : ItemListActivity() {

    @Bean
    protected lateinit var source: DataSource

    override var isRefreshing: Boolean
        get() = Companion.isRefreshing
        set(value) { Companion.isRefreshing = value }

    override var activeActivity: ItemListActivity?
        get() = Companion.activeActivity
        set(value) { Companion.activeActivity = value as EventsListActivity }

    override val emptyListTextResId: Int
        get() = R.string.events_no_item_text

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val searchView = MenuItemCompat.getActionView(
                menu.findItem(R.id.menu_search)
        ) as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                FilterList(newText)
                return false
            }
        })
        searchView.setOnCloseListener {
            FilterList("")
            false
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun FilterList(query: String) {
        (list.adapter as AdvSimpleAdapter).filter.filter(query)
    }

    override fun createAdapterFromStorageItems(): ListAdapter {
        val events = source.allEvents
        val data: MutableList<Map<String, Any?>> = ArrayList()
        for (i in events.indices) {
            val item = events[i]
            val datum: MutableMap<String, Any?> = HashMap(3)
            datum["title"] = item.title
            val f = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            datum["date"] = f.format(item.start)
            if (item.place != null) datum["place"] = item.place!!.name else datum["place"] = getText(R.string.events_no_place_text)
            datum["obj"] = item
            data.add(datum)
        }
        return AdvSimpleAdapter(this, data,
                R.layout.i_event_item, arrayOf("title", "date", "place"), intArrayOf(R.id.event_item_title,
                R.id.event_item_date,
                R.id.event_item_place))
    }

    @Throws(NoInternetException::class)
    override fun refreshItemStorage() {
        source.refreshEvents()
    }

    @ItemClick(R.id.list)
    open fun eventItemClicked(item: Map<String?, Any?>) {
        EventDetailActivity_.intent(this).eventId((item["obj"] as Event?)!!.id).start()
    }

    companion object {
        private var isRefreshing = false
        private var activeActivity: EventsListActivity? = null
    }
}