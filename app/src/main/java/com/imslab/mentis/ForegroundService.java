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
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
    public boolean openDB = false;
    public int userID=-1;
    Thread th;
    Connection con = null;

    Statement st = null;

    IBinder mBinder = new MyBinder();

    class MyBinder extends Binder {
        ForegroundService getService() { // 서비스 객체를 리턴
            return ForegroundService.this;
        }
    }
    public ForegroundService getForegroundService()
    {
        if(th==null) return null;
        return ForegroundService.this;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        data_list= new ArrayList<>();

        time_list = new ArrayList<>();
        isCalli = false;
        openDB = false;

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
        closeDB();
        th.interrupt();
        return true;
    }
    public  void initialize()
    {
        data_list.clear();
        time_list.clear();
        openDB = false;
    }


    class NewRunnable2 implements Runnable {
        @Override
        public  void run()
        {
           if(connectionName())
           {
               respondDB(5);
           }
        }
    }
    class NewRunnable implements Runnable {
        @Override
        public void run()
        {
            initialize();
            while (true)
            {
                //우선 db 가 연결이 되어있냐?
                if(!openDB)
                {
                    openDB = connectDB();
                   if(openDB) {
                       ((MainActivity)MainActivity.context_main).changeDBicon(true);
                   }
                   else {
                       if(time_list.size()>300) // 5분동안 미연결 시 지움
                       {
                           initialize();
                       }
                       ((MainActivity)MainActivity.context_main).changeDBicon(false);
                      return;
                   }
                }

                int sleeptime = 10000;//10초마다 sleep

                int willsend = time_list.size();//애초에 둘다 연결될때 이 리스트가 채워지니깐
                if(willsend!=0)
                {
                   // 10초마다 쌓인 거 다 집어넣기
                    addItemToDB();
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


    public void nameEvent()
    {
        NewRunnable2 nr2 = new NewRunnable2() ;
        Thread th2 = new Thread(nr2) ;
        th2.setDaemon(true);//앱이 종료되면 쓰레드 종료-이건 쓰레드 실행되기 전에 설정되어야하니까/
        th2.start() ;
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
                ((MainActivity)MainActivity.context_main).changeDBicon(false);
                return;
            }

        }

    }
    // 검색



    void respondDB(int flag)
    {

        Handler handler = new Handler(Looper.getMainLooper());


            handler.post(new Runnable() {

                @Override
                public void run() {
                    if (flag == 1) {

                        Toast.makeText(getApplicationContext(),
                                "데이터 베이스-write to user table가 거부 되었습니다",
                                Toast.LENGTH_SHORT).show();
                    } else if (flag == 2) {
                        Toast.makeText(getApplicationContext(),
                                "데이터 베이스 write to monitoring table 가 거부되었습니다.",
                                Toast.LENGTH_SHORT).show();

                    } else if (flag == 3) {
                        Toast.makeText(getApplicationContext(),
                                "사용자에 할당된 ID가 없습니다.",
                                Toast.LENGTH_SHORT).show();
                    }else if(flag==5)
                    {
                        Toast.makeText(getApplicationContext(),
                                "ID 설정이 완료되었습니다.",
                                Toast.LENGTH_SHORT).show();
                    }
                }

            });

    }
    public boolean connectionName() {

        if(!openDB) return false;

        String content = null;
        StringBuilder towrite= new StringBuilder();
        towrite.append("insert into user values (");
        towrite.append("'");
        towrite.append(user_name);//String
        towrite.append("'");
        towrite.append(",,,,)");

        content= "INSERT INTO user (name,sex,age,height,weight) VALUES ('"+user_name+"',1,29,170,60)";
        try {

            st.executeUpdate(content);

        } catch (SQLException e) {

            // TODO Auto-generated catch block
            e.printStackTrace();

            respondDB(1);

            return false;//"데이터베이스 쓰기 거부";

        }
        StringBuilder sb= new StringBuilder();

        String sql = sb.append("select * from " + "user" + " where")
                .append(" name = ")
                .append("'")
                .append(user_name)
                .append("'")
                .append(";").toString();
        try {
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()){
                userID= rs.getInt("user_id");
            }



        } catch (SQLException e) {
            // TODO Auto-generated catch block
            respondDB(3);
            e.printStackTrace();
            return  false;
        }
        return  true;

    }
    public Boolean closeDB()
    {

            if(null != con) {

                try {

                    con.close();
                    con=null;
                    st=null;

                } catch (SQLException e) {

                    e.printStackTrace();
                    return false;// "데이터베이스 종료 거부";

                }
            }
            return true;
    }


    public boolean connectDB()
    {
        try {

            Class.forName("org.mariadb.jdbc.Driver");


        } catch (ClassNotFoundException e) {

            e.printStackTrace();
            return  false;//"error1";


        }

        // 만약 위와 같이 Driver를 설정하지 않는다면, Driver를 찾을수 없다는 Error 메세지가 발생될 것 입니다.

        try {

            con = DriverManager.getConnection("jdbc:mariadb://192.168.0.15:3306/iot_stress", "root", "imslab!@#");
            st = con.createStatement();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;//"데이터베이스 연동이 되지 않습니다.(errorcode:1)";
        }
        return  true;
    }


    public boolean connectionJDBCTest() {

        String content = "";

        StringBuilder towrite= new StringBuilder();
        towrite.append("insert into monitoring values (");
        towrite.append(userID);//id
        towrite.append(",'");
        towrite.append(time_list.get(0));//String
        time_list.remove(0);
        towrite.append("','");
        towrite.append(data_list.get(0));
        data_list.remove(0);
        towrite.append("',");
        if(isCalli) towrite.append("1)");
        else towrite.append("0)");


        content = towrite.toString();//"insert into monitoring values (1,'2020/09/20 05:20:01','123456',1)";

        try {

            st.executeUpdate(content);

            } catch (SQLException e) {
//한번 에러나면 끊고 다시 들어가도록
                // TODO Auto-generated catch block
                e.printStackTrace();
                openDB = false;
                closeDB();
                ((MainActivity)MainActivity.context_main).changeDBicon(false);
                respondDB(2);
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



//        finally {
//
//            if(null != con) {
//
//                try {
//
//                    con.close();
//
//                } catch (SQLException e) {
//
//                    e.printStackTrace();
//                    return false;// "데이터베이스 종료 거부";
//
//                }
//            }
//        }
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