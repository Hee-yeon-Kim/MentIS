package com.imslab.mentis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    public  List<String> time_list;
    public  List<String> data_list;

    public String name="no_name";
    public boolean isECGStart=false;
    public boolean state0=false;
    Thread t2;

    IBinder mBinder = new MyBinder();

    class MyBinder extends Binder {
        ForegroundService getService() { // 서비스 객체를 리턴
            return ForegroundService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        data_list= new ArrayList<>();

        time_list = new ArrayList<>();

        IntentFilter intentFilter = new IntentFilter();
        {
            intentFilter.addAction("com.ims.empalink.sendintent");
        }


         NewRunnable nr2 = new NewRunnable() ;
        t2 = new Thread(nr2) ;
        t2.start() ;

    }

    @Override
    public IBinder onBind(Intent intent) {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MentLS 동작 중...")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        //do heavy work on a background thread


        //stopSelf();

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        data_list.clear();

        time_list.clear();
        isECGStart=false;
        state0=false;
        return true;
    }

    class NewRunnable implements Runnable {
        @Override
        public void run()
        {
            while (true)
            {
                int sleeptime = 6000;

                if(isECGStart&&state0)
                {
                    if(data_list.size()>=6)
                    {

                        addItemToSheet(); //둘다 연결된 상태일 때 올리기

                    }
                }
                else
                {
                    sleeptime=1000;
                    data_list.clear();
                    time_list.clear();
                }
                try
                {
                    if(data_list.size()>6)
                    {
                        sleeptime=500;
                    }
                    Thread.sleep(sleeptime);
                }
                catch (Exception e)
                {
                    e.printStackTrace() ;
                }
            }
        }
    }

//    private String makestring()
//    {
//        int bvpCount = bvp_list.size()/6;
//        int edaCount = eda_list.size()/6;
//        int tempCount =temp_list.size()/6;
//
//        StringBuilder tmp1= new StringBuilder();
//        for(int i=0;i<212; i++)
//        {
//            if(ecg_list.get(0)!=null)
//            {
//
//                tmp1.append( Integer.toString(ecg_list.get(0)));
//
//            }
//            ecg_list.remove(0);
//            if(i==127||i==211) tmp1.append("/");
//            else tmp1.append(",");
//        }
//
//        for(int i=0;i<bvpCount; i++)
//        {
//            if(bvp_list.get(0)!=null)
//            {
//                tmp1.append(Float.toString(bvp_list.get(0)));
//            }
//            bvp_list.remove(0);
//            if(i==bvpCount-1) tmp1.append("/");
//            else tmp1.append(",");
//        }
//        for(int i=0;i<edaCount; i++)
//        {
//            if(eda_list.get(0)!=null)
//            {
//                tmp1.append(Float.toString(eda_list.get(0)));
//            }
//
//            eda_list.remove(0);
//            if(i==edaCount-1) tmp1.append("/");
//            else tmp1.append(",");
//        }
//        for(int i=0;i<tempCount; i++)
//        {
//            if(temp_list.get(0)!=null)
//            {
//                tmp1.append(Float.toString(temp_list.get(0)));
//            }
//            temp_list.remove(0);
//            if(i==tempCount-1) tmp1.append("/");
//            else tmp1.append(",");
//        }
//        return tmp1.toString();
//    }
    private void   addItemToSheet() {

        //  final ProgressDialog loading = ProgressDialog.show(this,"Adding Item","Please wait");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://script.google.com/macros/s/AKfycbxx6jfjjrorF4dl8H7bBYlI-ddHR1QF4yjRqqzzI-2iGfpp1Hs/exec",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {

                        Handler handler = new Handler();
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        response,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Toast.makeText(getApplication(),response,Toast.LENGTH_LONG).show();
                        //  Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        // startActivity(intent);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        Handler handler = new Handler();
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        error.toString(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        //Toast.makeText(getApplication(),error.toString(),Toast.LENGTH_LONG).show();

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parmas = new HashMap<>();

                //here we pass params
                parmas.put("action", "addItem");
                parmas.put("userName", name);

                parmas.put("Date1", time_list.get(0));
                time_list.remove(0);
                parmas.put("Data1", data_list.get(0));
                data_list.remove(0);

                parmas.put("Date2", time_list.get(0));
                time_list.remove(0);
                parmas.put("Data2", data_list.get(0));
                data_list.remove(0);
                parmas.put("Date3", time_list.get(0));
                time_list.remove(0);
                parmas.put("Data3", data_list.get(0));
                data_list.remove(0);
                parmas.put("Date4", time_list.get(0));
                time_list.remove(0);
                parmas.put("Data4", data_list.get(0));
                data_list.remove(0);
                parmas.put("Date5", time_list.get(0));
                time_list.remove(0);
                parmas.put("Data5",data_list.get(0));
                data_list.remove(0);
                parmas.put("Date6", time_list.get(0));
                time_list.remove(0);
                parmas.put("Data6", data_list.get(0));
                data_list.remove(0);
                return parmas;
            }
        };

        int socketTimeOut = 60000;// u can change this .. here it is 50 seconds

      /*  RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(this);

        queue.add(stringRequest);*/


    }


    @Override
    public void onDestroy() {
        super.onDestroy();


    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}