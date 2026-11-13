package com.campusmarket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.entity.ConsultEvent;
import com.campusmarket.mapper.ConsultEventMapper;
import com.campusmarket.service.ConsultEventService;
import org.springframework.stereotype.Service;

@Service
public class ConsultEventServiceImpl extends ServiceImpl<ConsultEventMapper, ConsultEvent> implements ConsultEventService {
}
