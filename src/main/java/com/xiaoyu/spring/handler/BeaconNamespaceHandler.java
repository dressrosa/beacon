package com.xiaoyu.spring.handler;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.xiaoyu.spring.config.BeaconExporter;
import com.xiaoyu.spring.config.BeaconProtocol;
import com.xiaoyu.spring.config.BeaconReference;
import com.xiaoyu.spring.config.BeaconRegistry;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public class BeaconNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        this.registerBeanDefinitionParser("protocol", new BeaconBeanDefinitionParser(BeaconProtocol.class));
        this.registerBeanDefinitionParser("registry", new BeaconBeanDefinitionParser(BeaconRegistry.class));
        this.registerBeanDefinitionParser("reference", new BeaconBeanDefinitionParser(BeaconReference.class));
        this.registerBeanDefinitionParser("exporter", new BeaconBeanDefinitionParser(BeaconExporter.class));
    }

}
