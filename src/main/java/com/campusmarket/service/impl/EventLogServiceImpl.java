package com.campusmarket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.entity.EventLog;
import com.campusmarket.mapper.EventLogMapper;
import com.campusmarket.service.EventLogService;
import org.springframework.stereotype.Service;

@Service
public class EventLogServiceImpl extends ServiceImpl<EventLogMapper, EventLog> implements EventLogService {
}
