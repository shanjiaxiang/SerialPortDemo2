package com.mit.serialportdemo2;

/**
 * Created by Administrator on 2018\11\12 0012.
 */

public class StringUtil {

    public static String bytesToHex( byte[] b) {
        StringBuilder strTmp = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            strTmp.append(hex);
        }
        return strTmp.toString();
    }

}
