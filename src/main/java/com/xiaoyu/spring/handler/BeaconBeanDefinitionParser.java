package com.xiaoyu.spring.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.common.utils.IdUtil;
import com.xiaoyu.core.common.utils.StringUtil;
import com.xiaoyu.core.register.Registry;
import com.xiaoyu.core.rpc.context.Context;
import com.xiaoyu.spring.config.BeaconFactoryBean;
import com.xiaoyu.spring.config.BeaconReference;
import com.xiaoyu.spring.config.BeaconRegistry;

public class BeaconBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private static final Logger LOG = LoggerFactory.getLogger(BeaconBeanDefinitionParser.class);
    private Class<?> cls;

    public BeaconBeanDefinitionParser(Class<?> cls) {
        this.cls = cls;
    }

    @Override
    protected Class<?> getBeanClass(Element element) {
        return cls;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        try {
            if (cls == BeaconReference.class) {
                doParseReference(element, parserContext, builder);
            } else if (cls == BeaconRegistry.class) {
                doParseRegistry(element, parserContext, builder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doParseRegistry(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
            throws Exception {
        String address = element.getAttribute("address");
        String protocol = element.getAttribute("protocol");
        element.setAttribute("id", "registry");
        if (protocol == null) {
            protocol = "zookeeper";
        }
        if (StringUtil.isBlank(address)) {
            throw new Exception("address cannot be null in xml tag->" + element.getTagName());
        }
        if (!StringUtil.isIP(address)) {
            throw new Exception("address is illegal in xml tag->" + element.getTagName());
        }
        if (StringUtil.isBlank(protocol)) {
            throw new Exception("protocol can ignore but not empty in xml tag->" + element.getTagName());
        }

        try {
            Context context = SpiManager.defaultSpiExtender(Context.class);
            // TODO 默认只有zookeeper
            Registry reg = SpiManager.defaultSpiExtender(Registry.class);
            reg.address(address);
            context.registry(reg);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    private void doParseReference(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
            throws Exception {
        String interfaceName = element.getAttribute("interfaceName");
        String id = element.getAttribute("id");
        if (StringUtil.isBlank(interfaceName)) {
            throw new Exception("interfaceName cannot be null in xml tag->" + element.getTagName());
        }
        if (StringUtil.isBlank(id)) {
            throw new Exception("id cannot be null in xml tag->" + element.getTagName());
        }
        try {
            Class<?> target = Class.forName(interfaceName);
            String beanName = StringUtil.lowerFirstChar(target.getSimpleName());
            BeanDefinitionRegistry registry = parserContext.getRegistry();
            if (registry.containsBeanDefinition(beanName)) {
                LOG.warn("repeat register.please check in xml tag->{} with id->{},interface->{}", element.getTagName(),
                        id,
                        interfaceName);
                return;
            }
            BeanDefinition facDef = this.generateFactoryBean(target);
            parserContext.getRegistry().registerBeanDefinition(beanName, facDef);
        } catch (Exception e) {
            LOG.error("cannot resolve reference,please check in xml tag->{} with id->{},interface->{}",
                    element.getTagName(), id, interfaceName);
            return;
        }
    }

    // 接口没有构造函数所以无法初始化为bean,通过工厂bean,来避免初始化
    private BeanDefinition generateFactoryBean(Class<?> target) {
        BeaconFactoryBean fac = new BeaconFactoryBean(target);
        GenericBeanDefinition facDef = new GenericBeanDefinition();
        facDef.setBeanClass(fac.getClass());
        facDef.setLazyInit(false);
        facDef.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        // 构造函数
        ConstructorArgumentValues val = new ConstructorArgumentValues();
        val.addGenericArgumentValue(target);
        facDef.setConstructorArgumentValues(val);
        return facDef;
    }
}
