package com.uksusoff.rock63.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.app.ShareCompat;

import com.uksusoff.rock63.R;
import com.uksusoff.rock63.services.IRadioPlayerServiceListener;
import com.uksusoff.rock63.services.RadioPlayingService;
import com.uksusoff.rock63.services.RadioPlayingService_;
import com.uksusoff.rock63.utils.CommonUtils;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

@EActivity(R.layout.a_radio_player)
@OptionsMenu(R.menu.menu_detail)
public class RadioPlayerActivity extends BaseMenuActivity {

    private static final String RADIO_INFO_URL = "https://rock63.ru/a/vz/play.json";

    private RadioPlayingService mBoundService = null;
    private boolean mIsBound = false;
    private Timer loadTitleTimer;

    private static String lastLoadedTrackName = "";

    public static String getLastLoadedTrackName() {
        return lastLoadedTrackName;
    }

    @ViewById(R.id.radio_track_title)
    TextView trackTitle;

    @ViewById(R.id.radio_play_btn)
    ImageButton playBtn;

    @ViewById(R.id.radio_volume_bar)
    SeekBar volumeBar;

    private final IRadioPlayerServiceListener radioPlayerServiceListener = new IRadioPlayerServiceListener() {

        @Override
        public void OnPause() {
            playBtn.setImageResource(R.drawable.play_dark);
        }

        @Override
        public void OnPlay() {
            playBtn.setImageResource(R.drawable.pause_dark);
        }

        @Override
        public void OnStop() {
            playBtn.setImageResource(R.drawable.play_dark);
        }
    };

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((RadioPlayingService.RadioBinder) service).getService();

            mIsBound = true;

            syncUi();

            mBoundService.addListener(radioPlayerServiceListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService.removeListener(radioPlayerServiceListener);

            mBoundService = null;
        }
    };

    void doBindService() {
        bindService(
                RadioPlayingService_.intent(getApplicationContext()).get(),
                mConnection,
                BIND_AUTO_CREATE
        );
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void init() {
        super.init();

        if (!RadioPlayingService.isServiceRunning()) {
            getApplicationContext().startService(
                    new Intent(getApplicationContext(), RadioPlayingService.class)
            );
        }

        doBindService();

        loadTitleTimer = new Timer();
        loadTitleTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                loadTitle();
            }

        }, 0, 5000);

        trackTitle.setMovementMethod(LinkMovementMethod.getInstance());

        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeVolume((float) progress / (float) seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        syncUi();
    }

    @Override
    public void onPause() {
        super.onPause();
        loadTitleTimer.cancel();
        loadTitleTimer.purge();
    }

    @Override
    public void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }

    public void changeVolume(float val) {
        while (mBoundService == null) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        mBoundService.setStreamVolume(val);
    }

    @Click(R.id.radio_play_btn)
    void playButtonToggle() {
        while (mBoundService == null) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (mBoundService.isStreamPlaying()) {
            mBoundService.stopPlay();
            playBtn.setImageResource(R.drawable.play_dark);
        } else {
            mBoundService.startPlay();

            playBtn.setImageResource(R.drawable.pause_dark);
        }
    }

    private void syncUi() {

        if (mBoundService == null)
            return;

        if (mBoundService.isStreamPlaying())
            playBtn.setImageResource(R.drawable.pause_dark);
        else
            playBtn.setImageResource(R.drawable.play_dark);

        //TODO: make constant

        if (mBoundService.getLastVolume() == null) {
            mBoundService.setStreamVolume(0.5f);
            volumeBar.setProgress((int) (0.5f * (float) volumeBar.getMax()));
        } else {
            mBoundService.setStreamVolume(mBoundService.getLastVolume());
            volumeBar.setProgress((int) (mBoundService.getLastVolume() * (float) volumeBar.getMax()));
        }
    }

    public void loadTitle() {
        try {
            String jsonString = CommonUtils.convertStreamToString(
                    (new URL(RADIO_INFO_URL)).openConnection().getInputStream()
            );

            JSONObject info = new JSONObject(jsonString);

            final String res = String.format(
                    "%s - %s",
                    info.getString("artist"),
                    info.getString("title")
            );

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    trackTitle.setText(Html.fromHtml(res));
                    lastLoadedTrackName = Html.fromHtml(res).toString();
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            //Strange exception occurs here
            //but as long as this section isn't critical, we can skip it
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @OptionsItem(R.id.menu_share)
    void shareRadio() {
        startActivity(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setSubject(getString(R.string.share_radio_title))
                .setText(getString(R.string.share_radio_body, getLastLoadedTrackName()))
                .getIntent()
        );
    }

}
