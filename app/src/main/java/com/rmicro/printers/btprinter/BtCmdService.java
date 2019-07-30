package com.rmicro.printers.btprinter;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.rmicro.printers.btprinter.constant.ConstantDefine;
import com.rmicro.printers.btprinter.util.PrinterHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class BtCmdService extends IntentService {

    private static final String TAG = BtCmdService.class.getSimpleName();
    private BluetoothSdkManager manager;

    public BtCmdService() {
        super("BtCmdService");
    }

    public BtCmdService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = BluetoothSdkManager.INSTANCE;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        if (action.equals(ConstantDefine.ACTION_PRINT_TEST)) {
            printTest();
        }else if (action.equals(ConstantDefine.ACTION_IMG_PRINT_TEST)) {
            printImage();
        }else if (action.equals(ConstantDefine.ACTION_GET_PRINT_STATUS)) {
            getPrintStatus();;
        }else if (action.equals(ConstantDefine.ACTION_GET_PRINT_PARAM)) {
            getPrintParam();
        } else if (action.equals(ConstantDefine.ACTION_PRINT_ST_ONE)) {
            printSTOne();
        } else if (action.equals(ConstantDefine.ACTION_PRINT_ST_TWO)) {
            printSTTwo();
        } else if (action.equals(ConstantDefine.ACTION_GET_STATUS)) {
            getStatus();
        } else if (action.equals(ConstantDefine.ACTION_GET_PRINT_NAME)) {
            getPrintName();
        } else if (action.equals(ConstantDefine.ACTION_GET_PRINT_VERSION)) {
            getPrintVersion();
        } else if (action.equals(ConstantDefine.ACTION_GET_PRINT_ID)) {
            getPrintID();
        } else if (action.equals(ConstantDefine.ACTION_PRINT)) {
            print(intent.getByteArrayExtra(ConstantDefine.ACTION_PRINT_EXTRA));
        } else if (action.equals(ConstantDefine.ACTION_RESET)) {
            reSet();
        }
    }

    private void reSet() {
        //复位
        //manager.print(new byte[]{0x1b, 0x40});

        //清除禁止打印状态
        manager.print(new byte[]{0x1b, 0x41});

        //设备状态复位
        //manager.print(new byte[]{0x10,0x06,0x07,0x08,0x08});

        //feed line
        //manager.print(new byte[]{10});
    }

    private void getStatus() {
        try {
            int getstatus = PrinterHelper.getStatus();
            switch (getstatus) {
                case 0:
                    Log.d(TAG, "打印机准备就绪");
                    break;
                case 1:
                    Log.d(TAG, "打印机打印中");
                    break;
                case 2:
                    Log.d(TAG, "打印机缺纸");
                    break;
                case 6:
                    Log.d(TAG, "打印机开盖");
                    break;
                default:
                    Log.d(TAG, "出错");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPrintName() {
        try {
            Log.d(TAG, PrinterHelper.getPrintName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPrintVersion() {
        try {
            Log.d(TAG, PrinterHelper.getPrintVersion());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPrintStatus() {
        try {
            Log.d(TAG, PrinterHelper.getPrintStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getPrintParam() {
        try {
            Log.d(TAG, PrinterHelper.getPrintParam());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPrintID() {
        try {
            Log.d(TAG, PrinterHelper.getPrintID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printTest() {
        Log.d(TAG, "printTest..");
        String test = "123abcABC";
        try {
            print(test.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printImage() {
        Log.d(TAG, "printTest..");
        String test = "123abcABC";
        try {
            print(test.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printSTOne() {
        try {
            HashMap<String, String> pum = new HashMap<String, String>();
            pum.put("[barcode]", "363604310467");
            pum.put("[distributing]", "上海 上海市 长宁区");
            pum.put("[receiver_name]", "申大通");
            pum.put("[receiver_phone]", "13826514987");
            pum.put("[receiver_address1]", "上海市宝山区共和新路4719弄共");
            pum.put("[receiver_address2]", "和小区12号306室");//收件人地址第一行
            pum.put("[sender_name]", "快小宝");//收件人第二行（若是没有，赋值""）
            pum.put("[sender_phone]", "13826514987");//收件人第三行（若是没有，赋值""）
            pum.put("[sender_address1]", "上海市长宁区北曜路1178号（鑫达商务楼）");
            pum.put("[sender_address2]", "1号楼305室");
            Set<String> keySet = pum.keySet();
            Iterator<String> iterator = keySet.iterator();
            InputStream afis = this.getResources().getAssets().open("STO_CPCL.txt");//打印模版放在assets文件夹里
            String path = new String(InputStreamToByte(afis), "utf-8");//打印模版以utf-8无bom格式保存
            while (iterator.hasNext()) {
                String string = iterator.next();
                path = path.replace(string, pum.get(string));
            }
            PrinterHelper.printText(path);
            InputStream inbmp = this.getResources().getAssets().open("logo_sto_print1.png");
            Bitmap bitmap = BitmapFactory.decodeStream(inbmp);
            InputStream inbmp2 = this.getResources().getAssets().open("logo_sto_print2.png");
            Bitmap bitmap2 = BitmapFactory.decodeStream(inbmp2);
            PrinterHelper.Expanded("10", "20", bitmap, (byte) 0);//向打印机发送LOGO
            PrinterHelper.Expanded("10", "712", bitmap2, (byte) 0);//向打印机发送LOGO
            PrinterHelper.Expanded("10", "1016", bitmap2, (byte) 0);//向打印机发送LOGO
            PrinterHelper.Form();
            PrinterHelper.Print();
        } catch (Exception e) {

        }
    }

    private void printSTTwo() {
        try {
            HashMap<String, String> pum = new HashMap<String, String>();
            pum.put("[barcode]", "363604310467");
            pum.put("[distributing]", "上海 上海市 长宁区");
            pum.put("[receiver_name]", "申大通");
            pum.put("[receiver_phone]", "13826514987");
            pum.put("[receiver_address1]", "上海市宝山区共和新路4719弄共");
            pum.put("[receiver_address2]", "和小区12号306室");//收件人地址第一行
            pum.put("[sender_name]", "快小宝");//收件人第二行（若是没有，赋值""）
            pum.put("[sender_phone]", "13826514987");//收件人第三行（若是没有，赋值""）
            pum.put("[sender_address1]", "上海市长宁区北曜路1178号（鑫达商务楼）");
            pum.put("[sender_address2]", "1号楼305室");
            InputStream afis = this.getResources().getAssets().open("STO_CPCL.txt");//打印模版放在assets文件夹里
            PrinterHelper.printSingleInterface(afis, pum);

            InputStream inbmp = this.getResources().getAssets().open("logo_sto_print1.png");
            Bitmap bitmap = BitmapFactory.decodeStream(inbmp);
            InputStream inbmp2 = this.getResources().getAssets().open("logo_sto_print2.png");
            Bitmap bitmap2 = BitmapFactory.decodeStream(inbmp2);
            PrinterHelper.Expanded("10", "20", bitmap, (byte) 0);//向打印机发送LOGO
            PrinterHelper.Expanded("10", "712", bitmap2, (byte) 0);//向打印机发送LOGO
            PrinterHelper.Expanded("10", "1016", bitmap2, (byte) 0);//向打印机发送LOGO
            PrinterHelper.Form();
            PrinterHelper.Print();
        } catch (Exception e) {

        }
    }

    private void print(byte[] bytes) {
        if (null == bytes || bytes.length <= 0) {
            return;
        }
        manager.print(bytes);
    }

    private byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }


}
