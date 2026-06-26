package com.example.auth.mapper;

import com.example.auth.entity.Dict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据字典 Mapper 接口
 * 对应表: sys_dict
 * 功能: 字典项的增删改查，用于动态下拉选项配置
 */
@Mapper
public interface DictMapper {

    /**
     * 按字典类型查询字典项列表
     * @param dictType 字典类型（如 server_status, phone_operator 等）
     * @return 字典项列表
     */
    List<Dict> selectByType(@Param("dictType") String dictType);

    /**
     * 根据ID查询字典项
     * @param id 字典ID
     * @return 字典项
     */
    Dict selectById(@Param("id") Long id);

    /**
     * 新增字典项
     * @param dict 字典实体
     * @return 影响行数
     */
    int insert(Dict dict);

    /**
     * 更新字典项
     * @param dict 字典实体（含ID）
     * @return 影响行数
     */
    int update(Dict dict);

    /**
     * 根据ID删除字典项
     * @param id 字典ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 级联更新：将业务表中引用旧值的字段更新为新值
     * @param tableName 表名
     * @param columnName 列名
     * @param oldValue 旧值
     * @param newValue 新值
     * @return 影响行数
     */
    int cascadeUpdateValue(@Param("tableName") String tableName,
                           @Param("columnName") String columnName,
                           @Param("oldValue") String oldValue,
                           @Param("newValue") String newValue);

    /**
     * 级联更新：将业务表中包含旧值的逗号分隔字段更新为新值
     * @param tableName 表名
     * @param columnName 列名
     * @param oldValue 旧值
     * @param newValue 新值
     * @return 影响行数
     */
    int cascadeUpdateCsvValue(@Param("tableName") String tableName,
                              @Param("columnName") String columnName,
                              @Param("oldValue") String oldValue,
                              @Param("newValue") String newValue);
}