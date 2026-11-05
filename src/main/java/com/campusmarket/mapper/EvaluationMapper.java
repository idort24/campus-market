package com.campusmarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusmarket.entity.Evaluation;
import org.apache.ibatis.annotations.Mapper;

// 继承 BaseMapper 后，自动拥有 CRUD 方法，无需手写 XML
@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {
}
