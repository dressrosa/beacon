package com.xiaoyu.core.common.utils;

import java.util.Random;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class IdUtil {

    private static final Random RAN = new Random();

    public static String requestId() {
        return Math.abs(RAN.nextLong()) + System.currentTimeMillis()/1000 + "";
    }

    public static int randomNum(int bound) {
        return RAN.nextInt(bound);
    }

    /**
     * 随机数不能与上一个随机数相同
     * 
     * @param bound
     * @param lastOne
     * @return
     */
    public static int randomNum(int bound, int lastOne) {
        int num = RAN.nextInt(bound);
        while (num == lastOne) {
            num = RAN.nextInt(bound);
        }
        return num;
    }

    public static long randomNum() {
        return RAN.nextLong();
    }

}
