package com.imslab.mentis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.math3.analysis.function.Constant;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SelfReport extends Fragment {

    private RadioGroup preradioGroup1,preradioGroup2,preradioGroup3,preradioGroup4,preradioGroup5,preradioGroup6,preradioGroup7,preradioGroup8,preradioGroup9,preradioGroup10;
    Button donereport;
    String mytime="";
    View view;
    

    int mode=1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        view =inflater.inflate(R.layout.selfreportbefore, container, false);
      return  view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {

        //라디오 그룹
        preradioGroup1 = (RadioGroup) view.findViewById(R.id.preradioGroup1);
        preradioGroup2 = (RadioGroup) view.findViewById(R.id.preradioGroup2);
        preradioGroup3 = (RadioGroup) view.findViewById(R.id.preradioGroup3);
        preradioGroup4 = (RadioGroup) view.findViewById(R.id.preradioGroup4);
        preradioGroup5 = (RadioGroup) view.findViewById(R.id.preradioGroup5);
       preradioGroup6 = (RadioGroup) view.findViewById(R.id.preradioGroup6);
            preradioGroup7 = (RadioGroup) view.findViewById(R.id.preradioGroup7);
            preradioGroup8 = (RadioGroup) view.findViewById(R.id.preradioGroup8);
            preradioGroup9 = (RadioGroup) view.findViewById(R.id.preradioGroup9);
            preradioGroup10 = (RadioGroup) view.findViewById(R.id.preradioGroup10);


        //확인버튼
        donereport = (Button) view.findViewById(R.id.donereport);
        donereport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                donereportListner(mode);
            }
        });

        SimpleDateFormat timeformat= new SimpleDateFormat ( "yyyy/MM/dd HH:mm:ss");
        Date time = new Date();
        mytime = timeformat.format(time);
    }

    public void donereportListner(int mymode)
    {
        int ans1=0,ans2=0,ans3=0,ans4=0,ans5=0;
        int ans6=0,ans7=0,ans8=0,ans9=0,ans10=0;
        int rb1 = preradioGroup1.getCheckedRadioButtonId();
        switch (rb1){
            case R.id.preq1_1:
                ans1=1;
                break;
            case R.id.preq1_2:
                ans1=2;
                break;
            case R.id.preq1_3:
                ans1=3;
                break;
            case R.id.preq1_4:
                ans1=4;
                break;
            case R.id.preq1_5:
                ans1=5;
                break;
        }
        int rb2 = preradioGroup2.getCheckedRadioButtonId();
        switch (rb2){
            case R.id.preq2_1:
                ans2=1;
                break;
            case R.id.preq2_2:
                ans2=2;
                break;
            case R.id.preq2_3:
                ans2=3;
                break;
            case R.id.preq2_4:
                ans2=4;
                break;
            case R.id.preq2_5:
                ans2=5;
                break;
        }
        int rb3 = preradioGroup3.getCheckedRadioButtonId();
        switch (rb3){
            case R.id.preq3_1:
                ans3=1;
                break;
            case R.id.preq3_2:
                ans3=2;
                break;
            case R.id.preq3_3:
                ans3=3;
                break;
            case R.id.preq3_4:
                ans3=4;
                break;
            case R.id.preq3_5:
                ans3=5;
                break;
        }
        int rb4 = preradioGroup4.getCheckedRadioButtonId();
        switch (rb4){
            case R.id.preq4_1:
                ans4=1;
                break;
            case R.id.preq4_2:
                ans4=2;
                break;
            case R.id.preq4_3:
                ans4=3;
                break;
            case R.id.preq4_4:
                ans4=4;
                break;
            case R.id.preq4_5:
                ans4=5;
                break;
        }
        int rb5 = preradioGroup5.getCheckedRadioButtonId();
        switch (rb5){
            case R.id.preq5_1:
                ans5=1;
                break;
            case R.id.preq5_2:
                ans5=2;
                break;
            case R.id.preq5_3:
                ans5=3;
                break;
            case R.id.preq5_4:
                ans5=4;
                break;
            case R.id.preq5_5:
                ans5=5;
                break;
        }

        int rb6 = preradioGroup6.getCheckedRadioButtonId();
        switch (rb6){
            case R.id.preq6_1:
                ans6=1;
                break;
            case R.id.preq6_2:
                ans6=2;
                break;
            case R.id.preq6_3:
                ans6=3;
                break;
            case R.id.preq6_4:
                ans6=4;
                break;
            case R.id.preq6_5:
                ans6=5;
                break;
        }
        int rb7 = preradioGroup7.getCheckedRadioButtonId();
        switch (rb7){
            case R.id.preq7_1:
                ans7=1;
                break;
            case R.id.preq7_2:
                ans7=2;
                break;
            case R.id.preq7_3:
                ans7=3;
                break;
            case R.id.preq7_4:
                ans7=4;
                break;
            case R.id.preq7_5:
                ans7=5;
                break;
        }
        int rb8 = preradioGroup8.getCheckedRadioButtonId();
        switch (rb8){
            case R.id.preq8_1:
                ans8=1;
                break;
            case R.id.preq8_2:
                ans8=2;
                break;
            case R.id.preq8_3:
                ans8=3;
                break;
            case R.id.preq8_4:
                ans8=4;
                break;
            case R.id.preq8_5:
                ans8=5;
                break;
        }
        int rb9 = preradioGroup9.getCheckedRadioButtonId();
        switch (rb9){
            case R.id.preq9_1:
                ans9=1;
                break;
            case R.id.preq9_2:
                ans9=2;
                break;
            case R.id.preq9_3:
                ans9=3;
                break;
            case R.id.preq9_4:
                ans9=4;
                break;
            case R.id.preq9_5:
                ans9=5;
                break;
        }
        int rb10 = preradioGroup10.getCheckedRadioButtonId();
        switch (rb10){
            case R.id.preq10_1:
                ans10=1;
                break;
            case R.id.preq10_2:
                ans10=2;
                break;
            case R.id.preq10_3:
                ans10=3;
                break;
            case R.id.preq10_4:
                ans10=4;
                break;
            case R.id.preq10_5:
                ans10=5;
                break;
        }
        //완료
        Bundle bundle = new Bundle();
        int tmp[] = new int[10];
        tmp[0]=ans1-1;
        tmp[1]=ans2-1;
        tmp[2]=ans3-1;
        tmp[3]=ans4-1;
        tmp[4]=ans5-1;
        tmp[5]=ans6-1;
        tmp[6]=ans7-1;
        tmp[7]=ans8-1;
        tmp[8]=ans9-1;
        tmp[9]=ans10-1;

        bundle.putBoolean("Flag",false);
        bundle.putString("REPORTTIME",mytime);
        bundle.putIntArray("REPORTDATA",tmp);
        bundle.putInt("REPORTMODE",1);

        bundle.putInt("MODE",mode);
        ((MainActivity) MainActivity.context_main).sendReportResult(bundle);
        getActivity().finish();
    }
}

