package com.example.mybatis.controller;

import com.example.mybatis.dao.DemoDao;
import com.example.mybatis.model.TempTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auther chen.haitao
 * @date 2019-09-12
 */
@RestController
@RequestMapping("hello")
public class HelloController {

    @Autowired
    DemoDao demoDao;

    @GetMapping("hello")
    public String hello(){
        Integer total = demoDao.getDiaryById(2);
        return "hello"+ total;
    }
}
