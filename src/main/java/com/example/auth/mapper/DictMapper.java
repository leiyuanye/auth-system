package com.example.auth.mapper;

import com.example.auth.entity.Dict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DictMapper {
    List<Dict> selectByType(@Param("dictType") String dictType);
    int insert(Dict dict);
    int update(Dict dict);
    int deleteById(@Param("id") Long id);
}
