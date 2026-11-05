package com.campusmarket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.entity.GoodsStat;
import com.campusmarket.mapper.GoodsStatMapper;
import com.campusmarket.service.GoodsStatService;
import org.springframework.stereotype.Service;

@Service
public class GoodsStatServiceImpl extends ServiceImpl<GoodsStatMapper, GoodsStat> implements GoodsStatService {
}
