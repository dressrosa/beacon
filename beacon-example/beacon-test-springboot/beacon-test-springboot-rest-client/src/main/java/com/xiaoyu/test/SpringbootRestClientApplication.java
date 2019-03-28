/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.xiaoyu.beacon.starter.EnableBeacon;

@SpringBootApplication
@ComponentScan(basePackages = { "com.xiaoyu.test" })
@EnableBeacon
@EnableWebMvc
public class SpringbootRestClientApplication {

    public static void main(String args[]) {
        ConfigurableApplicationContext ctx = SpringApplication.run(SpringbootRestClientApplication.class);
        ctx.toString();
    }
}
