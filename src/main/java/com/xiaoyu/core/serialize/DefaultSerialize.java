/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.core.serialize;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * 2017年4月20日下午4:51:39
 * 
 * @author xiaoyu
 * @description 默认序列化
 */
public class DefaultSerialize {

    private static Serializer serializer() {
        Serializer serializer = null;
        ServiceLoader<Serializer> loader = ServiceLoader.load(Serializer.class);
        Iterator<Serializer> iter = loader.iterator();
        while (serializer == null && iter.hasNext()) {
            serializer = iter.next();
        }
        if (serializer == null) {
            serializer = new ProtostuffSerialize();
        }
        return serializer;
    }

    public static <T> byte[] serialize(T obj) {
        return DefaultSerialize.serializer().serialize(obj);
    }

    public static <T> T deserialize(byte[] data, Class<T> cls) {
        return DefaultSerialize.serializer().deserialize(data, cls);
    }
}
