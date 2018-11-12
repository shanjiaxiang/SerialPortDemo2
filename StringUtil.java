package com.mit.serialportdemo2;

/**
 * Created by Administrator on 2018\11\12 0012.
 */

public class StringUtil {
    public static String bytesToHex(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for(byte b : bytes) { // 使用String的format方法进行转换
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }

        return buf.toString();
    }

}
