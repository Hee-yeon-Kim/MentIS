package com.imslab.mentis;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;
import  com.imslab.mentis.ForegroundService.MyBinder;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.empatica.empalink.EmpaDeviceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements EmpaDataDelegate, EmpaStatusDelegate {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private static final String myService="0000fff0-0000-1000-8000-00805f9b34fb";
    private static final String myCharacteristic="0000fff4-0000-1000-8000-00805f9b34fb";
    private static final String EMPATICA_API_KEY = "024adfd621aa40aebfac89310e607e61"; // TODO insert your API Key here

    private long currentMillis=0;

    ForegroundService serviceClass;
    boolean isService = false;

    ImageView bgapp,img_blue;
    Switch ecgswitch, e4switch;
    LinearLayout splashtext, firstmain;
    Animation frombottom;
    Button goUnity,calli;
    TextView e4connected;
    TextView bletitle;
    ListView blelist;
    AlertDialog bleDialog;
    AlertDialog e4Dialog;


    private BleDevice mybleDevice;
    private EmpaDeviceManager deviceManager = null;

    private DeviceAdapter mDeviceAdapter;


    private TextView batteryLabel;

    private TextView E4statusLabel;

    private static Handler mHandler ;

    String user_name="no_name";

    private ImageView ecg_loading;
    private ProgressDialog progressDialog;
    private Animation operatingAnim;

    private List<Float> u_bvp_list;
    private List<Float> u_temp_list;
    private List<Float> u_eda_list;

    private ArrayList<Integer> u_ecg_list;
    private  ArrayList<Integer> u_accx_list;
    private  ArrayList<Integer> u_accy_list;
    private  ArrayList<Integer> u_accz_list;

    private List<Float> bvp_list;
    private List<Float> temp_list;
    private List<Float> eda_list;

    private boolean state1=false;
    private boolean state0=false;

    private String devicename="";
    private boolean isECGStart = false;
    private ImageView e4_loading;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            MyBinder mb = (MyBinder) iBinder;
            serviceClass = mb.getService(); // 서비스가 제공하는 메소드 호출하여 서비스쪽 객체를 전달받을수 있다.
            isService = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler() ;
        u_bvp_list= new ArrayList<>();
        u_temp_list= new ArrayList<>();
        u_eda_list= new ArrayList<>();
        u_ecg_list = new ArrayList<>();
        u_accx_list = new ArrayList<>();
        u_accy_list = new ArrayList<>();
        u_accz_list = new ArrayList<>();


        bvp_list= new ArrayList<>();
        temp_list= new ArrayList<>();
        eda_list= new ArrayList<>();

        UImanager();


        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction("com.ims.empalink.sendintent");

        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);


        ecgswitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ecgswitch.isChecked())
                {
                    checkPermissions();
                    OnBLE();
                }
                else
                {
                    OffBLE();
                }
            }

        });
        e4switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(e4switch.isChecked())
                {
                    //e4 toggle on
                    OnE4();
                    initEmpaticaDeviceManager();


                }
                else {
                    //e4 toggle off
                    if (deviceManager != null) {
                        deviceManager.disconnect();
                    }
                    OffE4();

                }
            }

        });
        goUnity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MiddleActivity.class);
                startActivity(intent);
            }
        });
        calli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, callibrationView.class);
                startActivity(intent);
            }
        });

        mDeviceAdapter = new DeviceAdapter(this);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    connect(bleDevice);
                    mybleDevice = bleDevice;
                    updateLabel(bletitle,"연결완료-정보를 요청해주세요");


                }
            }

            @Override
            public void onDisConnect(final BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    bvp_list.clear();
                    eda_list.clear();
                    temp_list.clear();

                    serviceClass.isECGStart=false;
                }
                OffBLE();

            }

            @Override
            public void onDetail(BleDevice bleDevice) {

                if (BleManager.getInstance().isConnected(bleDevice)) {
                    progressDialog.setMessage("정보 요청 중...");
                    progressDialog.show();

                    blenotify(bleDevice);
                }
            }
        });
        makeBleDialog();
        makeE4Dialog();


        OffE4();
        OffBLE();

        Intent intent = new Intent(
                MainActivity.this, // 현재 화면
                ForegroundService.class); // 다음넘어갈 컴퍼넌트

        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);

        // 브로드 캐스트 1000ms 씩 가장 최근 것
        NewRunnable nr = new NewRunnable() ;
        Thread t = new Thread(nr) ;
        t.start() ;

    }
    private void UImanager()
    {

        frombottom= AnimationUtils.loadAnimation(this,R.anim.frombottom);
        splashtext = (LinearLayout) findViewById(R.id.splashtext);
       // hometext = (LinearLayout) findViewById(R.id.hometext);
        firstmain = (LinearLayout) findViewById(R.id.firstmain);
        bgapp=(ImageView) findViewById(R.id.bgapp);
        ecgswitch = (Switch) findViewById(R.id.ecgswitch);
        e4switch = (Switch) findViewById(R.id.e4switch);
        goUnity = (Button) findViewById((R.id.goUnity));
        calli = (Button) findViewById(R.id.calli);
        //

        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        progressDialog = new ProgressDialog(this);


        //메인창을 보이기 위한 애니메이션
        bgapp.animate().translationY(-1700).setDuration(800).setStartDelay(300);
        splashtext.animate().translationY(140).alpha(0).setDuration(800).setStartDelay(300);
        firstmain.startAnimation(frombottom);
    }
    // 핸들러로 전달할 runnable 객체. 수신 스레드 실행.
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {

            Intent sendIntent = new Intent();

            // We add flags for example to work from background
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_FROM_BACKGROUND|Intent.FLAG_INCLUDE_STOPPED_PACKAGES  );

            // SetAction uses a string which is an important name as it identifies the sender of the itent and that we will give to the receiver to know what to listen.
            // By convention, it's suggested to use the current package name com.MentlsCompany.MentlsTest
            sendIntent.setAction("com.ims.empalink.sendintent");

            Bundle extras= new Bundle();
            extras.putString("USERNAME",user_name);
            extras.putString("DEVICENAME",devicename);
            boolean state_data [] ={isECGStart, state0,state1};
            extras.putBooleanArray("STATE",state_data);

            if(isECGStart && state0)
            {
                int ecg_count =u_ecg_list.size();
                if(ecg_count<=0)
                {
                    int data1[] = {0};
                    extras.putIntArray("ECG", data1);//0:bva 1:eda 2:temp
                }
                else
                {
                    int data1[] = new int[ecg_count];
                    for (int c =0; c<ecg_count ; c++)
                    {
                        data1[c] = (u_ecg_list.get(0)!= null ? u_ecg_list.get(0):0); // Or whatever default you want.
                        u_ecg_list.remove(0);
                    }
                    extras.putIntArray("ECG", data1);//0:bva 1:eda 2:temp
                }

                int accx_count =u_accx_list.size();
                if(accx_count<=0)
                {
                    int data1[] = {0};
                    extras.putIntArray("ACCX", data1);//0:bva 1:eda 2:temp
                }
                else
                {
                    int data1[] = new int[accx_count];
                    for (int c =0; c<accx_count ; c++)
                    {
                        data1[c] = (u_accx_list.get(0)!= null ? u_accx_list.get(0):0); // Or whatever default you want.
                        u_accx_list.remove(0);
                    }
                    extras.putIntArray("ACCX", data1);//0:bva 1:eda 2:temp
                }

                int accy_count =u_accy_list.size();
                if(accy_count<=0)
                {
                    int data1[] = {0};
                    extras.putIntArray("ACCY", data1);//0:bva 1:eda 2:temp
                }
                else
                {
                    int data1[] = new int[accy_count];
                    for (int c =0; c<accy_count ; c++)
                    {
                        data1[c] = (u_accy_list.get(0)!= null ? u_accy_list.get(0):0); // Or whatever default you want.
                        u_accy_list.remove(0);
                    }
                    extras.putIntArray("ACCY", data1);//0:bva 1:eda 2:temp
                }

                int accz_count =u_accz_list.size();
                if(accz_count<=0)
                {
                    int data1[] = {0};
                    extras.putIntArray("ACCZ", data1);//0:bva 1:eda 2:temp
                }
                else
                {
                    int data1[] = new int[accz_count];
                    for (int c =0; c<accz_count ; c++)
                    {
                        data1[c] = (u_accz_list.get(0)!= null ? u_accz_list.get(0):0); // Or whatever default you want.
                        u_accz_list.remove(0);
                    }
                    extras.putIntArray("ACCZ", data1);//0:bva 1:eda 2:temp
                }




                int bvp_count =u_bvp_list.size();
                if(bvp_count<=0)
                {
                    float data1[] = {0};
                    extras.putFloatArray("BVP",data1);//0:bva 1:eda 2:temp
                }
                else
                {
                    float data1[] = new float[bvp_count];
                    for (int c =0; c<bvp_count ; c++)
                    {
                        data1[c] = (u_bvp_list.get(0)!= null ? u_bvp_list.get(0): Float.NaN); // Or whatever default you want.
                        u_bvp_list.remove(0);
                    }
                    extras.putFloatArray("BVP",data1);//0:bva 1:eda 2:temp
                }


                int eda_count =u_eda_list.size();
                if(eda_count<=0)
                {
                    float data2[]= {0};
                    extras.putFloatArray("EDA",data2);//0:bva 1:eda 2:temp

                }
                else
                {
                    float data2[] = new float[eda_count];
                    for (int c =0; c<eda_count ; c++)
                    {
                        data2[c] = (u_eda_list.get(0)!= null ? u_eda_list.get(0): Float.NaN); // Or whatever default you want.
                        u_eda_list.remove(0);
                    }
                    extras.putFloatArray("EDA",data2);//0:bva 1:eda 2:temp
                }



                int temp_count =u_temp_list.size();
                if(temp_count<=0)
                {
                    float data3[]= {0};
                    extras.putFloatArray("TEMP",data3);//0:bva 1:eda 2:temp
                }
                else
                {
                    float data3[] = new float[temp_count];
                    for (int c =0; c<temp_count ; c++)
                    {
                        data3[c] = (u_temp_list.get(0)!= null ? u_temp_list.get(0): Float.NaN); // Or whatever default you want.
                        u_temp_list.remove(0);
                    }
                    extras.putFloatArray("TEMP",data3);//0:bva 1:eda 2:temp
                }

            }


            sendIntent.putExtras(extras);
            // Here we fill the Intent with our data, here just a string with an incremented number in it.
            // sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
            // And here it goes ! our message is send to any other app that want to listen to it.
            sendBroadcast(sendIntent);
            //  Log.d("test",msg);
            // In our case we run this method each second with postDelayed
            // mHandler.removeCallbacks(this);
            //mHandler.postDelayed(this, 1000);
        }
    } ;

    class NewRunnable implements Runnable {
        @Override
        public void run()
        {
            while (true)
            {

                mHandler.post(runnable);

                try
                {
                    Thread.sleep(1000);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }


    private void showConnectedDevice() {
        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        mDeviceAdapter.clearConnectedDevice();
        for (BleDevice bleDevice : deviceList) {
            mDeviceAdapter.addDevice(bleDevice);
        }
        mDeviceAdapter.notifyDataSetChanged();
    }
    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
                if(ecg_loading!=null) {
                    ecg_loading.startAnimation(operatingAnim);
                    ecg_loading.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                if(ecg_loading!=null) {
                    ecg_loading.clearAnimation();
                    ecg_loading.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.setMessage("ECG PATCH 연결 중...");
                progressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                if(ecg_loading!=null) {
                    ecg_loading.clearAnimation();
                    ecg_loading.setVisibility(View.INVISIBLE);
                }
                progressDialog.dismiss();

                myToast("ECG Patch 연결을 다시 시도해주세요.");
                isECGStart = false;
                serviceClass.isECGStart=false;
                OffBLE();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();
                //mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.setDevice(bleDevice);
                setMtu(bleDevice,512);
                mDeviceAdapter.notifyDataSetChanged();
                //Toast.makeText(MainActivity.this, "연결됨", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();
                isECGStart = false;
                serviceClass.isECGStart = false;

                bvp_list.clear();
                eda_list.clear();
                temp_list.clear();
                OffBLE();
                 if (isActiveDisConnected) {
                     myToast(getString(R.string.active_disconnected));
                } else {
                     myToast(getString(R.string.disconnected));
                     ObserverManager.getInstance().notifyObserver(bleDevice);
                }
            }
        });
    }

    private void makeBleDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.blealert, null);
        ecg_loading = (ImageView) view.findViewById(R.id.ecg_loading);
        bletitle = (TextView) view.findViewById(R.id.dialog_title);

        builder.setView(view);

        blelist = (ListView)view.findViewById(R.id.list_device2);
        bleDialog = builder.create();


        blelist.setAdapter(mDeviceAdapter);

        bleDialog.setCancelable(true);
        ;
        bleDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


    }
    private void makeE4Dialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.e4alert, null);
        e4_loading = (ImageView) view.findViewById(R.id.ecg_loading);

        builder.setView(view);

        img_blue=   (ImageView) view.findViewById(R.id.img_blue);
        E4statusLabel = (TextView) view.findViewById(R.id.e4_name);
        e4connected= (TextView) view.findViewById((R.id.e4connected));

        e4Dialog = builder.create();

        e4Dialog.setCancelable(true);

        e4Dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }

    private void blenotify(final BleDevice bleDevice)
    {
        BleManager.getInstance().notify(
                bleDevice,
                myService,
                myCharacteristic,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {

                        blewrite(bleDevice);
                    }


                    @Override
                    public void onNotifyFailure(BleException exception) {
                        progressDialog.dismiss();
                        myToast("ECG PATCH의 연결을 다시 시도해주세요.");
                        mybleDevice = null;
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] value) {

                        StringBuilder sb = new StringBuilder();

                        if(!isECGStart) {
                            isECGStart = true;
                            serviceClass.isECGStart = true;
                            currentMillis= System.currentTimeMillis();
                            myToast("ECG PATCH의 연결이 완료되었습니다.");
                            updateLabel(bletitle,"정보요청이 완료되었습니다.");

                            bleDialog.dismiss();
                            progressDialog.dismiss();
                            mybleDevice = bleDevice;
                            if(state0)
                            {
                                //updateLabel(readytext,"센서연결이 완료되었습니다.");
                            }
                        }
                        if(value.length!=237||!state0||bvp_list.size()==0)
                        {
                            currentMillis= System.currentTimeMillis();
                            return;
                        }

                        // 현재시간을 msec 으로 구한다.
                        // 현재시간을 date 변수에 저장한다.
                        Date date = new Date(currentMillis);
                        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                        // nowDate 변수에 값을 저장한다.
                        String formatDate = sdfNow.format(date);
                        serviceClass.time_list.add(formatDate);
                        currentMillis+=1000;

                        int buf = 0;
                        int pre_buf = value[0];

                        for(int c=0;c<128; c++)
                        {
                            int level = 0;
                            buf = (int) value[c];
                            if(Math.abs(buf-pre_buf)>128)
                            {
                                if (buf > pre_buf) level--;
                                else level++;
                            }
                            pre_buf = buf;
                            buf = (int)(buf + level * 255);
                            u_ecg_list.add(buf);
                            sb.append(buf);
                            if(c==127) sb.append("/");
                            else sb.append(",");
                        }
                        int index=0;
                        for (int c = 0; c < 21; c++)
                        {
                            int acc1 = (value[5 * c + 128]);
                            acc1= (int)((acc1 << 2) + (value[5 * c + 128 + 1] >> 6));
                            int acc2 = (int)(value[5 * c + 128 + 1] & 0b00111111);
                            acc2= (int)(acc2+ (value[5 * c + 128 + 2 ] >> 4));
                            int acc3 = (int)(value[5 * c + 128 + 2] & 0b00001111);
                            acc3 = (int)(acc3 + (value[5 * c + 128 + 3] >> 2));
                            int acc4 = (int)(value[5 * c + 128 + 3] & 0b00000011);
                            acc4 = (int)(acc4+ (value[5 * c + 128 + 4] >> 0));

                            if (acc1 > 511) acc1 = acc1 - 1023;
                            if (acc2 > 511) acc2 = acc2 - 1023;
                            if (acc3 > 511) acc3 = acc3 - 1023;
                            if (acc4 > 511) acc4 = acc4 - 1023;

                            if(index%3==0)
                            {
                                u_accx_list.add(acc1);
                                u_accy_list.add(acc2);
                                u_accz_list.add(acc3);
                                u_accx_list.add(acc4);
                                index+=4;
                            }
                            else if( index%3==1)
                            {
                                u_accy_list.add(acc1);
                                u_accz_list.add(acc2);
                                u_accx_list.add(acc3);
                                u_accy_list.add(acc4);
                                index+=4;
                            }
                            else
                            {
                                u_accz_list.add(acc1);
                                u_accx_list.add(acc2);
                                u_accy_list.add(acc3);
                                u_accz_list.add(acc4);
                                index+=4;
                            }

                            sb.append(acc1);
                            sb.append(",");
                            sb.append(acc2);
                            sb.append(",");
                            sb.append(acc3);
                            sb.append(",");
                            sb.append(acc4);
                            if(c==20)sb.append("/");
                            else sb.append(",");

                        }
                        int bvpcount = bvp_list.size();
                        for( int c=0; c<bvpcount ;c++)
                        {
                            sb.append(bvp_list.get(0));
                            bvp_list.remove(0);
                            if(c==bvpcount-1) sb.append("/");
                            else sb.append(",");
                        }
                        int edacount = eda_list.size();
                        for( int c=0; c<edacount;c++)
                        {
                            sb.append(eda_list.get(0));
                            eda_list.remove(0);
                            if(c==edacount-1) sb.append("/");
                            else sb.append(",");
                        }
                        int tempcount = temp_list.size();
                        for( int c=0; c<tempcount;c++)
                        {
                            sb.append(temp_list.get(0));
                            temp_list.remove(0);
                            if(c==tempcount-1) sb.append("/");
                            else sb.append(",");
                        }
                        serviceClass.data_list.add(sb.toString());
                    }
                });
    }

    void stopNotify(BleDevice bleDevice)
    {
        BleManager.getInstance().stopNotify(bleDevice,myService,myCharacteristic);

    }

    void blewrite(BleDevice bleDevice)
    {

        String hex = "67";
        BleManager.getInstance().write(
                bleDevice,
                myService,
                myCharacteristic,
                HexUtil.hexStringToBytes(hex),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {

                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        OffBLE();
                        myToast("ECG Patch 정보 요청 실패, 다시 시도해주세요");
                        BleManager.getInstance().disconnectAllDevice();
                        mybleDevice = null;
                    }
                });
    }

    private void setMtu(BleDevice bleDevice, int mtu) {
        BleManager.getInstance().setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {
                Log.i("tag", "onsetMTUFailure" + exception.toString());
            }

            @Override
            public void onMtuChanged(int mtu) {
                Log.i("tag", "onMtuChanged: " + mtu);
            }
        });
    }

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            myToast("핸드폰의 블루투스기능을 켜주세요");
            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new android.app.AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {

                    startScan();
                }
                break;
        }
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, yay!
                    initEmpaticaDeviceManager();
                } else {
                    // Permission denied, boo!
                    final boolean needRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Without this permission bluetooth low energy devices cannot be found, allow it in order to connect to the device.")
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // try again
                                    if (needRationale) {
                                        // the "never ask again" flash is not set, try again with permission request
                                        initEmpaticaDeviceManager();
                                    } else {
                                        // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // without permission exit is the only way
                                    finish();
                                }
                            })
                            .show();
                }
                break;
        }
    }

    private void initEmpaticaDeviceManager() {
        // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        } else {

            if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Please insert your API KEY")
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // without permission exit is the only way
                                finish();
                            }
                        })
                        .show();
                return;
            }

            // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
            deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);

            // Initialize the Device Manager using your API key. You need to have Internet access at this point.
            deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        showConnectedDevice();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (deviceManager != null) {
            deviceManager.stopScanning();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceManager != null) {
            deviceManager.cleanUp();
        }
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
        unbindService(serviceConnection);
    }

    @Override
    public void didDiscoverDevice(EmpaticaDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php
        if (allowed) {

            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning();
            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);
                // updateLabel(deviceNameLabel, "To: " + deviceName);
                this.devicename=deviceName;
                serviceClass.state0=false;
                state0=false;
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                myToast("E4 Band 연결에 실패하였습니다.");
                serviceClass.state0=false;//실패
                state0= false;
                OffE4();

            }
        }
    }

    @Override
    public void didFailedScanning(int errorCode) {

    }

    @Override
    public void didRequestEnableBluetooth() {
        // Request the user to enable Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void bluetoothStateChanged() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // You should deal with this
            startScan();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void didUpdateSensorStatus(@EmpaSensorStatus int status, EmpaSensorType type) {

        didUpdateOnWristStatus(status);
    }

    @Override
    public void didUpdateStatus(EmpaStatus status) {
        // Update the UI

        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            updateLabel(E4statusLabel, status.name() + " - 기기를 켜주세요.");
            // Start scanning
            serviceClass.state0=false; //대기 및 스캔 중
            state0=false;
            deviceManager.startScanning();
            // The device manager has established a connection

        } else if (status == EmpaStatus.CONNECTED) {

            serviceClass.state0=true;///연결함
            state0= true;

            updateLabel(E4statusLabel, devicename);

            ConnectedE4();
            if(isECGStart)
            {
               // updateLabel(readytext,"센서연결이 완료되었습니다.");

            }
            // The device manager disconnected from a device
        } else if (status == EmpaStatus.DISCONNECTED) {//e4 disconnected

            myToast("E4 Band 연결이 해제되었습니다.");

            OffE4();

            serviceClass.state0=false;//연결끝
            state0= false;
            bvp_list.clear();
            eda_list.clear();
            temp_list.clear();
        }
    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
//        updateLabel(accel_xLabel, "" + x);
//        updateLabel(accel_yLabel, "" + y);
//        updateLabel(accel_zLabel, "" + z);
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {

        if(!state0)
        {
            serviceClass.state0=true;///연결함
            state0= true;
            ConnectedE4();

        }
//        updateLabel(bvpLabel, "" + bvp);
        if(isECGStart) {
            bvp_list.add(bvp);
            u_bvp_list.add(bvp);
        }
    }

    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {
        //updateLabel(batteryLabel, String.format("%.0f %%", battery * 100));
    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        if(isECGStart)  {
            eda_list.add(gsr);
            u_eda_list.add(gsr);
        }
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
      //  updateLabel(ibiLabel, "" + ibi);
    }

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
        if(isECGStart) {
            temp_list.add(temp);
            u_temp_list.add(temp);
        }
    }


    // Update a label with some text, making sure this is run in the UI thread
    private void updateLabel(final TextView label, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label.setText(text);
            }
        });
    }

    @Override
    public void didReceiveTag(double timestamp) {

    }

    @Override
    public void didEstablishConnection() {

     }

    @Override
    public void didUpdateOnWristStatus(@EmpaSensorStatus final int status) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (status == EmpaSensorStatus.ON_WRIST) {

                    state1=true;
                }
                else {

                    state1=false;
                }
            }
        });
    }

    void  OnBLE() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
               if(!ecgswitch.isChecked()) ecgswitch.setChecked(true);
                bleDialog.show();
            }
        });
    }

    void OffBLE() {
        if(mybleDevice!=null)
        {
            BleManager.getInstance().cancelScan();
            if (BleManager.getInstance().isConnected(mybleDevice)) {
                BleManager.getInstance().disconnect(mybleDevice);
                mDeviceAdapter.removeDevice(mybleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }
            mDeviceAdapter.removeDevice(mybleDevice);
            mDeviceAdapter.notifyDataSetChanged();
            mybleDevice=null;
        }
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                bleDialog.dismiss();
                progressDialog.dismiss();
                ecgswitch.setChecked(false);
             //   updateLabel( readytext, "센서를 연결해주세요.");

            }
        });
    }
    void OnE4()
    {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if(e4_loading!=null) {
                    e4_loading.startAnimation(operatingAnim);
                    e4_loading.setVisibility(View.VISIBLE);
                }
                e4connected.setVisibility(View.INVISIBLE);
                e4Dialog.show();
                e4switch.setChecked(true);
            }
        });
    }

    void OffE4()
    {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                e4_loading.clearAnimation();
                e4_loading.setVisibility(View.INVISIBLE);

                e4connected.setVisibility(View.INVISIBLE);
                E4statusLabel.setTextColor(getColor(R.color.colorGrayt));
                img_blue.setImageResource(R.mipmap.ic_blue_remote);
                e4Dialog.dismiss();

                if(e4switch.isChecked()) e4switch.setChecked(false);
              //  updateLabel( readytext, "센서를 연결해주세요.");

            }
        });
    }

    void myToast(final String string)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();

            }
        });
    }
    void ConnectedE4() {
//연결되었을 때
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(!e4switch.isChecked()) e4switch.setChecked(true);
                if(e4_loading!=null) {
                    e4_loading.clearAnimation();
                    e4_loading.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "E4 Band 연결이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                }

                e4connected.setVisibility(View.VISIBLE);
                E4statusLabel.setTextColor(getColor(R.color.colorPrimary));
                img_blue.setImageResource(R.mipmap.ic_blue_connected);

            }
        });
    }



}
