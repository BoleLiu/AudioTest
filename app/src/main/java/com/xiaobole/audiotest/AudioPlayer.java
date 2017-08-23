package com.xiaobole.audiotest;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by liujingbo on 17/8/17.
 */

public class AudioPlayer {
    private static final String TAG = "AudioPlayer";

    private static final int DEFAULT_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private boolean mIsPlayerStarted = false;
    private int mMinBufferSize = 0;
    private AudioTrack mAudioTrack;

    public boolean startPlayer() {
        return startPlayer(DEFAULT_STREAM_TYPE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
    }

    public boolean startPlayer(int streamType, int sampleRate, int channelConfig, int audioFormat) {
        if (mIsPlayerStarted) {
            Log.d(TAG, "Player already start!!!");
            return false;
        }

        mMinBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        if (mMinBufferSize == AudioTrack.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid params!!!");
            return false;
        }

        mAudioTrack = new AudioTrack(streamType, sampleRate, channelConfig, audioFormat, mMinBufferSize, AudioTrack.MODE_STREAM);
        if (mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioTrack initialize fail !");
            return false;
        }

        mIsPlayerStarted = true;
        Log.d(TAG, "Start Audio player successfully !");
        return true;
    }

    public int getMinBufferSize() {
        return mMinBufferSize;
    }

    public boolean stopPlayer() {
        if (!mIsPlayerStarted) {
            Log.e(TAG, "Player not start!");
            return false;
        }

        if (mAudioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING) {
            mAudioTrack.stop();
        }

        mAudioTrack.release();
        mAudioTrack = null;
        mIsPlayerStarted = false;
        Log.d(TAG, "Stop player successfully !");
        return true;
    }

    public boolean play(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if (!mIsPlayerStarted) {
            Log.e(TAG, "Player not started!");
            return false;
        }

        if (mAudioTrack.write(audioData, offsetInBytes, sizeInBytes) != sizeInBytes) {
            Log.e(TAG, "Could not write all the samples to the audio device !");
        }

        mAudioTrack.play();
        Log.d(TAG , "OK, Played "+sizeInBytes+" bytes !");

        return true;
    }
}
