/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author hongyu
 * @date 2018-05
 * @description
 */
public class NetUtil {

    public static String localIP() {
        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return null;
        }
        return addr.getHostAddress();
    }
}
