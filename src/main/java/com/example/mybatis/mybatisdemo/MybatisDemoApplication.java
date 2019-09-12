package com.example.mybatis.mybatisdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.mybatis")
@MapperScan(value = "com.example.mybatis")
public class MybatisDemoApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = null;
		try {
			context = SpringApplication.run(MybatisDemoApplication.class, args);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			context.close();
		}
	}

}
