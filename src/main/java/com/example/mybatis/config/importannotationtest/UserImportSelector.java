package com.example.mybatis.config.importannotationtest;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @auther chen.haitao
 * @date 2019-09-13
 */
public class UserImportSelector implements ImportSelector {
    /**
     * Select and return the names of which class(es) should be imported based on
     * the {@link AnnotationMetadata} of the importing @{@link Configuration} class.
     *
     * @param importingClassMetadata
     */
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        System.out.println(importingClassMetadata.getAllAnnotationAttributes(EnableUser.class.getName()));
        return new String[]{User.class.getName()};
    }
}
