package com.xiaoyu.spring.handler;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
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

import com.xiaoyu.core.common.constant.From;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.common.utils.NetUtil;
import com.xiaoyu.core.common.utils.StringUtil;
import com.xiaoyu.core.register.Registry;
import com.xiaoyu.core.rpc.config.bean.BeaconPath;
import com.xiaoyu.core.rpc.context.Context;
import com.xiaoyu.spring.config.BeaconExporter;
import com.xiaoyu.spring.config.BeaconFactoryBean;
import com.xiaoyu.spring.config.BeaconProtocol;
import com.xiaoyu.spring.config.BeaconReference;
import com.xiaoyu.spring.config.BeaconRegistry;
import com.xiaoyu.spring.listener.SpringContextListener;

/**
 * @author hongyu
 * @date 2018-04
 * @description
 */
public class BeaconBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private static final Logger LOG = LoggerFactory.getLogger(BeaconBeanDefinitionParser.class);

    private static Set<BeaconPath> referenceSet = new HashSet<>();

    private Class<?> cls;

    private static String serverPort = null;

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
                doParseReference(element, parserContext);
            } else if (cls == BeaconRegistry.class) {
                doParseRegistry(element, parserContext, builder);
            } else if (cls == BeaconProtocol.class) {
                doParseProtocol(element, parserContext, builder);
            } else if (cls == BeaconExporter.class) {
                doParseExporter(element, parserContext, builder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void doParseProtocol(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
            throws Exception {
        String name = element.getAttribute("name");
        String port = element.getAttribute("port");
        element.setAttribute("id", "protocol");
        if (StringUtil.isBlank(name)) {
            throw new Exception("name cannot be null in xml tag->" + element.getTagName());
        }
        if (StringUtil.isBlank(port)) {
            port = Integer.toString(1992);
        }
        if (!NumberUtils.isCreatable(port)) {
            throw new Exception("port should be a positive integer in xml tag->" + element.getTagName());
        }
        try {
            Context context = SpiManager.defaultSpiExtender(Context.class);
            serverPort = port;
            context.server(Integer.valueOf(port));
            // 处理exporter
            for (BeaconPath p : referenceSet) {
                if (p.getSide() == From.SERVER) {
                    // 注册中心还没好的话,一部分放到doParseRegistry里注册
                    if (context.getRegistry().isInit()) {
                        p.setPort(port);
                        context.getRegistry().registerService(p);
                    } else {
                        p.setPort(port);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void doParseExporter(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
            throws Exception {
        String interfaceName = element.getAttribute("interfaceName");
        String ref = element.getAttribute("ref");
        String id = element.getAttribute("id");
        if (StringUtil.isBlank(interfaceName)) {
            throw new Exception(" interfaceName cannot be null in xml tag->" + element.getTagName());
        }
        if (StringUtil.isBlank(ref)) {
            throw new Exception(" ref cannot be null in xml tag->" + element.getTagName());
        }
        // 检查接口的合法性
        Class<?> interfaceCls = Class.forName(interfaceName);
        Class<?> refCls = Class.forName(ref);
        Class<?>[] interfaces = refCls.getInterfaces();
        boolean isExist = false;
        for (Class<?> inter : interfaces) {
            if (interfaceCls.getName().equals(inter.getName())) {
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            throw new Exception("ref->" + ref + " is not implement of interface->" + interfaceName + " in xml tag->"
                    + element.getTagName());
        }
        try {
            // 注册服务
            BeaconPath beaconPath = new BeaconPath();
            beaconPath
                    .setSide(From.SERVER)
                    .setService(interfaceName)
                    .setRef(ref)
                    .setHost(NetUtil.localIP());
            Registry registry = SpiManager.defaultSpiExtender(Registry.class);
            if (registry.isInit() && serverPort != null) {
                beaconPath.setPort(serverPort);
                registry.registerService(beaconPath);
            } else {
                // registry还未解析到,导致这里没有registry
                referenceSet.add(beaconPath);
            }
        } catch (Exception e) {
            LOG.error("cannot resolve exporter,please check in xml tag->{} with id->{},interface->{}",
                    element.getTagName(), id, interfaceName, e);
            return;
        }
    }

    private void doParseReference(Element element, ParserContext parserContext)
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

            // 注册服务
            BeaconPath beaconPath = new BeaconPath();
            beaconPath
                    .setSide(From.CLIENT)
                    .setService(interfaceName)
                    .setHost(NetUtil.localIP());
            Registry registry = SpiManager.defaultSpiExtender(Registry.class);
            if (registry.isInit()) {
                registry.registerService(beaconPath);
            } else {
                // registry还未解析到,导致这里没有registry
                referenceSet.add(beaconPath);
            }

            String beanName = StringUtil.lowerFirstChar(target.getSimpleName());
            BeanDefinitionRegistry beanReg = parserContext.getRegistry();
            if (beanReg.containsBeanDefinition(beanName)) {
                LOG.warn("repeat register.please check in xml tag->{} with id->{},interface->{}", element.getTagName(),
                        id, interfaceName);
                return;
            }
            BeanDefinition facDef = this.generateFactoryBean(target, registry, From.CLIENT);
            parserContext.getRegistry().registerBeanDefinition(beanName, facDef);

        } catch (Exception e) {
            LOG.error("cannot resolve reference,please check in xml tag->{} with id->{},interface->{}",
                    element.getTagName(), id, interfaceName, e);
            return;
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
            String beanName = "beaconReg";
            BeanDefinitionRegistry registry = parserContext.getRegistry();
            if (registry.containsBeanDefinition(beanName)) {
                LOG.warn("repeat tag.please check in xml tag->{}", element.getTagName());
                return;
            }
            BeaconRegistry beaconReg = new BeaconRegistry();
            beaconReg.setAddress(address).setProtocol(protocol);
            GenericBeanDefinition def = new GenericBeanDefinition();
            def.setBeanClass(beaconReg.getClass());
            def.setLazyInit(false);
            parserContext.getRegistry().registerBeanDefinition(beanName, def);

            Context context = SpiManager.defaultSpiExtender(Context.class);
            // TODO 默认只有zookeeper
            Registry reg = SpiManager.defaultSpiExtender(Registry.class);
            reg.address(address);
            context.registry(reg);
            // 将之前未注册的refer进行注册
            for (BeaconPath p : referenceSet) {
                if (p.getSide() == From.SERVER) {
                    // protocol还没好的话,一部分放到doParseProtocol里面注册
                    if (serverPort != null) {
                        p.setPort(serverPort);
                        reg.registerService(p);
                    }
                } else {
                    reg.registerService(p);
                }
            }
            // 监听spring的close
            GenericBeanDefinition closeEvent = new GenericBeanDefinition();
            ConstructorArgumentValues val = new ConstructorArgumentValues();
            val.addGenericArgumentValue(context);
            closeEvent.setConstructorArgumentValues(val);
            closeEvent.setBeanClass(SpringContextListener.class);
            closeEvent.setLazyInit(false);
            parserContext.getRegistry().registerBeanDefinition("beaconCloseEvent", closeEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    // 接口没有构造函数所以无法初始化为bean,通过工厂bean,来避免初始化
    private BeanDefinition generateFactoryBean(Class<?> target, Registry registry, From side) {
        BeaconFactoryBean fac = new BeaconFactoryBean(target, registry, side);
        GenericBeanDefinition facDef = new GenericBeanDefinition();
        facDef.setBeanClass(fac.getClass());
        facDef.setLazyInit(false);
        facDef.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        // 构造函数
        ConstructorArgumentValues val = new ConstructorArgumentValues();
        val.addGenericArgumentValue(target);
        val.addGenericArgumentValue(registry);
        val.addGenericArgumentValue(side);
        facDef.setConstructorArgumentValues(val);
        return facDef;
    }
}
