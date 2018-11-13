package com.mit.serialportdemo2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortUtil;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //循环查询
    private static final String  START_INVENTORY = "BB 17 02 00 00 19 0D 0A";
    // 单次查询
//    private static final String  START_INVENTORY = "BB 16 00 16 0D 0A";
    private static final String STOP_INVENTORY = "BB 18 00 18 0D 0A";
    private Button start_inventory;
    private Button stop_inventory;
    private Button connect_serial;
    private Button clear_inventory;
    private TextView tvEpcList;
    private static final String TAG = "serialport";

    HashMap<String, Integer> epcMap = new HashMap <>();
    HashMap<String, Integer> epcMapTotal = new HashMap <>();
    String dispTmp;
    Timer timer;
    private Boolean isDisplayInfo = false;
    private SerialPortUtil serialPortUtil = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewAndSetClick();

        startTiming(2000);
        connect();
        startInventory();
//        stopInventory();
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
                getEPC(buffer, size);
                displayEpcInfo();
            }
        });
        Log.d(TAG, "set listener....");

        //启动监听线程
        serialPortUtil.startNewThreadRead();
        Log.d(TAG, "start reading....");
        Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show();
    }

    private void getEPC(byte[] buffer, int size){
        if (((int)buffer[1] == -105)&&((int)buffer[2] == 34)){
//        if (((int)buffer[1] == -106)&&((int)buffer[2] == 34)){
            Log.d(TAG, "getEpc if branch");
            byte[] epc_buffer = new byte[12];
            String epcStr = null;
            System.arraycopy(buffer,5, epc_buffer,0, 12);
            epcStr = StringUtil.bytesToHex(epc_buffer);
            Log.d(TAG, epcStr.length() + ":" + epcStr);

            //将标签保存到全局变量
            if (epcStr != null){
                if (epcMap.containsKey(epcStr)){
                    epcMap.put(epcStr, epcMap.get(epcStr) + 1);
                }else {
                    epcMap.put(epcStr, 1);
                }
                if (epcMapTotal.containsKey(epcStr)){
                    epcMapTotal.put(epcStr, epcMapTotal.get(epcStr) + 1);
                }else {
                    epcMapTotal.put(epcStr, 1);
                }
                Log.d(TAG, "reader:" + Thread.currentThread().getName());
            }

//            stopInventory();
        } else {
            Log.d(TAG, "getEpc if-else branch");
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
        tvEpcList = (TextView)findViewById(R.id.tv_epc_list);
        start_inventory = (Button) findViewById(R.id.bt_start_inventory);
        stop_inventory = (Button) findViewById(R.id.bt_stop_inventory);
        connect_serial = (Button) findViewById(R.id.bt_connect_serial_port);
        clear_inventory = (Button) findViewById(R.id.bt_clear_inventory);
        start_inventory.setOnClickListener(this);
        stop_inventory.setOnClickListener(this);
        connect_serial.setOnClickListener(this);
        clear_inventory.setOnClickListener(this);
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
            case R.id.bt_clear_inventory:
                clearCountNum();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialPortUtil.closeSerialPort();
        Log.d(TAG, "closed....");
    }


    private void displayEpcInfo(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "disp:" + Thread.currentThread().getName());
                if (isDisplayInfo){
                    dispTmp = "";
                    for (String key : epcMapTotal.keySet()){
                        if (!epcMap.containsKey(key))
                            epcMap.put(key, 0);
                        dispTmp = dispTmp + key + "\t\t\t\t" + epcMap.get(key) + "\t\t\t\t" + epcMapTotal.get(key) + "\n";
                    }
                    tvEpcList.setText("");
                    tvEpcList.setText(dispTmp);
                    Log.d(TAG, dispTmp);
                    epcMap.clear();
                    isDisplayInfo = false;
                }
            }
        }
        );
    }


    //定时任务两秒显示一次数据
    private void startTiming(int intevalPeriod){
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                isDisplayInfo = true;
                Log.d(TAG, "Timer:" + Thread.currentThread().getName());
                Log.d(TAG,"timer task is execute...");
            }
        };
        timer = new Timer();
        long delay = 0;
        // schedules the task to be run in an interval
        timer.scheduleAtFixedRate(task, delay, intevalPeriod);
        Log.d(TAG,"timer is start timing...");
    }

    //清空当前盘存数据
    private void clearCountNum(){
        stopInventory();
        epcMap.clear();
        epcMapTotal.clear();
        Log.d(TAG, "已停止盘存");
        Log.d(TAG, "已清空epcMap");
        //setStartInventory();
        tvEpcList.setText("");
        isDisplayInfo = false;
    }




}
