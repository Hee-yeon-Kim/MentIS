package com.imslab.mentis;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

public class callibrationView extends AppCompatActivity {
    private static final long START_TIME_IN_MILLIS = 300000; //5min
    private CountDownTimer MyCountDownTimer;
    //    TODO Accept Input from user & store it in  START_TIME_IN_MILLIS

    private boolean TimerRunning;
    private TextView CountDown_Tv;
    private WaveView waveView;

    //        Initially TimeLeftInMillis will be same as START_TIME_IN_MILLIS
    private long TimeLeftInMillis = START_TIME_IN_MILLIS;

    ProgressBar MyProgressBar;
    private int  ProgressBarStatus;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callibration);

        CountDown_Tv = findViewById(R.id.countertext);

        ProgressBarStatus=0;
        MyProgressBar = findViewById(R.id.fpb);
        startTimer();

        waveView = findViewById(R.id.wave_three);
        waveView.addDefaultWaves(2, 1);
       // waveView.updateWaveHeight(0,2);
        waveView.updateWaveHeight(1,2);
        waveView.updateWaveHeight(2,2);

        waveView.updateWaveFixedHeight(0,5);
        waveView.updateWaveFixedHeight(1,3);
        waveView.updateWaveFixedHeight(2,5);

       // waveView.updateWaveHeight(2,3);
        waveView.startAnimation();

       /* StartPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        ResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });
*/
        updateCountDownText();

        MyProgressBar.setProgress(0);


    }

    private void startTimer() {
        ((MainActivity)MainActivity.context_main).setIsCalli(true);

        MyCountDownTimer = new CountDownTimer(START_TIME_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                TimeLeftInMillis = millisUntilFinished;
                updateCountDownText(); //  Updating CountDown_Tv


                /*for incrementing progressbar every second calculating progress for every second*/
                ProgressBarStatus=  (int)((1f-  (float)TimeLeftInMillis/(float)START_TIME_IN_MILLIS)*100);
                //incrementing progress on every tick
                MyProgressBar.setProgress(ProgressBarStatus);

            }

            @Override
            public void onFinish() {
                TimerRunning = false;
                CountDown_Tv.setText("00:00");
                waveView.pauseAnimation();
                ((MainActivity)MainActivity.context_main).setIsCalli(false);
               // ((MainActivity)MainActivity.context_main).setInit(false);
                finish();

                //   MyProgressBar.setProgress(100);

              /*  StartPauseButton.setText("Start");
                StartPauseButton.setVisibility(View.INVISIBLE);
                ResetButton.setVisibility(View.VISIBLE);*/
            }
        }.start();

        TimerRunning = true;
      /*  StartPauseButton.setText("Pause");
        ResetButton.setVisibility(View.INVISIBLE);*/


    }
    @Override
    protected  void onStop()
    {
        super.onStop();
        ((MainActivity)MainActivity.context_main).setIsCalli(false);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        MyProgressBar.setProgress(100);
//        ProgressBarStatus=1;
//        waveView.resumeAnimation();
//        ((MainActivity)MainActivity.context_main).setIsCalli(true);
//
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        ((MainActivity)MainActivity.context_main).setIsCalli(false);
//
//        waveView.pauseAnimation();
//    }


    private void pauseTimer() {
        MyCountDownTimer.cancel();
        TimerRunning = false;
      //  StartPauseButton.setText("Resume");
      //  ResetButton.setVisibility(View.VISIBLE);
        MyProgressBar.clearAnimation();
    }

    private void resetTimer() {
        TimeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
      //  ResetButton.setVisibility(View.INVISIBLE);
       // StartPauseButton.setVisibility(View.VISIBLE);

        MyProgressBar.setProgress(0);
       // StartPauseButton.setText("Start");
    }

    private void updateCountDownText() {
        int minutes = (int) (TimeLeftInMillis / 1000) / 60;
        int seconds = (int) (TimeLeftInMillis / 1000) % 60;


        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes,seconds);

        //        String newtime = hours + ":" + minutes + ":" + seconds;

        CountDown_Tv.setText(timeLeftFormatted);

    }

}
