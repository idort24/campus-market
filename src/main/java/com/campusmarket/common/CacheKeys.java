package com.campusmarket.common;

/**
 * 缓存键统一常量
 *
 * 所有 Redis/内存缓存键都从这里取，避免各处硬编码字符串导致对不上。
 */
public final class CacheKeys {

    private CacheKeys() {
    }

    /** 图形验证码：captcha:{captchaKey} -> 正确答案，TTL 60 秒 */
    public static final String CAPTCHA = "captcha:";

    /** token 黑名单：token:black:{token} -> 拉黑时间戳，TTL = token 剩余有效期 */
    public static final String TOKEN_BLACK = "token:black:";

    /** 登录失败次数限流：login:fail:{账号} */
    public static final String LOGIN_FAIL = "login:fail:";

    /** 字典/静态数据缓存：dict:{类型} */
    public static final String DICT = "dict:";
}
