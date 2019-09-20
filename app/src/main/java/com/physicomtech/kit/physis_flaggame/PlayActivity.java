package com.physicomtech.kit.physis_flaggame;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.physicomtech.kit.physis_flaggame.customize.NumberPickView;
import com.physicomtech.kit.physis_flaggame.customize.OnSingleClickListener;
import com.physicomtech.kit.physis_flaggame.customize.SerialNumberView;
import com.physicomtech.kit.physis_flaggame.dialog.LoadingDialog;
import com.physicomtech.kit.physis_flaggame.helper.PHYSIsPreferences;
import com.physicomtech.kit.physis_flaggame.helper.PlayManager;
import com.physicomtech.kit.physis_flaggame.helper.SoundEffect;
import com.physicomtech.kit.physis_flaggame.helper.TextSpeech;
import com.physicomtech.kit.physislibrary.PHYSIsBLEActivity;

import java.util.Locale;

public class PlayActivity extends PHYSIsBLEActivity {

    private static final String TAG = "PlayActivity";

    private final int ROUND_STEP = 5;                   // 라운드 증가 폭
    private final int INTERVAL_STEP = 500;              // 라운드 시간 증가 폭

    private static final float SPEECH_PITCH = 1.0f;     // 음성 출력 톤 높이
    private static final float SPEECH_RATE = 1.75f;     // 음성 출력 속도

    SerialNumberView snvSetup;
    Button btnConnect, btnPlay;
    NumberPickView npvRound, npvInterval;
    TextView tvFlagOrder, tvPlayRound, tvGameScore;
    ImageView ivCharacter;

    private PHYSIsPreferences preferences;
    private PlayManager playManager;
    private TextSpeech textSpeech;
    private SoundEffect soundEffect;

    private String serialNumber = null;
    private boolean isConnected = false;
    private boolean isPlaying = false;

