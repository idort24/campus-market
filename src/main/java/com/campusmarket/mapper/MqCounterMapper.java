package com.campusmarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusmarket.entity.MqCounter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 生产计数 Mapper
 */
@Mapper
public interface MqCounterMapper extends BaseMapper<MqCounter> {

    /** produced 计数 +1，行不存在时由调用方补插 */
    @Update("UPDATE mq_demo.mq_counter SET counter_value = counter_value + 1 " +
            "WHERE counter_key = 'produced'")
    int incrementProduced();
}
