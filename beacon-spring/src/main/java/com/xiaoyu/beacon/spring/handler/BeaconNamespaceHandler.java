/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.spring.handler;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.xiaoyu.beacon.spring.config.BeaconExporter;
import com.xiaoyu.beacon.spring.config.BeaconProtocol;
import com.xiaoyu.beacon.spring.config.BeaconReference;
import com.xiaoyu.beacon.spring.config.BeaconRegistry;

/**
 * @author hongyu
 * @date 2018-04
 * @description 解析xml
 */
public class BeaconNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        // 这里实际上是非顺序化执行
        this.registerBeanDefinitionParser("protocol", new BeaconBeanDefinitionParser(BeaconProtocol.class));
        this.registerBeanDefinitionParser("registry", new BeaconBeanDefinitionParser(BeaconRegistry.class));
        this.registerBeanDefinitionParser("reference", new BeaconBeanDefinitionParser(BeaconReference.class));
        this.registerBeanDefinitionParser("exporter", new BeaconBeanDefinitionParser(BeaconExporter.class));
    }

}
