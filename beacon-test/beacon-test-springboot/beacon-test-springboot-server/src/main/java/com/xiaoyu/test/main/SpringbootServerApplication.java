/**
 * 不要因为走了很远就忘记当初出发的目的:whatever happened,be yourself
 */
package com.xiaoyu.test.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.xiaoyu.beacon.autoconfigure.EnableBeacon;

@SpringBootApplication
@ComponentScan(basePackages = { "com.xiaoyu.test" })
@EnableBeacon
public class SpringbootServerApplication {

    public static void main(String args[]) {
        ConfigurableApplicationContext context = SpringApplication.run(SpringbootServerApplication.class);
        System.out.println(context.getStartupDate());
    }
}
