/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test.main;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author hongyu
 * @date 2018-08
 * @description
 */
public class SpringTestServer {

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:beacon-server.xml");
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
