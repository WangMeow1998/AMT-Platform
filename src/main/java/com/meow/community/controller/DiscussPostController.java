package com.meow.community.controller;


import com.meow.community.entity.*;
import com.meow.community.event.EventProducer;
import com.meow.community.service.CommentService;
import com.meow.community.service.DiscussPostService;
import com.meow.community.service.LikeService;
import com.meow.community.service.UserService;
import com.meow.community.util.CommunityConstant;
import com.meow.community.util.CommunityUtil;
import com.meow.community.util.HostHolder;
import com.meow.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

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

        //触发发帖事件，将帖子保存到Elasticsearch服务器中
        Event publishEvent = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(publishEvent);

        //缓存需要计算分数的贴子
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());


        //报错的情况，等着将来会处理。
        return CommunityUtil.getJSONString(0,"发布成功!");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String findDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);

        //点赞数
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);

        //点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        //作者
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);

        //评论与回复
        page.setLimit(5);
        page.setPath("/discussPost/detail/" + discussPostId);
        page.setRows(commentService.findCommentRows(ENTITY_TYPE_POST, discussPostId));

        //评论列表
        List<Comment> commentList = commentService.findComments(ENTITY_TYPE_POST, discussPostId,
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

                //点赞数
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentMap.put("likeCount", likeCount);
                //点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentMap.put("likeStatus", likeStatus);

                //回复列表（从0开始，不分页）
                List<Comment> replyList = commentService.findComments(ENTITY_TYPE_COMMENT, comment.getId(),
                        0, Integer.MAX_VALUE);
                //回复的ValueObject列表
                List<Map<String, Object>> replyValObjList = new ArrayList<>();
                if(replyList != null){
                    for(Comment reply : replyList){
                        //回复的ValueObject
                        Map<String, Object> replyMap = new HashMap<>();
                        //回复的作者
                        replyMap.put("user", userService.findUserById(reply.getUserId()));
                        //回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyMap.put("target", target);
                        //回复
                        replyMap.put("reply", reply);

                        //点赞数
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyMap.put("likeCount", likeCount);
                        //点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyMap.put("likeStatus", likeStatus);


                        replyValObjList.add(replyMap);
                    }
                }
                commentMap.put("replys", replyValObjList);

                //回复数量
                int replyCount = commentService.findCommentRows(ENTITY_TYPE_COMMENT, comment.getId());
                commentMap.put("replyCount", replyCount);

                commentValObjList.add(commentMap);
            }
        }
        model.addAttribute("comments",commentValObjList);
        return "/site/discuss-detail";
    }


    //帖子置顶
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        //更新帖子的类型 '0-普通; 1-置顶'
        discussPostService.updateDiscussPostType(id, 1);

        //触发发帖事件，将帖子保存到Elasticsearch服务器中
        Event publishEvent = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id);
        eventProducer.fireEvent(publishEvent);

        return CommunityUtil.getJSONString(0);
    }

    //帖子取消置顶
    @RequestMapping(path = "/notTop", method = RequestMethod.POST)
    @ResponseBody
    public String setNotTop(int id){
        //更新帖子的类型 '0-普通; 1-置顶'
        discussPostService.updateDiscussPostType(id, 0);

        //触发发帖事件，将帖子保存到Elasticsearch服务器中
        Event publishEvent = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id);
        eventProducer.fireEvent(publishEvent);

        return CommunityUtil.getJSONString(0);
    }

    //帖子加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        //更新帖子的状态 '0-正常; 1-精华; 2-拉黑;'
        discussPostService.updateDiscussPostStatus(id, 1);

        //触发发帖事件，将帖子保存到Elasticsearch服务器中
        Event publishEvent = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id);
        eventProducer.fireEvent(publishEvent);

        //缓存需要计算分数的贴子
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    //帖子取消加精
    @RequestMapping(path = "/notWonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setNotWonderful(int id){
        //更新帖子的状态 '0-正常; 1-精华; 2-拉黑;'
        discussPostService.updateDiscussPostStatus(id, 0);

        //触发发帖事件，将帖子保存到Elasticsearch服务器中
        Event publishEvent = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id);
        eventProducer.fireEvent(publishEvent);

        //缓存需要计算分数的贴子
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    //帖子删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        //更新帖子的状态 '0-正常; 1-精华; 2-拉黑;'
        discussPostService.updateDiscussPostStatus(id, 2);

        //触发删帖事件，将帖子保存到Elasticsearch服务器中
        Event deleteEvent = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id);
        eventProducer.fireEvent(deleteEvent);

        return CommunityUtil.getJSONString(0);
    }
}
