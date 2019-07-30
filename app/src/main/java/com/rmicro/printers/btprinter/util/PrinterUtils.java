/*
 * RD打印机接口类
 * 初始化打印机
 * 打印换行
 * 打印文字“编码转换”
 * 打印图片“转换”
 * 打印字／行调整
 * 打印字体清晰度调整
 * 等。。。。
 */
package com.rmicro.printers.btprinter.util;

import android.graphics.Bitmap;

import java.util.Arrays;
public class PrinterUtils {
    private static final String TAG = "PrinterUtils";
    //状态消息
    public static final int RESULT_OPEN_SUCCESS = 1000;
    public static final int RESULT_OPEN_FAIL = 1001;
    public static final int RESULT_ERRO_CLOSE = 1002;
    public static final int RESULT_ERRO_SENDCMD = 1003;
    // 初始化打印机 清楚缓冲区数据
    public static final byte[] INIT = getByteArray(0x1B, 0x40);
    // 回读
    public static final byte[] READ_REBACK = getByteArray(0x1B, 0x76);
    // 打印并回车
    public static final byte[] ENTER_PRINT = getByteArray(0x0D);
    // 打印并换行
    public static final byte[] WRAP_PRINT = getByteArray(0x0A);
    // 左对齐
    public static final byte[] LEFT = getByteArray(0x1B, 0x61, 0);
    // 居中
    public static final byte[] MIND = getByteArray(0x1B, 0x61, 1);
    // 右对齐
    public static final byte[] RIGHT = getByteArray(0x1B, 0x61, 2);
    // 水平放大2倍
    public static final byte[] LARGE_TWICE = getByteArray(0x1B, 0x58, 0x02);
    // 打印并进纸1行
    public static final byte[] LINE_FEED = getByteArray(0x1B, 0x64, 0x01);
    // 打印并进纸4行
    public static final byte[] LINE_FEED4 = getByteArray(0x1B, 0x64, 0x06);
    // 打印并进纸8行
    public static final byte[] LINE_FEED8 = getByteArray(0x1B, 0x64, 0x08);
    // 模块波特率
    public static final byte[] BAUD_RATE_U = getByteArray(0x1B, 0xBC, 0x0);
    // 手机波特率
    public static final byte[] BAUD_RATE_P = getByteArray(0xEA, 0x0B);
    // 条码打印
    public static final byte[] CODE_BOTTOM = getByteArray(0x1B, 0x4D, 0x02);
    public static final byte[] CODE_ENA13 = getByteArray(0x1D, 0x6B, 67, 13);
    public static final byte[] GET_STATE = getByteArray(0x1B, 0x76);
    // 打印图片m  1,8点单倍宽
    public static final byte[] IMAGECMD = getByteArray(0x00, 0x1B, 0x2A, 1);
    // 打印图片m
    public static final byte[] IMAGECMD24 = getByteArray(0x00, 0x1B, 0x2A, 0x21);
    //打印字体／图片颜色浓度／清晰度设置
    public static final byte[] CHROMA_SET = getByteArray(0x1B, 0x73, 0x2B);
    //默认字体／图片深度／清晰度，字体默认0x32，图片默认0x16
    public static final byte[] TXT_CHROMA_DEF = getByteArray(0x1B, 0x73, 0x2B, 0x32);
    public static final byte[] IMG_CHROMA_DEF = getByteArray(0x1B, 0x73, 0x2B, 0x16);
    //打印字体／图片行间距 命令头
    public static final byte[] PRINT_LINESPACE = getByteArray(0x1B, 0x31);
    // 颜色
    public static final int BLACK = 0xff000000;
    public static final int WITER = 0xffffffff;

    public static PrinterUtils mInstance = null;

    public PrinterUtils() {
    }
    public static PrinterUtils getInstance() {
        if (mInstance == null) {
            mInstance = new PrinterUtils();
        }
        return mInstance;
    }

    public static final byte[] getByteArray(int... array) {
        byte[] bytes = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            bytes[i] = (byte) array[i];
        }
        return bytes;
    }

    //byte[]转十六进制字符串
    public String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv).append(" ");
        }
        return stringBuilder.toString().toUpperCase();
    }

    //获取位图数据
    public int[] getBitmapData(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i++) {

            if (pixels[i] == WITER) {
                pixels[i] = 0;
            } else {
                pixels[i] = 1;
            }
        }
        return get8BitData(pixels, width, height);
    }

    // 二进制转十进制
    public int binaryToDecimal(int[] src) {
        int result = 0;
        for (int i = 0; i < src.length; i++) {
            result += src[i] * Math.pow(2, src.length - i - 1);
        }
        return result;
    }

    //获得8bit数据
    public int[] get8BitData(int[] source, int width, int height) {
        int[] targData = new int[width * height / 8];
        // 组织数据
        for (int i = 0; i < height / 8; i++) {
            for (int j = 0; j < width; j++) {
                int[] temp = new int[8];
                for (int k = 0; k < 8; k++) {
                    temp[k] = source[(k + i * 8) * width + j];
                }
                targData[i * width + j] = binaryToDecimal(temp);
            }
        }
        return targData;
    }

    //获得24bit图像数据
    public int[] getBitmapData24(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i++) {

            if (pixels[i] == WITER) {
                pixels[i] = 0;
            } else {
                pixels[i] = 1;
            }
        }
        return get24BitData(pixels, width, height);
    }

    /**
     * 把原始数据转换为24位像素数据
     *
     * @param source
     *            初始数据
     * @param width
     *            图片宽度
     * @return
     */
    public int[] get24BitData(int[] source, int width, int height) {
        int[] target = new int[width * height / 8];
        for (int i = 0; i < height / 24; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < 3; k++) {
                    int[] temp = new int[8];
                    for (int m = 0; m < 8; m++) {
                        temp[m] = source[(i * 24 + k * 8 + m) * width + j];
                    }
                    target[3 * (i * width + j) + k] = binaryToDecimal(temp);
                }
            }
        }
        return target;

    }

    public byte[] getImageCmd(byte[] CMD, int width) {
        String[] result = new String[2];
        String str = Integer.toHexString(width).toUpperCase();
        StringBuffer sbuffer = new StringBuffer();
        int olen = 4 - str.length();
        for (int i = 0; i < olen; i++) {
            sbuffer.append("0");
        }
        sbuffer.append(str);
        result[0] = sbuffer.toString().substring(2, 4);
        result[1] = sbuffer.toString().substring(0, 2);
        int[] end = new int[2];
        end[0] = Integer.parseInt(result[0], 16);
        end[1] = Integer.parseInt(result[1], 16);
        return concat(CMD, getByteArray(end));
    }

    /**
     * @param first
     *            第一个数据
     * @param second
     *            第一个数据
     * @return 两个数据合并
     */
    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * @return
     */
    public int[] getCacheData(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // Log.v(TAG, "width = " + width + " ,height = " + height);
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] == WITER) {
                pixels[i] = 0;
            } else {
                pixels[i] = 1;
            }
        }
        int[] target = new int[width * height / 8];
        for (int i = 0; i < height / 8; i++) {
            for (int j = 0; j < width; j++) {
                int[] temp = new int[8];
                for (int m = 0; m < 8; m++) {
                    temp[m] = pixels[(i * 8 + m) * width + j];
                }
                target[j * height / 8 + i] = binaryToDecimal(temp);
            }
        }
        return target;
    }
}
