package com.imslab.mentis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


public class stressView extends AppCompatActivity {

    TextView ch_text;
    ImageView character;
    LineChart HRChart;
    LineChart STChart;
    BarChart STRESSChart;
    Button backbutton;
    ValueFormatter formatter;
    Bundle DBData;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           Bundle bundle =  intent.getExtras();
           if(bundle==null)
           {
               ch_text.setText("데이터를 받아오지 못하였습니다.\n나중에 다시 시도해주세요.");
              // finish();

           }
           else
           {
               DBData = bundle;
               settingdata();

           }
            // intent ..
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stressview);


        HRChart= (LineChart) findViewById(R.id.meanHRChart);
        STChart= (LineChart) findViewById(R.id.meanSTChart);
        STRESSChart= (BarChart) findViewById(R.id.StressChart);

        ch_text = (TextView) findViewById(R.id.now_status);

        character = (ImageView) findViewById(R.id.character_capture);
        backbutton = (Button) findViewById(R.id.backbutton);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter("DBDATA"));
        if(!((ForegroundService) ForegroundService.foregroundService).dataEvent()) {
            ch_text.setText("데이터를 받아오지 못하였습니다.\n나중에 다시 시도해주세요.");
           // finish();
        }
//        ecg_sqa_list=new ArrayList<>();
//        ppg_sqa_list=new ArrayList<>();
//        HRmean_list=new ArrayList<>();
//        STmean_list=new ArrayList<>();
//        RESP_list=new ArrayList<>();
//        Stress_list=new ArrayList<>();
//        DateTime_list=new ArrayList<>();


    }
    @Override
    public void onResume() {
        super.onResume();
        if(ch_text!=null){
        if (!((ForegroundService) ForegroundService.foregroundService).dataEvent()) {
            ch_text.setText("데이터를 받아오지 못하였습니다.\n나중에 다시 시도해주세요.");
            // finish();
            }
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }
    
    private void settingdata()
    {
        ArrayList<Integer> ecg_sqa_list=null;
        ArrayList<Integer> HRmean_list=null;
        ArrayList<Integer>  STmean_list=null;
        ArrayList<Integer> Stress_list=null;
        ArrayList<String> DateTime_list=null;
        ArrayList<Entry> HRentries = new ArrayList<>();
        ArrayList<Entry> STentries = new ArrayList<>();
        List<BarEntry> Stressentries = new ArrayList<>();
        try{
            if(DBData.getStringArrayList("TIME")!=null)
                DateTime_list = DBData.getStringArrayList("TIME");
                ecg_sqa_list = DBData.getIntegerArrayList("ECG_SQA");
                HRmean_list =  DBData.getIntegerArrayList("HRmean");
                STmean_list =  DBData.getIntegerArrayList("STmean");
                Stress_list= DBData.getIntegerArrayList("Stress");



            for(int i =0; i<DateTime_list.size();i++)
            {
//                String[] tmp1= DateTime_list.get(i).split(" ");
//                tmp1[1].split(":")
                if(ecg_sqa_list.get(i)==1) {
                    HRentries.add(new Entry(i, HRmean_list.get(i)));
                    STentries.add(new Entry(i, STmean_list.get(i)));
                    Stressentries.add(new BarEntry(i,Stress_list.get(i)+0.1f));
                }
            }


            if(Stress_list!=null&&HRentries!=null&&STentries!=null)
            {
                settingCh(Stress_list.get(Stress_list.size()-1));
                drawChart(HRentries,1,DateTime_list);
                drawChart(STentries,2,DateTime_list);
                drawBar(Stressentries,DateTime_list);
            }
            else
            {
                settingCh(-1);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            //text.setText("오류");
        }
        finally {
           //text.setText(tmp.toString());
        }
    }
//시간축
    public class XAxisValueFormatter extends ValueFormatter
    {
        private ArrayList<String> mValues =null;
        private int mValueCount = 0;

        /**
         * An empty constructor.
         * Use `setValues` to set the axis labels.
         */


        public XAxisValueFormatter(ArrayList<String> _list) {
            if (_list!= null)
                setValues(_list);
        }


        @Override
        public String getAxisLabel(float value, AxisBase axis){
            int index = Math.round(value);

            if (index < 0 || index >= mValueCount || index != (int)value)
                return String.valueOf(value);

            return getValues((int)value);
        }

        public String getValues(int index)
        {
            String tmp = mValues.get(index);
            try {
                String tmp2[] = tmp.split(" ");
                String tmp3[] = tmp2[1].split(":");
                String mytime = tmp3[0] + ":" + tmp3[1];
                return mytime;
            }catch (NullPointerException e )
            {
                e.printStackTrace();
            }
            return "";


//                String[] tmp1= DateTime_list.get(i).split(" ");
//                tmp1[1].split(":")            return mValues.get(index);
        }

        public void setValues(ArrayList<String> values)
        {
            if (values == null)
                values = new ArrayList<String>();

            this.mValues = values;
            this.mValueCount = values.size();
        }
    }

    private void settingCh(int val)
    {
        if(val==0)
        {
            character.setImageResource(R.mipmap.ch_happy);
            ch_text.setText("My stress level now: Low");
        }
        else if(val==1)
        {
            character.setImageResource(R.mipmap.ch_angry);
            ch_text.setText("My stress level now : High");
        }
        else
        {
            ch_text.setText("데이터가 없습니다.");
        }

    }

    private void drawBar(List<BarEntry> Stressentries,ArrayList<String> timelist)
    {
        ValueFormatter yformatter = new ValueFormatter(){
            @Override
            public String getAxisLabel(float value, AxisBase axis)
            {
                if(value<0.5)
                return  "나쁨";
                else
                     return "양호";
            }
        };

        BarDataSet barSet = new BarDataSet(Stressentries,"stress");
        BarData barData = new BarData(barSet);
        barData.setBarWidth(1.4f); // set custom bar width

        barSet.setDrawValues(false);
        barSet.setColor(getColor(R.color.panel2));
        barSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        STRESSChart.setData(barData);
        STRESSChart.setDragDecelerationEnabled(true);
        STRESSChart.setVisibleXRangeMaximum(30);

        Description des= STRESSChart.getDescription();
        des.setEnabled(false);

        YAxis y = STRESSChart.getAxisLeft();

        y.setTextColor(Color.BLACK);
        y.setSpaceTop(6);
        y.setSpaceBottom(0);

        y.setValueFormatter(yformatter);
        y.setLabelCount(2,true);

        YAxis y2 = STRESSChart.getAxisRight();
        y2.setEnabled(false);

        XAxis x = STRESSChart.getXAxis();
        x.setValueFormatter(new XAxisValueFormatter(timelist));
        x.setTextColor(Color.BLACK);
        x.setDrawGridLines(false);
        //x.setSpaceMin(1.9f);
      //  x.setXOffset(-1.9f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);

        Legend legend = STRESSChart.getLegend();
        legend.setEnabled(false);

        STRESSChart.animateXY(2000, 2000); //애니메이션 기능 활성화
        STRESSChart.setFitBars(false); // make the x-axis fit exactly all bars
        STRESSChart.invalidate(); // refresh


    }

    private void drawChart(ArrayList<Entry> entries,int flag,ArrayList<String> timelist)
    {

        //데이터
        LineDataSet lineDataSet = new LineDataSet(entries,flag+"test1");
        lineDataSet.setLineWidth(2.0f);
        lineDataSet.setDrawFilled(true); //선아래로 색상표시
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setCircleRadius(3f);
        //데이터 셋
        LineChart lineChart = null;
        if(flag==1) {
            lineChart = HRChart;
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);//setDrawCubic(true);

            lineDataSet.setColor(getColor(R.color.myblue));
            lineDataSet.setFillColor(getColor(R.color.myblue));
            lineDataSet.setCircleColor(getColor(R.color.myblue));

            lineDataSet.setFillAlpha(50);

        }
        else if(flag==2) {
            lineChart = STChart;
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);//setDrawCubic(true);

            lineDataSet.setColor(getColor(R.color.mygreen));
            lineDataSet.setFillColor(getColor(R.color.mygreen));
            lineDataSet.setCircleColor(getColor(R.color.mygreen));

            lineDataSet.setFillAlpha(50);

        }


        //데이터 셋


        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineData lineData = new LineData(lineDataSet);

        lineChart.setData(lineData); // set the data and list of lables into chart
        lineChart.setDragDecelerationEnabled(true);
        lineChart.setVisibleXRangeMaximum(30);


        Description des= lineChart.getDescription();
        des.setEnabled(false);

        YAxis y = lineChart.getAxisLeft();

        y.setTextColor(Color.BLACK);

        y.setSpaceBottom(0);
        y.setSpaceTop(0);
        if(flag==1) {
            y.setAxisMaximum(100);
            y.setAxisMinimum(50);
        }
        else {
            y.setAxisMaximum(40);
            y.setAxisMinimum(30);
        }
        YAxis y2 = lineChart.getAxisRight();
        y2.setEnabled(false);

        XAxis x = lineChart.getXAxis();
        //  x.setValueFormatter(formatter);
        x.setTextColor(Color.BLACK);
        x.setDrawGridLines(false);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setValueFormatter(new XAxisValueFormatter(timelist));
        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);
//
//        legend.setTextColor(Color.BLACK);

        lineChart.animateXY(2000, 2000); //애니메이션 기능 활성화
        lineChart.invalidate();
    }
}
