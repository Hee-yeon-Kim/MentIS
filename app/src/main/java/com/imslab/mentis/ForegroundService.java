package com.imslab.mentis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Person;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.IconCompat;
import android.util.Log;
import android.widget.Toast;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final String CHANNEL_ID2 = "ForegroundServiceChannel2";
    public static final String CHANNEL_ID3 = "ConnectionErrorChannel2";
    public static final String CHANNEL_ID4 = "HighStressChannel";

    public  List<String> time_list;
    public  List<String> data_list;
    public List<Answer> answer_list;

    public String user_name="no_name";
    public String user_pw="";
    public boolean isECGStart=false;
    public boolean isFeedback=false;
    public boolean state0=false;
    public  int isCalli = 0;
    public boolean openDB = false;
    public int userID=-1;
    public int userGender=0;
    public int userAge=0;
    public float userThres=7/10f;
    String checkedTime="";
    int readcount=0;

    Connection con = null;

    Statement st = null;

    IBinder mBinder = new MyBinder();
    public Handler sendHandler=null;
    Thread senddatathread = null;

    Timer timer,stressCheckTimer;
    TimerTask TT,stressCheckTask;
    NotificationManager notimanager;
    PendingIntent pendingIntent, pendingIntent2,pendingIntent3;
    class MyBinder extends Binder {
        ForegroundService getService() { // 서비스 객체를 리턴
            return ForegroundService.this;
        }
    }


    static ForegroundService foregroundService;

    @Override
    public void onCreate() {
        super.onCreate();

        foregroundService = this;
        answer_list = new ArrayList<>();
        data_list= new ArrayList<>();
        time_list = new ArrayList<>();
        isCalli = 0;
        openDB = false;

        senddatathread = new sendDataThread();
        senddatathread.setDaemon(true);
        senddatathread.start();
        notimanager = getSystemService(NotificationManager.class);

        Intent notificationIntent = new Intent(this, stressView.class);

        Intent notificationIntent2 = new Intent(this, reportMain.class);

        Intent notificationIntent3 = new Intent(this, MiddleActivity.class);

        pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntent2 = PendingIntent.getActivity(this,
                0, notificationIntent2, PendingIntent.FLAG_UPDATE_CURRENT
        );
        pendingIntent3 = PendingIntent.getActivity(this,
                0, notificationIntent3, PendingIntent.FLAG_UPDATE_CURRENT
        );

//(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);

        timer = new Timer();
        TT = new TimerTask() {
            @Override
            public void run() {
                // 반복실행할 구문
                try {
                    Notification notification2 = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID2)
                            .setContentTitle("Please fill out the self report")
                            .setContentText("Touch here to open the app.")
                            .setColor(getColor(R.color.colorPrimary))
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentIntent(pendingIntent2)
                            .setTimeoutAfter(1000*60*45)
                            .build();
                    notification2.flags |= Notification.FLAG_AUTO_CANCEL|Notification.FLAG_INSISTENT|Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE;

                    notimanager.notify(2, notification2);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            }

        };

        stressCheckTimer = new Timer();
        stressCheckTask = new TimerTask() {
            @Override
            public void run() {
                // 반복실행할 구문
                StressCheckfromDB();
            }

        };


    }

    public void disconnectNotification(String str,int id)
    {

       try {

           Notification notification22 = new NotificationCompat.Builder(this, CHANNEL_ID3)
                   .setContentTitle(str + "is disconnected")
                   // .setContentText("자가 진단해주세요")
                   .setColor(getColor(R.color.colorPrimary))
                   .setSmallIcon(R.mipmap.ic_launcher_round)
                   .setTimeoutAfter(1000*60*5)
                   .build();

           notification22.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_INSISTENT | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;

           notimanager.notify(id, notification22);

       }
       catch (Exception e)
       {
           e.printStackTrace();
       }
    }
    public void EndingNotification()
    {

        try {
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID3)
                    .setContentTitle("Please fill out the post-self report")
                    .setContentText("Touch here to enter the app.")
                    .setColor(getColor(R.color.colorPrimary))
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(pendingIntent2)
                    .build();

            notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_INSISTENT | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;

            notimanager.notify(5, notification);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
       // String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MentIS is running...")
              //  .setContentText(input)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .build();

        startForeground(1, notification);

        //do heavy work on a background thread

        initialize();

        timer.schedule(TT, 1000*60*60, 1000*60*60); //Timer 실행 설문지
        stressCheckTimer.schedule(stressCheckTask, 1000*60*30, 1000*60*30); //30분마다 스트레스 체크 Timer 실행
        readcount = 0;//read count 셋팅
        return mBinder;
    }
    public  void initialize() {
        try {
            data_list.clear();
            time_list.clear();

            answer_list.clear();
            isCalli = 0;
         //   openDB = false;


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onUnbind(Intent intent)
    {

        timer.cancel();
        stressCheckTimer.cancel();
        data_list.clear();
        time_list.clear();
        answer_list.clear();
        isECGStart=false;
        state0=false;
        closeDB();
        notimanager.cancelAll();
        return true;
    }
    class FeedbackThread extends Thread {
        @Override
        public void run() {
            while(isFeedback)
            {
                getRespfromDB();//10초마다
                try{
                    Thread.sleep(10000);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    //id에 맞는 분석된 정보 받아오는 쓰레드 - 이벤트 속성 - 설정할 때만 동작
    class NewRunnable3 implements Runnable {
        @Override
        public  void run()
        {
            Bundle bundle = getDatafromDB();
            if(bundle==null)
            {
               // respondDB(  "데이터 베이스 write to monitoring table 가 거부되었습니다.");
                openDB=false;
            }
            else
            {
                Intent intent = new Intent("DBDATA");
                intent.putExtras(bundle);
                LocalBroadcastManager.getInstance(ForegroundService.this).sendBroadcast(intent);

            }

        }
    }

//이름 설정 및 ID찾기 쓰레드 - 이벤트 속성 - 설정할 때만 동작
    class NewRunnable2 implements Runnable {
        @Override
        public  void run()
        {
            if(connectionName())
            {
                ((MainActivity)MainActivity.context_main).isLogin = true;
                ((MainActivity)MainActivity.context_main).updateNameLabel(true);
            }
            else
            {
                clearUser();
            }
        }
    }

    //데이터 보내는 쓰레드 oncreate부터 계속 동작함
    class sendDataThread extends Thread {
        @Override
        public void run()
        {
            //우선 db 가 연결이 되어있냐?

            while(!openDB)//계속 서버 연결 시도 3초 간격으로
            {
                openDB = connectDB();
                    if(openDB) {
                        ((MainActivity)MainActivity.context_main).changeDBicon(true);
                        break;
                    }
                    else
                    {
                        ((MainActivity)MainActivity.context_main).changeDBicon(false);
                        try {
                            Thread.sleep(3000);
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }

            }

            //서버가 처음 연결되었을 때 이제 받음
            Looper.prepare();
            sendHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(Message msg)
                {
                    if(openDB)
                    {
                        //받은 메시지 db로 보내기
                        try {
                            Bundle bundle = msg.getData();
                            if (bundle.getBoolean("Flag")) {//그냥 데이터 제출
                                String _time = bundle.getString("DATATIME");
                                String _data = bundle.getString("FULLDATA");

                                time_list.add(_time);
                                data_list.add(_data);
                            }
                            else//응답데이터 제출
                            {
                                connectionJDBCTest(2,bundle);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Log.d("CUSTOMERROR","서비스핸들러 받기오류");
                        }

                        int willsend = time_list.size();//애초에 둘다 연결될때 이 리스트가 채워지니깐
                        if(willsend>30)//30초 단위로 보냄
                        {
                            for (int i = 0; i < willsend; i++) {
                                if (!connectionJDBCTest(1,null)) {
                                    ((MainActivity) MainActivity.context_main).changeDBicon(false);
                                    openDB=false;
                                    break;
                                }
                            }
                        }
                    }

                    if(!openDB)//혹시나 중간에 오류로 감지가되어 or 연결이 끊겨서 db가 안열렸을 때
                    {
                        if(time_list.size()>1200)//20분 쌓임-과부하 염려
                        {
                            for(int i=0; i<900;i++) {
                                time_list.remove(0);
                                data_list.remove(0);
                            }
                            Log.d("CUSTOMERROR","과부하");//오류 띄우기
                        }
                        openDB = connectDB();
                        if(openDB) {
                            ((MainActivity) MainActivity.context_main).changeDBicon(true);
                        }
                        else
                        {
                            respondDB("서버 연결을 확인해주세요.");//오류띄우기
                            if(time_list.size()>=300) // 5분동안 미연결 시 지움
                            {
                                data_list.clear();
                                time_list.clear();
                            }
                        }
                    }
                }
            };
            Looper.loop();
        }
    }

    public void nameEvent()
    {
        NewRunnable2 nr2 = new NewRunnable2() ;
        Thread th2 = new Thread(nr2) ;
        th2.setDaemon(true);//앱이 종료되면 쓰레드 종료-이건 쓰레드 실행되기 전에 설정되어야하니까/
        th2.start() ;
    }
    public boolean dataEvent()
    {
        if(userID<0||!openDB)
        {
           respondDB( "MentIS - 서버 연결 및 로그인을 확인해주세요.");
           return false;
        }
        NewRunnable3 nr3 = new NewRunnable3() ;
        Thread th3 = new Thread(nr3) ;
        th3.setDaemon(true);
        th3.start() ;
        return true;
    }

    public void feedbackdataEvent(boolean flag)
    {
        if(flag){
            isFeedback=true;
            FeedbackThread th3 = new FeedbackThread() ;
            isCalli = 2;
            th3.setDaemon(true);
            th3.start() ;
        }
        else
        {
            isFeedback=false;
            isCalli=1;

        }
    }

    void clearUser()
    {
        userID = -1;
        user_pw="0000";
        user_name="no_name";
        ((MainActivity)MainActivity.context_main).isLogin=false;
        ((MainActivity)MainActivity.context_main).updateNameLabel(false);
    }

    void respondDB(final String text)
    {

        Handler handler = new Handler(Looper.getMainLooper());


        handler.post(new Runnable() {

            @Override
            public void run() {


                Toast.makeText(getApplicationContext(),
                        text,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }



    public boolean connectionName() {

        if(!openDB)
        {
            respondDB("서버 오류");//서버 오류
            return false;
        }
        StringBuilder sb= new StringBuilder();
        int tmp_id=-100;
        String tmp_pw="@";
        String sql = sb.append("SELECT * FROM " + "user" + " WHERE")
                .append(" name = ")
                .append("'")
                .append(user_name)
                .append("'")
                .append(";").toString();
        try
        {
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()){
                tmp_id= rs.getInt("user_id");
                tmp_pw = rs.getString("password");
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            respondDB("로그인 오류");
            e.printStackTrace();
            return  false;
        }

        //id 얻음
        if(tmp_id==-100)// 신규가입하는 경우
        {
            userID = tmp_id;//-1 으로 DB에 뜨면 id넘버 매칭 잘 못된거임
            //아이디 비밀번호 db에 insert
            String content = "INSERT INTO user (name,password,sex,age,height,weight) VALUES ('" + user_name + "','"+user_pw+"',"+userGender+','+userAge+','+"170,60)";
            try {
                st.executeUpdate(content);//회원가입 완료
            } catch (SQLException e) {

                // TODO Auto-generated catch block
                e.printStackTrace();
                respondDB("데이터 베이스-write to user table가 거부 되었습니다");//회원가입 오류
                return false;//"데이터베이스 쓰기 거부";
            }

            try
            {
                //회원가입이 잘 된 후에 여기 도달
                StringBuilder sb2= new StringBuilder();
                String sql2 = sb2.append("SELECT * FROM " + "user" + " WHERE")
                        .append(" name = ")
                        .append("'")
                        .append(user_name)
                        .append("'")
                        .append(";").toString();
                ResultSet rs = st.executeQuery(sql2);
                while(rs.next()){
                    userID = rs.getInt("user_id");
                    user_pw = rs.getString("password");
                }
            } catch (SQLException e)
            {
                // 회원가입은 되었는 데 로그인이 안됨
                respondDB( "회원가입 오류-회원가입을 다시 시도해주세요.");
                e.printStackTrace();
                return  false;
            }


            //회원가입 완료되고 id 매칭하기-20번 도전
            for(int i=0; i<20; i++)
            {
                if(findID()) break;
                if(i==19) {
                    respondDB( "회원가입 오류-회원가입을 다시 시도해주세요.");
                    return false;
                }
            }

            respondDB(  "계정 생성이 완료되었습니다.");
            return true;
        }
        else
        {
            //이미 존재
            if(tmp_pw.equals(user_pw))
            {
                userID = tmp_id;
                respondDB( "로그인 되었습니다");//로그인 되었습니다.

                //로그인 성공
                return true;
            }
            else
            {
                //로그인 실패
                respondDB("비밀번호가 일치하지 않습니다.");
                return false;
            }
        }
    }

    private boolean findID()
    {
        try
        {
            StringBuilder sb2= new StringBuilder();

            String sql2 = sb2.append("SELECT * FROM " + "user" + " WHERE")
                    .append(" name = ")
                    .append("'")
                    .append(user_name)
                    .append("'")
                    .append(";").toString();
            ResultSet rs = st.executeQuery(sql2);
            while(rs.next()){
                userID = rs.getInt("user_id");
                user_pw = rs.getString("password");
            }
        } catch (Exception e)
        {
            // 회원가입은 되었는 데 로그인이 안됨
            e.printStackTrace();
            return  false;
        }

        if(userID>0)
        {
            ((MainActivity)MainActivity.context_main).isLogin = true;
        }
        else
        {
            return false;
        }

        return true;
    }

    public Boolean getRespfromDB() {


        String sql = "SELECT * FROM " + "user_feedback" + " WHERE"
                + " user_id = "
                + userID;

        String datetime = null;

        int initial = -100;
        int current = -100;
        try {
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                //   datetime = rs.getString("time");
                initial = rs.getInt("initial_HR");
                current = rs.getInt("current_HR");
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        int gap = current - initial;
        int score = -1;
        if (initial != -100 && current!=-100) {

            if(gap<-10){
                score=100;
            } else if(gap<-8){
                score=80;
            } else if(gap<-6){
                score=60;
            } else if(gap<-4){
                score=40;
            } else  if(gap<-2){
                score=20;
            } else {
                score=0;
            }
        }

        if(score>0) {
            //유니티로 보내자
            Intent sendIntent = new Intent();
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendIntent.setAction("com.ims.mentis.feedbackscore");
            Bundle extras = new Bundle();
            extras.putString("Time", "202102021234");
            extras.putInt("Score", score);
            sendIntent.putExtra("FeedbackBundle", extras);
            sendBroadcast(sendIntent);
            respondDB("점수 보냄 성공");
        } else {
            respondDB("점수 산출 시도");
        }
        return  true;
    }

    public int getCalliResultfromDB()
    {


        String sql = "SELECT * FROM " + "user_callibration" + " WHERE"
                +" user_id = "
                +userID
                ;
        int count=0;

        try {
            ResultSet rs = st.executeQuery(sql);

            while(rs.next()){
                count++;

            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }




        return count;
    }

    public Boolean StressCheckfromDB() {

//        respondDB("아1");
//        if(userID==-1||!openDB) return false;
//        String frontdate="2020-09-24 17:04:44";
//        String backdate="2020-09-24 16:38:06";
//        respondDB("아2");
//
//        String sql = "SELECT * FROM " + "user_analysis" + " WHERE"
//                + " (user_id = "
//                + userID+") AND time >= str_to_date('"+frontdate+"', '%Y-%m-%d %T')"
//                + ";";
        String sql = "SELECT * FROM " + "user_analysis" + " WHERE"
                +" user_id = "
                +userID
                +";";

        int Stresscount=0;
        int Goodcount=0;
        int Totalcount=0;

         //
        long starttime = 0, endtime=0;
        Date date = new Date();
        SimpleDateFormat sdformat = new SimpleDateFormat("yyyyMMddHHmmss");
        starttime=Long.parseLong(sdformat.format(date));
        Log.d("check",String.valueOf(starttime));
       // starttime=20200924164506L;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date); // 30분 빼기
        cal.add(Calendar.MINUTE, -30);
        endtime=Long.parseLong(sdformat.format(cal.getTime()));
        Log.d("check",String.valueOf(endtime));
       // endtime=20200924161506L;

        //
        try {
            ResultSet rs = st.executeQuery(sql);
            while(rs.next())
            {
                String str = rs.getString("time");
                str = str.replace("-", "");
                str = str.replace(":", "");
                str = str.replace(" ", "");
                double tmp =Double.parseDouble(str);
               // Log.d("check","볼것"+String.valueOf(tmp)+"스타트"+String.valueOf(starttime)+"끝 "+String.valueOf(endtime));
                if(tmp<=starttime && tmp>=endtime)
                {
                    int latestStress=rs.getInt("Stress");

                    if(latestStress==1) Stresscount++;
                    else if(latestStress==0) Goodcount++;
                }

            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            respondDB("데이터베이스 읽기 오류_3");

            return false;
        }
      //  respondDB("스: "+String.valueOf(Stresscount)+"굿: "+String.valueOf(Goodcount));
        Totalcount = Stresscount + Goodcount;

        if(Totalcount>0)
        {
            if(Stresscount/Totalcount>userThres)
            {
                Notification notification3 = new NotificationCompat.Builder(this, CHANNEL_ID2)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle("Your stress level is high")
                        .setContentText("Touch here to do breathing exercises to relax.")
                        .setColor(getColor(R.color.colorPrimary))
                        .setContentIntent(pendingIntent3)//훈련창
                        .setTimeoutAfter(1000*60*25)
                        .build();
                notification3.flags |= Notification.FLAG_AUTO_CANCEL|Notification.FLAG_INSISTENT|Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE;
                //알림
                notimanager.notify(200,notification3);

            }else{

                Notification notification32 = new NotificationCompat.Builder(this, CHANNEL_ID2)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle("Check your stress now")
                        .setContentText("Touch here to enter the app.")
                        .setColor(getColor(R.color.colorPrimary))
                        .setContentIntent(pendingIntent)//결과창
                        .setTimeoutAfter(1000*60*25)
                        .build();
                notification32.flags |= Notification.FLAG_AUTO_CANCEL|Notification.FLAG_INSISTENT|Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE;


                //알림
                notimanager.notify(220,notification32);
            }
            return true;
        }
        else {
            //아무것도 데이터 없을 때
            respondDB("분석된 스트레스 데이터 없음");
            return false;
        }

    }
    public Bundle getDatafromDB()
    {


        String sql = "SELECT * FROM " + "user_analysis" + " WHERE"
                +" user_id = "
                +userID
                +";";

        ArrayList<Integer> ecg_sqa_list=new ArrayList<>();
        ArrayList<Integer>  HRmean_list=new ArrayList<>();
        ArrayList<Integer>  STmean_list=new ArrayList<>();
        ArrayList<Integer> Stress_list=new ArrayList<>();
        ArrayList<String> DateTime_list=new ArrayList<>();


        try {
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()){
                String datetime = rs.getString("time");
                DateTime_list.add(datetime);
                Integer ecg_sqa = rs.getInt("ECG_SQA");
                ecg_sqa_list.add(ecg_sqa);

                Integer hrmean = rs.getInt("HRmean");
                HRmean_list.add(hrmean);
                Integer stmean = rs.getInt("STmean");
                STmean_list.add(stmean);

                Integer stress = rs.getInt("Stress");
                Stress_list.add(stress);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            respondDB( "데이터베이스 읽어들이기 실패");//측정 데이터가 부족합니다.
            return  null;
        }

        Bundle bundle = new Bundle();

        if(DateTime_list.size()>0) {

            bundle.putStringArrayList("TIME", DateTime_list);
            bundle.putIntegerArrayList("ECG_SQA", ecg_sqa_list);
            bundle.putIntegerArrayList("HRmean", HRmean_list);
            bundle.putIntegerArrayList("STmean", STmean_list);
            bundle.putIntegerArrayList("Stress", Stress_list);

           // ((MainActivity) MainActivity.context_main).startStressView(bundle,true);
        }
        else {
            respondDB( "측정 데이터가 더 필요합니다.");//측정 데이터가 부족합니다.
            return null;
           // ((MainActivity) MainActivity.context_main).startStressView(null,false);
        }


        return bundle;
    }

    public Boolean closeDB()
    {

            if(null != con) {

                try {

                    con.close();
                    con=null;
                    st=null;

                } catch (Exception e) {

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


        } catch (Exception e) {

            e.printStackTrace();
            respondDB("db연동에러_1");
            return  false;//"error1";


        }

        // 만약 위와 같이 Driver를 설정하지 않는다면, Driver를 찾을수 없다는 Error 메세지가 발생될 것 입니다.

        try {

            con = DriverManager.getConnection("jdbc:mariadb://141.223.196.216:5522/iot_stress", "root", "imslab!@#");
            st = con.createStatement();

        } catch (Exception e) {
            e.printStackTrace();
            respondDB("db연동에러_2");
            return false;//"데이터베이스 연동이 되지 않습니다.(errorcode:1)";
        }
        return  true;
    }


    public boolean connectionJDBCTest(int flag,Bundle bundle) {
        String content = "";

        if(flag==1) {//데이터 보내기

            StringBuilder towrite = new StringBuilder();
            towrite.append("insert into monitoring values (");
            towrite.append(userID);//id
            towrite.append(",'");
            towrite.append(time_list.get(0));//String
            time_list.remove(0);
            towrite.append("','");
            towrite.append(data_list.get(0));
            data_list.remove(0);
            towrite.append("',");
            towrite.append(isCalli);
            towrite.append(")");
            content = towrite.toString();//"insert into monitoring values (1,'2020/09/20 05:20:01','123456',1)";
        }
        else if(flag==2) //자가진단테스트 보내기
        {
            String reporttime= bundle.getString("REPORTTIME");
            int[] stringdata = bundle.getIntArray("REPORTDATA");
            int mode = bundle.getInt("REPORTMODE");

            if(reporttime==null||stringdata==null){
                return false;
            }

            StringBuilder towrite = new StringBuilder();
            if(mode==2)
            {
                towrite.append("insert into user_pss values (");
                towrite.append(userID);//id
                towrite.append(",'");
                towrite.append(reporttime);//report time 문자열
                towrite.append("',");
                towrite.append(userAge);//report time 문자열
                towrite.append(",");
                towrite.append(userGender);//report time 문자열
                towrite.append(",");

                towrite.append(stringdata[0]);
                towrite.append(",");
                towrite.append(stringdata[1]);
                towrite.append(",");
                towrite.append(stringdata[2]);
                towrite.append(",");
                towrite.append(stringdata[3]);
                towrite.append(",");
                towrite.append(stringdata[4]);
            }
            else {
                towrite.append("insert into user_question values (");

                towrite.append(userID);//id
                towrite.append(",'");
                towrite.append(reporttime);//report time 문자열
                towrite.append("',");

                towrite.append(stringdata[0]);
                towrite.append(",");
                towrite.append(stringdata[1]);
                towrite.append(",");
                towrite.append(stringdata[2]);
                towrite.append(",");
                towrite.append(stringdata[3]);
                towrite.append(",");
                towrite.append(stringdata[4]);
            }

            towrite.append(")");
            content = towrite.toString();//"insert into monitoring values (1,'2020/09/20 05:20:01','123456',1)";
        }
        try {

            st.executeUpdate(content);
            respondDB("Reported");

            } catch (Exception e) {
//한번 에러나면 끊고 다시 들어가도록
                // TODO Auto-generated catch block
                e.printStackTrace();
                openDB = false;
                closeDB();
                ((MainActivity)MainActivity.context_main).changeDBicon(false);
                respondDB(  "데이터 베이스 write to question table 가 거부되었습니다.");
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
            NotificationChannel serviceChannel2 = new NotificationChannel(
                    CHANNEL_ID2,
                    "Survey Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationChannel serviceChannel3 = new NotificationChannel(
                    CHANNEL_ID3,
                    "Disconnection Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationChannel serviceChannel4 = new NotificationChannel(
                    CHANNEL_ID4,
                    "High Stress Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            notimanager.createNotificationChannel(serviceChannel);
            notimanager.createNotificationChannel(serviceChannel2);
            notimanager.createNotificationChannel(serviceChannel3);
            notimanager.createNotificationChannel(serviceChannel4);


        }
    }
}