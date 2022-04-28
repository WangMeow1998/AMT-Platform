package com.meow.community.config;


import com.meow.community.util.CommunityConstant;
import com.meow.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        //忽略掉所有的静态资源，不拦截静态资源
        web.ignoring().antMatchers("/resources/**");
    }

    //本项目已经写好了登录认证方案，因此绕过Security认证流程
    //....

    //授权
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discussPost/add",
                        "/comment/add/**",
                        "/conversation/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority( //以上URL的请求，需要满足下列三个权限才能访问
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discussPost/top",
                        "/discussPost/wonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discussPost/delete",
                        "/data/**"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll(); //除了以上请求外，其他的请求都允许访问

        //权限不够时的处理
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    //没有登录
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with"); //从请求头部获取当前请求的方式是异步请求还是正常请求
                        if("XMLHttpRequest".equals(xRequestedWith)){ //如果是异步请求
                            //为了响应一个字符串，而进行设置
                            response.setContentType("application/plain;charset=utf-8");
                            //获取输出流
                            PrintWriter writer = response.getWriter();
                            //向前端输出一个JSON字符串
                            writer.write(CommunityUtil.getJSONString(403,"你还没有登录哦!"));
                        } else { //如果是正常请求的异常，则直接重定向到提示用户的错误页面
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    //权限不足
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with"); //从请求头部获取当前请求的方式是异步请求还是正常请求
                        if("XMLHttpRequest".equals(xRequestedWith)){ //如果是异步请求
                            //为了响应一个字符串，而进行设置
                            response.setContentType("application/plain;charset=utf-8");
                            //获取输出流
                            PrintWriter writer = response.getWriter();
                            //向前端输出一个JSON字符串
                            writer.write(CommunityUtil.getJSONString(403,"你没有访问此功能的权限!"));
                        } else { //如果是正常请求的异常，则直接重定向到提示用户的错误页面
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });

        //Security底层默认会拦截/logout请求，进行退出处理
        //覆盖它默认的逻辑，才能执行我们自己的退出代码
        //  在本项目中没有这个/securitylogout，只是为了骗过Spring Security
        http.logout().logoutUrl("/securitylogout");
    }
}
