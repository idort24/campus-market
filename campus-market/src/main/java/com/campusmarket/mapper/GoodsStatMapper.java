package com.campusmarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusmarket.entity.GoodsStat;
import org.apache.ibatis.annotations.Mapper;

/**
 * 继承 BaseMapper 后自动拥有 CRUD 方法，无需手写 XML
 */
@Mapper
public interface GoodsStatMapper extends BaseMapper<GoodsStat> {
}
