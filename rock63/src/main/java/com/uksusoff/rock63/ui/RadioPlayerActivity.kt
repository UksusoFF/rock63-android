package com.uksusoff.rock63.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.text.method.LinkMovementMethod
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.ShareCompat
import androidx.core.text.HtmlCompat
import com.uksusoff.rock63.R
import com.uksusoff.rock63.services.IRadioPlayerServiceListener
import com.uksusoff.rock63.services.RadioPlayingService
import com.uksusoff.rock63.services.RadioPlayingService.RadioBinder
import com.uksusoff.rock63.services.RadioPlayingService_
import com.uksusoff.rock63.utils.readJsonFromUrl
import org.androidannotations.annotations.*
import java.util.*

/**
 * Created by Vyacheslav Vodyanov on 16.05.2016.
 */
@SuppressLint("Registered")
@EActivity(R.layout.a_radio_player)
@OptionsMenu(R.menu.menu_detail)
open class RadioPlayerActivity : BaseMenuActivity() {

    private var boundService: RadioPlayingService? = null
    private var lastLoadedTrackName = ""

    private lateinit var loadTitleTimer: Timer


    @ViewById(R.id.radio_track_title)
    protected lateinit var trackTitle: TextView
    @ViewById(R.id.radio_play_btn)
    protected lateinit var playBtn: ImageButton
    @ViewById(R.id.radio_volume_bar)
    protected lateinit var volumeBar: SeekBar

    private val onConnectionReadyHandlers = LinkedList<(service: RadioPlayingService) -> Unit>()

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

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            val service = (binder as RadioBinder).service
            boundService = service
            handleConnectionReady(service)
            syncUI()
            service.addListener(radioPlayerServiceListener)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            boundService?.removeListener(radioPlayerServiceListener)
            boundService = null
        }
    }

    private fun onConnectionReady(handler: (service: RadioPlayingService) -> Unit) {
        boundService?.let {
            handler(it)
        } ?: run {
            this.onConnectionReadyHandlers.add(handler)
        }
    }

    private fun handleConnectionReady(service: RadioPlayingService) {
        for (handler in this.onConnectionReadyHandlers) {
            handler(service)
        }

        this.onConnectionReadyHandlers.clear()
    }

    private fun doBindService() {
        bindService(
                RadioPlayingService_.intent(applicationContext).get(),
                connection,
                Context.BIND_AUTO_CREATE
        )
    }

    private fun doUnbindService() {
        unbindService(connection)
    }

    override fun init() {
        super.init()

        if (!RadioPlayingService.isServiceRunning) {
            applicationContext.startService(
                Intent(applicationContext, RadioPlayingService::class.java)
            )
        }

        doBindService()

        loadTitleTimer = Timer().apply {
            this.schedule(object : TimerTask() {
                override fun run() {
                    loadTitle()
                }
            }, 0, 5000)
        }

        trackTitle.movementMethod = LinkMovementMethod.getInstance()
        volumeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                changeVolume(progress.toFloat() / seekBar.max.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        syncUI()
    }

    public override fun onPause() {
        super.onPause()
        loadTitleTimer.cancel()
        loadTitleTimer.purge()
    }

    public override fun onDestroy() {
        doUnbindService()
        super.onDestroy()
    }

    fun changeVolume(`val`: Float) {
        onConnectionReady(fun(service: RadioPlayingService) {
            service.volume = `val`
        })
    }

    @Click(R.id.radio_play_btn)
    fun playButtonToggle() {
        onConnectionReady {
            if (it.isStreamPlaying) {
                it.stopPlay()
                playBtn.setImageResource(R.drawable.play_dark)
            } else {
                it.startPlay()
                playBtn.setImageResource(R.drawable.pause_dark)
            }
        }
    }

    private fun syncUI() {
        val service = boundService ?: return

        if (service.isStreamPlaying) {
            playBtn.setImageResource(R.drawable.pause_dark)
        } else {
            playBtn.setImageResource(R.drawable.play_dark)
        }

        volumeBar.progress = (service.volume * volumeBar.max.toFloat()).toInt()
    }

    fun loadTitle() {
        try {
            var title = ""
            var artist = ""

            readJsonFromUrl(RADIO_INFO_URL) {
                it.beginObject()
                while (it.hasNext()) {
                    when (it.nextName()) {
                        "title" -> title = it.nextString()
                        "artist" -> artist = it.nextString()
                        else -> it.skipValue()
                    }
                }
                it.endObject()
            }

            val res = "$title - $artist"
            runOnUiThread {
                trackTitle.text = HtmlCompat.fromHtml(res, HtmlCompat.FROM_HTML_MODE_LEGACY)
                lastLoadedTrackName = HtmlCompat.fromHtml(res, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            }
        } catch (e: NullPointerException) {
            //Strange exception occurs here
            //but as long as this section isn't critical, we can skip it
            e.printStackTrace()
        } catch (e: Throwable) {
            //if something went wrong,
            //radio title just wont update
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
    }
}