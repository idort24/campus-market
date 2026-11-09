package com.campusmarket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.campusmarket.cache.CacheStore;
import com.campusmarket.common.CacheKeys;
import com.campusmarket.entity.DictData;
import com.campusmarket.service.DictDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典数据启动预热
 *
 * 吴老师要求：鉴权后的热点/字典数据"一次加载、多次使用"。
 * 启动时把字典按类型分组全部塞进内存缓存，运行期不再因字典查询打 MySQL。
 * 静态字典不设 TTL；将来加写操作时"先写库、再删缓存"。
 */
@Component
public class DictWarmupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DictWarmupRunner.class);

    @Autowired
    private DictDataService dictDataService;

    @Autowired
    private CacheStore cacheStore;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        List<DictData> all = dictDataService.list();
        List<String> types = all.stream()
                .map(DictData::getDictType)
                .distinct()
                .collect(Collectors.toList());

        int hit = 0;
        for (String type : types) {
            List<DictData> list = all.stream()
                    .filter(d -> type.equals(d.getDictType()))
                    .sorted((a, b) -> {
                        int sa = a.getSort() == null ? 0 : a.getSort();
                        int sb = b.getSort() == null ? 0 : b.getSort();
                        return Integer.compare(sa, sb);
                    })
                    .collect(Collectors.toList());
            try {
                cacheStore.set(CacheKeys.DICT + type, objectMapper.writeValueAsString(list), 0);
                hit++;
            } catch (Exception e) {
                log.warn("字典预热失败: type={}", type, e);
            }
        }
        log.info("字典缓存预热完成：{} 个类型，{} 条数据", hit, all.size());
    }
}
