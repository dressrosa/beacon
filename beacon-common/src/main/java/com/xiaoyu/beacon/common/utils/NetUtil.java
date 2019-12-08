/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.common.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author hongyu
 * @date 2018-05
 * @description
 */
public class NetUtil {

    public static String localIP() {
        try {
            InetAddress addr = null;
            // 遍历网络接口
            Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
            while (nifs.hasMoreElements()) {
                NetworkInterface iface = nifs.nextElement();
                // 遍历ip
                Enumeration<InetAddress> iads = iface.getInetAddresses();
                while (iads.hasMoreElements()) {
                    InetAddress inetAddr = iads.nextElement();
                    // 排除loopback类型地址
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // site-local地址
                            return inetAddr.getHostAddress();
                        } else if (addr == null) {
                            addr = inetAddr;
                        }
                    }
                }
            }
            if (addr != null) {
                return addr.getHostAddress();
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
