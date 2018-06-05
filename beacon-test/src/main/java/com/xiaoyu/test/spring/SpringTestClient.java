package com.xiaoyu.test.spring;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
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
            CountDownLatch latch = new CountDownLatch(1);
            CyclicBarrier cb = new CyclicBarrier(10);
            IHelloService service = (IHelloService) context.getBean(IHelloService.class);
            for (int i = 0; i < 1; i++) {
                 int a = ato1.getAndIncrement();
                 System.out.println(service.hello("xiao鱼" + a) + a);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //cb.await();
                            int a = ato1.getAndIncrement();
                            System.out.println(service.hello("xiao鱼" + a) + a);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("失败个数:" + ato.getAndIncrement());
                            // System.out.println(e.getMessage());
                        }

                    }
                }).start();
            }
            latch.await(6, TimeUnit.SECONDS);
        } finally {
            context.stop();
            context.close();
        }
    }
}
