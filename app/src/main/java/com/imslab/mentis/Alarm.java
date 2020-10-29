package com.imslab.mentis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class Alarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ((MainActivity)MainActivity.context_main).serviceClass.EndingNotification();
        Log.e("Alarm","알람입니다.");    // 로그 확인용

    }
}

