/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test.main;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.xiaoyu.beacon.common.generic.GenericReference;
import com.xiaoyu.beacon.proxy.common.GenericRequestLauncher;
import com.xiaoyu.beacon.rpc.service.GenericService;
import com.xiaoyu.test.api.IHelloService;

/**
 * @author hongyu
 * @date 2018-08
 * @description
 */
public class SpringTestClient {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("classpath:beacon-client.xml");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            // service调用
            IHelloService service = (IHelloService) context.getBean(IHelloService.class);
            System.out.println(service.hello("xiaoming"));
            // 泛型调用
            GenericReference refer = new GenericReference();
            refer.setInterfaceName("com.xiaoyu.test.api.IHelloService")
                    .setTimeout("3000")
                    .setGroup("dev");
            for (int i = 0; i < 10; i++) {
                GenericService generic = GenericRequestLauncher.launch(refer);
                Object result = generic.$_$invoke("hello", String.class, new Object[] { "cat" },
                        new Class<?>[] { String.class });
                System.out.println("re:" + result);
            }

            latch.await();
        } finally {
            context.stop();
            context.close();
        }
    }
}
