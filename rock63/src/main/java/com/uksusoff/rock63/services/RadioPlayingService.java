package com.uksusoff.rock63.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.uksusoff.rock63.R;
import com.uksusoff.rock63.ui.RadioPlayerActivity_;

import org.androidannotations.annotations.EService;

import java.util.LinkedList;
import java.util.List;

@EService
public class RadioPlayingService extends Service {

    public static final String RADIO_URL = "http://play.vzradio.ru:8000/onair";

    public static final String ACTION_STOP = "com.uksusoff.rock63.ACTION_STOP";
    public static final String ACTION_PLAY = "com.uksusoff.rock63.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.uksusoff.rock63.ACTION_PAUSE";

    private static final int NOTIFICATION_ID = 1;

    private static final int REQUEST_CODE_STOP = 1;
    private static final int REQUEST_CODE_PLAY = 2;
    private static final int REQUEST_CODE_PAUSE = 3;
    private static final int REQUEST_CODE_ACTIVITY = 4;

    private static boolean mRunning = false;
    private MediaPlayer mediaPlayer;
    private Float lastVolume = null;
    private boolean notificationPlaced = false;
    private final List<IRadioPlayerServiceListener> listeners = new LinkedList<>();

    public void addListener(IRadioPlayerServiceListener object) {
        listeners.add(object);
    }

    public void removeListener(IRadioPlayerServiceListener object) {
        listeners.remove(object);
    }

    public static boolean isServiceRunning() {
        return mRunning;
    }

    public RadioPlayingService() {}

    public class RadioBinder extends Binder {
        public RadioPlayingService getService() {
            return RadioPlayingService.this;
        }
    }

    private final IBinder mBinder = new RadioBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mRunning = true;
    }

    @Override
    public void onDestroy() {
        mRunning = false;

        super.onDestroy();
    }

    public Float getLastVolume() {
        return lastVolume;
    }

    public void startPlay() {
        playStream();
        for (IRadioPlayerServiceListener listener : listeners) {
            listener.OnPlay();
        }
    }

    public void stopPlay() {
        stopStream(true);
        for (IRadioPlayerServiceListener listener : listeners) {
            listener.OnStop();
        }
    }

    public void pausePlay() {
        stopStream(false);
        for (IRadioPlayerServiceListener listener : listeners) {
            listener.OnPause();
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                switch (action) {
                    case ACTION_PLAY:
                        startPlay();
                        break;
                    case ACTION_PAUSE:
                        pausePlay();
                        break;
                    case ACTION_STOP:
                        stopPlay();
                        break;
                }
            }
        } else {
            initMediaPlayer();
        }

        return START_STICKY;
    }

    private void startForegroundPlayer() {
        if (notificationPlaced) {
            return;
        }
        Intent notificationIntent = new Intent(this, RadioPlayerActivity_.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                REQUEST_CODE_ACTIVITY,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.player_notification_control);

        remoteView.setOnClickPendingIntent(R.id.stop_btn,
                PendingIntent.getService(getApplicationContext(),
                        REQUEST_CODE_STOP, new Intent(ACTION_STOP),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        );

        remoteView.setOnClickPendingIntent(R.id.play_btn,
                PendingIntent.getService(getApplicationContext(),
                        REQUEST_CODE_PLAY, new Intent(ACTION_PLAY),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        );

        remoteView.setOnClickPendingIntent(R.id.pause_btn,
                PendingIntent.getService(getApplicationContext(),
                        REQUEST_CODE_PAUSE, new Intent(ACTION_PAUSE),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        );

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContent(remoteView)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_launcher).setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .build();

        startForeground(NOTIFICATION_ID, notification);

        notificationPlaced = true;
    }

    private void stopForegroundPlayer() {
        stopForeground(true);
        notificationPlaced = false;
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
    }

    private MediaPlayer getPlayer() {
        if (mediaPlayer == null) {
            initMediaPlayer();
        }

        return mediaPlayer;
    }

    public boolean isStreamPlaying() {
        return getPlayer().isPlaying();
    }

    public void setStreamVolume(float v) {
        lastVolume = v;
        getPlayer().setVolume(v, v);
    }

    private void playStream() {
        try {
            if (!getPlayer().isPlaying()) {
                startForegroundPlayer();
                try {
                    getPlayer().setDataSource(RADIO_URL);
                } catch (IllegalStateException e) {
                    getPlayer().reset();
                    getPlayer().setDataSource(RADIO_URL);
                }
                getPlayer().prepareAsync();
            }
        } catch (Exception e) {
            //TODO: bad idea
            e.printStackTrace();
        }
    }

    private void stopStream(boolean stopForeground) {
        if (stopForeground) {
            stopForegroundPlayer();
        }

        getPlayer().stop();
        getPlayer().reset();
    }
}
