package com.meow.community.controller.advice;


import com.meow.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常: " + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()){
            logger.error(element.toString());
        }
        String xRequestedWith = request.getHeader("x-requested-with"); //从请求头部获取当前请求的方式是异步请求还是正常请求
        if("XMLHttpRequest".equals(xRequestedWith)){ //如果是异步请求
            //为了响应一个字符串，而进行设置
            response.setContentType("application/plain;charset=utf-8");
            //获取输出流
            PrintWriter writer = response.getWriter();
            //向前端输出一个JSON字符串
            writer.write(CommunityUtil.getJSONString(1,"服务器异常"));
        } else { //如果是正常请求的异常，则直接重定向到提示用户的错误页面
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
