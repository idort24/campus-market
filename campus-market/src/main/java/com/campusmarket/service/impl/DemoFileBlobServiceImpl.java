package com.campusmarket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.entity.DemoFileBlob;
import com.campusmarket.mapper.DemoFileBlobMapper;
import com.campusmarket.service.DemoFileBlobService;
import org.springframework.stereotype.Service;

@Service
public class DemoFileBlobServiceImpl extends ServiceImpl<DemoFileBlobMapper, DemoFileBlob> implements DemoFileBlobService {
}
