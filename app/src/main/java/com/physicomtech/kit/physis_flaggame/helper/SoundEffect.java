package com.physicomtech.kit.physis_flaggame.helper;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.physicomtech.kit.physis_flaggame.R;

public class SoundEffect {

    private Context context;
    private SoundPool soundPool = null;

    private int correctSoundID = 0;
    private int wrongSoundID = 0;

    private final float LEFT_VOLUME = 1f;                               // 사운드 볼륨 ( 범위 : 0.0f ~ 1.0f )
    private final float RIGHT_VOLUME = 1f;
    private final int BLOW_PRIORITY = 0;                                // 우선 순위
    private final int BLOW_LOOP = 0;                                    // 반복 여부 ( -1 : 무한 반복, 0 : 반복 X )
    private final float BLOW_RATE = 2.0f;                               // 재생 속도 ( 범위 : 0.5f ~ 2.0f )

    public SoundEffect(Context context){
        this.context = context;
        createSoundPool();
        setSoundID();
    }

    private void createSoundPool(){
        // 안드로이드 버전에 따른 객체 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes).setMaxStreams(2).build();
        }
        else {
            soundPool = new SoundPool(2, AudioManager.STREAM_NOTIFICATION, 0);
        }
    }

    private void setSoundID() {
        correctSoundID = soundPool.load(context, R.raw.correct, 1);
        wrongSoundID = soundPool.load(context, R.raw.wrong, 1);
    }

    public void blowSound(boolean isCorrect){
        if(isCorrect)
            soundPool.play(correctSoundID, LEFT_VOLUME, RIGHT_VOLUME, BLOW_PRIORITY, BLOW_LOOP, BLOW_RATE);
        else
            soundPool.play(wrongSoundID, LEFT_VOLUME, RIGHT_VOLUME, BLOW_PRIORITY, BLOW_LOOP, BLOW_RATE);
    }
}
