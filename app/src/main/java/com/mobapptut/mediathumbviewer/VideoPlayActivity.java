package com.mobapptut.mediathumbviewer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;

public class VideoPlayActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnCompletionListener,
    AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mMediaPlayer;
    private Uri mVideoUri;
    private ImageButton mPlayPauseButton;
    private SurfaceView mSurfaceView;
    private AudioManager mAudioManager;
    private IntentFilter mNoisyIntentFilter;
    private AudioBecommingNoisy mAudioBecommingNoisy;

    private class AudioBecommingNoisy extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaPause();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        mPlayPauseButton = (ImageButton) findViewById(R.id.videoPlayPauseButton);
        mSurfaceView = (SurfaceView) findViewById(R.id.videoSurfaceView);

        Intent callingIntent = this.getIntent();
        if(callingIntent != null) {
            mVideoUri = callingIntent.getData();
        }

        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mAudioBecommingNoisy = new AudioBecommingNoisy();
        mNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    }

    public void playPauseClick(View view) {
        if(mMediaPlayer.isPlaying()) {
            mediaPause();
        } else {
            mediaPlay();
        }
    }

    @Override
    protected void onStop() {

        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mediaPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mMediaPlayer != null) {
            mediaPlay();
        } else {
            SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
            surfaceHolder.addCallback(this);
        }
    }

    private void mediaPlay() {
        registerReceiver(mAudioBecommingNoisy, mNoisyIntentFilter);
        int requestAudioFocusResult = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if(requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mMediaPlayer.start();
            mPlayPauseButton.setImageResource(R.mipmap.ic_media_pause);
        }
    }

    private void mediaPause() {
        mMediaPlayer.pause();
        mPlayPauseButton.setImageResource(R.mipmap.ic_media_play);
        mAudioManager.abandonAudioFocus(this);
        unregisterReceiver(mAudioBecommingNoisy);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mMediaPlayer = MediaPlayer.create(this, mVideoUri, surfaceHolder);
        mMediaPlayer.setOnCompletionListener(this);
        mediaPlay();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mPlayPauseButton.setImageResource(R.mipmap.ic_media_play);
    }

    @Override
    public void onAudioFocusChange(int audioFocusChanged) {
        switch (audioFocusChanged) {
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                mediaPause();
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                mediaPlay();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                mediaPause();
                break;
        }
    }
}
