package com.meow.community.controller;


import com.meow.community.entity.Comment;
import com.meow.community.entity.DiscussPost;
import com.meow.community.entity.Page;
import com.meow.community.entity.User;
import com.meow.community.service.CommentService;
import com.meow.community.service.DiscussPostService;
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

import java.util.*;

@Controller
@RequestMapping(path = "/discussPost")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"用户未登录!");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);
        //报错的情况，等着将来会处理。
        return CommunityUtil.getJSONString(0,"发布成功!");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String findDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);

        //作者
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);

        //评论与回复
        page.setLimit(5);
        page.setPath("/discussPost/detail/" + discussPostId);
        page.setRows(commentService.findCommentRows(ENTITY_TYPE_POST_COMMENT, discussPostId));

        //评论列表
        List<Comment> commentList = commentService.findComments(ENTITY_TYPE_POST_COMMENT, discussPostId,
                page.getOffset(),page.getLimit());

        //评论的ValueObject列表
        List<Map<String, Object>> commentValObjList = new ArrayList<>();
        if(commentList != null){
            for (Comment comment : commentList){
                //评论的ValueObject
                Map<String, Object> commentMap = new HashMap<>();
                //评论者
                commentMap.put("user", userService.findUserById(comment.getUserId()));
                //评论
                commentMap.put("comment", comment);
                //回复列表（从0开始，不分页）
                List<Comment> replyList = commentService.findComments(ENTITY_TYPE_COMMENT_REPLY, comment.getId(),
                        0, Integer.MAX_VALUE);
                //回复的ValueObject列表
                List<Map<String, Object>> replyValObjList = new ArrayList<>();
                if(replyList != null){
                    for(Comment reply : replyList){
                        //回复的ValueObject
                        Map<String, Object> replyMap = new HashMap<>();
                        //回复者
                        replyMap.put("user", userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyMap.put("target", target);
                        //回复
                        replyMap.put("reply", reply);
                        replyValObjList.add(replyMap);
                    }
                }
                commentMap.put("replys", replyValObjList);

                //回复数量
                int replyCount = commentService.findCommentRows(ENTITY_TYPE_COMMENT_REPLY, comment.getId());
                commentMap.put("replyCount", replyCount);

                commentValObjList.add(commentMap);
            }
        }
        model.addAttribute("comments",commentValObjList);
        return "/site/discuss-detail";
    }
}
