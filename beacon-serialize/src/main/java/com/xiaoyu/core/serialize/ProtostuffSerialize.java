/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.core.serialize;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * 2017年4月20日下午2:15:40
 * 
 * @author xiaoyu
 * @description
 */
public class ProtostuffSerialize implements Serializer {

    private static Map<Class<?>, Schema<?>> schemaMap = new ConcurrentHashMap<>(16);
    private static Objenesis objenesis = new ObjenesisStd(true);

    private <T> Schema<T> getSchema(Class<T> cls) {
        @SuppressWarnings("unchecked")
        Schema<T> schema = (Schema<T>) schemaMap.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                schemaMap.put(cls, schema);
            }
        }
        return schema;
    }

    @Override
    public <T> byte[] serialize(T obj) {
        @SuppressWarnings("unchecked")
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buf = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        Schema<T> schema = this.getSchema(cls);
        return ProtostuffIOUtil.toByteArray(obj, schema, buf);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        T t = objenesis.newInstance(cls);
        Schema<T> schema = this.getSchema(cls);
        ProtostuffIOUtil.mergeFrom(data, t, schema);
        return t;
    }

}
