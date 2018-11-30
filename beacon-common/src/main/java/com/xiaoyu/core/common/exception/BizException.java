/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.common.exception;

/**
 * @author hongyu
 * @date 2018-05
 * @description
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BizException() {
        super();
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(Throwable cause) {
        super(cause);
    }

}
