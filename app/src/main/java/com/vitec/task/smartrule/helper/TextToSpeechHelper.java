package com.vitec.task.smartrule.helper;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TextToSpeechHelper {

    private TextToSpeech mEnTextToSpeech;
    private TextToSpeech mCnTextToSpeech;
    private Context mContext;
    private final String TAG = "TextToSpeechHelper";

    public TextToSpeechHelper(Context mContext) {
        this.mContext = mContext;
    }

    public TextToSpeechHelper (Context mContext,String content) {
        this.mContext = mContext;
        initSpeech(content);
    }

    private void initSpeech(final String content) {
        mEnTextToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                int result = mEnTextToSpeech.setLanguage(Locale.ENGLISH);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "onInit: 语言无法使用");
                } else {
                    Log.e(TAG, "onInit: 英文设置成功");
                }
            }
        });

        mCnTextToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                int result = mCnTextToSpeech.setLanguage(Locale.CHINA);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "onInit: 语言无法使用");
                } else {
                    Log.e(TAG, "onInit: 中文设置成功");
                    mCnTextToSpeech.speak(content, TextToSpeech.QUEUE_ADD, null);

                }
            }
        });
    }

    public void speakEnglish(String content) {
        mEnTextToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void speakChinese(String content) {
        /**
         * text 需要转成语音的文字
         queueMode 队列方式：
         QUEUE_ADD：播放完之前的语音任务后才播报本次内容
         QUEUE_FLUSH：丢弃之前的播报任务，立即播报本次内容
         params 设置TTS参数，可以是null。
         KEY_PARAM_STREAM：音频通道，可以是：STREAM_MUSIC、STREAM_NOTIFICATION、STREAM_RING等
         KEY_PARAM_VOLUME：音量大小，0-1f
         返回值：int SUCCESS = 0，int ERROR = -1。
         */

        int result = mCnTextToSpeech.speak(content, TextToSpeech.QUEUE_ADD, null);
        if (result == -1) {
            stopSpeech();
            initSpeech(content);

        }
        Log.e(TAG, "speakChinese: 要说中文："+content +",结果返回值："+result);
    }

    public void stopSpeech() {
        if (mCnTextToSpeech != null) {
            mCnTextToSpeech.stop();
            mCnTextToSpeech.shutdown();
        }
        if (mEnTextToSpeech != null) {
            mEnTextToSpeech.stop();
            mEnTextToSpeech.shutdown();
        }
    }
}
