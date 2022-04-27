package com.meow.community.controller.interceptor;

import com.meow.community.entity.LoginTicket;
import com.meow.community.entity.User;
import com.meow.community.service.UserService;
import com.meow.community.util.CookieUtil;
import com.meow.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
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


    //在Controller之前执行
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

                //SecurityContext存储认证的结果，便于Security进行授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    //在Controller之后执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //在模板引擎之前获取User
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser",user);
        }
    }

    //在TemplateEngine之后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();

        /**
         * SecurityContextHolder.clearContext();这句话要注释掉！！！
         * 点击私信后，在页面完成渲染后执行了afterCompletion中的SecurityContextHolder.clearContext()
         * 已经将Security存储的认证清除掉了
         * 然后再点具体通知时，由于Filter在Interceptor之前，
         * 所以经过filter时发现原有的认证已经被清除掉了
         * 就会被拦截访问通知的请求
         * 而由于Filter在Interceptor之前
         * 设置SecurityContextHolder是在之后的interceptor中
         * 所以就无法使Security存储认证，就会一直强制让你登录
         *
         * 和hostHolder不一样的点在于：
         * hostHolder的拦截、set、clear方法都在interceptor中
         * 而SecurityContextHolder只有set和clear方法在interceptor中
         * 而拦截过程在interceptor之前的filter中
         */
        //SecurityContextHolder.clearContext();
    }
}
