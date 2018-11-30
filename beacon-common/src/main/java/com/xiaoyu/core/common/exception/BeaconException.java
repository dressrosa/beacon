/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.common.exception;

/**
 * @author hongyu
 * @date 2018-07
 * @description
 */
public class BeaconException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BeaconException() {
        super();
    }

    public BeaconException(String message) {
        super(message);
    }

    public BeaconException(Throwable cause) {
        super(cause);
    }

}
