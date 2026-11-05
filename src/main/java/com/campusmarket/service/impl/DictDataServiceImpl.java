package com.campusmarket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.entity.DictData;
import com.campusmarket.mapper.DictDataMapper;
import com.campusmarket.service.DictDataService;
import org.springframework.stereotype.Service;

@Service
public class DictDataServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataService {
}
