package com.physicomtech.kit.physis_flaggame.helper;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PlayManager {

    private static final String TAG = "PlayManager";

    public static final int PLAY_START = 221;               // Handler Message ID
    public static final int PLAY_STOP = 222;
    public static final int PLAY_FLAG = 223;
    public static final int PLAY_SCORE = 224;
    public static final int PLAY_CORRECT = 225;

    private List<String> flagStates = new ArrayList<>();;       // 깃발 상태 리스트
    private List<String> flagMsgs = new ArrayList<>();;         // 깃발 상태 메시지 리스트
    private Handler handler;                                    // Handler
    private Timer playTimer;                                    // 게임 진행 타이머

    private int roundTime = 2500;                               // 라운드 진행 시간
    private int totalRound = 10;                                // 총 게임 라운드
    private int playingCount = 0;                               // 현재 게임 라운드
    private String flagState;                                   // 현재 라운드의 깃발 상태(Key)
    private int score;                                          // 게임 점수
    private long startTime;                                     // 라운드 시작 시작 ( 게임 점수 환산을 위한 변수 )
    private boolean isStayed = false;                           // 특정 상태("33", 깃발 상태 수신하지 않는 상태) 처리를 위한 변수
    private boolean isRecvState = true;

    public PlayManager(Handler handler){
        this.handler = handler;
        setFlagItems();
    }

    /*
        # 깃발 게임 항목 설정
     */
    private void setFlagItems() {
        // UP = 1 / DOWN = 2 / MID = 3
        flagStates.add("11");
        flagMsgs.add("백기 올리고 청기 올려");
        flagStates.add("13");
        flagMsgs.add("백기 올리고 청기 올리지마");
        flagStates.add("13");
        flagMsgs.add("백기 올리고 청기 내리지마");
        flagStates.add("12");
        flagMsgs.add("백기 올리고 청기 내려");

        flagStates.add("31");
        flagMsgs.add("백기 올리지말고 청기 올려");
        flagStates.add("31");
        flagMsgs.add("백기 내리지말고 청기 올려");
        flagStates.add("33");
        flagMsgs.add("백기 올리지말고 청기 올리지마");
        flagStates.add("33");
        flagMsgs.add("백기 내리지말고 청기 내리지마");
        flagStates.add("33");
        flagMsgs.add("백기 내리지말고 청기 올리지마");
        flagStates.add("33");
        flagMsgs.add("백기 올리지말고 청기 내리지마");
        flagStates.add("32");
        flagMsgs.add("백기 올리지말고 청기 내려");
        flagStates.add("32");
        flagMsgs.add("백기 내리지말고 청기 내려");

        flagStates.add("21");
        flagMsgs.add("백기 내리고 청기 올려");
        flagStates.add("23");
        flagMsgs.add("백기 내리고 청기 올리지마");
        flagStates.add("23");
        flagMsgs.add("백기 내리고 청기 내리지마");
        flagStates.add("22");
        flagMsgs.add("백기 내리고 청기 내려");
    }

    // 라운드 진행 시간 설정
    public void setRoundTime(int time){
        roundTime = time;
    }

    // 총 게임 라운드 설정
    public void setTotalRound(int totalRound){
        this.totalRound = totalRound;
    }

    private void nextFlag(){
        playTimer.cancel();
        playTimer = null;
        playTimer = new Timer();
        playTimer.schedule(flagTask(), 0, roundTime);
    }

    public void start(){
        // init play values
        playingCount = 0;
        score = 0;
        isRecvState = true;
        isStayed = false;
        // start timer
        playTimer = new Timer();
        playTimer.schedule(flagTask(), 0, roundTime);

        handler.obtainMessage(PLAY_START).sendToTarget();
    }

    public void stop(){
        playTimer.cancel();
        playTimer = null;

        handler.obtainMessage(PLAY_STOP).sendToTarget();
    }

    private TimerTask flagTask(){
        return new TimerTask() {
            @Override
            public void run() {
                if(isStayed){
                    score += 500;
                    isStayed = false;
                    handler.obtainMessage(PLAY_CORRECT, true).sendToTarget();
                }else if(!isRecvState){
                    handler.obtainMessage(PLAY_CORRECT, false).sendToTarget();
                }
                sendPlayScore();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendFlagState();
                    }
                }, 400);
            }
        };
    }


    private void sendFlagState(){
        if(playingCount == totalRound){
            stop();
        }else{
            isRecvState = false;
            playingCount++;
            startTime = System.currentTimeMillis();
            // Set Flag State
            int itemIndex = new Random().nextInt(flagStates.size());    // 임의의 인덱스 설정
            flagState = flagStates.get(itemIndex);                      // 현재 라운드의 깃발 상태를 저장
            isStayed = flagState.equals("33");
            handler.obtainMessage(PLAY_FLAG, flagMsgs.get(itemIndex)).sendToTarget();
        }
    }

    private void sendPlayScore(){
        handler.obtainMessage(PLAY_SCORE, score).sendToTarget();
    }

    public void receiveFlagState(String state){
        isRecvState = true;
        isStayed = false;

        boolean isCorrect = state.equals(flagState);
        if(isCorrect){
            score += roundTime + startTime - System.currentTimeMillis();
        }
        handler.obtainMessage(PLAY_CORRECT, isCorrect).sendToTarget();
        nextFlag();
    }

    /*
      # 깃발 정보 클래스
      - 깃발의 상태 및 메시지 정보를 관리
   */
    private class FlagData{
        private String flagState;
        private String flagMsg;

        FlagData(String flagState, String flagMsg){
            this.flagState = flagState;
            this.flagMsg = flagMsg;
        }

        String getFlagMsg() {
            return flagMsg;
        }

        String getFlagState() {
            return flagState;
        }
    }
}
