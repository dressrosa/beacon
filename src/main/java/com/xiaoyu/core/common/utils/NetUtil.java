/**
 * 
 */
package com.xiaoyu.core.common.utils;

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
            e.printStackTrace();
            return null;
        }
        return addr.getHostAddress();
    }
}
