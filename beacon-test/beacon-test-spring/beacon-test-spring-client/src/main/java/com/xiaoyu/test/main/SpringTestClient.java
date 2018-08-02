package com.xiaoyu.test.main;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.xiaoyu.core.rpc.service.GenericService;
import com.xiaoyu.filter.generic.GenericReference;
import com.xiaoyu.filter.generic.GenericRequestLauncher;
import com.xiaoyu.test.api.IHelloService;

public class SpringTestClient {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("classpath:beacon-client.xml");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            IHelloService service = (IHelloService) context.getBean(IHelloService.class);
            System.out.println(service.hello("xiaoming"));
            GenericReference refer = new GenericReference();
            refer.setInterfaceName("com.xiaoyu.test.api.IUserService")
                    .setTimeout("3000")
                    .setGroup("produce");

            for (int i = 0; i < 10; i++) {
                GenericService generic = GenericRequestLauncher.launch(refer);
                Object result = generic.$_$invoke("name", new String[] { "String" }, new Object[] { "cat" });
                System.out.println("re:" + result);
            }
            latch.await();
        } finally {
            context.stop();
            context.close();
        }
    }
}
