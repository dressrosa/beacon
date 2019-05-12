/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.spring.config;

import java.lang.reflect.Method;

import com.xiaoyu.beacon.common.bean.BeaconBean;
import com.xiaoyu.beacon.common.bean.BeaconPath;
import com.xiaoyu.beacon.common.constant.From;
import com.xiaoyu.beacon.common.extension.SpiManager;
import com.xiaoyu.beacon.common.utils.NetUtil;
import com.xiaoyu.beacon.common.utils.StringUtil;
import com.xiaoyu.beacon.registry.Registry;
import com.xiaoyu.beacon.rpc.api.Context;
import com.xiaoyu.beacon.spring.handler.BeaconBeanDefinitionParser;

/**
 * @author hongyu
 * @date 2018-05
 * @description 对应beacon-exporter
 */
public class BeaconExporter extends BeaconBean {

    /**
     * 接口名
     */
    private String interfaceName;
    /**
     * 实现类
     */
    private String ref;

    /**
     * 服务分组
     */
    private String group = "";
    /**
     * 暴露的方法
     */
    private String methods;

    public String getMethods() {
        return methods;
    }

    public BeaconExporter setMethods(String methods) {
        this.methods = methods;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public BeaconExporter setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public BeaconExporter setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return this;
    }

    public String getRef() {
        return ref;
    }

    public BeaconExporter setRef(String ref) {
        this.ref = ref;
        return this;
    }

    /**
     * 主动暴露服务
     * 
     * @throws Exception
     */
    public void export() throws Exception {
        if (StringUtil.isBlank(interfaceName)) {
            throw new Exception(" InterfaceName cannot be null");
        }
        if (StringUtil.isBlank(ref)) {
            throw new Exception(" Reference cannot be null");
        }
        if (StringUtil.isBlank(group)) {
            group = "";
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
            throw new Exception(
                    " Reference->" + ref + " is not implement of interface->" + interfaceName);
        }
        if (StringUtil.isNotEmpty(methods)) {
            if (methods.contains("&")) {
                throw new Exception(" Methods contain the illegal character '&'");
            }
        } else {
            // 取所有的方法
            Method[] mes = refCls.getDeclaredMethods();
            StringBuilder namesBuilder = new StringBuilder();
            for (int i = 0; i < mes.length - 1; i++) {
                namesBuilder.append(mes[i].getName()).append(",");
            }
            namesBuilder.append(mes[mes.length - 1].getName());
            methods = namesBuilder.toString();
        }
        try {
            Context context = SpiManager.defaultSpiExtender(Context.class);
            // 注册服务
            BeaconPath beaconPath = new BeaconPath();
            beaconPath
                    .setSide(From.SERVER)
                    .setService(interfaceName)
                    .setRef(ref)
                    .setHost(NetUtil.localIP())
                    .setMethods(methods)
                    .setGroup(group)
                    .setDowngrade("")
                    .setTolerant("")
                    .setPort(BeaconBeanDefinitionParser.getBeaconProtocol().getPort());
            Registry beaconRegistry = context.getRegistry();
            if (beaconRegistry == null || !beaconRegistry.isInit()) {
                throw new Exception(" No registry is init");
            }
            beaconRegistry.registerService(beaconPath);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
