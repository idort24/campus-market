package com.campusmarket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campusmarket.entity.MqCounter;

public interface MqCounterService extends IService<MqCounter> {

    /** 生产者累计发送数 +1（行不存在则初始化为 1） */
    void incrementProduced();

    /** 当前生产总数 */
    long getProduced();

    /** 重复投递被幂等拦截 +1（行不存在则初始化为 1） */
    void incrementDedupBlocked();

    /** 被幂等拦截的重复投递条数 */
    long getDedupBlocked();
}
