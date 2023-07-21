package com.uksusoff.rock63.ui

import android.view.View
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.uksusoff.rock63.R
import com.uksusoff.rock63.data.DataProviderComponent.NoInternetException
import org.androidannotations.annotations.Background
import org.androidannotations.annotations.EActivity
import org.androidannotations.annotations.UiThread
import org.androidannotations.annotations.ViewById

/**
 * Created by User on 16.05.2016.
 */
@EActivity(R.layout.a_list)
abstract class ItemListActivity : BaseMenuActivity() {

    @ViewById(R.id.list)
    protected lateinit var list: ListView
    @ViewById(R.id.refresh)
    protected lateinit var refresh: SwipeRefreshLayout
    @ViewById(R.id.list_empty)
    protected lateinit var emptyView: TextView

    protected abstract var isRefreshing: Boolean
    protected abstract var activeActivity: ItemListActivity?
    protected abstract fun createAdapterFromStorageItems(): ListAdapter?
    @Throws(NoInternetException::class)
    protected abstract fun refreshItemStorage()

    protected abstract val emptyListTextResId: Int
    override fun init() {
        super.init()
        activeActivity = this
        emptyView!!.setText(emptyListTextResId)
        loadNewsFromDatabase()
        refresh!!.setOnRefreshListener { refreshList() }
        refresh!!.post {
            if (isRefreshing) {
                setRefreshIndicatorActive(true)
            } else if (list!!.adapter.isEmpty) {
                refreshList()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activeActivity === this) {
            activeActivity = null
        }
    }

    fun refreshList() {
        setRefreshIndicatorActive(true)
        reloadAll()
    }

    @Background
    open fun reloadAll() {
        try {
            this.isRefreshing = true
            refreshItemStorage()
            activeActivity!!.loadNewsFromDatabase()
        } catch (e: NoInternetException) {
            showWarning(R.string.error_no_internet)
        } finally {
            this.isRefreshing = false
        }
        setRefreshIndicatorActive(false)
    }

    @UiThread
    open fun setRefreshIndicatorActive(active: Boolean) {
        refresh!!.isRefreshing = active
    }

    @UiThread
    open fun loadNewsFromDatabase() {
        list.adapter = createAdapterFromStorageItems()
        if (list.adapter.isEmpty) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
    }
}