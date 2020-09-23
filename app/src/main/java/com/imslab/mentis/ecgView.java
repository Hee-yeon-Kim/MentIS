package com.imslab.mentis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class ecgView extends AppCompatActivity {
    private BroadcastReceiver broadcastReceiver;
    private LineChart chartE,chartX,chartY,chartZ;
    boolean isListening=false;
    private  ArrayList<Integer> ecglist;
    int lastecg=0;
    Thread t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecgview);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.ims.ECG");
        ((MainActivity)MainActivity.context_main).toggleECGView(true);
        ecglist = new ArrayList<>();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                StringBuilder s = new StringBuilder();
                if(extras!=null)
                {
                    int[] temp_ecg= extras.getIntArray("ECG");
                    if(temp_ecg!=null)
                    {
                        s.append("옴");
                        isListening = true;
                        for(int i=0; i<temp_ecg.length;i++)
                        {

                            try {
                                ecglist.add(temp_ecg[i]);
                            }
                            catch (NullPointerException e)
                            {
                                s.append("nullpointecg");
                            }
                        }

                    }

                }


            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,filter);
        chartE = (LineChart ) findViewById(R.id.ECGChart);
        SettingChart(chartE);
        chartX = (LineChart) findViewById(R.id.ACCXChart);
        chartY= (LineChart) findViewById(R.id.ACCYChart);
        chartZ = (LineChart) findViewById(R.id.ACCZChart);

        NewRunnable nr = new NewRunnable() ;
        t = new Thread(nr) ;
        t.setDaemon(true);
        t.start() ;


    }
    class NewRunnable implements Runnable {
        @Override
        public void run()
        {
            long tmp =(long)1000/(long)128;
            boolean atstart = true;

            while(true) {
                if(isListening&&atstart)//연결된 상태에서 그래프 딱 그리기 시점
                {
                    try {//약간 딜레이주기
                        Thread.sleep(1000);
                        atstart = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else if(!atstart)// 그 후에는 쭉 잘가기
                {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            if (ecglist.size() != 0) {
                                lastecg = ecglist.get(0);
                                addEntry(lastecg);
                                ecglist.remove(0);
                            } else {
                                //addEntry(lastecg);
                            }

                        }

                    });
                }

                try {
                     Thread.sleep(tmp);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }

        }
    }


    public void SettingChart(LineChart chart)
    {

        chart.setDrawGridBackground(true);
        chart.setBackgroundColor(Color.WHITE);
        chart.setGridBackgroundColor(Color.WHITE);

// description text
//        chart.getDescription().setEnabled(true);
        Description des = chart.getDescription();//raw_Chart.getDescription();
        des.setEnabled(true);
        des.setText("ECG");
        des.setTextSize(12f);
        des.setTextColor(getColor(R.color.colorPrimary));

// touch gestures (false-비활성화)
        chart.setTouchEnabled(false);

// scaling and dragging (false-비활성화)
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);

//auto scale
        chart.setAutoScaleMinMaxEnabled(true);

// if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

//X축
        chart.getXAxis().setDrawGridLines(true);
        chart.getXAxis().setDrawAxisLine(false);

        chart.getXAxis().setEnabled(false);
        chart.getXAxis().setDrawGridLines(false);

//Legend
        Legend l = chart.getLegend();
        l.setEnabled(true);
        l.setFormSize(10f); // set the size of the legend forms/shapes
        l.setTextSize(12f);
        l.setTextColor(Color.WHITE);

//Y축
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.DKGRAY);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);


// don't forget to refresh the drawing
        chart.invalidate();
    }

    private void addEntry(double num) {

        LineData data = chartE.getData();

        if (data == null) {
            data = new LineData();
            chartE.setData(data);
        }

        ILineDataSet set = data.getDataSetByIndex(0);
        // set.addEntry(...); // can be called as well

        if (set == null) {
            set = createSet();
            data.addDataSet(set);
        }



        data.addEntry(new Entry((float)set.getEntryCount(), (float)num), 0);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        chartE.notifyDataSetChanged();

        chartE.setVisibleXRangeMaximum(512);
        // this automatically refreshes the chart (calls invalidate())
        chartE.moveViewTo(data.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);

    }

    private LineDataSet createSet() {



        LineDataSet set = new LineDataSet(null, "Real-time Line Data");
        set.setLineWidth(1f);
        set.setDrawValues(false);
        set.setValueTextColor(getColor(R.color.colorPrimary));
        set.setColor(getColor(R.color.colorPrimary));
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawCircles(false);
        set.setHighLightColor(Color.rgb(190, 190, 190));

        return set;
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        ((MainActivity)MainActivity.context_main).toggleECGView(false);
        if(broadcastReceiver!=null)
        {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            broadcastReceiver=null;
        }
        if(t!=null)
            t.interrupt();
    }
    @Override
    protected  void onPause()
    {
        super.onPause();
    }
    @Override
    protected  void onResume()
    {
        super.onResume();
    }



 }
