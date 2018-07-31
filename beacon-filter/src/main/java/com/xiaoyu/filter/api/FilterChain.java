/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.filter.api;

/**
 * 调用链
 * 
 * @author hongyu
 * @date 2018-07
 * @description 调用链本身也是一个filter,通过此来调用所有的filter
 */
public interface FilterChain extends Filter {

}
