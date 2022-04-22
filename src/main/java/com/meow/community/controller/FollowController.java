package com.meow.community.controller;


import com.meow.community.entity.Page;
import com.meow.community.entity.User;
import com.meow.community.service.FollowService;
import com.meow.community.service.UserService;
import com.meow.community.util.CommunityConstant;
import com.meow.community.util.CommunityUtil;
import com.meow.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(1, "请登录后再关注!");
        }
        followService.follow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0,"关注成功!");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(1, "请登录后再取消关注!");
        }
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0,"取消关注成功!");
    }

    @RequestMapping(path = "/followings/{userId}", method = RequestMethod.GET)
    public String findFollowings(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);
        page.setPath("/followings/" + userId);
        page.setLimit(5);
        page.setRows((int) followService.findFollowingCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> followingUserList = followService.findFollowings(userId, page.getOffset(), page.getLimit());
        if(followingUserList != null){
            for(Map<String, Object> map : followingUserList){
                User followingUser = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(followingUser.getId()));
            }
        }
        model.addAttribute("followingUsers", followingUserList);
        return "/site/followee";
    }


    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String findFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);
        page.setPath("/followers/" + userId);
        page.setLimit(5);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String, Object>> followerUserList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if(followerUserList != null){
            for(Map<String, Object> map : followerUserList){
                User followerUser = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(followerUser.getId()));
            }
        }
        model.addAttribute("followerUsers", followerUserList);
        return "/site/follower";
    }

    public boolean hasFollowed(int entityId){
        if(hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, entityId);
    }
}
