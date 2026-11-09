package com.campusmarket.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.campusmarket.cache.CacheStore;
import com.campusmarket.common.CacheKeys;
import com.campusmarket.common.R;
import com.campusmarket.entity.DictData;
import com.campusmarket.service.DictDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * 字典数据接口
 *
 * 采用 Cache Aside：
 * - 按 dictType 查询时先查缓存，命中直接返回；未命中查库后回填缓存；
 * - 字典属于静态数据，启动时预热、缓存不设 TTL；
 * - 后续如果加写接口，遵循"先写库、再删缓存"，不要先删缓存。
 */
@RestController
@RequestMapping("/dict/data")
public class DictDataController {

    private static final Logger log = LoggerFactory.getLogger(DictDataController.class);

    @Autowired
    private DictDataService dictDataService;

    @Autowired
    private CacheStore cacheStore;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 查询字典列表
     *
     * @param dictType 字典类型编码，可选；不传时返回全量（管理端用，不缓存）
     */
    @GetMapping("/list")
    public R<List<DictData>> list(@RequestParam(value = "dictType", required = false) String dictType) {
        if (dictType == null || dictType.isBlank()) {
            return R.ok(dictDataService.list());
        }

        String key = CacheKeys.DICT + dictType;
        String cached = cacheStore.get(key);
        if (cached != null) {
            try {
                return R.ok(objectMapper.readValue(cached, new TypeReference<List<DictData>>() {}));
            } catch (Exception e) {
                log.warn("字典缓存反序列化失败，回源查库: key={}", key);
            }
        }

        List<DictData> list = dictDataService.lambdaQuery()
                .eq(DictData::getDictType, dictType)
                .orderByAsc(DictData::getSort)
                .list();
        try {
            // 静态字典不设 TTL，启动预热时也会写一遍
            cacheStore.set(key, objectMapper.writeValueAsString(list), 0);
        } catch (Exception e) {
            log.warn("字典缓存写入失败: key={}", key, e);
        }
        return R.ok(list);
    }
}
