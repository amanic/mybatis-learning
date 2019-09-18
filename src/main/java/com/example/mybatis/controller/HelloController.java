package com.example.mybatis.controller;

import com.example.mybatis.config.importannotationtest.User;
import com.example.mybatis.dao.DemoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auther chen.haitao
 * @date 2019-09-12
 */
@RestController
@RequestMapping("hello")
//@ConditionalOnClass(name = "com.example.mybatis.config.conditiontest.MyConditionalClass")
@ConditionalOnBean(value = User.class)
public class HelloController {

    @Autowired
    DemoDao demoDao;

    @GetMapping("hello")
    public String hello(){
        Integer total = demoDao.getDiaryById(2);
        return "hello"+ total;
    }
}
