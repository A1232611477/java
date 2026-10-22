package com.fenglin.springboottest.exception;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.common.Result;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * REST 接口的全局异常处理：将异常统一转换为 {@link Result}（HTTP 状态码仍是 200）
 *
 * <p><b>注意 basePackages 的范围</b>：只挂在 {@code com.fenglin.springboottest.controller}
 * 这个包（AuthController / ApiUserController）上。后端的 Thymeleaf 页面流程在
 * {@code com.fenglin.springboottest.web} 包里，返回的是 HTML，不能吃这套 JSON 包装
 * ——否则页面出错时会试图把 Result 序列化成 text/html 而二次失败。</p>
 */
@RestControllerAdvice(basePackages = "com.fenglin.springboottest.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("参数校验失败");
        return Result.error(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        return Result.error(500, e.getMessage());
    }
}
