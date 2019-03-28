/**
 * 不要因为走了很远就忘记当初出发的目的:whatever happened,be yourself
 */
package com.xiaoyu.test.main;

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
public class SpringbootClientApplication {

    public static void main(String args[]) {
        ConfigurableApplicationContext ctx =  SpringApplication.run(SpringbootClientApplication.class);
        ctx.toString();
    }
}
