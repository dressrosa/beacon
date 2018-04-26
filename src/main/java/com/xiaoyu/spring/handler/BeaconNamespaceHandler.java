package com.xiaoyu.spring.handler;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.xiaoyu.spring.config.BeaconReference;
import com.xiaoyu.spring.config.BeaconRegistry;

public class BeaconNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        this.registerBeanDefinitionParser("reference", new BeaconBeanDefinitionParser(BeaconReference.class));
        this.registerBeanDefinitionParser("registry", new BeaconBeanDefinitionParser(BeaconRegistry.class));
    }

}