    private int playRound = 1;                          // 초기 게임 설정 값
    private int totalRound = 10;
    private int roundTime = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isPlaying){
            playManager.stop();
            textSpeech.shutdown();
        }
    }

    @Override
    protected void onBLEConnectedStatus(int result) {
        super.onBLEConnectedStatus(result);
        LoadingDialog.dismiss();
        setConnectedResult(result);
    }

    @Override
    protected void onBLEReceiveMsg(String msg) {
        super.onBLEReceiveMsg(msg);
        if(isPlaying)
            playManager.receiveFlagState(msg);
    }

    /*
            Event
     */
    final SerialNumberView.OnSetSerialNumberListener onSetSerialNumberListener = new SerialNumberView.OnSetSerialNumberListener() {
        @Override
        public void onSetSerialNumber(String serialNum) {
            preferences.setPhysisSerialNumber(serialNumber = serialNum);
            Log.e(TAG, "# Set Serial Number : " + serialNumber);
        }
    };

    final NumberPickView.OnNumberChangeListener onNumberChangeListener = new NumberPickView.OnNumberChangeListener() {
        @Override
        public void onMinus(int pickerTag, int currentValue) {
            if(isPlaying)
                return;
            if(pickerTag == 0){
                totalRound = totalRound - ROUND_STEP;
                npvRound.setNumber(totalRound);
                playManager.setTotalRound(totalRound);
            }else{
                roundTime = roundTime - INTERVAL_STEP;
                npvInterval.setNumber(roundTime);
                playManager.setRoundTime(roundTime);
            }
        }

        @Override
        public void onPlus(int pickerTag, int currentValue) {
            if(isPlaying)
                return;
            if(pickerTag == 0){
                totalRound = totalRound + ROUND_STEP;
                npvRound.setNumber(totalRound);
                playManager.setTotalRound(totalRound);
            }else{
                roundTime = roundTime + INTERVAL_STEP;
                npvInterval.setNumber(roundTime);
                playManager.setRoundTime(roundTime);
            }
        }
    };

    final OnSingleClickListener onClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()){
                case R.id.btn_connect:
                    if(serialNumber == null){
                        Toast.makeText(getApplicationContext(), "PHYSIs Kit의 시리얼 넘버를 설정하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(isConnected){
                        disconnectDevice();
                    }else{
                        LoadingDialog.show(PlayActivity.this, "Connecting..");
                        connectDevice(serialNumber);
                    }
                    break;
                case R.id.btn_play:
                    if(!isConnected){
                        Toast.makeText(getApplicationContext(), "PHYSIs Kit가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(!isPlaying)
                        playManager.start();
                    else {
                        textSpeech.shutdown();
                        playManager.stop();
                    }
                    break;
            }
        }
    };

    /*
            Handler
     */
    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what){
            case PlayManager.PLAY_START:
                playRound = 1;
                btnPlay.setText("Game Stop");
                isPlaying = true;
                ivCharacter.setImageResource(R.drawable.ic_default_character);
                break;
            case PlayManager.PLAY_STOP:
                btnPlay.setText("Game Start");
                isPlaying = false;
                break;
            case PlayManager.PLAY_FLAG:
                tvPlayRound.setText(String.valueOf(playRound++));
                tvFlagOrder.setText(msg.obj.toString());
                textSpeech.speak(msg.obj.toString());
                ivCharacter.setImageResource(R.drawable.ic_default_character);
                break;
            case PlayManager.PLAY_SCORE:
                tvGameScore.setText(msg.obj.toString());
                break;
            case PlayManager.PLAY_CORRECT:
                soundEffect.blowSound((Boolean) msg.obj);
                showEffect((Boolean) msg.obj);
                break;
        }
    }

    /*
            Helper Methods.
     */
    @SuppressLint("WrongConstant")
    private void showEffect(boolean correct){
        int blinkColor;
        if(correct) {
            blinkColor = Color.rgb(105, 95, 255);
            ivCharacter.setImageResource(R.drawable.ic_correct_character);
        }else {
            blinkColor = Color.rgb(255, 66, 88);
            ivCharacter.setImageResource(R.drawable.ic_wrong_character);
        }
        ObjectAnimator anim = ObjectAnimator.ofInt(tvFlagOrder, "textColor", Color.WHITE, blinkColor, Color.WHITE);
        anim.setDuration(100);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(2);
        anim.start();
    }

    @SuppressLint("SetTextI18n")
    private void setConnectedResult(int state){
        // set button
        if(isConnected = state == CONNECTED){
            btnConnect.setText("Disconnect");
            if(isPlaying)
                playManager.stop();
        }else{
            btnConnect.setText("Connect");
        }
        // show toast
        String toastMsg;
        if(state == CONNECTED){
            toastMsg = "Physi Kit와 연결되었습니다.";
        }else if(state == DISCONNECTED){
            toastMsg = "Physi Kit 연결이 실패/종료되었습니다.";
        }else{
            toastMsg = "연결할 Physi Kit가 존재하지 않습니다.";
        }
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
    }

    private void init() {
        preferences = new PHYSIsPreferences(getApplicationContext());
        serialNumber = preferences.getPhysisSerialNumber();

        playManager = new PlayManager(physisHandle);
        playManager.setRoundTime(roundTime);
        playManager.setTotalRound(totalRound);

        textSpeech = new TextSpeech(getApplicationContext(), Locale.KOREA, SPEECH_PITCH, SPEECH_RATE);
        soundEffect = new SoundEffect(getApplicationContext());

        snvSetup = findViewById(R.id.snv_setup);
        snvSetup.setSerialNumber(serialNumber);
        snvSetup.showEditView(serialNumber == null);
        snvSetup.setOnSetSerialNumberListener(onSetSerialNumberListener);

        btnConnect = findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(onClickListener);
        btnPlay = findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(onClickListener);

        npvRound = findViewById(R.id.npv_play_round);
        npvInterval = findViewById(R.id.npv_round_interval);
        npvRound.setOnNumberChanged(onNumberChangeListener);
        npvInterval.setOnNumberChanged(onNumberChangeListener);
        npvRound.setNumber(totalRound);
        npvInterval.setNumber(roundTime);

        tvFlagOrder = findViewById(R.id.tv_flag_order);
        tvPlayRound = findViewById(R.id.tv_play_round);
        tvGameScore = findViewById(R.id.tv_game_score);
        ivCharacter = findViewById(R.id.iv_character);
    }
}

