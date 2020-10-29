package com.imslab.mentis;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.unity3d.player.UnityPlayerActivity;

public class MiddleActivity extends AppCompatActivity {
    private Boolean activityWasJustCreated = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityWasJustCreated = true;
        Intent intent = new Intent(this, UnityPlayerActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(!activityWasJustCreated) {
            finish();
        }

        activityWasJustCreated = false;
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        ((MainActivity)MainActivity.context_main).serviceClass.feedbackdataEvent(false);
    }


}
