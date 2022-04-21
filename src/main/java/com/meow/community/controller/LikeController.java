package com.meow.community.controller;


import com.meow.community.entity.User;
import com.meow.community.service.LikeService;
import com.meow.community.util.CommunityUtil;
import com.meow.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId){
        User user = hostHolder.getUser();
        if (user == null){
            return CommunityUtil.getJSONString(1,"请登录后再点赞!");
        }
        //点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);

        //点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);

        //点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        //返回结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        return CommunityUtil.getJSONString(0, null, map);
    }
}
