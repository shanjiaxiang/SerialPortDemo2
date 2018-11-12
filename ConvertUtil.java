package com.mit.serialportdemo2;

/**
 * Created by Administrator on 2018\11\9 0009.
 */

public class ConvertUtil {

    public static String bytesToHex(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for(byte b : bytes) { // 使用String的format方法进行转换
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }

        return buf.toString();
    }

    public static byte[] hexToBytes(String str) {
        String strTmp = str.replace(" ","");
        if(strTmp == null || strTmp.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[strTmp.length() / 2];
        for(int i = 0; i < strTmp.length() / 2; i++) {
            String subStr = strTmp.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }

}


