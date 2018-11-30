/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.common.exception;

/**
 * 熔断错误
 * 
 * @author hongyu
 * @date 2018-11
 * @description 如果使用者配置了熔断策略,则应该捕获此异常,并进行相应的后续补偿处理
 */
public class FusedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FusedException() {
        super();
    }

    public FusedException(String message) {
        super(message);
    }

    public FusedException(Throwable cause) {
        super(cause);
    }

}
