/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.xiaoyu.beacon.starter.EnableBeacon;

@SpringBootApplication
@EnableWebMvc
@EnableBeacon
public class SpringBootRestServerApplication {

    public static void main(String args[]) {
        SpringApplication.run(SpringBootRestServerApplication.class);
    }
}
