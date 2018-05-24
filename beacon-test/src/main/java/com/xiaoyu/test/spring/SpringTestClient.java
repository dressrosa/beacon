package com.xiaoyu.test.spring;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.xiaoyu.test.api.IHelloService;

public class SpringTestClient {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("classpath:beacon-client.xml");
        try {
            for (int i = 0; i < 20; i++) {
                IHelloService service = (IHelloService) context.getBean(IHelloService.class);
                System.out.println(service.hello("xiao鱼"+i) + i);
                // CountDownLatch latch = new CountDownLatch(1);
                // latch.await();
            }

        }

        finally {
            context.stop();
            context.close();
        }
    }
}
