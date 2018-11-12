package com.mit.serialportdemo2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.util.Arrays;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortUtil;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String  START_INVENTORY = "BB 17 02 00 00 19 0D 0A";
    private static final String STOP_INVENTORY = "BB 18 00 18 0D 0A";
    private Button start_inventory;
    private Button stop_inventory;
    private Button connect_serial;
    private static final String TAG = "serialport";

    private SerialPortUtil serialPortUtil = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewAndSetClick();

    }

    private void connect(){
        //打开串口
        if (serialPortUtil == null){
            serialPortUtil = SerialPortUtil.getInstance();
        }
        Log.d(TAG, "open serial port....");

        //接收到数据监听
        serialPortUtil.setOnDataReceiveListener(new SerialPortUtil.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer, int size) {
                Log.d(TAG, "received:" + Arrays.toString(buffer));
                Log.d(TAG, "received size:" + size);

//                boolean flag = true;
//                if (flag) {
//
//                    serialPortUtil.sendBuffer(ConvertUtil.hexToBytes(STOP_INVENTORY));
//                    flag = false;
//                }
            }
        });
        Log.d(TAG, "set listener....");

        //启动监听线程
        serialPortUtil.startNewThreadRead();
        Log.d(TAG, "start reading....");
        Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show();
    }

    private void getEPC(byte[] buffer, int size){
        byte[] epc_buffer = null;
        String epc_str = null;
        if ((buffer[1] == -69)&&(buffer[2] == 22)){
            for (int i=5; i<17; i++){
                epc_buffer[i-5] = buffer[i];
            }
            epc_str = StringUtil.bytesToHex(epc_buffer);
            Log.d(TAG, epc_str);
        }


    }


    private void startInventory(){

        Log.d(TAG, "send bytes: " + Arrays.toString(ConvertUtil.hexToBytes(START_INVENTORY)));

        boolean flag = serialPortUtil.sendBuffer(ConvertUtil.hexToBytes(START_INVENTORY));
        if (flag){
            Log.d(TAG, "send success...");
        }else {
            Log.d(TAG, "send failed...");
        }
        Toast.makeText(this, "开始盘点", Toast.LENGTH_SHORT).show();
    }


    private void stopInventory(){
        serialPortUtil.sendBuffer(ConvertUtil.hexToBytes(STOP_INVENTORY));
        Log.d(TAG, "stoped....");
        Toast.makeText(this, "结束盘点", Toast.LENGTH_SHORT).show();

    }


    private void findViewAndSetClick() {
        start_inventory = (Button) findViewById(R.id.bt_start_inventory);
        stop_inventory = (Button) findViewById(R.id.bt_stop_inventory);
        connect_serial = (Button) findViewById(R.id.bt_connect_serial_port);
        start_inventory.setOnClickListener(this);
        stop_inventory.setOnClickListener(this);
        connect_serial.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_connect_serial_port:
                connect();
                break;

            case R.id.bt_start_inventory:
                startInventory();
                break;
            case R.id.bt_stop_inventory:
                stopInventory();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialPortUtil.closeSerialPort();
        Log.d(TAG, "closed....");
    }
}
