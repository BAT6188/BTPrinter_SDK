package com.rmicro.printers.btprinter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.rmicro.printers.btprinter.R;
import com.rmicro.printers.btprinter.library.core.PrintExecutor;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initClickListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initBT();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_printer_setting_change_device:
                if(mBtAdapter.isEnabled())
                    discoveryDevices();
                break;
            case R.id.btn_printTest:
                break;
            case R.id.btn_imgprintTest:
                break;
            case R.id.btn_getPrintStatus:
                break;
            case R.id.btn_getPrintParam:
                break;
            case R.id.btn_getPrintID:
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
}
