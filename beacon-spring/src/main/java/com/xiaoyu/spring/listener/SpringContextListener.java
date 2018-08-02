/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.spring.listener;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import com.xiaoyu.core.common.bean.BeaconPath;
import com.xiaoyu.core.common.constant.From;
import com.xiaoyu.core.register.Registry;
import com.xiaoyu.core.rpc.api.Context;
import com.xiaoyu.spring.handler.BeaconBeanDefinitionParser;

/**
 * @author hongyu
 * @date 2018-04
 * @description spring监听事件,对spring启动完成和结束进行监听
 */

public class SpringContextListener implements ApplicationListener<ApplicationEvent>, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(SpringContextListener.class);

    private ApplicationContext applicationContext;

    private Context beaconContext;

    public SpringContextListener(Context beaconContext) {
        this.beaconContext = beaconContext;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            doInitExporter();
        } else if (event instanceof ContextClosedEvent) {
            LOG.info("close the beacon context.");
            this.beaconContext.shutdown();
        }
    }

    /**
     * 注册exporter
     */
    private void doInitExporter() {
        Registry registry = this.beaconContext.getRegistry();
        final Set<BeaconPath> sets = BeaconBeanDefinitionParser.getBeaconPathSet();
        try {
            for (BeaconPath p : sets) {
                if (p.getSide() == From.SERVER) {
                    Class<?> cls = Class.forName(p.getService());
                    Object proxyBean = this.applicationContext.getBean(cls);
                    // 设置spring bean的引用
                    if (proxyBean != null) {
                        p.setProxy(proxyBean);
                    }
                    registry.registerService(p);
                }
            }
            // 使命完成
            BeaconBeanDefinitionParser.removeBeaconPathSet();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
