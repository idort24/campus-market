package com.campusmarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusmarket.entity.ConsultEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 咨询事件 Mapper，继承 BaseMapper 自动拥有 CRUD 方法
 */
@Mapper
public interface ConsultEventMapper extends BaseMapper<ConsultEvent> {
}
