package com.example.foothelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

public class Foot_pressure_detection extends AppCompatActivity {
    private TextView textView, textView_for, textView_mid, textView_after, textView_data;
    CountDownTimer timer;

    private BluetoothSocket bluetoothSocket;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice connectDevice = null;

    ImageButton btn_detect, btn_detected, btn_bluetooth, btn_warning, btn_upload;

    private UUID myUUID;

    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;

    boolean detect_state = false;
    boolean connect_state = false;

    MeasureValue measureValue = new MeasureValue();

    int[] foot_pressure = new int[3];

    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_foot_pressure_detection);

        findView();

        String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        btn_bluetooth.setOnClickListener(v -> {
            if(!textView.getText().toString().equals("連接中")&&!textView.getText().toString().equals("設備連接成功")){
                if(!connect_state){
                    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
                        Toast.makeText(Foot_pressure_detection.this,
                                "不支持藍芽功能", //FEATURE_BLUETOOTH NOT support
                                Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (bluetoothAdapter == null) {
                        Toast.makeText(Foot_pressure_detection.this,
                                "此平台不支援藍芽", //Bluetooth is not supported on this hardware platform
                                Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, 1);
                    }else{
                        setup();
                    }

                }else {
                    Toast.makeText(Foot_pressure_detection.this,
                            "已連接藍芽", //Bluetooth is not supported on this hardware platform
                            Toast.LENGTH_SHORT).show();
                }
            }

        });

        btn_warning.setOnClickListener(v -> {
            if(!connect_state){
                Toast.makeText(Foot_pressure_detection.this,
                        "請連接指定設備",
                        Toast.LENGTH_SHORT).show();
            }else {
                if (!detect_state){
                    Intent intent = new Intent(Foot_pressure_detection.this, Set_warning_value.class);
                    startActivityForResult(intent, 2);
                }else{
                    Toast.makeText(Foot_pressure_detection.this,
                            "正在進行偵測",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_detect.setOnClickListener(v -> {
            if(!connect_state){
                Toast.makeText(Foot_pressure_detection.this,
                        "尚未連接藍芽設備",
                        Toast.LENGTH_SHORT).show();
            }else {
                if(detect_state){
                    Toast.makeText(Foot_pressure_detection.this,
                            "您已進行偵測",
                            Toast.LENGTH_SHORT).show();
                }else {
                    if (textView_data.getVisibility() == View.VISIBLE) {
                        getDialog();
                    }else{
                        detect_state = true;
                        measureValue = new MeasureValue();
                    }
                }
            }
        });

        btn_detected.setOnClickListener(v -> {
            if (!detect_state){
                Toast.makeText(Foot_pressure_detection.this,
                        "您還未開始偵測",
                        Toast.LENGTH_SHORT).show();
            }else {
                textView_data.setVisibility(View.VISIBLE);

                textView_for.setText("0");
                textView_mid.setText("0");
                textView_after.setText("0");

                Toast.makeText(Foot_pressure_detection.this,
                        "已結束偵測",
                        Toast.LENGTH_SHORT).show();

                detect_state = false;
            }
        });

        btn_upload.setOnClickListener(v -> {
            if (textView_data.getVisibility() == View.VISIBLE){
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                measureValue.setTimestamp(Timestamp.now());
                db.collection("users/Brian/Measure").add(measureValue);

                Toast.makeText(Foot_pressure_detection.this, "上傳成功" ,Toast.LENGTH_SHORT).show();
                textView_data.setVisibility(View.INVISIBLE);
                measureValue = new MeasureValue();
            }else{
                Toast.makeText(Foot_pressure_detection.this, "您尚未有數據可上傳" ,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK )
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(Foot_pressure_detection.this);
            builder.setCancelable(false);
            //這邊是設定使用者可否點擊空白處返回
            //builder.setIcon();
            //setIcon可以在Title旁邊放一個小插圖

            builder.setTitle("確認");
            builder.setMessage("如您「確認」返回，數據、連接將都被清除。");
            //alterDialog最多可以放三個按鈕，且位置是固定的，分別是
            //setPositiveButton()右邊按鈕
            //setNegativeButton()中間按鈕
            //setNeutralButton()左邊按鈕

            builder.setNegativeButton("確認", (dialogInterface, i) -> {

                dialogInterface.dismiss();

                if(myThreadConnected != null){
                    myThreadConnected.cancel();
                }

                finish();
            });

            builder.setPositiveButton("取消", (dialogInterface, i) -> dialogInterface.dismiss());
            builder.create().show();
        }

        return false;
    }



    private void getDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Foot_pressure_detection.this);
        builder.setCancelable(false);
        //這邊是設定使用者可否點擊空白處返回
        //builder.setIcon();
        //setIcon可以在Title旁邊放一個小插圖
        builder.setTitle("確認");
        builder.setMessage("您已存在數據，若按下「確認」此數據將被清除，並開始進行偵測。");
        //alterDialog最多可以放三個按鈕，且位置是固定的，分別是
        //setPositiveButton()右邊按鈕
        //setNegativeButton()中間按鈕
        //setNeutralButton()左邊按鈕
        builder.setNegativeButton("確認", (dialogInterface, i) -> {
            detect_state = true;
            measureValue = new MeasureValue();
            textView_data.setVisibility(View.INVISIBLE);
            dialogInterface.dismiss();
        });
        builder.setPositiveButton("取消", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }

    @SuppressLint("SetTextI18n")
    @Override // 覆寫 onActivityResult，傳值回來時會執行此方法。
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1){
            if (resultCode == Activity.RESULT_OK) {
                setup();
            }else {
                Toast.makeText(Foot_pressure_detection.this, "藍芽授權失敗" ,Toast.LENGTH_SHORT).show();
                textView.setText("APP尚未與復健鞋做連接");
            }
        }else{
            if (resultCode == Activity.RESULT_OK) {

                Bundle argument;

                //將包裹從 Intent 中取出。
                if (data == null){
                    return;
                }else{
                    argument = data.getExtras();
                }

                //將回傳值用指定的 key 取出，並從整數轉為字串。
                if (argument != null)
                {
                    int warning_value = argument.getInt("returnValueName");
                    myThreadConnected.write(Integer.toString(warning_value).getBytes(StandardCharsets.UTF_8));
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void findView() {
        textView = findViewById(R.id.textView3);
        textView_for = findViewById(R.id.textView5);
        textView_mid = findViewById(R.id.textView6);
        textView_after = findViewById(R.id.textView7);
        textView_data = findViewById(R.id.textView8);
        btn_detect = findViewById(R.id.imageButton_detect);
        btn_detected = findViewById(R.id.imageButton_detected);
        btn_bluetooth = findViewById(R.id.imageButton1);
        btn_warning = findViewById(R.id.imageButton2);
        btn_upload = findViewById(R.id.imageButton3);
    }

    //Called in ThreadConnectBTdevice once connect successed to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket){
        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }

    @SuppressLint("SetTextI18n")
    private void setup() {
        textView.setText("連接中");
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {


            for (BluetoothDevice device : pairedDevices) {
                if(device.getName().equals("six")) {
                    connectDevice = device;
                }
            }

            if(connectDevice == null){
                runOnUiThread(() -> {
                    Toast.makeText(Foot_pressure_detection.this,
                            "設備尚未進行配對", //Connection lost
                            Toast.LENGTH_SHORT).show();
                    textView.setText("APP尚未與復健鞋做連接");
                });
                return;
            }

            timer = new CountDownTimer(5000, 1000) {
                boolean connect = false;

                @Override
                public void onTick(long millisUntilFinished) {
                    if(!connect){
                        connect = true;

                        myThreadConnectBTdevice = new ThreadConnectBTdevice(connectDevice);
                        myThreadConnectBTdevice.start();
                    }
                }
                @Override
                public void onFinish() {
                    if(!bluetoothSocket.isConnected()){
                        runOnUiThread(() -> {
                            Toast.makeText(Foot_pressure_detection.this,
                                    "連線逾時", //Connection lost
                                    Toast.LENGTH_SHORT).show();
                            textView.setText("APP尚未與復健鞋做連接");
                            try {
                                bluetoothSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            myThreadConnectBTdevice = null;
                        });
                    }
                }
            };

            timer.start();

        }
    }


    /*
    ThreadConnectBTdevice: Background Thread to handle BlueTooth connecting
    */
    private class ThreadConnectBTdevice extends Thread {

        private ThreadConnectBTdevice(BluetoothDevice device) {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
                Log.e("ThreadConnectBTD Error", e.getMessage());
                e.printStackTrace();
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                bluetoothSocket.connect();
                connect_state = true;
            } catch (IOException e) {
                e.printStackTrace();

                final String eMessage = e.getMessage();
                runOnUiThread(() -> {
                    textView.setText("APP尚未與復健鞋做連接");

                    Log.e("ConnectError", eMessage);
                });

                cancel();
                timer.cancel();
            }

            if(connect_state){
                //connect successful

                runOnUiThread(() -> textView.setText("設備連接成功"));

                startThreadConnected(bluetoothSocket);

            }else{
                //connect fail
                runOnUiThread(() -> {
                    Toast.makeText(Foot_pressure_detection.this,
                            "設備連接失敗", //Connection lost
                            Toast.LENGTH_SHORT).show();
                    textView.setText("APP尚未與復健鞋做連接");
                });

                cancel();
            }
        }

        @SuppressLint("SetTextI18n")
        public void cancel() {
            runOnUiThread(() -> textView.setText("APP尚未與復健鞋做連接"));

            try {
                connect_state = false;
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e("Cancel Error",e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /*
    ThreadConnected:
    Background Thread to handle Bluetooth data communication after connected
     */
    private class ThreadConnected extends Thread {
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;
        private final BluetoothSocket connectedBluetoothSocket;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("ThreadConnected Error",e.getMessage());
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            boolean state = true;

            while (state) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    final String strReceived = new String(buffer, 0, bytes);

                    runOnUiThread(() -> {
                        if(detect_state){
                            String[] foot_pressure_str = strReceived.split(",");

                            for(int i=0;i<=2;i++){
                                try{
                                    foot_pressure[i] = Integer.parseInt(foot_pressure_str[i]);
                                }catch (NumberFormatException e){
                                    i+=3;
                                    e.printStackTrace();
                                    Toast.makeText(Foot_pressure_detection.this, "傳入數值有誤，請檢查設備", Toast.LENGTH_SHORT).show();;
                                    cancel();
                                }
                            }

                            textView_for.setText(Integer.toString(foot_pressure[2]));
                            textView_mid.setText(Integer.toString(foot_pressure[1]));
                            textView_after.setText(Integer.toString(foot_pressure[0]));

                            measureValue.add_Forefoot_pressure(foot_pressure[2]);
                            measureValue.add_Midfoot_pressure(foot_pressure[1]);
                            measureValue.add_Hindfoot_pressure(foot_pressure[0]);
                        }
                    });

                } catch (IOException e) {
                    Log.e("connectedIS Error",e.getMessage());
                    e.printStackTrace();

                    state = false;

                    runOnUiThread(() -> {
                        Toast.makeText(Foot_pressure_detection.this,
                                "連接已中斷", //Connection lost
                                Toast.LENGTH_SHORT).show();

                        textView.setText("APP尚未與復健鞋做連接");
                    });

                    cancel();
                }
            }


        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                Log.e("Write Error",e.getMessage());
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
                connect_state = false;

                runOnUiThread(() -> {
                    textView_data.setVisibility(View.VISIBLE);

                    textView_for.setText("0");
                    textView_mid.setText("0");
                    textView_after.setText("0");

                    Toast.makeText(Foot_pressure_detection.this,
                            "已結束偵測",
                            Toast.LENGTH_SHORT).show();
                });

                detect_state = false;

            } catch (IOException e) {
                Log.e("Cancel Error",e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
