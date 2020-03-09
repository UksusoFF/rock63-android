package com.uksusoff.rock63.ui

import android.annotation.SuppressLint
import android.view.Menu
import android.widget.ListAdapter
import androidx.appcompat.widget.SearchView
import com.uksusoff.rock63.R
import com.uksusoff.rock63.data.DataProviderComponent
import com.uksusoff.rock63.data.entities.Event
import com.uksusoff.rock63.ui.adapters.AdvSimpleAdapter
import org.androidannotations.annotations.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Vyacheslav Vodyanov on 13.05.2016.
 */
@SuppressLint("Registered")
@EActivity(R.layout.a_list)
@OptionsMenu(R.menu.menu_search_list)
open class EventsListActivity : ItemListActivity() {

    @Bean
    protected lateinit var providerComponent: DataProviderComponent

    override val emptyListTextResId: Int
        get() = R.string.events_no_item_text

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val searchView = menu.findItem(R.id.menu_search).actionView as SearchView

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

    @Background
    override fun createAdapterFromStorageItems(handler: (ListAdapter) -> Unit) {
        handler(AdvSimpleAdapter(this,
                providerComponent.allEvents.map {
                    val datum: HashMap<String, Any?> = HashMap(4)
                    datum["title"] = it.title
                    val f = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    datum["date"] = f.format(it.start)
                    datum["place"] = it.place?.name ?: getText(R.string.events_no_place_text)
                    datum["obj"] = it
                    datum
                },
                R.layout.i_event_item,
                arrayOf("title", "date", "place"),
                intArrayOf( R.id.event_item_title, R.id.event_item_date, R.id.event_item_place )
        ))
    }

    override fun refreshItemStorage() {
        providerComponent.refreshEvents()
    }

    @ItemClick(R.id.list)
    open fun eventItemClicked(item: Map<String?, Any?>) {
        EventDetailActivity_.intent(this).eventId((item["obj"] as Event).id).start()
    }
}