package com.example.mybatis.dao;

import com.example.mybatis.model.TempTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @auther chen.haitao
 * @date 2019-09-12
 */
@Mapper
public interface DemoDao {

    @Select("select count(*) from temp_table where uid = #{id}")
    Integer getDiaryById(@Param("id")Integer id);
}
