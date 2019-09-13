package com.example.mybatis.config.importannotationtest;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @auther chen.haitao
 * @description
 * @date 2019-09-13
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(UserImportSelector.class)
public @interface EnableUser {
    String name() default "default";
}



