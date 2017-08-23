package com.xiaobole.audiotest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements AudioCapture.OnAudioFrameCapturedListener{

    private static final String TAG = "MainActivity";

    private AudioCapture mAudioCapture;
    private AudioPlayer mAudioPlayer;
    private boolean mIsCaptureStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAudioCapture = new AudioCapture();
        mAudioCapture.setOnAudioFrameCapturedListener(this);

        mAudioPlayer = new AudioPlayer();
    }

    public void onClickStartCapture(View v) {
        if (!mIsCaptureStarted) {
            mAudioCapture.startCapture();
            mAudioPlayer.startPlayer();
        } else {
            mAudioCapture.stopCapture();
            mAudioPlayer.stopPlayer();
        }
        mIsCaptureStarted = !mIsCaptureStarted;
    }

    @Override
    public void onAudioFrameCaptured(byte[] audioData) {
        mAudioPlayer.play(audioData, 0, audioData.length);
    }
}
