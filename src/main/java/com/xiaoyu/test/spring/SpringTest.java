package com.xiaoyu.test.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.xiaoyu.spring.config.BeaconReference;
import com.xiaoyu.test.api.IHelloService;

public class SpringTest {

    public static void main(String[] args) {
        @SuppressWarnings("resource")
        
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:beacon.xml");
        try {
            for(int i =0; i < 200;i++) {
                IHelloService service = (IHelloService) context.getBean(IHelloService.class);
                System.out.println(service.hello("xiaoming"));
            }
        }
        finally {
            
        }
    }
}
