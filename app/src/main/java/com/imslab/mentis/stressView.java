package com.imslab.mentis;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.ArrayList;



public class stressView extends AppCompatActivity {

    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stressview);

        text = (TextView) findViewById(R.id.stress_text1);

//        ecg_sqa_list=new ArrayList<>();
//        ppg_sqa_list=new ArrayList<>();
//        HRmean_list=new ArrayList<>();
//        STmean_list=new ArrayList<>();
//        RESP_list=new ArrayList<>();
//        Stress_list=new ArrayList<>();
//        DateTime_list=new ArrayList<>();



        settingdata();

    }
    private void settingdata()
    {
        Intent intent = getIntent(); /*데이터 수신*/
        ArrayList<Integer> ecg_sqa_list=null;
        ArrayList<Integer> ppg_sqa_list;
        ArrayList<Integer> HRmean_list;
        ArrayList<Integer> STmean_list;
        ArrayList<Integer> RESP_list=null;
        ArrayList<Integer> Stress_list=null;
        ArrayList<String> DateTime_list=null;
        StringBuilder tmp = new StringBuilder();

        try{
            if(intent.getExtras().getStringArrayList("TIME")!=null)
               DateTime_list = intent.getExtras().getStringArrayList("TIME");
            ecg_sqa_list = intent.getExtras().getIntegerArrayList("ECG_SQA");
            Stress_list= intent.getExtras().getIntegerArrayList("ECG_SQA");

            for(int i =0; i<DateTime_list.size();i++)
            {
                tmp.append(DateTime_list.get(i).toString());
                tmp.append("//");
                tmp.append(ecg_sqa_list.get(i).toString());
                tmp.append("//");
                tmp.append(Stress_list.get(i).toString());
                tmp.append("\n");

            }


        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            text.setText("오류");

        }
        finally {
           text.setText(tmp.toString());
        }


    }


}
