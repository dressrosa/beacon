/**
 * 不要因为走了很远就忘记当初出发的目的:whatever happened,be yourself
 */
package com.xiaoyu.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.xiaoyu.beacon.autoconfigure.EnableBeacon;
import com.xiaoyu.test.api.IHelloService;

@SpringBootApplication
@ComponentScan(basePackages = { "com.xiaoyu.test" })
@EnableBeacon
@EnableWebMvc
public class TestApplication {

    public static void main(String args[]) {
        ConfigurableApplicationContext context = null;
       // SpringFactoriesLoader
        try {
            context = SpringApplication.run(TestApplication.class);
            // IHelloService service = (IHelloService) context.getBean(IHelloService.class);
            // System.out.println(service.hello("lan"));
        } finally {
            // context.stop();
            // context.close();
        }
    }
}
