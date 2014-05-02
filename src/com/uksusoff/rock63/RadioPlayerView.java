package com.uksusoff.rock63;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.uksusoff.rock63.utils.CommonUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

@EFragment(R.layout.radio_fragment)
public class RadioPlayerView extends Fragment implements OnClickListener, OnSeekBarChangeListener, IRadioPlayerServiceListener {

    private static final String RADIO_TITLE_URL = "http://vzradio.ru/temp_title_and.txt";
    
    private RadioPlayingService mBoundService = null;
    private boolean mIsBound = false;
    private Timer loadTitleTimer;
        
    @ViewById(R.id.radio_track_title)
    TextView trackTitle;
    
    @ViewById(R.id.radio_play_btn)
    ImageButton playBtn;
    
    @ViewById(R.id.radio_volume_bar)
    SeekBar volumeBar;

    private ServiceConnection mConnection = new ServiceConnection() {
        
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((RadioPlayingService.RadioBinder)service).getService();
            
            mIsBound = true;
            
            syncUi();
            
            mBoundService.addListener(RadioPlayerView.this);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService.removeListener(RadioPlayerView.this);
            
            mBoundService = null;
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        getActivity().bindService(new Intent(getActivity().getApplicationContext(), 
                RadioPlayingService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }
    
    @AfterViews
    void init() {
        
        if (!RadioPlayingService.isServiceRunning())
            getActivity().getApplicationContext().startService(new Intent(getActivity().getApplicationContext(), RadioPlayingService.class));
        
        doBindService();
        
        loadTitleTimer = new Timer();
        loadTitleTimer.schedule(new TimerTask() {          
            @Override
            public void run() {
                if (RadioPlayerView.this.getView()!=null)
                    loadTitle();
            }

        }, 0, 5000);
        
        trackTitle.setMovementMethod(LinkMovementMethod.getInstance());
        
        playBtn.setOnClickListener(this);
        volumeBar.setOnSeekBarChangeListener(this);
                
        syncUi();
        
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        // TODO Auto-generated method stub
        
        float max = (float)seekBar.getMax();
        
        while (mBoundService==null) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        mBoundService.setStreamVolume((float)progress / max);
        
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClick(View v) {
        
        while (mBoundService==null) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (mBoundService.isStreamPlaying()) {
            mBoundService.stopPlay();
            playBtn.setImageResource(CommonUtils.getThemedResource(getActivity(), R.attr.radio_play));
        
            Flurry.endEvent(getString(R.string.flurry_radio_play));
        } else {
            mBoundService.startPlay();
            
            playBtn.setImageResource(CommonUtils.getThemedResource(getActivity(), R.attr.radio_pause));
        
            Flurry.logEvent(getString(R.string.flurry_radio_play));
        }
    }
    
    @Override
    public void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }
    
    private void syncUi() {
        
        if (mBoundService==null)
            return;
        
        if (mBoundService.isStreamPlaying())
            RadioPlayerView.this.playBtn.setImageResource(CommonUtils.getThemedResource(getActivity(), R.attr.radio_pause));
        else
            RadioPlayerView.this.playBtn.setImageResource(CommonUtils.getThemedResource(getActivity(), R.attr.radio_play));
        
        //TODO: make constant
        
        if (mBoundService.getLastVolume()==null) {
            mBoundService.setStreamVolume(0.5f);
            volumeBar.setProgress((int)(0.5f * (float)volumeBar.getMax()));
        } else {
            mBoundService.setStreamVolume(mBoundService.getLastVolume().floatValue());
            volumeBar.setProgress((int)(mBoundService.getLastVolume().floatValue() * (float)volumeBar.getMax()));
        }
    }
    
    public void loadTitle() {
        if (getActivity()!=null) {
            try{            
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                        (new URL(RADIO_TITLE_URL)).openStream()));
    
                String str;
                StringBuilder builder = new StringBuilder();
        
                while ((str = in.readLine()) != null)
                    builder.append(str);
                
                final String res = builder.toString();
                
                getActivity().runOnUiThread(new Runnable() {
    
                    @Override
                    public void run() {
                        trackTitle.setText(Html.fromHtml(res));
                    } 
                    
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                //Strange exception occurs here
                //but as long as this section isn't critical, we can skip it
                e.printStackTrace();
            }
        }
    }

    @Override
    public void OnPause() {
        playBtn.setImageResource(CommonUtils.getThemedResource(getActivity(), R.attr.radio_play));
    }

    @Override
    public void OnPlay() {
        playBtn.setImageResource(CommonUtils.getThemedResource(getActivity(), R.attr.radio_pause));
    }

    @Override
    public void OnStop() {
        playBtn.setImageResource(CommonUtils.getThemedResource(getActivity(), R.attr.radio_play));
    }
    
}
