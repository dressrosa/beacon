package com.xiaoyu.test.main;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.xiaoyu.test.api.IHelloService;

public class SpringTestClient {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("classpath:beacon-client.xml");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            IHelloService service = (IHelloService) context.getBean(IHelloService.class);
            System.out.println(service.hello("xiaoming"));
            latch.await();
        } finally {
            context.stop();
            context.close();
        }
    }
}
