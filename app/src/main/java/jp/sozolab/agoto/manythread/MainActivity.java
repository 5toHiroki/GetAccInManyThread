/*
研究用メイン
・加速度の取得
・スレッドの複製
・ログへの記録→SDへの保存
*/
package jp.sozolab.agoto.manythread;//adress

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;

import jp.sozolab.agoto.manythread.R;

public class MainActivity extends AppCompatActivity //メインスレッド開始
        implements SensorEventListener {

    private SensorManager sensorManager;//
//    private TextView textInfo, textView;//
    private long lastTime;//時間計測
    private File file;
    private boolean isActiveSensor;

    private String getFileName() {
        final Calendar calendar = Calendar.getInstance();

        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        final int second = calendar.get(Calendar.SECOND);
        final int ms = calendar.get(Calendar.MILLISECOND);

        return Environment.getExternalStorageDirectory().getPath() +
                "/" + getString(R.string.dirname) +
                "/" + year + "-" + (month + 1) + "-" + day + "-" + hour + "-" + minute + "-" + second
                + "_" + getString(R.string.filename);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isActiveSensor = false;
        super.onCreate(savedInstanceState);//
        setContentView(R.layout.activity_main);// R.layout　resのlayout
        lastTime = System.nanoTime();//時間計測
        file = new File(getFileName());//SD用のfile名

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        textInfo = (TextView) findViewById(R.id.text_info);
        // Get an instance of the TextView
//        textView = (TextView) findViewById(R.id.text_view);

        //スレッド　インスタンス作成
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {//繰り返す
//                    Log.d("Thread", "aho");//logに表示する
//                }
//            }
//        }).start();

//        for (int i = 0;i < 10; ++i){
//            new MyThread(i).start();
//        }

        CompoundButton buttonRead = (CompoundButton) findViewById(R.id.toggleButton);
        buttonRead.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isButtonActive) {
//                getFileName();
                file = new File(getFileName());
                isActiveSensor = isButtonActive;
                TextView textView = (TextView) findViewById(R.id.text_info);
                if (isButtonActive == true) {
                    textView.setText("Now collecting");

                } else{
                    textView.setText("Not collect");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Listenerの登録
        Sensor accel = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);//?
        //sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        //sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI);
    }

    // 解除するコードも入れる!
    @Override
    protected void onPause() {
        super.onPause();
        // Listenerを解除
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isActiveSensor) {
            //センサ値の定義
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long nowTime = System.nanoTime();
                float sensorX = event.values[0];
                float sensorY = event.values[1];
                float sensorZ = event.values[2];

                long distance = nowTime - lastTime;

                try (FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                     OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
                     BufferedWriter bw = new BufferedWriter(outputStreamWriter);
                ) {
                    bw.write(String.valueOf(distance));
                    bw.write(',');
                    bw.write(String.valueOf(sensorX));
                    bw.write(',');
                    bw.write(String.valueOf(sensorY));
                    bw.write(',');
                    bw.write(String.valueOf(sensorZ));
                    bw.write("\n");
                    bw.flush();
//デバッグ                    Log.d("thread", "can write" );
                    bw.close();
                } catch (Exception e) {
                    e.printStackTrace();
//デバッグ                    Log.d("thread", "can not write" );
                }

                lastTime = nowTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    } //中身なし

//ファイルに書き込むリソースを使い込むことで欠損を発生させる
//低消費電力モードにするとどうか？

    // （お好みで）加速度センサーの各種情報を表示
   private void showInfo(SensorEvent event){
//        // センサー名
//        StringBuffer info = new StringBuffer("Name: ");
//        info.append(event.sensor.getName());
//        info.append("\n");
//
//        // ベンダー名
//        info.append("Vendor: ");
//        info.append(event.sensor.getVendor());
//        info.append("\n");
//
//        // 型番
//        info.append("Type: ");
//        info.append(event.sensor.getType());
//        info.append("\n");
//
//        // 最小遅れ
//        int data = event.sensor.getMinDelay();
//        info.append("Mindelay: ");
//        info.append(String.valueOf(data));
//        info.append(" usec\n");
//
//        // 最大遅れ
//        data = event.sensor.getMaxDelay();
//        info.append("Maxdelay: ");
//        info.append(String.valueOf(data));
//        info.append(" usec\n");
//
//        // レポートモード
//        data = event.sensor.getReportingMode();
//        String stinfo = "unknown";
//        if(data == 0){
//            stinfo = "REPORTING_MODE_CONTINUOUS";
//        }else if(data == 1){
//            stinfo = "REPORTING_MODE_ON_CHANGE";
//        }else if(data == 2){
//            stinfo = "REPORTING_MODE_ONE_SHOT";
//        }
//        info.append("ReportingMode: ");
//        info.append(stinfo);
//        info.append("\n");
//
//        // 最大レンジ
//        info.append("MaxRange: ");
//        float fData = event.sensor.getMaximumRange();
//        info.append(String.valueOf(fData));
//        info.append("\n");
//
//        // 分解能
//        info.append("Resolution: ");
//        fData = event.sensor.getResolution();
//        info.append(String.valueOf(fData));
//        info.append(" m/s^2\n");
//
//        // 消費電流
//        info.append("Power: ");
//        fData = event.sensor.getPower();
//        info.append(String.valueOf(fData));
//        info.append(" mA\n");
//
//        textInfo.setText(info);
    } //中身なし

}   //メインスレッド終了

//class MyThread extends Thread{
//
//    public MyThread(int i){
//        this.i = i;
//    }
//    int i;

//    public void run(){
//        while (true) {
//            Log.d("Thread", ""+i);
//        }

//    }
//}