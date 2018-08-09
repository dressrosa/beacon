/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.transport.http;

import static feign.Util.ensureClosed;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;

import feign.Response;
import feign.Util;
import feign.codec.Decoder;

/**
 * @author hongyu
 * @date 2018-08
 * @description
 */
public class HttpDecoder implements Decoder {

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (response.status() == 404)
            return Util.emptyValueOf(type);
        if (response.body() == null)
            return null;
        InputStream is = response.body().asInputStream();
        try {
            return JSON.parseObject(is, type, Feature.IgnoreNotMatch);
        } finally {
            ensureClosed(is);
        }
    }
}