package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;


@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){ // 判断要拦截的对象是否是方法(HandlerMethod)
            HandlerMethod handlerMethod = (HandlerMethod) handler; // 强制由object转换成HandlerMethod类型，便于处理
            Method method = handlerMethod.getMethod();// 调用该类型的getMethod()方法获取method对象
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class); // 从方法对象上取方法的注解
            if(loginRequired != null && hostHolder.getUser() == null){ // loginRequired != null说明该方法需要登录才能访问
                response.sendRedirect(request.getContextPath() + "/login"); // 因为是实现接口定义的方法，不能直接像Controller中return redirect，Controller中底层实际上也是这样写的
                return false; // 拒绝后续请求
            }
        }
        return true;
    }

//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        if (handler instanceof HandlerMethod) {
//            HandlerMethod handlerMethod = (HandlerMethod) handler;
//            Method method = handlerMethod.getMethod();
////            MethodInvocation invocation = null;
////            Method method = invocation.getThis().getClass().getDeclaredMethod(invocation.getMethod().getName(),invocation.getMethod().getParameterTypes());
//            LoginRequired loginRequired = method.getDeclaredAnnotation(LoginRequired.class);
//            if (loginRequired != null && hostHolder.getUser() == null) {
//                response.sendRedirect(request.getContextPath() + "/login");
//                return false;
//            }
//        }
//        return true;
//    }

}


