/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.registry.zookeeper;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class JsonSerializer implements ZkSerializer {

    @Override
    public byte[] serialize(Object data) throws ZkMarshallingError {
        return JSON.toJSONBytes(data, SerializerFeature.EMPTY);

    }

    @Override
    public Object deserialize(byte[] bytes) throws ZkMarshallingError {
        return JSON.parse(bytes, Feature.IgnoreNotMatch);
    }

}