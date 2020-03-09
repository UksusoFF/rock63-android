package com.uksusoff.rock63.ui

import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.uksusoff.rock63.R
import com.uksusoff.rock63.exceptions.NoInternetException
import com.uksusoff.rock63.ui.adapters.AdvSimpleAdapter
import org.androidannotations.annotations.Background
import org.androidannotations.annotations.EActivity
import org.androidannotations.annotations.UiThread
import org.androidannotations.annotations.ViewById

/**
 * Created by Vyacheslav Vodyanov on 16.05.2016.
 */
@EActivity(R.layout.a_list)
abstract class ItemListActivity : BaseMenuActivity() {

    @ViewById(R.id.list)
    protected lateinit var list: ListView
    @ViewById(R.id.refresh)
    protected lateinit var refresh: SwipeRefreshLayout
    @ViewById(R.id.list_empty)
    protected lateinit var emptyView: TextView

    protected abstract fun createAdapterFromStorageItems(handler: (ListAdapter) -> Unit)
    protected abstract fun refreshItemStorage()
    protected abstract val emptyListTextResId: Int

    private var isTriedToLoad = false

    override fun init() {
        super.init()

        emptyView.setText(emptyListTextResId)
        loadItemsFromDatabase()

        refresh.setOnRefreshListener { reloadAll() }
    }

    @Background
    open fun reloadAll() {
        try {
            setRefreshIndicatorActive(true)
            refreshItemStorage()
        } catch (e: NoInternetException) {
            showWarning(R.string.error_no_internet)
        } finally {
            setRefreshIndicatorActive(false)
        }

        loadItemsFromDatabase()
    }

    @UiThread
    open fun setRefreshIndicatorActive(active: Boolean) {
        refresh.isRefreshing = active
    }

    @UiThread
    open fun loadItemsFromDatabaseInternal(adapter: ListAdapter) {
        list.adapter = adapter
        if (list.adapter.isEmpty) {
            if (!isTriedToLoad) {
                isTriedToLoad = true
                reloadAll()
            }
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
    }

    @UiThread
    open fun loadItemsFromDatabase() {
        createAdapterFromStorageItems { loadItemsFromDatabaseInternal(it) }
    }
}