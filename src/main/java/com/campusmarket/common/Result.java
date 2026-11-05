package com.campusmarket.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    // 成功 code=1
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(1);
        r.setMsg("success");
        r.setData(data);
        return r;
    }
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败，和群里约定一致
    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
