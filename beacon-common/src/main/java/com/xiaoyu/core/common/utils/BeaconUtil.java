/**
 * 
 */
package com.xiaoyu.core.common.utils;

import java.lang.reflect.Field;

/**
 * @author hongyu
 * @date 2018-09
 * @description
 */
public class BeaconUtil {

    /**
     * 获取spring aop原生bean
     * 
     * @param proxy
     * @return
     */
    @SuppressWarnings("unchecked")
    public static final <T> T getOriginBean(T proxy) {
        Class<?> cls = proxy.getClass();
        if (cls.getSimpleName().startsWith("$Proxy")) {
            try {
                cls = cls.getSuperclass();
                Field hField = cls.getDeclaredField("h");
                hField.setAccessible(true);
                Object hObject = hField.get(proxy);

                Class<?> dynamicProxyClass = hObject.getClass();
                Field advisedField = dynamicProxyClass.getDeclaredField("advised");
                advisedField.setAccessible(true);
                Object advisedObject = advisedField.get(hObject);

                Class<?> advisedSupportClass = advisedObject.getClass().getSuperclass().getSuperclass();
                Field targetField = advisedSupportClass.getDeclaredField("targetSource");
                targetField.setAccessible(true);
                Object targetObject = targetField.get(advisedObject);

                Class<?> targetSourceClass = targetObject.getClass();
                Field targetClassField = targetSourceClass.getDeclaredField("target");
                targetClassField.setAccessible(true);
                return (T) targetClassField.get(targetObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
