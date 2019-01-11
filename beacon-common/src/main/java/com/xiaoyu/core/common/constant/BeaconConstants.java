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
public class BeaconConstants {

    public static final int PORT = 1992;

    /**
     * 
     */
    public static final int INT_LEN = 4;
    /**
     * 
     */
    public static final int FROM_LEN = 1;

    /**
     * 
     */
    public static final int LEN_OFFSET = 1;

    /**
     * 
     */
    public static final int MAX_LEN = 65526;

    /**
     * 读超时
     */
    public static final int IDLE_READ_TIMEOUT = 30_000;

    /**
     * equals
     */
    public static final String EQUALS = "equals";
    /**
     * toString
     */
    public static final String TO_STRING = "toString";
    /**
     * hashCode
     */
    public static final String HASHCODE = "hashCode";

    /**
     * 请求超时时间
     */
    public static final String REQUEST_TIMEOUT = "3000";

    /**
     * 过滤链
     */
    public static final String FILTER_CHAIN = "filterchain";

    /**
     * 泛型方法
     */
    public static final String $_$INVOKE = "$_$invoke";

    /**
     * 容错
     */
    public static final String TOLERANT_FAILFAST = "failfast";

    // 降级策略
    public static final String FUSE_TIMEOUT = "timeout";
    public static final String FUSE_FAULT = "fault";
    public static final String FUSE_QUERY = "query";
    public static final String FUSE_OFFLINE = "offline";
}
