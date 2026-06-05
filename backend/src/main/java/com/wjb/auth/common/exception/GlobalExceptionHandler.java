package com.wjb.auth.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.wjb.auth.common.Result;
import com.wjb.auth.common.ResultCode;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 全局异常处理,统一转 Result */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 未登录 / token 失效 */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLogin(NotLoginException e) {
        return Result.fail(ResultCode.UNAUTHORIZED);
    }

    /** 无权限码 */
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermission(NotPermissionException e) {
        return Result.fail(ResultCode.FORBIDDEN.getCode(), "无操作权限");
    }

    /** 无角色 */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRole(NotRoleException e) {
        return Result.fail(ResultCode.FORBIDDEN.getCode(), "无访问权限");
    }

    /** 业务异常 */
    @ExceptionHandler(ServiceException.class)
    public Result<Void> handleService(ServiceException e) {
        return Result.fail(ResultCode.ERROR.getCode(), e.getMessage());
    }

    /** 参数校验异常 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        String msg = fe == null ? "参数错误" : fe.getDefaultMessage();
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /** 兜底 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 实际项目此处应记录日志:log.error("系统异常", e);
        return Result.fail(ResultCode.ERROR);
    }
}
