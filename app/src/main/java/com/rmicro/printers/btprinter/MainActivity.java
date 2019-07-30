package com.rmicro.printers.btprinter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.rmicro.printers.btprinter.R;
import com.rmicro.printers.btprinter.api.PrintExecutor;
import com.rmicro.printers.btprinter.constant.ConstantDefine;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ListDialog.OnDismissListener, ListDialog.OnItemSelectedListener{

    private final String TAG = this.getClass().getSimpleName();

    private static final int REQ_ENABLE_BT = 10001;
    public static final String SP_FILE_CUR_PRINTER = "cur_printer";
    public static final String SP_KEY_ADDRESS = "address";
    private BluetoothAdapter mBtAdapter;
    private ListDialog.ListAdapter mListAdapter;
    private ListDialog mListDialog;
    private TextView tvStatus;
    private BluetoothDevice mDevice;
    private BluetoothDevice mUsedDevice;
    private PrintExecutor executor;

    private TextView tvPrinterTitle;
    private TextView tvPrinterSummary;
    private BluetoothSdkManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        initViews();
        initClickListener();
    }

    public void initViews() {
        tvPrinterTitle = findViewById(R.id.txt_printer_setting_title);
        tvPrinterSummary = findViewById(R.id.txt_printer_setting_summary);
        if (!TextUtils.isEmpty(BluetoothSdkManager.btName) && !TextUtils.isEmpty(BluetoothSdkManager.btAddress)) {
            tvPrinterTitle.setText("已绑定蓝牙：" + BluetoothSdkManager.btName);
            tvPrinterSummary.setText(BluetoothSdkManager.btAddress);
        }
        imgPrinter = findViewById(R.id.img_printer_setting_icon);
        manager = BluetoothSdkManager.INSTANCE;
        manager.checkBluetooth();
    }

    public void initClickListener() {
        tvPrinterTitle = findViewById(R.id.txt_printer_setting_title);
        tvPrinterSummary = findViewById(R.id.txt_printer_setting_summary);
        //if (!TextUtils.isEmpty(BluetoothSdkManager.btName) && !TextUtils.isEmpty(BluetoothSdkManager.btAddress)) {
        //    tvPrinterTitle.setText("已绑定蓝牙：" + BluetoothSdkManager.btName);
        //    tvPrinterSummary.setText(BluetoothSdkManager.btAddress);
        //}
        findViewById(R.id.ll_printer_setting_change_device).setOnClickListener(this);
        findViewById(R.id.btn_printTest).setOnClickListener(this);
        findViewById(R.id.btn_imgprintTest).setOnClickListener(this);
        findViewById(R.id.btn_getPrintStatus).setOnClickListener(this);
        findViewById(R.id.btn_getPrintParam).setOnClickListener(this);
        findViewById(R.id.btn_getPrintID).setOnClickListener(this);

        //接收蓝牙数据回调
        manager.setReceiveDataListener(new IReceiveDataListener() {
            @Override
            public void onReceiveData(byte[] data) {
                Log.d(TAG, "onReceiveData ==>> " + Arrays.toString(data));
            }
        });

        //连接状态结果回调
        manager.setBlueStateListener(new BluetoothStateListener() {
            @Override
            public void onConnectStateChanged(int state) {
                switch (state) {
                    case ConstantDefine.CONNECT_STATE_NONE:
                        Log.d(TAG, "  -----> none <----");
                        break;
                    case ConstantDefine.CONNECT_STATE_LISTENER:
                        Log.d(TAG, "  -----> listener <----");
                        break;
                    case ConstantDefine.CONNECT_STATE_CONNECTING:
                        Log.d(TAG, "  -----> connecting <----");
                        break;
                    case ConstantDefine.CONNECT_STATE_CONNECTED:
                        Log.d(TAG, "  -----> connected <----");
                        break;
                }
            }
        });

        manager.setBluetoothConnectListener(new BluetoothConnectListener() {
            @Override
            public void onBTDeviceConnected(String address, String name) {
                Toast.makeText(BtPrinterActivity.this, "已连接到名称为" + name + "的设备", Toast.LENGTH_SHORT).show();
                tvPrinterTitle.setText("已绑定蓝牙：" + name);
                tvPrinterSummary.setText(address);
                imgPrinter.setImageResource(R.drawable.ic_bluetooth_device_connected);
                //mBtnPrintTest.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBTDeviceDisconnected() {
                /*tvPrinterTitle.setText("蓝牙连接已断开");
                tvPrinterSummary.setText("");
                imgPrinter.setImageResource(R.drawable.ic_bluetooth_device_connected);
                Toast.makeText(MainActivity.this, "连接已经断开，请重新尝试连接...", Toast.LENGTH_SHORT).show();*/
            }

            @Override
            public void onBTDeviceConnectFailed() {
                Toast.makeText(BtPrinterActivity.this, "连接失败，请重新连接...", Toast.LENGTH_SHORT).show();
                tvPrinterTitle.setText("蓝牙连接失败");
                tvPrinterSummary.setText("");
                imgPrinter.setImageResource(R.drawable.ic_bluetooth_device_connected);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //initBT();
        if (manager != null) {
            Log.d(TAG, "onStart");
            manager.setupService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (manager != null) {
            Log.d(TAG, "onDestroy");
            manager.stopService();
        }
    }

    private void initBT(){
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(MainActivity.this, "该设备不支持蓝牙，无法使用蓝牙打印功能", Toast.LENGTH_SHORT).show();
            return;
        }
        // Enable Bluetooth.
        if (!mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQ_ENABLE_BT);
        }else{
            registerBTBroadcastReceiver();
        }
        mListAdapter = new ListDialog.ListAdapter(MainActivity.this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_printer_setting_change_device:
                if(mBtAdapter.isEnabled())
                    discoveryDevices();
//                Intent serverIntent = new Intent(BtPrinterActivity.this, DeviceListActivity.class);
//                startActivity(serverIntent);
                break;
            case R.id.btn_printTest:
                startBtCmdService(ConstantDefine.ACTION_PRINT_TEST);
                break;
//            case R.id.btn_printSTOne:
//                printSTOne();
//                break;
//            case R.id.btn_printSTTwo:
//                printSTTwo();
//                break;
//            case R.id.btn_getStatus:
//                startBtCmdService(ConstantDefine.ACTION_GET_STATUS);
//                break;
//            case R.id.btn_getPrintName:
//                startBtCmdService(ConstantDefine.ACTION_GET_PRINT_NAME);
//                break;
//            case R.id.btn_getPrintVersion:
//                startBtCmdService(ConstantDefine.ACTION_GET_PRINT_VERSION);
//                break;
            case R.id.btn_imgprintTest:
                startBtCmdService(ConstantDefine.ACTION_IMG_PRINT_TEST);
                break;
            case R.id.btn_getPrintStatus:
                startBtCmdService(ConstantDefine.ACTION_GET_PRINT_STATUS);
                break;
            case R.id.btn_getPrintParam:
                startBtCmdService(ConstantDefine.ACTION_GET_PRINT_PARAM);
                break;
            case R.id.btn_getPrintID:
                startBtCmdService(ConstantDefine.ACTION_GET_PRINT_ID);
                break;
            default:
                break;
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    // 用于开启“发现设备”后，通知状态
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    // Discovery has found a device.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d("tag", "ACTION_FOUND, BluetoothDevice: " + device.getAddress());
                    mListAdapter.add(device);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d("tag", "ACTION_DISCOVERY_FINISHED, isDiscovering: " + mBtAdapter.isDiscovering());
                    mListDialog.setProgressBarVisible(false);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.d("tag", "ACTION_DISCOVERY_STARTED, isDiscovering: " + mBtAdapter.isDiscovering());
                    mListDialog.setProgressBarVisible(true);
                    break;
            }
        }
    };

    private void registerBTBroadcastReceiver(){
        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    /**
     * 发现所有蓝牙设备
     */
    private void discoveryDevices() {
        mListAdapter.clear();
        mBtAdapter.startDiscovery();
        showDevices();
    }

    /**
     * 列表展示所有搜索到的设备
     */
    private void showDevices() {
        mListDialog = new ListDialog();
        Bundle args = new Bundle();
        args.putSerializable(ListDialog.ARG_ADAPTER, mListAdapter);
        mListDialog.setArguments(args);
        mListDialog.show(getFragmentManager(), "bt_list");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_ENABLE_BT:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(MainActivity.this, "未能成功打开蓝牙，无法使用蓝牙打印功能", Toast.LENGTH_SHORT).show();
                    return;
                }
                registerBTBroadcastReceiver();
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mBtAdapter.cancelDiscovery();
    }

    @Override
    public void onItemSelected(BluetoothDevice btDevice) {
        mDevice = btDevice;
        tvPrinterTitle.setText("已绑定蓝牙：" + mDevice.getName());
        tvPrinterSummary.setText(mDevice.getAddress());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    //服务打印
    //打印测试
    public void printTest() {
        /*manager.printText("可以正常打印出这句话吗？\n");
        manager.printText("Hello World.\n");

        manager.printImage(markImage());*/

        Intent intent = new Intent(getApplicationContext(), BtCmdService.class);
        intent.setAction(ConstantDefine.ACTION_PRINT_TEST);
        startService(intent);
    }

    private void printSTOne() {
        Intent intent = new Intent(getApplicationContext(), BtCmdService.class);
        intent.setAction(ConstantDefine.ACTION_PRINT_ST_ONE);
        startService(intent);
    }

    private void printSTTwo() {
        Intent intent = new Intent(getApplicationContext(), BtCmdService.class);
        intent.setAction(ConstantDefine.ACTION_PRINT_ST_TWO);
        startService(intent);
    }

    private void startBtCmdService(String action) {
        Intent intent = new Intent(getApplicationContext(), BtCmdService.class);
        intent.setAction(action);
        startService(intent);
    }
}
