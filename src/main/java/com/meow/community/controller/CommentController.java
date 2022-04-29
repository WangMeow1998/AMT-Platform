package com.meow.community.controller;


import com.meow.community.entity.Comment;
import com.meow.community.entity.DiscussPost;
import com.meow.community.entity.Event;
import com.meow.community.event.EventProducer;
import com.meow.community.service.CommentService;
import com.meow.community.service.DiscussPostService;
import com.meow.community.util.CommunityConstant;
import com.meow.community.util.HostHolder;
import com.meow.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping(path = "/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId()); //当前登录的用户，即评论的作者
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论通知事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if(comment.getEntityType() == ENTITY_TYPE_COMMENT){
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);


        //当有了新的评论的时候，评论的数量会发生改变，而disscuss_post表中有comment_count属性，所以这里修改了帖子
        //帖子更新了，要把更新的帖子再加到ElasticSearch服务器中
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            //触发更新帖子事件，将帖子保存到Elasticsearch服务器中
            Event updateEvent = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(updateEvent);

            //缓存需要计算分数的贴子 -- 有评论才缓存，回复不缓存
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, discussPostId);
        }

        return "redirect:/discussPost/detail/" + discussPostId;
    }
}
