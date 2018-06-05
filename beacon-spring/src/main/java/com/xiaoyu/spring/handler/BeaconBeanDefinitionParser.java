/**
 * 唯有读书,不慵不扰
 * 
 */
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

import com.xiaoyu.core.common.bean.BeaconPath;
import com.xiaoyu.core.common.constant.From;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.common.utils.NetUtil;
import com.xiaoyu.core.common.utils.StringUtil;
import com.xiaoyu.core.register.Registry;
import com.xiaoyu.core.rpc.api.Context;
import com.xiaoyu.spring.config.BeaconExporter;
import com.xiaoyu.spring.config.BeaconFactoryBean;
import com.xiaoyu.spring.config.BeaconProtocol;
import com.xiaoyu.spring.config.BeaconReference;
import com.xiaoyu.spring.config.BeaconRegistry;
import com.xiaoyu.spring.listener.SpringContextListener;

/**
 * @author hongyu
 * @date 2018-04
 * @description 解析xml
 */
public class BeaconBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private static final Logger LOG = LoggerFactory.getLogger(BeaconBeanDefinitionParser.class);

    private static Set<BeaconPath> referenceSet = new HashSet<>();

    private Class<?> cls;

    private static BeaconProtocol beaconProtocol = null;

    private static BeaconRegistry beaconRegistry = null;

    public static Set<BeaconPath> getBeaconPathSet() {
        return referenceSet;
    }

    public BeaconBeanDefinitionParser(Class<?> cls) {
        this.cls = cls;
    }

    private static void setBeaconRegistry(String registryProtocol) {
        BeaconBeanDefinitionParser.beaconRegistry = new BeaconRegistry();
        BeaconBeanDefinitionParser.beaconRegistry.setProtocol(registryProtocol);

    }

    private static void setBeaconProtocol(String beaconProtocol, String port) {
        BeaconBeanDefinitionParser.beaconProtocol = new BeaconProtocol();
        BeaconBeanDefinitionParser.beaconProtocol.setPort(port).setName(beaconProtocol);
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

    private void doRegisterBeaconListenerEvent(ParserContext parserContext, Context context) {
        if (!parserContext.getRegistry().containsBeanDefinition("beaconCloseEvent")) {
            GenericBeanDefinition event = new GenericBeanDefinition();
            ConstructorArgumentValues val = new ConstructorArgumentValues();
            val.addGenericArgumentValue(context);
            event.setConstructorArgumentValues(val);
            event.setBeanClass(SpringContextListener.class);
            event.setLazyInit(false);
            parserContext.getRegistry().registerBeanDefinition("beaconListenerEvent", event);
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
        if (!NumberUtils.isNumber(port)) {
            throw new Exception("port should be a positive integer in xml tag beacon-protocol");
        }
        try {
            Context context = SpiManager.holder(Context.class).target(name);
            // 设置beaconProtocol
            setBeaconProtocol(name, port);
            context.server(Integer.valueOf(port));
            // 处理exporter
            for (BeaconPath p : referenceSet) {
                if (p.getSide() == From.SERVER) {
                    // 注册中心还没好的话,一部分放到doParseRegistry里注册
                    if (beaconRegistry != null) {
                        p.setPort(port);
                        // context.getRegistry().registerService(p);
                    } else {
                        p.setPort(port);
                    }
                }
            }
            // server端这个和doParseRegistry里面的只有一个会执行
            if (beaconRegistry != null) {
                context.registry(SpiManager.holder(Registry.class).target(beaconRegistry.getProtocol()));
                // 监听spring的close
                doRegisterBeaconListenerEvent(parserContext, context);
            }

        } catch (Exception e) {
            e.printStackTrace();
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
        String[] addr = address.split(":");
        if (addr.length != 2) {
            throw new Exception("address->" + address + " is illegal in xml tag->" + element.getTagName());
        }
        if (!StringUtil.isIP(addr[0]) || !NumberUtils.isParsable(addr[1])) {
            throw new Exception("address->" + address + " is illegal in xml tag->" + element.getTagName());
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
            beaconReg.setAddress(addr[0]).setPort(addr[1]).setProtocol(protocol);
            GenericBeanDefinition def = new GenericBeanDefinition();
            def.setBeanClass(beaconReg.getClass());
            def.setLazyInit(false);
            parserContext.getRegistry().registerBeanDefinition(beanName, def);

            Registry reg = SpiManager.holder(Registry.class).target(protocol);
            if (reg == null) {
                throw new Exception("cannot find protocol->" + protocol + " in xml tag->" + element.getTagName());
            }
            reg.address(address);

            // server端这个和doParseProtocol里面的只有一个会执行
            Context context = null;
            if (beaconProtocol != null) {
                context = SpiManager.holder(Context.class).target(beaconProtocol.getName());
                context.registry(reg);
                // 监听spring的close
                doRegisterBeaconListenerEvent(parserContext, context);
            } else {
                // client端没有beaconProtocol
                context = SpiManager.defaultSpiExtender(Context.class);
                context.registry(reg);
                // 监听spring的close
                doRegisterBeaconListenerEvent(parserContext, context);
            }

            // 设置beaconRegistry
            setBeaconRegistry(protocol);
            // 将之前未注册的refer进行注册
            for (BeaconPath p : referenceSet) {
                if (p.getSide() == From.SERVER) {
                    // protocol还没好的话,一部分放到doParseProtocol里面注册
                    if (beaconProtocol != null) {
                        p.setPort(beaconProtocol.getPort());
                        // reg.registerService(p);
                    }
                } else if (p.getSide() == From.CLIENT) {
                    reg.registerService(p);
                    // 注册referbean
                    String interfaceName = p.getService();
                    Class<?> target = Class.forName(interfaceName);
                    String referBeanName = StringUtil.lowerFirstChar(target.getSimpleName());
                    BeanDefinitionRegistry beanReg = parserContext.getRegistry();
                    if (beanReg.containsBeanDefinition(referBeanName)) {
                        LOG.warn("repeat register.please check in xml with beacon-reference ,interface->{}",
                                interfaceName);
                        return;
                    }
                    BeanDefinition facDef = this.generateFactoryBean(target, reg);
                    parserContext.getRegistry().registerBeanDefinition(beanName, facDef);
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
            // 有接口暴漏,则启动context,相当于启动nettyServer
            Context context = SpiManager.defaultSpiExtender(Context.class);
            context.start();
            // 注册服务
            BeaconPath beaconPath = new BeaconPath();
            beaconPath
                    .setSide(From.SERVER)
                    .setService(interfaceName)
                    .setRef(ref)
                    .setHost(NetUtil.localIP());
            if (beaconRegistry != null) {
                if (beaconProtocol != null) {
                    beaconPath.setPort(beaconProtocol.getPort());
                    // 检查bean是否是spring中已有了
                    referenceSet.add(beaconPath);
                } else {
                    // protocol还未解析到 ,导致这里没有port
                    referenceSet.add(beaconPath);
                }
            } else {
                // registry还未解析到,导致这里没有registry
                referenceSet.add(beaconPath);
            }
        } catch (Exception e) {
            LOG.error("cannot resolve exporter,please check in xml tag beacon-exporter with id->{},interface->{}", id,
                    interfaceName);
            return;
        }
    }

    private void doParseReference(Element element, ParserContext parserContext)
            throws Exception {
        String interfaceName = element.getAttribute("interfaceName");
        String id = element.getAttribute("id");
        String timeout = element.getAttribute("timeout");
        if (StringUtil.isBlank(interfaceName)) {
            throw new Exception("interfaceName cannot be null in xml tag->" + element.getTagName());
        }
        if (StringUtil.isBlank(id)) {
            throw new Exception("id cannot be null in xml tag->" + element.getTagName());
        }
        try {

            // 注册服务
            BeaconPath beaconPath = new BeaconPath();
            beaconPath
                    .setSide(From.CLIENT)
                    .setService(interfaceName)
                    .setHost(NetUtil.localIP())
                    .setTimeout(timeout);

            if (beaconRegistry != null) {
                Registry registry = SpiManager.holder(Registry.class).target(beaconRegistry.getProtocol());
                registry.registerService(beaconPath);
                Class<?> target = Class.forName(interfaceName);
                String beanName = StringUtil.lowerFirstChar(target.getSimpleName());
                BeanDefinitionRegistry beanReg = parserContext.getRegistry();
                if (beanReg.containsBeanDefinition(beanName)) {
                    LOG.warn("repeat register.please check in xml tag beacon-reference with id->{},interface->{}", id,
                            interfaceName);
                    return;
                }
                BeanDefinition facDef = this.generateFactoryBean(target, registry);
                parserContext.getRegistry().registerBeanDefinition(beanName, facDef);
            } else {
                // registry还未解析到,导致这里没有registry
                referenceSet.add(beaconPath);
            }

        } catch (Exception e) {
            LOG.error("cannot resolve reference,please check in xml tag beacon-reference with id->{},interface->{}",
                    id, interfaceName);
            return;
        }
    }

    // 接口没有构造函数所以无法初始化为bean,通过工厂bean,来避免初始化
    private BeanDefinition generateFactoryBean(Class<?> target, Registry registry) {
        BeaconFactoryBean fac = new BeaconFactoryBean(target, registry);
        GenericBeanDefinition facDef = new GenericBeanDefinition();
        facDef.setBeanClass(fac.getClass());
        facDef.setLazyInit(false);
        facDef.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        // 构造函数
        ConstructorArgumentValues val = new ConstructorArgumentValues();
        val.addGenericArgumentValue(target);
        val.addGenericArgumentValue(registry);
        facDef.setConstructorArgumentValues(val);
        return facDef;
    }

}
