package com.example.mybatis.config.importannotationtest;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @auther chen.haitao
 * @date 2019-09-13
 */
@Component
public class UserRunnable implements Runnable{

    @Override
    @Async
    public void run() {
        try{
            for (int i = 0; i <10 ; i++) {
                System.out.println("============"+i);

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}