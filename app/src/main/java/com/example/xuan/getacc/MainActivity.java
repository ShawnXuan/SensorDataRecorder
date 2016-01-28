package com.example.xuan.getacc;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.os.Handler;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    Sensor accelerometer;
    Sensor Gyroscope;
    Sensor Orientation;
    SensorManager sm;
    TextView tvAcceleration;
    TextView tvGyroscope;
    TextView tvOrientation;

    //WifiManager wifi;

    int number;
    TextView rssi;
    Handler handler;
    boolean Running = true;

    String filePath = "/sdcard/Test/";
    String fileName = "test.txt";
    String StrAcceleration, StrGyroscope, StrOrientation, StrRSSI;
    long currentTimeMillis;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //initData();

        writeTxtToFile("AX, AY, AZ, GX, GY, GZ, Time", filePath, fileName);//, Azimuth, Pitch, Roll
        //WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);
        rssi=(TextView)findViewById(R.id.rssi);
        handler = new Handler();
        Runnable runnable = new Runnable(){
            @Override
            public void run(){
                while(Running){
                    try{
                        Thread.sleep(100);}
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    handler.post(new Runnable(){
                        @Override
                        public void run(){
                            //number+=1;
                            //rssi.setText(String.valueOf(number));
                            WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);
                            wifi.startScan();
                            List<ScanResult> sr = wifi.getScanResults();
                            String str="";
                            for(ScanResult r : sr){
                                str = str+"\n"+r.SSID+": "+r.level;
                            }
                            rssi.setText(str);
                            //StrRSSI = r.level;
                            currentTimeMillis = System.currentTimeMillis();
                            StrRSSI=StrAcceleration+StrGyroscope+currentTimeMillis;//StrOrientation+
                            writeTxtToFile(StrRSSI, filePath, fileName);
                        }
                    });
                }
            }
        };
        new Thread(runnable).start();
        sm=(SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        tvAcceleration=(TextView)findViewById(R.id.acceleration);

        Gyroscope=sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sm.registerListener(this, Gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        tvGyroscope=(TextView)findViewById(R.id.txtGyroscope);

        Orientation=sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sm.registerListener(this, Orientation, SensorManager.SENSOR_DELAY_NORMAL);
        tvOrientation=(TextView)findViewById(R.id.txtOrientation);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){
        //
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        if (event.sensor == null) {
            return;
        }
        //
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            tvAcceleration.setText("X: " + event.values[0] +
                    "\nY: " + event.values[1] +
                    "\nZ: " + event.values[2]);
            StrAcceleration=event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ", ";
        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            tvGyroscope.setText("X: " + event.values[0] +
                    "\nY: " + event.values[1] +
                    "\nZ: " + event.values[2]);
            StrGyroscope=event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ", ";
        }
        else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            tvOrientation.setText("Azimuth: " + event.values[0] +
                    "\nPitch: " + event.values[1] +
                    "\nRoll: " + event.values[2]);
            StrOrientation=event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ", ";
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*private void initData() {
        String filePath = "/sdcard/Test/";
        String fileName = "test.txt";

        writeTxtToFile("txt content", filePath, fileName);
    }*/

    // 将字符串写入到文本文件中
    public void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath+fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    // 生成文件
    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }
}
