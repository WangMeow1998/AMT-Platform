package com.meow.community.controller;

import com.meow.community.entity.User;
import com.meow.community.service.UserService;
import com.meow.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class RegisterController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage(Model model){
        return "/site/register";
    }

    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()){ //注册时无相关的错误信息，正常注册
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }
        else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }
    //http://localhost:8080/community/activation/101/activationcode
    @RequestMapping(path = "/activation/{userId}/{activationCode}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("activationCode") String activationCode){
        int activationRes = userService.activation(userId, activationCode);
        if(activationRes == ACTIVATION_SUCCESS){ //激活成功后，提示，并跳转到登录界面
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target","/login");
        }else if(activationRes == ACTIVATION_REPEAT){ //重复激活，提示，并跳转到首页
            model.addAttribute("msg","无效操作，该账号已经激活过了！");
            model.addAttribute("target","/index");
        }else{ //激活失败，提示，并跳转到首页
            model.addAttribute("msg","激活失败，您点击的激活链接有误！");
            model.addAttribute("target","index");
        }
        return "/site/operate-result";
    }
}
