package com.xiaoyu.spring.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import com.xiaoyu.core.rpc.api.Context;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */

public class SpringContextListener implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(SpringContextListener.class);

    private Context beaconContext;

    public SpringContextListener(Context beaconContext) {
        this.beaconContext = beaconContext;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextClosedEvent) {
            LOG.info("close the beacon context...");
            this.beaconContext.stop();
        }
    }

}
