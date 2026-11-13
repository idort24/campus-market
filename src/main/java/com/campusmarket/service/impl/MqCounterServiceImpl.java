package com.campusmarket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.entity.MqCounter;
import com.campusmarket.mapper.MqCounterMapper;
import com.campusmarket.service.MqCounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MqCounterServiceImpl extends ServiceImpl<MqCounterMapper, MqCounter>
        implements MqCounterService {

    @Autowired
    private MqCounterMapper counterMapper;

    @Override
    public void incrementProduced() {
        int rows = counterMapper.incrementProduced();
        if (rows == 0) {
            MqCounter counter = new MqCounter();
            counter.setCounterKey("produced");
            counter.setCounterValue(1L);
            save(counter);
        }
    }

    @Override
    public long getProduced() {
        MqCounter counter = lambdaQuery().eq(MqCounter::getCounterKey, "produced").one();
        return counter == null || counter.getCounterValue() == null ? 0L : counter.getCounterValue();
    }
}
