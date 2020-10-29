package com.imslab.mentis;



import android.graphics.Color;
import android.os.Bundle;
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


public class e4View extends Fragment {
    private LineChart chartBVP,chartEDA,chartTEMP;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view =inflater.inflate(R.layout.ecgview, container, false);

        return  view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        chartBVP= (LineChart ) view.findViewById(R.id.ECGChart);
        SettingChart(chartBVP,1);
        chartEDA = (LineChart) view.findViewById(R.id.ACCXChart);
        SettingChart(chartEDA,2);
        chartTEMP= (LineChart) view.findViewById(R.id.ACCYChart);
        SettingChart(chartTEMP,3);
        LineChart tmp = (LineChart) view.findViewById(R.id.ACCZChart);
        tmp.setVisibility(View.INVISIBLE);
    }

    private void addEntry(LineChart mychart, int flag, double num) {

        LineData data = mychart.getData();

        if (data == null) {
            data = new LineData();
            mychart.setData(data);
        }
        ILineDataSet set = data.getDataSetByIndex(0);
        // set.addEntry(...); // can be called as well

        if (set == null) {
            set = createSet(flag);
            data.addDataSet(set);
        }
        if(set!=null)
            data.addEntry(new Entry((float)set.getEntryCount(), (float)num), 0);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        mychart.notifyDataSetChanged();

        if(flag==1)
            mychart.setVisibleXRangeMaximum(256);//4초동안
        else
            mychart.setVisibleXRangeMaximum(16);//4초동안

        // this automatically refreshes the chart (calls invalidate())
        mychart.moveViewTo(data.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);

    }

    private LineDataSet createSet(int flag) {

        LineDataSet set=null;

        if(getActivity()==null) return null;

        switch (flag)
        {

            case 1:
                set = new LineDataSet(null, "ACC X Data");

                set.setValueTextColor(getActivity().getColor(R.color.accxcolor));
                set.setColor(getActivity().getColor(R.color.accxcolor));
                break;
            case 2:
                set = new LineDataSet(null, "ACC Y Data");

                set.setValueTextColor(getActivity().getColor(R.color.accycolor));
                set.setColor(getActivity().getColor(R.color.accycolor));
                break;
            case 3:
                set = new LineDataSet(null, "ACC Z Data");

                set.setValueTextColor(getActivity().getColor(R.color.acczcolor));
                set.setColor(getActivity().getColor(R.color.acczcolor));
                break;

        }

        if(set==null) return null;
        set.setLineWidth(1f);
        set.setDrawValues(false);

       if(flag==2) set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        else set.setMode(LineDataSet.Mode.LINEAR);

        set.setDrawCircles(false);
        set.setHighLightColor(Color.rgb(190, 190, 190));

        return set;
    }

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

// touch gestures (false-비활성화)
        chart.setTouchEnabled(false);

// scaling and dragging (false-비활성화)
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);

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
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        if(getActivity()==null) return;

        switch (flag)
        {
            case 1://BVP
                des.setText("BVP");
                leftAxis.setLabelCount(6,true);
                des.setTextColor(getActivity().getColor(R.color.accxcolor));
//auto scale
                chart.setAutoScaleMinMaxEnabled(true);
                break;
            case 2: // EDA
                des.setText("EDA");
                des.setTextColor(getActivity().getColor(R.color.accycolor));
                leftAxis.setLabelCount(5,true);
                leftAxis.setGranularity(0.1f);
                leftAxis.setSpaceTop(100);
                leftAxis.setSpaceBottom(100);
                chart.setAutoScaleMinMaxEnabled(false);
                break;
            case 3: // TEMP
                des.setText("Temperature");
                des.setTextColor(getActivity().getColor(R.color.acczcolor));
                leftAxis.setLabelCount(8,true);
                leftAxis.setGranularity(1f);
                leftAxis.setAxisMaximum(37);
                leftAxis.setAxisMinimum(30);
                chart.setAutoScaleMinMaxEnabled(false);
                break;
        }

// don't forget to refresh the drawing
        chart.invalidate();
    }
    @Override
    public void onPause(){
        super.onPause();
        if(getActivity()!=null) getActivity().finish();
     }

    public void insertBVP(float raw)
    {
        if(getActivity()==null) return;
        getActivity().runOnUiThread(new Runnable()
        {

            @Override
            public void run() {

                addEntry(chartBVP,1,raw);
            }
        });

//        if(bvplist!=null)
//            bvplist.add(raw);
    }
    public void insertEDA(float raw)
    {
        if(getActivity()==null) return;
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run() {

                addEntry(chartEDA,2,raw);
            }
        });

    }
    public void insertTEMP(float raw)
    {
        if(getActivity()==null) return;
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run() {
                addEntry(chartTEMP,3,raw);
            }
        });

    }


}
