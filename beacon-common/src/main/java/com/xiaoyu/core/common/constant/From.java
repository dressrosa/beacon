/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.common.constant;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public enum From {

    /**
     * 0 client
     */
    CLIENT,
    /**
     * 1 server
     */
    SERVER;

    public static From fromName(String name) {
        From[] values = From.values();
        for (From f : values) {
            if (f.name().equals(name)) {
                return f;
            }
        }
        return null;
    }
}
