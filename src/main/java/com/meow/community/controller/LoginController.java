package com.meow.community.controller;

import com.google.code.kaptcha.Producer;
import com.meow.community.service.UserService;
import com.meow.community.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(Model model){
        return "/site/login";
    }

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private UserService userService;

    @Value("${server.servlet.context-path}")
    private String contextPath; //项目名

    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码存入Session
        session.setAttribute("kaptcha",text);

        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean isRemember,
                        HttpServletResponse response, HttpSession session, Model model){
        //1.先验证激活码是否正确
        String kaptchaCode = (String) session.getAttribute("kaptcha");
        if(StringUtils.isBlank(kaptchaCode) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(kaptchaCode)){
            model.addAttribute("codeMsg","验证码不正确！");
            return "/site/login";
        }
        //2.再验证账号和密码是否正确
        int expiredTime = isRemember ? REMEMBER_EXPIRED_TIME : DEFAULT_EXPIRED_TIME;
        Map<String, Object> map = userService.login(username,password,expiredTime);
        if(!map.containsKey("ticket")){ //账号和密码等不正确
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }else {
            //登录成功时，将登录凭证加到Cookie中返回给客户端
            Cookie cookie = new Cookie("ticket", (String) map.get("ticket"));
            cookie.setPath(contextPath); //Cookie的作用域
            cookie.setMaxAge(expiredTime); //Cookie的持续时间
            response.addCookie(cookie);
            return "redirect:/index";
        }
    }
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    //携带Cookie发送请求
    public String logout(@CookieValue("ticket") String cookieCode){
        userService.logout(cookieCode);
        return "redirect:/login";
    }
}
