package com.xiaoyu.test.spring;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.xiaoyu.test.api.IHelloService;

public class SpringTest {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("classpath:beacon.xml");
        try {
            for (int i = 0; i < 1; i++) {
                IHelloService service = (IHelloService) context.getBean(IHelloService.class);
                System.out.println(service.hello("xiaoming"));
            }
        } finally {
            context.stop();
            context.close();
        }
    }
}
