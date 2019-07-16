# beacon
(beacon:灯塔.意为技术乃coder前进之方向)  
![image](https://img.shields.io/github/stars/dressrosa/beacon.svg?style=social)
![image](https://img.shields.io/github/followers/dressrosa.svg?label=Follow)
[![LICENSE](https://img.shields.io/badge/license-NPL%20(The%20996%20Prohibited%20License)-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE)
[![996.icu](https://img.shields.io/badge/link-996.icu-red.svg)](https://996.icu)
## 基本原理:  
![基本原理](https://dressrosa.github.io/resources/beacon-principle.jpg)
## 调用流程:
### consumer:
spring factory bean,基于接口生成  
->proxy,生成代理类(cglib/jdk)  
 ->registry,注册接口  
->filter 自定义过滤器,对接口进行过滤  
->tolerant,容错机制(failfast等)  
->loadBalance(负载均衡,random等)  
->strategy(熔断降级)  
->invocation(生成调用者)  
->serialize(进行消息序列化)  
->tranporter to pro ,wait result(通讯层传输给provider端)

### provider:
 ->transporter接收消息  
 ->deserialize 反序列化消息  
 ->local invoke  
 ->serialize 进行消息序列化  
 ->transporter to con,send result(通讯层传输给consumer端)
## 快速开始:
### provider端:
1. 在pom文件中引入:
```
<dependency>
    <groupId>com.xiaoyu</groupId>
    <artifactId>beacon</artifactId>
    <version>0.0.1</version>
</dependency>
```
2. 创建公共api
```
public interface IHelloService {
    public String hello(String name);
}
```
3. 实现IHelloService 
```
public class HelloServiceImpl implements IHelloService {
    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
```
4. 创建beacon-server.xml
```
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:beacon="http://www.iwouldbe.com/schema/beacon"
        xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.iwouldbe.com/schema/beacon http://www.iwouldbe.com/schema/beacon.xsd">

        <!--beacon相关配置  -->
        <beacon:protocol name="beacon" port="1992" />

        <beacon:registry address="127.0.0.1:2181" protocol="zookeeper" />

        <beacon:exporter id="helloService1" group="dev"
            interfaceName="com.xiaoyu.test.api.IHelloService" ref="com.xiaoyu.test.api.impl.HelloServiceImpl" />

    </beans>
```

5.  在你的spring xml中引入beacon-server.xml
```
   <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:tx="http://www.springframework.org/schema/tx"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="
	   http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context-3.0.xsd
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
       ">

    <context:component-scan base-package="com.xiaoyu " />

    <import resource="classpath:beacon-server.xml" />

    </beans>   
```
6. 创建启动类

```
    public static void main(String[] args) throws Exception {
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-application.xml");
            try {
                CountDownLatch latch = new CountDownLatch(1);
                context.start();
                latch.await();
            } finally {
                context.stop();
                context.close();
            }
    }
```

### consumer端:
1. 在pom文件中引入:
```
<dependency>
    <groupId>com.xiaoyu</groupId>
    <artifactId>beacon</artifactId>
    <version>0.0.1</version>
</dependency>
```
2. 创建beacon-server.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:beacon="http://www.iwouldbe.com/schema/beacon"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.iwouldbe.com/schema/beacon http://www.iwouldbe.com/schema/beacon.xsd">

    <beacon:protocol name="beacon" port="1992" />

    <beacon:registry address="127.0.0.1:2181" protocol="zookeeper" />

    <beacon:reference id="helloService"
        interfaceName="com.xiaoyu.test.api.IHelloService" timeout="3000" retry="0"
        tolerant="failfast" group="dev" />

</beans>
```
3. 创建启动类

```
     public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("classpath:beacon-client.xml");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            // service调用
            IHelloService service = (IHelloService) context.getBean(IHelloService.class);
            System.out.println(service.hello("xiaoming"));
            latch.await();
        } finally {
            context.stop();
            context.close();
        }
    }
```
### 更多详细使用可见[beacon-example](https://github.com/dressrosa/beacon/tree/master/beacon-example)

## 配置

### consumer端常用配置: 
1. beacon:protocol 配置协议
2. beacon:registry 配置注册中心
3. beacon:reference 配置接口引用

**beacon:protocol:** 

| 字段名称 | 含义     | 备注               |
| -------- | -------- | ------------------ |
| name     | 协议名称 | 支持beacon或http   |
| port     | 暴露端口 | 用于连接provider端 |

**beacon:registry:** 

| 字段名称 | 含义         | 备注                  |
| -------- | ------------ | --------------------- |
| protocol | 协议名称     | 支持zookeeper         |
| address  | 注册中心地址 | 格式如:127.0.0.1:2181 |

**beacon:reference:** 

| 字段名称      | 含义         | 备注                                             |
| ------------- | ------------ | ------------------------------------------------ |
| id            | 接口id.      | 如userService                                    |
| interfaceName | 接口名称     | 如 com.xxx.com.xxx.UserService                   |
| timeout       | 接口超时时间 | 默认3000(ms)                                     |
| retry         | 重试次数     | 默认0次                                          |
| tolerant      | 容错机制     | 默认failfast  支持failfast,failover              |
| group         | 接口分组     |
| check         | 启动时检查   | true或false                                      |
| generic       | 泛型接口     | true或false                                      |
| downgrade     | 降级策略     | 支持timeout:xxx或retry:xxx或fault:xxx或limit:xxx |

### provider端常用配置:

### provider端配置释义: 
1. beacon:protocol 配置协议
2. beacon:registry 配置注册中心
3. beacon:exporter 配置接口暴露

**beacon:protocol:** 

| 字段名称 | 含义     | 备注               |
| -------- | -------- | ------------------ |
| name     | 协议名称 | 支持beacon或http   |
| port     | 暴露端口 | 用于连接provider端 |

**beacon:registry:** 

| 字段名称 | 含义         | 备注                  |
| -------- | ------------ | --------------------- |
| protocol | 协议名称     | 支持zookeeper         |
| address  | 注册中心地址 | 格式如:127.0.0.1:2181 |

**beacon:exporter:** 

| 字段名称      | 含义           | 备注                               |
| ------------- | -------------- | ---------------------------------- |
| id            | 接口id.        | 如userService                      |
| interfaceName | 接口名称       | 如 com.xxx.UserService             |
| ref           | 接口实现类     | 如com.xxx.UserServiceImpl          |
| methods       | 接口暴露的方法 | 按逗号分隔,默认全部暴露            |
| tolerant      | 容错机制       | 默认failfast 支持failfast,failover |
| group         | 接口分组       |

## 泛型调用
泛型调用的作用是无需配置consumer信息来直接调用provider端方法.  
### 使用方法
1. 在consumer端配置文件中<beacon:reference />中增加配置
generic=true(默认false),来启动泛型调用.  
2.beacon提供了一个通用泛型接口GenericService
```
public interface GenericService {

    /**
     * @param method
     *            需要调用的方法
     * @param returnType
     *            返回类型
     * @param args
     *            参数
     * @return
     */
    public Object $_$invoke(String method, Object returnType, Object[] args);
}
```
2.在需要调用的地方
```
public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("classpath:beacon-client.xml");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            // 泛型调用
            GenericReference refer = new GenericReference();
            //设置相关信息
            refer.setInterfaceName("com.xiaoyu.test.api.IHelloService")
                    .setTimeout("3000")
                    .setGroup("dev");
            GenericService generic = GenericRequestLauncher.launch(refer);
            Object result = generic.$_$invoke("hello", String.class, new Object[] { "cat" });
            System.out.println("re:" + result);
            latch.await();
        } finally {
            context.stop();
            context.close();
        }
    }
```

## 使用Spring boot:
[beacon-spring-boot-starter](https://github.com/dressrosa/beacon-spring-boot-starter)
## 目标:
健壮,扩展,优化细节.