package com.meow.community.controller.interceptor;

import com.meow.community.entity.LoginTicket;
import com.meow.community.entity.User;
import com.meow.community.service.UserService;
import com.meow.community.util.CookieUtil;
import com.meow.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从Cookie中获取ticket
        String ticket = CookieUtil.getValue(request,"ticket");
        //ticket不为null，则说明用户已经登录
        if(ticket != null){
            //查询凭证
            LoginTicket loginTicket = userService.getLoginTicket(ticket);
            //检查凭证是否有效 --> 不为空，且状态为登录状态，且Cookie的超时时间晚于当前时间
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                //根据登录凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有用户
                //由于用户请求服务器是并发的情况，应该单独为每一个用户开辟一个线程，防止出现并发冲突（这里利用ThreadLocal）
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //在模板引擎之前获取User
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
