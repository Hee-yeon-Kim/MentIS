package com.imslab.mentis;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class SetAlarm extends AppCompatActivity {

    private TimePicker timePicker;
    private AlarmManager alarmManager;
    private int hour, minute;
    SeekBar seekBar;
    TextView textView;
    int id=2;
    public static Context context_setAlarm; // context 변수 선언


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setalarm);
        context_setAlarm = this;
        Button backbutton = (Button) findViewById(R.id.backbutton);
        seekBar = findViewById(R.id.seekBar);
        textView = findViewById(R.id.seekBarText);
        int old=(int)((ForegroundService) ForegroundService.foregroundService).userThres*100;
        textView.setText(String.valueOf(old)+"%");
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //윈도우 밝기 값 10% ~ 100% 사이로 제한
                if(progress < 10){
                    progress = 10;
                }else if(progress>100){
                    progress = 100;
                }
                textView.setText(String.valueOf(progress)+"%");
                ((ForegroundService) ForegroundService.foregroundService).userThres=progress/100f;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        timePicker=findViewById(R.id.tp_timepicker);
        alarmManager =(AlarmManager)getSystemService(Context.ALARM_SERVICE) ;

    }// onCreate()..

    public void regist(View view) {

        Intent intent = new Intent(this, Alarm.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, id++,intent, PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour=timePicker.getHour();
            minute=timePicker.getMinute();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        alarmManager.set(AlarmManager.RTC,calendar.getTimeInMillis(),pIntent);

        Toast.makeText(getApplicationContext(), hour+"시 "+minute+"분에 알람이 설정되었습니다.", Toast.LENGTH_SHORT).show();

        finish();
        // 지정한 시간에 매일 알림
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),  AlarmManager.INTERVAL_DAY, pIntent);

    }// regist()..

    public void unregist(View view) {
        Intent intent = new Intent(this, Alarm.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.cancel(pIntent);
        Toast.makeText(getApplicationContext(), "알람이 해제되었습니다.", Toast.LENGTH_SHORT).show();

    }// unregist()..

}


