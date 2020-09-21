package com.imslab.mentis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.unity3d.player.UnityPlayerActivity;

import java.util.ArrayList;
import java.util.Random;

public class SensorView extends AppCompatActivity {
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private Runnable mTimer2;
    private LineGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;
    private double graph2LastXValue = 5d;
    private BroadcastReceiver broadcastReceiver;
    private ArrayList<Integer> ecglist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensorview);

        ecglist = new ArrayList<>();
        GraphView graph = (GraphView) findViewById(R.id.graph);
        mSeries1 = new LineGraphSeries<>(generateData());
        graph.addSeries(mSeries1);
        //graph.getViewport().setScalable(true);

        GraphView graph2 = (GraphView) findViewById(R.id.graph2);
        mSeries2 = new LineGraphSeries<>();
        graph2.addSeries(mSeries2);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(40);

        final IntentFilter intentFilter = new IntentFilter();
        {
            intentFilter.addAction("com.ims.empalink.sendintent");
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();

                if (extras != null) {
                    String tmp =extras.getString("DEVICENAME");
                    String name = extras.getString("USERNAME");
                    float[]bvp_data = extras.getFloatArray("BVP");
                    float[]eda_data = extras.getFloatArray("EDA");
                    float[] temp_data = extras.getFloatArray("TEMP");
                    int[] temp_ecg= extras.getIntArray("ECG");
                    boolean[] state = extras.getBooleanArray("STATE");
                    boolean isConnectECG=false,isConnectE4=false, OnWrist=false;
                    if (state != null) {
                        isConnectECG = state[0];
                        isConnectE4 = state[1];
                        OnWrist = state[2];
                    }

                    if(temp_ecg!=null)
                    {
                        for(int i=0; i<temp_ecg.length;i++)
                        {

                            try {
                                ecglist.add(temp_ecg[i]);
                                // s.append(temp_ecg[i]);
                            }
                            catch (NullPointerException e)
                            {
                               // s.append("nullpointecg");
                            }
                        }
                    }
                  //  s.append(" /bvp/ ");

                    if(bvp_data!=null)
                    {
                        for(int i=0; i<bvp_data.length;i++)
                        {

                            try {
                               // s.append(bvp_data[i]);
                            }
                            catch (NullPointerException e)
                            {
                                //s.append("/nullpointbvp/");
                            }
                        }
                    }
                //    s.append(" /EDA/ ");

                    if(eda_data!=null)
                    {
                        for(int i=0; i<eda_data.length;i++)
                        {

                            try {
                               // s.append(eda_data[i]);
                            }
                            catch (NullPointerException e)
                            {
                              //  s.append("/nullpointeda/");
                            }
                        }
                    }
                  //  s.append(" /Temp/ ");

                    if(temp_data!=null)
                    {
                        for(int i=0; i<temp_data.length;i++)
                        {

                            try {
                             //   s.append(temp_data[i]);
                            }
                            catch (NullPointerException e)
                            {
                            //    s.append("/nullpointtemp/");
                            }
                        }
                    }
                  //  text.setText(s.toString());


                }
                // Toast.makeText(UnityPlayer.currentActivity,"E4! "+text,Toast.LENGTH_SHORT).show();
            }
        };

        this.registerReceiver(broadcastReceiver, intentFilter);

    }
    @Override
    public void onResume() {
        super.onResume();
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                mSeries1.resetData(generateData());
                mHandler.postDelayed(this, 300);
            }
        };
        mHandler.postDelayed(mTimer1, 300);

        mTimer2 = new Runnable() {
            @Override
            public void run() {
                graph2LastXValue += 1d;
                mSeries2.appendData(new DataPoint(graph2LastXValue, getRandom()), true, 40);
                mHandler.postDelayed(this, 200);
            }
        };
        mHandler.postDelayed(mTimer2, 1000);
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer1);
        mHandler.removeCallbacks(mTimer2);
        super.onPause();
    }

    private DataPoint[] generateData() {
        int count = 30;
        DataPoint[] values = new DataPoint[count];
        for (int i=0; i<count; i++) {
            double x = i;
            double f = mRand.nextDouble()*0.15+0.3;
            double y = Math.sin(i*f+2) + mRand.nextDouble()*0.3;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }
    private DataPoint[] generateECG()
    {
        int count = 30;
        DataPoint[] values = new DataPoint[count];
        for (int i=0; i<count; i++) {
            double x = i;
            double f = mRand.nextDouble()*0.15+0.3;
            double y = Math.sin(i*f+2) + mRand.nextDouble()*0.3;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }


    double mLastRandom = 2;
    Random mRand = new Random();
    private double getRandom() {
        return mLastRandom += mRand.nextDouble()*0.5 - 0.25;
    }
}