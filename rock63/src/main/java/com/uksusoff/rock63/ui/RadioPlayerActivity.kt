package com.uksusoff.rock63.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.app.ShareCompat
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.uksusoff.rock63.R
import com.uksusoff.rock63.services.IRadioPlayerServiceListener
import com.uksusoff.rock63.services.RadioPlayingService
import com.uksusoff.rock63.services.RadioPlayingService.RadioBinder
import com.uksusoff.rock63.services.RadioPlayingService_
import com.uksusoff.rock63.utils.CommonUtils
import org.androidannotations.annotations.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.*

/**
 * Created by User on 16.05.2016.
 */
@SuppressLint("Registered")
@EActivity(R.layout.a_radio_player)
@OptionsMenu(R.menu.menu_detail)
open class RadioPlayerActivity : BaseMenuActivity() {

    private var mBoundService: RadioPlayingService? = null
    private var mIsBound = false
    private var loadTitleTimer: Timer? = null

    @ViewById(R.id.radio_track_title)
    protected lateinit var trackTitle: TextView
    @ViewById(R.id.radio_play_btn)
    protected lateinit var playBtn: ImageButton
    @ViewById(R.id.radio_volume_bar)
    protected lateinit var volumeBar: SeekBar

    private val radioPlayerServiceListener: IRadioPlayerServiceListener =
            object : IRadioPlayerServiceListener {
        override fun onPause() {
            playBtn.setImageResource(R.drawable.play_dark)
        }

        override fun onPlay() {
            playBtn.setImageResource(R.drawable.pause_dark)
        }

        override fun onStop() {
            playBtn.setImageResource(R.drawable.play_dark)
        }
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mBoundService = (service as RadioBinder).service
            mIsBound = true
            syncUi()
            mBoundService!!.addListener(radioPlayerServiceListener)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mBoundService!!.removeListener(radioPlayerServiceListener)
            mBoundService = null
        }
    }

    fun doBindService() {
        bindService(
                RadioPlayingService_.intent(applicationContext).get(),
                mConnection,
                Context.BIND_AUTO_CREATE
        )
        mIsBound = true
    }

    fun doUnbindService() {
        if (mIsBound) { // Detach our existing connection.
            unbindService(mConnection)
            mIsBound = false
        }
    }

    override fun init() {
        super.init()
        if (!RadioPlayingService.isServiceRunning) {
            applicationContext.startService(
                Intent(applicationContext, RadioPlayingService::class.java)
            )
        }
        doBindService()
        loadTitleTimer = Timer()
        loadTitleTimer!!.schedule(object : TimerTask() {
            override fun run() {
                loadTitle()
            }
        }, 0, 5000)
        trackTitle.movementMethod = LinkMovementMethod.getInstance()
        volumeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                changeVolume(progress.toFloat() / seekBar.max.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        syncUi()
    }

    public override fun onPause() {
        super.onPause()
        loadTitleTimer!!.cancel()
        loadTitleTimer!!.purge()
    }

    public override fun onDestroy() {
        doUnbindService()
        super.onDestroy()
    }

    fun changeVolume(`val`: Float) {
        while (mBoundService == null) {
            try {
                Thread.sleep(0)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
        mBoundService!!.setStreamVolume(`val`)
    }

    @Click(R.id.radio_play_btn)
    fun playButtonToggle() {
        while (mBoundService == null) {
            try {
                Thread.sleep(0)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        if (mBoundService!!.isStreamPlaying) {
            mBoundService!!.stopPlay()
            playBtn.setImageResource(R.drawable.play_dark)
        } else {
            mBoundService!!.startPlay()
            playBtn.setImageResource(R.drawable.pause_dark)
        }
    }

    private fun syncUi() {
        if (mBoundService == null) return
        if (mBoundService!!.isStreamPlaying) playBtn.setImageResource(R.drawable.pause_dark) else playBtn.setImageResource(R.drawable.play_dark)
        //TODO: make constant
        if (mBoundService!!.lastVolume == null) {
            mBoundService!!.setStreamVolume(0.5f)
            volumeBar.progress = (0.5f * volumeBar.max.toFloat()).toInt()
        } else {
            mBoundService!!.setStreamVolume(mBoundService!!.lastVolume!!)
            volumeBar.progress = (mBoundService!!.lastVolume!! * volumeBar.max.toFloat()).toInt()
        }
    }

    fun loadTitle() {
        try {
            val jsonString = CommonUtils.convertStreamToString(
                    URL(RADIO_INFO_URL).openConnection().getInputStream()
            )
            val info = JSONObject(jsonString)
            val res = String.format(
                    "%s - %s",
                    info.getString("artist"),
                    info.getString("title")
            )
            runOnUiThread {
                trackTitle.text = HtmlCompat.fromHtml(res, HtmlCompat.FROM_HTML_MODE_LEGACY)
                lastLoadedTrackName = HtmlCompat.fromHtml(res, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NullPointerException) { //Strange exception occurs here
//but as long as this section isn't critical, we can skip it
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    @OptionsItem(R.id.menu_share)
    fun shareRadio() {
        startActivity(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setSubject(getString(R.string.share_radio_title))
                .setText(getString(R.string.share_radio_body, lastLoadedTrackName))
                .intent
        )
    }

    companion object {
        private const val RADIO_INFO_URL = "https://rock63.ru/a/vz/play.json"
        var lastLoadedTrackName = ""
            private set
    }
}