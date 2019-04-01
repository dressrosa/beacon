# beacon
(beacon:灯塔.意为技术乃coder前进之方向)  
![image](https://img.shields.io/github/stars/dressrosa/beacon.svg?style=social)
![image](https://img.shields.io/github/followers/dressrosa.svg?label=Follow)
[![LICENSE](https://img.shields.io/badge/license-NPL%20(The%20996%20Prohibited%20License)-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE)
[![996.icu](https://img.shields.io/badge/link-996.icu-red.svg)](https://996.icu)
#### 基本原理:  
![基本原理](https://dressrosa.github.io/resources/beacon-principle.jpg)
#### 使用:
##### 配置:
consumer端配置:

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:beacon="http://www.iwouldbe.com/schema/beacon"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.iwouldbe.com/schema/beacon http://www.iwouldbe.com/schema/beacon.xsd">

    <beacon:protocol name="beacon" port="1992" />
    
    <beacon:registry address="192.168.61.239" protocol="zookeeper" />

    <beacon:reference id="helloService"
        interfaceName="com.xiaoyu.test.api.IHelloService" />

</beans>
```
consumer端配置释义: 
1. beacon:protocol 配置协议
2. beacon:registry 配置注册中心
3. beacon:reference 配置接口引用

**beacon:protocol:**
字段名称 | 含义 | 备注
---|---|---
name | 协议名称 | 支持beacon或http
port | 暴露端口 | 用于连接provider端

**beacon:registry:**
字段名称 | 含义 | 备注
---|---|---
protocol | 协议名称 | 支持zookeeper
address | 注册中心地址 |格式如:127.0.0.1:2181 

**beacon:reference:**
字段名称 | 含义 | 备注
---|---|---
id | 接口id. | 如userService
interfaceName | 接口名称 | 如 com.xxx.com.xxx.UserService
timeout | 接口超时时间 | 默认3000(ms)
retry | 重试次数 |默认0次
tolerant | 容错机制 | 默认failfast 支持failfast,failover
group | 接口分组 | 
check | 启动时检查 | true或false
generic | 泛型接口 | true或false
downgrade | 降级策略 | 支持timeout:xxx或retry:xxx或fault:xxx或limit:xxx

provider端的配置:

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:beacon="http://www.iwouldbe.com/schema/beacon"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.iwouldbe.com/schema/beacon http://www.iwouldbe.com/schema/beacon.xsd">


    <beacon:protocol name="beacon" port="1992" />

    <beacon:registry address="192.168.61.239" protocol="zookeeper" />

    <beacon:exporter id="helloService"
        interfaceName="com.xiaoyu.test.api.IHelloService" ref="com.xiaoyu.test.api.HelloServiceImpl" />

    <beacon:exporter id="userService"
        interfaceName="com.xiaoyu.test.api.IUserService" ref="com.xiaoyu.test.api.UserServiceImpl" />

</beans>
```
provider端配置释义: 
1. beacon:protocol 配置协议
2. beacon:registry 配置注册中心
3. beacon:exporter 配置接口暴露

**beacon:protocol:**
字段名称 | 含义 | 备注
---|---|---
name | 协议名称 | 支持beacon或http
port | 暴露端口 | 用于连接provider端

**beacon:registry:**
字段名称 | 含义 | 备注
---|---|---
protocol | 协议名称 | 支持zookeeper
address | 注册中心地址 |格式如:127.0.0.1:2181 

**beacon:exporter:**
字段名称 | 含义 | 备注
---|---|---
id | 接口id. | 如userService
interfaceName | 接口名称 | 如 com.xxx.UserService
ref | 接口实现类 |如com.xxx.UserServiceImpl
methods | 接口暴露的方法 |按逗号分隔,默认全部暴露
tolerant | 容错机制 | 默认failfast 支持failfast,failover
group | 接口分组 | 

更多详细用法可见[beacon-example](https://github.com/dressrosa/beacon/tree/master/beacon-example)
#### 兼容Springboot:
[beacon-spring-boot-starter](https://github.com/dressrosa/beacon-spring-boot-starter)
#### 目标:
健壮,扩展,优化细节.