package com.xiaoyu.test.spring;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.xiaoyu.test.api.IHelloService;

public class SpringTestClient {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("classpath:beacon-client.xml");
        AtomicInteger ato = new AtomicInteger(0);
        AtomicInteger ato1 = new AtomicInteger(0);
        try {
            IHelloService service = (IHelloService) context.getBean(IHelloService.class);
            
            for (int i = 0; i < 100; i++) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            System.out.println(service.hello("xiao鱼") + ato1.getAndIncrement());
                        } catch (Exception e) {
                           System.out.println("失败个数:" +ato.getAndIncrement());
                            //System.out.println(e.getMessage());
                        }

                    }
                }).start();
            }
//            CountDownLatch latch = new CountDownLatch(1);
//            latch.await();
            TimeUnit.SECONDS.sleep(10);
        }
        finally {
            context.stop();
            context.close();
        }
    }
}
