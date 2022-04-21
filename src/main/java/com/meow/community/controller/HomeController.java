package com.meow.community.controller;

import com.meow.community.dao.DiscussPostMapper;
import com.meow.community.dao.UserMapper;
import com.meow.community.entity.DiscussPost;
import com.meow.community.entity.Page;
import com.meow.community.entity.User;
import com.meow.community.service.DiscussPostService;
import com.meow.community.service.LikeService;
import com.meow.community.service.UserService;
import com.meow.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        /*对于Page page形参：
        方法调用前，SpringMVC会自动实例化Model和Page，并将Page注入Model
        所以。在thymeleaf中可以直接访问Page对象中的数据
        */
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        page.setLimit(10);

        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());

        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(list != null){
            for(DiscussPost discussPost : list){
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST_COMMENT, discussPost.getId());
                map.put("likeCount", likeCount);
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user", user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/index";  //对应/resources/templates/index.html
    }
}
