package com.campusmarket.common;

/**
 * 统一返回结果封装类
 */
public class R<T> {

    private Integer code;
    private String msg;
    private T data;

    // 成功：带提示信息
    public static <T> R<T> ok(String msg) {
        R<T> r = new R<>();
        r.setCode(1);
        r.setMsg(msg);
        return r;
    }

    // 成功：默认提示信息
    public static <T> R<T> ok() {
        return R.ok("操作成功");
    }

    // 成功：返回数据
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(1);
        r.setMsg("成功");
        r.setData(data);
        return r;
    }

    // 失败：带提示信息
    public static <T> R<T> fail(String msg) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }

    // 失败：默认提示信息
    public static <T> R<T> fail() {
        return R.fail("操作失败");
    }

    // 成功：兼容 success 调用
    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.setCode(1);
        r.setMsg("成功");
        r.setData(data);
        return r;
    }

    public static <T> R<T> success() {
        return R.success(null);
    }

    // 自定义状态码和提示信息（用来返回401/403/400这些）
    public static <T> R<T> error(Integer code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    // Getter 和 Setter
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
