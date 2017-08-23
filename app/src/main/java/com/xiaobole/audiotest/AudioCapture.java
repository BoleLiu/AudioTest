package com.xiaobole.audiotest;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by liujingbo on 17/8/16.
 */

public class AudioCapture {
    private static final String TAG = "AudioCapture";

    private static final int DEFAULT_SAMPLE_RATE = 44100;
    private static final int DEFAULT_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int SAMPLES_PER_FRAME = 1024;

    private AudioRecord mAudioRecord;
    private int mMinBufferSize = 0;

    private Thread mCaptureThread;
    private boolean mIsCaptureStarted = false;
    private volatile boolean mIsLoopExit = false;

    private OnAudioFrameCapturedListener mAudioFrameCapturedListener;

    public interface OnAudioFrameCapturedListener {
        void onAudioFrameCaptured(byte[] audioData);
    }

    public void setOnAudioFrameCapturedListener(OnAudioFrameCapturedListener listener) {
        mAudioFrameCapturedListener = listener;
    }

    public boolean isCaptureStarted() {
        return mIsCaptureStarted;
    }

    public boolean startCapture() {
        return startCapture(DEFAULT_SOURCE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
    }

    public boolean startCapture(int audioSource, int sampleRate, int channelConfig, int audioFormat) {
        if (mIsCaptureStarted) {
            Log.e(TAG, "Capture already start!!!");
            return false;
        }

        mMinBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        if (mMinBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter!!!");
            return false;
        }

        mAudioRecord = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, mMinBufferSize);
        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioRecord initialize failed!!!");
            return false;
        }

        mAudioRecord.startRecording();

        mCaptureThread = new Thread(new AudioCaptureRunnable());
        mCaptureThread.start();

        mIsLoopExit = false;
        mIsCaptureStarted = true;
        Log.d(TAG, "Audio capture start success!!!");

        return true;
    }

    public void stopCapture() {
        if (!mIsCaptureStarted) {
            return;
        }

        mIsLoopExit = true;
        try {
            mCaptureThread.interrupt();
            mCaptureThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mAudioRecord.getState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
        }
        mAudioRecord.release();
        mAudioRecord = null;

        mIsCaptureStarted = false;
        mAudioFrameCapturedListener = null;
        Log.d(TAG, "stop audio capture success!!!");
    }

//    private void calc1(short[] lin,int off,int len) {
//        int i,j;
//        for (i = 0; i < len; i++) {
//            j = lin[i+off];
//            lin[i+off] = (short)(j>>2);
//        }
//    }

    public class AudioCaptureRunnable implements Runnable {
        @Override
        public void run() {
            while(!mIsLoopExit) {
                byte[] audioData = new byte[mMinBufferSize];

                int ret = mAudioRecord.read(audioData, 0, audioData.length);
                if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG, "ERROR_INVALID_OPERATION");
                } else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "ERROR_BAD_VALUE");
                } else {
                    if (mAudioFrameCapturedListener != null) {
                        mAudioFrameCapturedListener.onAudioFrameCaptured(audioData);
                    }
                    Log.d(TAG , "OK, Captured "+ret+" bytes !");
                }
            }
        }
    }
}
