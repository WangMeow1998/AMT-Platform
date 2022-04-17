package com.meow.community.service;


import com.meow.community.dao.LoginTicketMapper;
import com.meow.community.dao.UserMapper;
import com.meow.community.entity.LoginTicket;
import com.meow.community.entity.User;
import com.meow.community.util.CommunityConstant;
import com.meow.community.util.CommunityUtil;
import com.meow.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain; //域名

    @Value("${server.servlet.context-path}")
    private String contextPath; //项目名

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg","该账号已存在");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg","该邮箱已存在");
            return map;
        }

        //用户注册
        user.setSalt(CommunityUtil.generateUUID().substring(0,5)); //给密码加“盐”
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt())); //密码加密
        user.setType(0); //0-普通用户; 1-超级管理员; 2-版主;
        user.setStatus(0); //0-未激活; 1-已激活;
        user.setActivationCode(CommunityUtil.generateUUID()); //设置用户的激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1001)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //http://localhost:8080/community/activation/101/activationcode  ---- 用户激活码的usl
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

    public int activation(int userId, String activationCode){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(activationCode)){
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","用户名不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }

        User user = userMapper.selectByName(username);
        //判断用户名是否正确
        if(user == null){
            map.put("usernameMsg","用户名不正确！");
            return map;
        }

        //判断密码是否正确
        password = CommunityUtil.md5(password + user.getSalt());
        if(!password.equals(user.getPassword())){
            map.put("passwordMsg","密码不正确！");
            return map;
        }

        //判断用户是否为激活
        if(user.getStatus() == 0){
            map.put("usernameMsg","该用户未激活！");
            return map;
        }

        //用户登录成功时，生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket,1);
    }

    public LoginTicket getLoginTicket(String ticket){ //根据当前凭证查询登录凭证记录
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId, String headerUrl){ //更新用户头像的路径
        return userMapper.updateHeader(userId, headerUrl);
    }
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword){
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if(StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg","原始密码不可为空!");
            return map;
        }
        if(StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg","新密码不可为空!");
            return map;
        }
        //验证原始密码
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if(!user.getPassword().equals(oldPassword)){
            map.put("oldPasswordMsg","原始密码不正确!");
            return map;
        }
        //更新密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);
        return map;
    }
}
