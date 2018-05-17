# beacon
(beacon:灯塔.意为希望所有的技术都是为了照亮程序员前进的放向.)
1. 通信层:netty
2. 注册中心:zookeeper
3. 扩展机制:java spi   
4. 兼容spring
测试类:
```
com.xiaoyu.test.rpc.spring.SpringTestClient
com.xiaoyu.test.rpc.spring.SpringTestServer
```
#### 基本使用:

##### 配置:
consumer端配置:

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:beacon="http://www.iwouldbe.com/schema/beacon"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.iwouldbe.com/schema/beacon http://www.iwouldbe.com/schema/beacon.xsd">


    <beacon:registry address="127.0.0.1:2181" protocol="zookeeper" />

    <beacon:reference id="helloService"
        interfaceName="com.xiaoyu.test.api.IHelloService" />

</beans>
```
provider端的配置:

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:beacon="http://www.iwouldbe.com/schema/beacon"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.iwouldbe.com/schema/beacon http://www.iwouldbe.com/schema/beacon.xsd">


    <beacon:protocol name="beacon" port="1992" />

    <beacon:registry address="127.0.0.1:2181" protocol="zookeeper" />

    <beacon:exporter id="helloService"
        interfaceName="com.xiaoyu.test.api.IHelloService" ref="com.xiaoyu.test.api.HelloServiceImpl" />

    <beacon:exporter id="userService"
        interfaceName="com.xiaoyu.test.api.IUserService" ref="com.xiaoyu.test.api.UserServiceImpl" />

</beans>
```

#### 目标:
健壮,扩展,优化细节.
=======
