package com.uksusoff.rock63.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.View
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.uksusoff.rock63.R
import com.uksusoff.rock63.exceptions.NoInternetException
import com.uksusoff.rock63.services.DataUpdateService
import com.uksusoff.rock63.services.DataUpdateService_
import org.androidannotations.annotations.Background
import org.androidannotations.annotations.EActivity
import org.androidannotations.annotations.UiThread
import org.androidannotations.annotations.ViewById

/**
 * Created by Vyacheslav Vodyanov on 16.05.2016.
 */
@EActivity(R.layout.a_list)
abstract class ItemListActivity : BaseMenuActivity(), DataUpdateService.IDataUpdateServiceListener {

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

    protected var dataUpdateService: DataUpdateService? = null

    private val dataUpdateConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            val service = (binder as DataUpdateService.DataUpdateBinder).service
            dataUpdateService = service
            service.subscribe(this@ItemListActivity)
            onDataUpdateServiceConnected(service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            dataUpdateService?.unsubscribe(this@ItemListActivity)
            dataUpdateService = null
        }
    }

    open fun onDataUpdateServiceConnected(service: DataUpdateService) {
    }

    override fun onNewsUpdateStarted() {
    }

    override fun onNewsUpdateFinished() {
    }

    override fun onEventsUpdateStarted() {
    }

    override fun onEventsUpdateFinished() {
    }

    override fun onStart() {
        super.onStart()

        Intent(this, DataUpdateService_::class.java).also { intent ->
            bindService(intent, dataUpdateConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun init() {
        super.init()

        emptyView.setText(emptyListTextResId)
        loadItemsFromDatabase()

        refresh.setOnRefreshListener { refreshItemStorage() }
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
                refreshItemStorage()
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