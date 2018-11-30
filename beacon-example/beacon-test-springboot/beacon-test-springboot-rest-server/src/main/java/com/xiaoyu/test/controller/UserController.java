/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.test.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.xiaoyu.test.api.IUserService;

@RestController
public class UserController {

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "/age", method = RequestMethod.POST)
    public int age(HttpServletRequest request, HttpServletResponse response,
            @RequestBody Map<String, String> map) {
        return this.userService.age(map.get("name"));
    }

}
