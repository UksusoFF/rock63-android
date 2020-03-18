package com.uksusoff.rock63.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.uksusoff.rock63.data.DataProviderComponent
import org.androidannotations.annotations.Background
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EService
import java.util.*

@SuppressLint("Registered")
@EService
open class DataUpdateService : Service() {

    interface IDataUpdateServiceListener {
        fun onNewsUpdateStarted();
        fun onNewsUpdateFinished();
        fun onEventsUpdateStarted();
        fun onEventsUpdateFinished();
    }

    inner class DataUpdateBinder : Binder() {
        val service: DataUpdateService
            get() = this@DataUpdateService
    }

    @Bean
    open lateinit var dataProvider: DataProviderComponent

    private val binder = DataUpdateBinder()
    private val listeners: MutableList<IDataUpdateServiceListener>
            = Collections.synchronizedList(LinkedList())

    var isNewsRefreshing:Boolean = false
        private set
    var isEventsRefreshing:Boolean = false
        private set

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun subscribe(listener: IDataUpdateServiceListener) {
        this.listeners.add(listener)
    }

    fun unsubscribe(listener: IDataUpdateServiceListener) {
        this.listeners.remove(listener)
    }

    @Background
    open fun updateNews() {
        synchronized(this) {
            if (this.isNewsRefreshing) {
                return
            }

            this.isNewsRefreshing = true

            synchronized(listeners) {
                for (listener in listeners) {
                    listener.onNewsUpdateStarted()
                }
            }
        }

        dataProvider.refreshNews()

        synchronized(this) {
            this.isNewsRefreshing = false

            synchronized(listeners) {
                for (listener in listeners) {
                    listener.onNewsUpdateFinished()
                }
            }
        }
    }

    @Background
    open fun updateEvents() {
        synchronized(this) {
            if (this.isEventsRefreshing) {
                return
            }

            this.isEventsRefreshing = true

            synchronized(listeners) {
                for (listener in listeners) {
                    listener.onEventsUpdateStarted()
                }
            }
        }

        dataProvider.refreshEvents()

        synchronized(this) {
            this.isEventsRefreshing = false

            synchronized(listeners) {
                for (listener in listeners) {
                    listener.onEventsUpdateFinished()
                }
            }
        }
    }

}