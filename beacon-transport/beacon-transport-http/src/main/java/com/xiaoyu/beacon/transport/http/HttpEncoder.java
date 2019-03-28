/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.beacon.transport.http;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

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
        if (bodyType == String.class) {
            template.body(object.toString());
        } else if (bodyType == byte[].class) {
            template.body((byte[]) object, null);
        } else if (object != null) {
            SerializeConfig config = SerializeConfig.globalInstance;
            config.setTypeKey(bodyType.getTypeName());
            template.body(JSON.toJSONBytes(object, config, SerializerFeature.EMPTY),
                    Charset.defaultCharset());
        }

    }
}
