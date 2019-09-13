package com.example.mybatis.mybatisdemo;

import com.example.mybatis.config.importannotationtest.EnableUser;
import com.example.mybatis.config.importannotationtest.User;
import com.example.mybatis.config.importannotationtest.UserRunnable;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.mybatis")
@MapperScan(value = "com.example.mybatis")
@EnableAsync
@EnableUser
public class MybatisDemoApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = null;
		try {
			context = SpringApplication.run(MybatisDemoApplication.class, args);
			context.getBean(Runnable.class).run();
			System.out.println("main");
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			context.close();
		}
	}

}
