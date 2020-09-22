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



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    public  List<String> time_list;
    public  List<String> data_list;

    public String user_name="no_name";
    public boolean isECGStart=false;
    public boolean state0=false;
    public  boolean isCalli = false;
    Thread th;

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



    }

    @Override
    public IBinder onBind(Intent intent) {
       // String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MentIS 실행 중...")
              //  .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        //do heavy work on a background thread

        NewRunnable nr = new NewRunnable() ;
        th = new Thread(nr) ;
        th.setDaemon(true);//앱이 종료되면 쓰레드 종료-이건 쓰레드 실행되기 전에 설정되어야하니까/
        th.start() ;

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
        th.interrupt();
        return true;
    }

    class NewRunnable implements Runnable {
        @Override
        public void run()
        {
            while (true)
            {
                int sleeptime = 10000;//10초마다 sleep

                int willsend = time_list.size();//애초에 둘다 연결될때 이 리스트가 채워지니깐
                if(willsend!=0)
                {
                   // 10초마다 쌓인 거 다 집어넣기
                    addItemToDB();
                }
                else
                {
                  //  sleeptime = 1000;//1초마다 sleep
                    data_list.clear();
                    time_list.clear();
                }
                try
                {
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
    private void   addItemToDB() {
        int sendsize = time_list.size();//가변적인 리스트는 항상 특정 타겟시기에 갯수 int로 따로 담아서 하기

        if(sendsize==0) return;

        for(int i =0; i<sendsize;i++)
        {
            if(!connectionJDBCTest()) {
                respondDB();
                return;
            }

        }

    }
    void respondDB()
    {
        Handler handler = new Handler();
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        "데이터 베이스 쓰기가 거부되었습니다.",
                        Toast.LENGTH_SHORT).show();
            }

        });
    }

    public boolean connectionJDBCTest() {


        Connection con = null;

        Statement st = null;

        String content = "";

        try {

            Class.forName("org.mariadb.jdbc.Driver");


        } catch (ClassNotFoundException e) {

            e.printStackTrace();
            return  false;//"error1";


        }

        // 만약 위와 같이 Driver를 설정하지 않는다면, Driver를 찾을수 없다는 Error 메세지가 발생될 것 입니다.

        try {


            con = DriverManager.getConnection("jdbc:mariadb://192.168.0.15:3306/iot_stress", "root", "imslab!@#");

            StringBuilder towrite= new StringBuilder();
            towrite.append("insert into monitoring values (");
            towrite.append(user_name);//id string으로 바꿔달라하기
            towrite.append(",'");
            towrite.append(time_list.get(0));
            time_list.remove(0);
            towrite.append("','");
            towrite.append(data_list.get(0));
            data_list.remove(0);
            towrite.append("',");
            if(isCalli) towrite.append("1)");
            else towrite.append("0)");


            content = towrite.toString();//"insert into monitoring values (1,'2020/09/20 05:20:01','123456',1)";
        } catch (SQLException e) {
            e.printStackTrace();
            return false;//"데이터베이스 연동이 되지 않습니다.(errorcode:1)";
        }

        try {

            st = con.createStatement();
            st.executeUpdate(content);

            } catch (SQLException e) {

                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;//"데이터베이스 쓰기 거부";

            }



            // 접속하고자 하는 Maria DB Server가 설치된 IP 주소 및 설정되어 있는 ID와 PassWord를 설정.

//
//            Statement st = null;
//
//            ResultSet rs = null;

//            st = con.createStatement();

//            if(st.execute("monitoring")) {
//
//                rs = st.getResultSet();
//
//            }
//
//
//
//            while(rs.next()) {
//
//                String str = rs.getString(1);
//
//                sb.append(str);
//
//                sb.append("\n");
//
//            }



        finally {

            if(null != con) {

                try {

                    con.close();

                } catch (SQLException e) {

                    e.printStackTrace();
                    return false;// "데이터베이스 종료 거부";

                }
            }
        }
        return true;

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