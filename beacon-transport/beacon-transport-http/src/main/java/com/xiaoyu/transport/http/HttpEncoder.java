/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.transport.http;

import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;

import feign.RequestTemplate;
import feign.codec.Encoder;

/**
 * @author hongyu
 * @date 2018-08
 * @description
 */
public class HttpEncoder implements Encoder {

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) {
        template.body(JSON.toJSONString(object));
    }
}
