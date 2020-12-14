package com.imslab.mentis;

import android.content.Context;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class ecgView extends Fragment {

    private LineChart chartE,chartX,chartY,chartZ;
    public Handler ecgGraphHandler;

     Thread drawgraphThread;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view =inflater.inflate(R.layout.ecgview, container, false);
        
        drawgraphThread = new drawGraphThread();
        drawgraphThread.setDaemon(true);
        drawgraphThread.start() ;
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        chartE = (LineChart ) view.findViewById(R.id.ECGChart);
        SettingChart(chartE,1);
        chartX = (LineChart) view.findViewById(R.id.ACCXChart);
        SettingChart(chartX,2);
        chartY= (LineChart) view.findViewById(R.id.ACCYChart);
        SettingChart(chartY,3);
        chartZ = (LineChart) view.findViewById(R.id.ACCZChart);
        SettingChart(chartZ,4);

    }


    class drawGraphThread extends Thread {
        ArrayList<Float> ecglist = new ArrayList<>();
        ArrayList<Integer> accXlist = new ArrayList<>();
        ArrayList<Integer> accYlist = new ArrayList<>();
        ArrayList<Integer> accZlist= new ArrayList<>();
        long ecgduration =(long)1000/(long)128;
        long accduration = (long)128/(long)28;
        long acccount=0;
        
        @Override
        public void run()
        {

           //try{ Thread.sleep(1000);} catch (Exception e ){e.printStackTrace();}//1초지연


            Looper.prepare();
            ecgGraphHandler = new Handler(Looper.myLooper()) {

            };//define Handler
            Looper.loop();
        }//run method
    }//Thread class




    public void SettingChart(LineChart chart,int flag)
    {

        chart.setDrawGridBackground(true);
       // chart.setBackgroundColor(Color.WHITE);
        chart.setGridBackgroundColor(Color.WHITE);

// description text
//        chart.getDescription().setEnabled(true);
        Description des = chart.getDescription();//raw_Chart.getDescription();
        des.setEnabled(true);
        des.setTextSize(13f);

        if(getActivity()==null)
        {
            return;
        }
        switch (flag)
        {
            case 1://ECG
                des.setText("ECG");
                des.setTextColor(getActivity().getColor(R.color.ecgcolor));
                break;
            case 2: // ACCX
                des.setText("ACCX");
                des.setTextColor(getActivity().getColor(R.color.accxcolor));
                break;
            case 3: // ACCY
                des.setText("ACCY");
                des.setTextColor(getActivity().getColor(R.color.accycolor));
                break;
            case 4: // ACCZ
                des.setText("ACCZ");
                des.setTextColor(getActivity().getColor(R.color.acczcolor));
                break;

        }


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
        l.setEnabled(false);
//        l.setFormSize(10f); // set the size of the legend forms/shapes
//        l.setTextSize(12f);
//        l.setTextColor(Color.WHITE);

//Y축
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.DKGRAY);
       // leftAxis (100);
        leftAxis.setLabelCount(6,true);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);


// don't forget to refresh the drawing
        chart.invalidate();
    }

    private void addEntryAcc(int num2,int num3,int num4)
    {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                LineData data2 = chartX.getData();
                LineData data3 = chartY.getData();
                LineData data4 = chartZ.getData();

                if (data2 == null) {
                    data2 = new LineData();
                    chartX.setData(data2);
                }
                if (data3 == null) {
                    data3 = new LineData();
                    chartY.setData(data3);
                }
                if (data4 == null) {
                    data4 = new LineData();
                    chartZ.setData(data4);
                }
                ILineDataSet set2 = data2.getDataSetByIndex(0);
                ILineDataSet set3 = data3.getDataSetByIndex(0);
                ILineDataSet set4 = data4.getDataSetByIndex(0);

                if (set2 == null) {
                    set2 = createSet(2);
                    data2.addDataSet(set2);
                }
                if (set3 == null) {
                    set3 = createSet(3);
                    data3.addDataSet(set3);
                }
                if (set4 == null) {
                    set4 = createSet(4);
                    data4.addDataSet(set4);
                }
                if (set2 != null)
                    data2.addEntry(new Entry((float)set2.getEntryCount(), (float)num2), 0);
                data2.notifyDataChanged();
                if (set3 != null)
                    data3.addEntry(new Entry((float)set3.getEntryCount(), (float)num3), 0);
                data3.notifyDataChanged();
                if (set4 != null)
                    data4.addEntry(new Entry((float)set4.getEntryCount(), (float)num4), 0);
                data4.notifyDataChanged();

                chartX.notifyDataSetChanged();
                chartX.setVisibleXRangeMaximum(112);//4초동안
                // this automatically refreshes the chart (calls invalidate())
                chartX.moveViewTo(data2.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);

                chartY.notifyDataSetChanged();
                chartY.setVisibleXRangeMaximum(112);//4초동안
                // this automatically refreshes the chart (calls invalidate())
                chartY.moveViewTo(data3.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);


                chartZ.notifyDataSetChanged();
                chartZ.setVisibleXRangeMaximum(112);//4초동안
                // this automatically refreshes the chart (calls invalidate())
                chartZ.moveViewTo(data4.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);
            }

        });//uithread


    }

    private void addEntry(float num) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {

                LineData data = chartE.getData();

                if (data == null) {
                    data = new LineData();
                    chartE.setData(data);
                }


                ILineDataSet set = data.getDataSetByIndex(0);

                // set.addEntry(...); // can be called as well

                if (set == null) {
                    set = createSet(1);
                    data.addDataSet(set);
                }
                if(set!=null)
                    data.addEntry(new Entry((float)set.getEntryCount(), num), 0);
                data.notifyDataChanged();


                // let the chart know it's data has changed
                chartE.notifyDataSetChanged();

                chartE.setVisibleXRangeMaximum(512);//4초동안
                // this automatically refreshes the chart (calls invalidate())
                chartE.moveViewTo(data.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);

            }

        });//uithread

    }

    private LineDataSet createSet(int flag) {

        LineDataSet set=null;

        if(getActivity()==null) return null;
        switch (flag)
        {
            case 1:
                set = new LineDataSet(null, "ECG Data");

                set.setValueTextColor(getActivity().getColor(R.color.ecgcolor));
                set.setColor(getActivity().getColor(R.color.ecgcolor));
                break;
            case 2:
                set = new LineDataSet(null, "ACC X Data");

                set.setValueTextColor(getActivity().getColor(R.color.accxcolor));
                set.setColor(getActivity().getColor(R.color.accxcolor));
                break;
            case 3:
                set = new LineDataSet(null, "ACC Y Data");

                set.setValueTextColor(getActivity().getColor(R.color.accycolor));
                set.setColor(getActivity().getColor(R.color.accycolor));
                break;
            case 4:
                set = new LineDataSet(null, "ACC Z Data");

                set.setValueTextColor(getActivity().getColor(R.color.acczcolor));
                set.setColor(getActivity().getColor(R.color.acczcolor));
                break;

        }

        if(set==null) return null;
        set.setLineWidth(1f);
        set.setDrawValues(false);

        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawCircles(false);
        set.setHighLightColor(Color.rgb(190, 190, 190));

        return set;
    }


    @Override
    public void onPause() {
        super.onPause();

        if(getActivity()!=null) getActivity().finish();

    }

 }
