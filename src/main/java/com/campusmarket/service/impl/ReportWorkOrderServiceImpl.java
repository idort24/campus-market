package com.campusmarket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.entity.ReportWorkOrder;
import com.campusmarket.mapper.ReportWorkOrderMapper;
import com.campusmarket.service.ReportWorkOrderService;
import org.springframework.stereotype.Service;

@Service
public class ReportWorkOrderServiceImpl extends ServiceImpl<ReportWorkOrderMapper, ReportWorkOrder> implements ReportWorkOrderService {
}
