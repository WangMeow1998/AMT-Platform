package com.meow.community.controller;


import com.alibaba.fastjson.JSONObject;
import com.meow.community.entity.Message;
import com.meow.community.entity.Page;
import com.meow.community.entity.User;
import com.meow.community.service.MessageService;
import com.meow.community.service.UserService;
import com.meow.community.util.CommunityConstant;
import com.meow.community.util.CommunityUtil;
import com.meow.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/conversation/list", method = RequestMethod.GET)
    public String findConversations(Model model, Page page){
        //获取用户
        User user = hostHolder.getUser();
        //分页
        page.setLimit(5);
        page.setPath("/conversation/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for (Message conversation : conversationList){
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", conversation);
                map.put("letterCount", messageService.findLetterCount(conversation.getConversationId()));
                map.put("letterUnreadCount", messageService.findLetterUnreadCount(user.getId(), conversation.getConversationId()));
                //会话列表要显示与当前用户会话的用户头像
                //如果当前用户是发起会话的人，那么它应该显示的是ToId的那个人的头像
                //如果当前用户使被发起会话的人，那么它显示的是FromId的那个人的头像
                int targetId = user.getId() == conversation.getFromId() ? conversation.getToId() : conversation.getFromId();
                map.put("target", userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        //查询未读私信总数量
        int allLetterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("allLetterUnreadCount", allLetterUnreadCount);

        //查询未读通知同数量
        int allNoticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("allNoticeUnreadCount", allNoticeUnreadCount);

        return "/site/letter";
    }

    @RequestMapping(path = "/conversation/detail/{conversationId}", method = RequestMethod.GET)
    public String getConversationDetail(@PathVariable("conversationId") String conversationId, Model model, Page page){
        //分页
        page.setLimit(5);
        page.setPath("/conversation/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if(letterList != null){
            for (Message letter : letterList){
                Map<String, Object> map = new HashMap<>();
                map.put("letter", letter);
                map.put("fromUser", userService.findUserById(letter.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        //私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        //查看私信的时候，要将未读私信状态设置为已读
        List<Integer> unreadLetterIds = getUnreadLetterIds(letterList);
        if (!unreadLetterIds.isEmpty()) {
            messageService.updateMessageStatus(unreadLetterIds, 1);
        }
        return "/site/letter-detail";
    }
    private List<Integer> getUnreadLetterIds(List<Message> letterList){
        List<Integer> unreadLetterIds = new ArrayList<>();
        if (letterList != null){
            for (Message letter : letterList){
                if (hostHolder.getUser().getId() == letter.getToId() && letter.getStatus() == 0){
                    unreadLetterIds.add(letter.getId());
                }
            }
        }
        return unreadLetterIds;
    }
    private User getLetterTarget(String conversationId){
        String[] userIds = conversationId.split("_");
        int userId0 = Integer.parseInt(userIds[0]);
        int userId1 = Integer.parseInt(userIds[1]);
        if(hostHolder.getUser().getId() == userId0){
            return userService.findUserById(userId1);
        }else{
            return userService.findUserById(userId0);
        }
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String addMessage(String toUsername, String content){
        User target = userService.findUserByName(toUsername);
        if(target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在!");
        }
        if(StringUtils.isBlank(content)){
            return CommunityUtil.getJSONString(2,"不能发送空白消息!");
        }
        Message message = new Message();
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setStatus(0);
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0,"发送成功!");
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model){
        //收到评论的那个用户
        User user = hostHolder.getUser();

        //查询评论通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        if(message != null){
            Map<String, Object> noticeVO = new HashMap<>();

            noticeVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            //进行评论的那个用户
            noticeVO.put("user", userService.findUserById((Integer) data.get("userId")));
            noticeVO.put("entityType", data.get("entityType"));
            noticeVO.put("entityId", data.get("entityId"));
            noticeVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            noticeVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            noticeVO.put("unread", unread);

            model.addAttribute("commentNotice", noticeVO);
        }


        //查询点赞通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);

        if(message != null){
            Map<String, Object> noticeVO = new HashMap<>();
            noticeVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            //进行点赞的那个用户
            noticeVO.put("user", userService.findUserById((Integer) data.get("userId")));
            noticeVO.put("entityType", data.get("entityType"));
            noticeVO.put("entityId", data.get("entityId"));
            noticeVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            noticeVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            noticeVO.put("unread", unread);
            model.addAttribute("likeNotice", noticeVO);
        }


        //查询关注通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);

        if(message != null){
            Map<String, Object> noticeVO = new HashMap<>();
            noticeVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            //进行关注的那个用户
            noticeVO.put("user", userService.findUserById((Integer) data.get("userId")));
            noticeVO.put("entityType", data.get("entityType"));
            noticeVO.put("entityId", data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            noticeVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            noticeVO.put("unread", unread);
            model.addAttribute("followNotice", noticeVO);
        }



        //查询未读私信总数量
        int allLetterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("allLetterUnreadCount", allLetterUnreadCount);

        //查询未读通知同数量
        int allNoticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("allNoticeUnreadCount", allNoticeUnreadCount);

        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model){
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();

        if(noticeList != null){
            for(Message notice : noticeList){
                Map<String, Object> map = new HashMap<>();
                //通知
                map.put("notice", notice);
                //内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                //通知作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        //设置已读
        List<Integer> ids = getUnreadLetterIds(noticeList);
        if(!ids.isEmpty()){
            messageService.updateMessageStatus(ids, 1);
        }
        return "/site/notice-detail";
    }
}
