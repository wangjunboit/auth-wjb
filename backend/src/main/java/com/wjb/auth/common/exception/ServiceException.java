package com.wjb.auth.common.exception;

/** 业务异常,携带提示信息 */
public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}
