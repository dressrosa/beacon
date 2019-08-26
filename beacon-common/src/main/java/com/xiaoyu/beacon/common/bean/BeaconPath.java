/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.common.bean;

import com.xiaoyu.beacon.common.constant.From;
import com.xiaoyu.beacon.common.utils.StringUtil;

/**
 * @author hongyu
 * @date 2018-05
 * @description 封装service信息
 */
public class BeaconPath {

    private String service;

    private String ref;

    private String host;

    private String port;

    private From side;

    // ms
    private String timeout;

    // server端调用
    private Object proxy;

    /**
     * 重试次数
     */
    private int retry;

    /**
     * 启动时检查
     */
    private boolean check;

    /**
     * 容错策略
     */
    private String tolerant;

    /**
     * 指定暴露的方法,逗号分隔
     */
    private String methods;

    /**
     * 泛型
     */
    private boolean generic = false;

    /**
     * 服务分组
     */
    private String group;

    /**
     * 服务降级 格式(策略:降级类)
     * timeout:次数
     * fault:次数
     * query:限流量
     * 注解:降级则会抛出异常,并非停止服务,没有把请求分配给其他服务
     */
    private String downgrade;

    public String getDowngrade() {
        return downgrade;
    }

    public BeaconPath setDowngrade(String downgrade) {
        this.downgrade = downgrade;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public BeaconPath setGroup(String group) {
        this.group = group;
        return this;
    }

    public boolean isGeneric() {
        return generic;
    }

    public BeaconPath setGeneric(boolean generic) {
        this.generic = generic;
        return this;
    }

    public String getMethods() {
        return methods;
    }

    public BeaconPath setMethods(String methods) {
        this.methods = methods;
        return this;
    }

    public String getTolerant() {
        return tolerant;
    }

    public BeaconPath setTolerant(String tolerant) {
        this.tolerant = tolerant;
        return this;
    }

    public boolean isCheck() {
        return check;
    }

    public BeaconPath setCheck(boolean check) {
        this.check = check;
        return this;
    }

    public int getRetry() {
        return retry;
    }

    public BeaconPath setRetry(int retry) {
        this.retry = retry;
        return this;
    }

    public Object getProxy() {
        return proxy;
    }

    public BeaconPath setProxy(Object proxy) {
        this.proxy = proxy;
        return this;
    }

    public String getTimeout() {
        return timeout;
    }

    public BeaconPath setTimeout(String timeout) {
        this.timeout = timeout;
        return this;
    }

    public String getService() {
        return service;
    }

    public BeaconPath setService(String service) {
        this.service = service;
        return this;
    }

    public String getRef() {
        return ref == null ? "" : ref;
    }

    public BeaconPath setRef(String ref) {
        this.ref = ref;
        return this;
    }

    public String getHost() {
        return host;
    }

    public BeaconPath setHost(String host) {
        this.host = host;
        return this;
    }

    public String getPort() {
        return port;
    }

    public BeaconPath setPort(String port) {
        this.port = port;
        return this;
    }

    public From getSide() {
        return side;
    }

    public BeaconPath setSide(From side) {
        this.side = side;
        return this;
    }

    @Override
    public int hashCode() {
        String key = this.toPath();
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BeaconPath)) {
            return false;
        }
        BeaconPath p = (BeaconPath) obj;
        return this.toPath().equals(p.toPath());
    }

    public String toPath() {
        final StringBuilder builder = new StringBuilder();
        boolean isServer = (this.getSide() == From.SERVER);
        if (isServer) {
            builder.append("host=").append(this.getHost() + ":" + this.getPort());
        } else {
            builder.append("host=").append(this.getHost());
        }

        builder.append("&service=").append(this.getService())
                .append("&ref=").append(this.getRef());
        if (isServer) {
            builder.append("&check=").append("")
                    .append("&tolerant=").append("")
                    .append("&timeout=").append("")
                    .append("&methods=").append(this.getMethods());
        } else {
            builder.append("&check=").append(this.isCheck())
                    .append("&tolerant=").append(this.getTolerant())
                    .append("&timeout=").append(this.getTimeout())
                    .append("&methods=").append("");
        }
        builder.append("&generic=").append(this.isGeneric())
                .append("&retry=").append(this.getRetry())
                .append("&group=").append(this.getGroup())
                .append("&downgrade=").append(this.getDowngrade());
        // 请注意side放在最后
        builder.append("&side=").append(this.getSide().name());
        return builder.toString();
    }

    public static BeaconPath toEntity(String path) {
        // [host=192.168.61.239:1992&service=com.xxx.IHelloService&ref=com.xxx.HelloServiceImpl&side=SERVER]
        BeaconPath bea = new BeaconPath();
        String[] arr = path.split("&");
        // host
        String str = arr[0];
        String[] tarr = str.substring(5).split(":");
        if (tarr.length == 1) {
            bea.setHost(tarr[0]);
        } else {
            bea.setHost(tarr[0]);
            bea.setPort(tarr[1]);
        }
        // service
        str = arr[1];
        bea.setService(str.substring(8));
        // ref
        str = arr[2];
        bea.setRef(str.substring(4));
        // check
        str = arr[3];
        bea.setCheck(Boolean.getBoolean(str.substring(6)));
        // tolerant
        str = arr[4];
        bea.setTolerant(str.substring(9));
        // timeout
        str = arr[5];
        bea.setTimeout(str.substring(8));
        // methods
        str = arr[6];
        String ms = str.substring(8);
        if (StringUtil.isNotEmpty(ms)) {
            bea.setMethods(ms);
        }
        // generic
        str = arr[7];
        bea.setGeneric(Boolean.valueOf(str.substring(8)));
        // retry
        str = arr[8];
        bea.setRetry(Integer.valueOf(str.substring(6)));
        // group
        str = arr[9];
        bea.setGroup(str.substring(6));
        // downgrade
        str = arr[10];
        bea.setDowngrade(str.substring(10));
        // side
        str = arr[11];
        bea.setSide(From.fromName(str.substring(5)));
        return bea;
    }

    // public static BeaconPath toEntity(String path) {
    // //
    // [host=192.168.61.239:1992&service=com.xxx.IHelloService&ref=com.xxx.HelloServiceImpl&side=SERVER]
    // BeaconPath bea = new BeaconPath();
    // String[] arr1 = path.split("&");
    // int len = arr1.length;
    // String str = null;
    // for (int i = 0; i < len; i++) {
    // str = arr1[i];
    // if (str.startsWith("host")) {
    // String[] arr = str.substring(5).split(":");
    // if (arr.length == 1) {
    // bea.setHost(arr[0]);
    // } else {
    // bea.setHost(arr[0]);
    // bea.setPort(arr[1]);
    // }
    // } else if (str.startsWith("service")) {
    // bea.setService(str.substring(8));
    // } else if (str.startsWith("ref")) {
    // bea.setRef(str.substring(4));
    // } else if (str.startsWith("timeout")) {
    // bea.setTimeout(str.substring(8));
    // } else if (str.startsWith("methods")) {
    // String ms = str.substring(8);
    // if (StringUtil.isNotEmpty(ms)) {
    // bea.setMethods(ms);
    // }
    // } else if (str.startsWith("generic")) {
    // bea.setGeneric(Boolean.valueOf(str.substring(8)));
    // } else if (str.startsWith("retry")) {
    // bea.setRetry(Integer.valueOf(str.substring(6)));
    // } else if (str.startsWith("group")) {
    // bea.setGroup(str.substring(6));
    // } else if (str.startsWith("check")) {
    // bea.setCheck(Boolean.getBoolean(str.substring(6)));
    // } else if (str.startsWith("tolerant")) {
    // bea.setTolerant(str.substring(9));
    // } else if (str.startsWith("downgrade")) {
    // bea.setDowngrade(str.substring(10));
    // } else if (str.startsWith("side")) {
    // bea.setSide(From.fromName(str.substring(5)));
    // }
    // }
    // return bea;
    // }
}
