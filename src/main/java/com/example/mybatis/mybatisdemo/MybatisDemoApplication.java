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

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.mybatis")
@MapperScan(value = "com.example.mybatis")
@EnableAsync
@EnableUser
public class MybatisDemoApplication {

	/**
	 * 这里需要分两种情况讨论：
	 * 一、 web工程(非SpringBoot) AbstractRefreshableApplicationContext,将bean定义加载到给定的BeanFactory中
	 * 		1. createBeanFactory(); 为此上下文创建内部 BeanFactory
	 * 		2. customizeBeanFactory(beanFactory); 定制 BeanFactory，是否允许 BeanDefinition 覆盖、是否允许循环引用
	 * 		3. loadBeanDefinitions(beanFactory); 通过 BeanDefinitionReader 解析 xml 文件，解析封装信息到 BeanDefinition，并将其 register 到 BeanFactory 中，以 beanName为key将beanDefinition 存	      *到 DefaultListableBeanFactory#beanDefinitionMap 中
	 * 二、 SpringBoot GenericApplicationContext，实际 register 过程在invokeBeanFactoryPostProcessors 中
	 */
	public static void main(String[] args) {
//		ConfigurableApplicationContext context = null;
//		try {
//			context = SpringApplication.run(MybatisDemoApplication.class, args);
////			context.getBean(Runnable.class).run();
////			System.out.println("main");
//			Arrays.stream(context.getBeanDefinitionNames()).forEach(System.out::println);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally {
//			context.close();
//		}
		String[] str = new String[] { "yang", "hao" };
		List list = Arrays.asList(str);
		list.add("yangguanbao");
	}

}
