package com.xiaoyu.test.spring;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringTestServer {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("classpath:beacon-server.xml");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            context.start();
            latch.await();
        } finally {
            context.stop();
            context.close();
        }
    }
}
