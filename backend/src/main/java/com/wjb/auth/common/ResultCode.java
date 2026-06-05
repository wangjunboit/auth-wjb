package com.wjb.auth.common;

/** 统一返回码 */
public enum ResultCode {
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "登录已过期,请重新登录"),
    FORBIDDEN(403, "无操作权限"),
    ERROR(500, "系统繁忙");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() { return code; }
    public String getMsg() { return msg; }
}
