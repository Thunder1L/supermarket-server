package com.supermarket.server.common.exception;

import com.supermarket.server.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器
 * 只要代码里抛出异常，都会被这里捕获，转为统一的 Result JSON 返回给前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获业务逻辑异常 (RuntimeException)
     * 例如: throw new RuntimeException("密码错误");
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage(), e); // 把堆栈也打印出来方便调试

        // 【关键修改】如果 e.getMessage() 是 null (比如空指针异常)，就给一个默认提示
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            message = "后端发生空指针异常或未知道错误";
        }

        return Result.error(message);
    }

    /**
     * 捕获系统级异常 (Exception)
     * 例如: 空指针异常、数据库连接失败等未预料到的错误
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error("系统繁忙，请联系管理员");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("文件上传过大: {}", e.getMessage());
        return Result.error("文件过大，请上传 10MB 以内的图片");
    }

}