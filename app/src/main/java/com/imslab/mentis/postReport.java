package com.imslab.mentis;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class postReport extends Fragment {

    private RadioGroup post_radioGroup1,post_radioGroup2,post_radioGroup3,post_radioGroup4,post_radioGroup5;
    Button donereport;
    String mytime="";
    View view;
    int mode=3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view =inflater.inflate(R.layout.selfreportpost, container, false);
        return  view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {

        //라디오 그룹
        post_radioGroup1 = (RadioGroup) view.findViewById(R.id.post_radioGroup1);
        post_radioGroup2 = (RadioGroup) view.findViewById(R.id.post_radioGroup2);
        post_radioGroup3 = (RadioGroup) view.findViewById(R.id.post_radioGroup3);
        post_radioGroup4 = (RadioGroup) view.findViewById(R.id.post_radioGroup4);
        post_radioGroup5 = (RadioGroup) view.findViewById(R.id.post_radioGroup5);



        //확인버튼
        donereport = (Button) view.findViewById(R.id.donereport);
        donereport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                donereportListner();
            }
        });

        SimpleDateFormat timeformat= new SimpleDateFormat ( "yyyy/MM/dd HH:mm:ss");
        Date time = new Date();
        mytime = timeformat.format(time);
    }

    private void donereportListner()
    {
        int ans1=0,ans2=0,ans3=0,ans4=0,ans5=0;
        int rb1 = post_radioGroup1.getCheckedRadioButtonId();
        switch (rb1){
            case R.id.pq1_1:
                ans1=1;
                break;
            case R.id.pq1_2:
                ans1=2;
                break;
            case R.id.pq1_3:
                ans1=3;
                break;
            case R.id.pq1_4:
                ans1=4;
                break;
            case R.id.pq1_5:
                ans1=5;
                break;
        }
        int rb2 = post_radioGroup2.getCheckedRadioButtonId();
        switch (rb2){
            case R.id.pq2_1:
                ans2=1;
                break;
            case R.id.pq2_2:
                ans2=2;
                break;
            case R.id.pq2_3:
                ans2=3;
                break;
            case R.id.pq2_4:
                ans2=4;
                break;
            case R.id.pq2_5:
                ans2=5;
                break;
        }
        int rb3 = post_radioGroup3.getCheckedRadioButtonId();
        switch (rb3){
            case R.id.pq3_1:
                ans3=1;
                break;
            case R.id.pq3_2:
                ans3=2;
                break;
            case R.id.pq3_3:
                ans3=3;
                break;
            case R.id.pq3_4:
                ans3=4;
                break;
            case R.id.pq3_5:
                ans3=5;
                break;
        }
        int rb4 = post_radioGroup4.getCheckedRadioButtonId();
        switch (rb4){
            case R.id.pq4_1:
                ans4=1;
                break;
            case R.id.pq4_2:
                ans4=2;
                break;
            case R.id.pq4_3:
                ans4=3;
                break;
            case R.id.pq4_4:
                ans4=4;
                break;
            case R.id.pq4_5:
                ans4=5;
                break;
        }
        int rb5 = post_radioGroup5.getCheckedRadioButtonId();
        switch (rb5){
            case R.id.pq5_1:
                ans5=1;
                break;
            case R.id.pq5_2:
                ans5=2;
                break;
            case R.id.pq5_3:
                ans5=3;
                break;
            case R.id.pq5_4:
                ans5=4;
                break;
            case R.id.pq5_5:
                ans5=5;
                break;
        }
        //완료
        Bundle bundle = new Bundle();
        int tmp[] = new int[5];
        tmp[0]=ans1-1;
        tmp[1]=ans2-1;
        tmp[2]=ans3-1;
        tmp[3]=ans4-1;
        tmp[4]=ans5-1;
        bundle.putBoolean("Flag",false);
        bundle.putString("REPORTTIME",mytime);
        bundle.putIntArray("REPORTDATA",tmp);
        bundle.putInt("REPORTMODE", 3);

        ((MainActivity) MainActivity.context_main).sendReportResult(bundle);
        getActivity().finish();


    }

}
