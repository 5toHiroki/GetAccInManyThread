/*
研究用メイン
・加速度の取得
・スレッドの複製
・ログへの記録→SDへの保存

計測開始と終了時刻をSD内に書き込む
//ファイルに書き込むリソースを使い込むことで欠損を発生させる
//低消費電力モードにするとどうか？
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
import android.support.v7.widget.ThemedSpinnerAdapter;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import jp.sozolab.agoto.manythread.R;

public class MainActivity extends AppCompatActivity //メインスレッド開始
        implements SensorEventListener {

    private SensorManager sensorManager;//
    private long lastTime;//時間計測
    private File file;
    boolean isActiveSensor;//センサ値取得の切り替え
    private List<Thread> threads;//スレッド管理用
    private int count = 0;//加速度の書き込み回数

    /*======負荷有り；true 負荷なし；false======*/
    private boolean makethread = false;

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
        threads = null;
        isActiveSensor = false;
        super.onCreate(savedInstanceState);//
        setContentView(R.layout.activity_main);// R.layout　resのlayout
        lastTime = System.nanoTime();//時間計測
        file = new File(getFileName());//加速度用のSDのfile名

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        CompoundButton buttonRead = (CompoundButton) findViewById(R.id.toggleButton);
        buttonRead.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isButtonActive) {
                File oldFile = file;
                file = new File(getFileName());
                isActiveSensor = isButtonActive;
                TextView textView = (TextView) findViewById(R.id.text_info);
                if (isButtonActive == true) {
                    textView.setText("Now collecting");

                    //負荷スレッド作成
                    threads = new ArrayList<Thread>();
                    if(makethread){
                        for (int i = 8; i != 0; --i) {
                            Thread t = new Thread(new MyThread(i));
                            t.start();
                            threads.add(t);
                        }
                    }
                } else {
                    threads = null;
                    textView.setText("Not collect");

                    //データ終了時間をファイルに書き込み
                    try (FileOutputStream fileOutputStream = new FileOutputStream(oldFile, true);
                         OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
                         BufferedWriter bw = new BufferedWriter(outputStreamWriter);) {
                        bw.write(getFileName() + "\n");
                        bw.flush();
                        Log.d("thread", "finished writing endtime" );// デバッグ
                        bw.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("thread", "don't finish write endtime" );// デバッグ
                    }
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

        //sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);//?
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
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
                float sensorX = event.values[0];
                float sensorY = event.values[1];
                float sensorZ = event.values[2];

                try (FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                     OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
                     BufferedWriter bw = new BufferedWriter(outputStreamWriter);
                ) {
                    long nowTime = System.nanoTime();
                    long distance = nowTime - lastTime;
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
                    lastTime = nowTime;
                } catch (Exception e) {
                    e.printStackTrace();
//デバッグ                    Log.d("thread", "can not write" );
                }
                count++;
                Log.d("number of writing acc", String.valueOf(count));
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    } //中身なし

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

    //負荷スレッド
    class MyThread implements Runnable{
        private File file;
        private String threadName;

        MyThread(int nameNumber) {
            this.file = new File(Environment.getExternalStorageDirectory().getPath() +
                    "/" + "DICOMO_Acc_data" + "/" + "trush" +
                    "/" + String.valueOf(nameNumber));
            this.threadName = String.valueOf(nameNumber);
        }

        @Override
        public void run() {
            for (long i = 0; isActiveSensor; ++i) {
                try (FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                     OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
                     BufferedWriter bw = new BufferedWriter(outputStreamWriter);
                ) {
                    bw.write(threadName + " : " + i);
                    bw.flush();
                    bw.close();
                    Log.d("MyThread", "threadname:" + threadName + " 実行回数: " + i);// デバッグ
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}   //メインスレッド終了

