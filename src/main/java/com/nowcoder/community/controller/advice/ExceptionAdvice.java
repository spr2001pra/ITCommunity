package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handleExpection(Exception e, HttpServletResponse response, HttpServletRequest request) throws IOException {

        log.error("服务器发生异常：" + e.getMessage());
        for(StackTraceElement element:e.getStackTrace()){
            log.error(element.toString());
        }

        // 区分请求是同步请求(返回html网页)还是异步请求(返回JSON字符串)
        String xRequestedWith = request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(xRequestedWith)){
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常！"));
        }else {
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
