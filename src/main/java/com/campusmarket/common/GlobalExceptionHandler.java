package com.campusmarket.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理
 *
 * 目的：任何未捕获异常都不再把 Java 堆栈直接抛给前端，
 * 统一转成 {code,msg,data} 结构，前端按同一套逻辑处理。
 *
 * 注意：500 场景只返回笼统提示，堆栈写日志，避免泄漏内部结构。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 请求体缺失或 JSON 格式错误 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return R.error(400, "请求参数格式错误");
    }

    /** 缺少必填请求参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return R.error(400, "缺少必要参数：" + e.getParameterName());
    }

    /** 参数类型不匹配，例如把汉字传给了 Long 类型的 id */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return R.error(400, "参数类型错误：" + e.getName());
    }

    /** 业务代码主动抛出的非法参数，按参数错误返回 */
    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return R.error(400, e.getMessage() == null ? "参数不合法" : e.getMessage());
    }

    /** 兜底：其余异常统一 500，堆栈只进日志 */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("未捕获异常", e);
        return R.error(500, "服务器开小差了，请稍后重试");
    }
}
