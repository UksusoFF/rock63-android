package com.uksusoff.rock63.services

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.uksusoff.rock63.R
import com.uksusoff.rock63.ui.RadioPlayerActivity_
import org.androidannotations.annotations.EService
import java.util.*


@SuppressLint("Registered")
@EService
open class RadioPlayingService : Service() {

    var lastVolume: Float? = null
        private set
    private var notificationPlaced = false
    private val listeners: MutableList<IRadioPlayerServiceListener> = LinkedList()
    private var mediaPlayer: MediaPlayer? = null

    private val player: MediaPlayer
        get() = mediaPlayer ?: run {
            return initMediaPlayer()
        }

    val isStreamPlaying: Boolean
        get() = player.isPlaying

    fun addListener(`object`: IRadioPlayerServiceListener): Boolean {
        return listeners.add(`object`)
    }

    fun removeListener(`object`: IRadioPlayerServiceListener?): Boolean {
        return listeners.remove(`object`)
    }

    inner class RadioBinder : Binder() {
        val service: RadioPlayingService
            get() = this@RadioPlayingService
    }

    private val mBinder: IBinder = RadioBinder()
    override fun onBind(intent: Intent): IBinder { // TODO: Return the communication channel to the service.
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
    }

    override fun onDestroy() {
        isServiceRunning = false
        super.onDestroy()
    }

    fun startPlay() {
        playStream()
        for (listener in listeners) {
            listener.onPlay()
        }
    }

    fun stopPlay() {
        stopStream(true)
        for (listener in listeners) {
            listener.onStop()
        }
    }

    private fun pausePlay() {
        stopStream(false)
        for (listener in listeners) {
            listener.onPause()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val action = it.action
            if (!TextUtils.isEmpty(action)) {
                when (action) {
                    ACTION_PLAY -> startPlay()
                    ACTION_PAUSE -> pausePlay()
                    ACTION_STOP -> stopPlay()
                }
            }
        } ?: run {
            initMediaPlayer()
        }
        return START_STICKY
    }

    private fun startForegroundPlayer() {
        if (notificationPlaced) {
            return
        }

        val notificationIntent = Intent(this, RadioPlayerActivity_::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val contentIntent = PendingIntent.getActivity(
                this,
                REQUEST_CODE_ACTIVITY,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val remoteView = RemoteViews(packageName, R.layout.player_notification_control)
        remoteView.setOnClickPendingIntent(R.id.stop_btn,
                PendingIntent.getService(applicationContext,
                        REQUEST_CODE_STOP, Intent(ACTION_STOP),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        )
        remoteView.setOnClickPendingIntent(R.id.play_btn,
                PendingIntent.getService(applicationContext,
                        REQUEST_CODE_PLAY, Intent(ACTION_PLAY),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        )
        remoteView.setOnClickPendingIntent(R.id.pause_btn,
                PendingIntent.getService(applicationContext,
                        REQUEST_CODE_PAUSE, Intent(ACTION_PAUSE),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        )

        var channel = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = createChannel()

        val notification = NotificationCompat.Builder(applicationContext, channel)
                .setContent(remoteView)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_launcher).setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .build()
        startForeground(NOTIFICATION_ID, notification)
        notificationPlaced = true
    }

    @TargetApi(26)
    @Synchronized
    private fun createChannel(): String {
        val manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val id = "player_channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(id, "Player Channel", importance)
        manager.createNotificationChannel(channel)
        return id
    }

    private fun stopForegroundPlayer() {
        stopForeground(true)
        notificationPlaced = false
    }

    private fun initMediaPlayer():MediaPlayer {
        val playerLocal = MediaPlayer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            playerLocal.setAudioAttributes(AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        } else {
            playerLocal.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
        playerLocal.setOnPreparedListener { playerLocal.start() }
        this.mediaPlayer = playerLocal
        return playerLocal
    }

    fun setStreamVolume(v: Float) {
        lastVolume = v
        player.setVolume(v, v)
    }

    private fun playStream() {
        try {
            if (!player.isPlaying) {
                startForegroundPlayer()
                try {
                    player.setDataSource(RADIO_URL)
                } catch (e: IllegalStateException) {
                    player.reset()
                    player.setDataSource(RADIO_URL)
                }
                player.prepareAsync()
            }
        } catch (e: Exception) { //TODO: bad idea
            e.printStackTrace()
        }
    }

    private fun stopStream(stopForeground: Boolean) {
        if (stopForeground) {
            stopForegroundPlayer()
        }
        player.stop()
        player.reset()
    }

    companion object {
        const val RADIO_URL = "http://play.vzradio.ru:8000/onair"
        const val ACTION_STOP = "com.uksusoff.rock63.ACTION_STOP"
        const val ACTION_PLAY = "com.uksusoff.rock63.ACTION_PLAY"
        const val ACTION_PAUSE = "com.uksusoff.rock63.ACTION_PAUSE"
        private const val NOTIFICATION_ID = 1
        private const val REQUEST_CODE_STOP = 1
        private const val REQUEST_CODE_PLAY = 2
        private const val REQUEST_CODE_PAUSE = 3
        private const val REQUEST_CODE_ACTIVITY = 4

        var isServiceRunning = false
            private set
    }
}