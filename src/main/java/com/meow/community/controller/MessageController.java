package com.meow.community.controller;


import com.meow.community.entity.Message;
import com.meow.community.entity.Page;
import com.meow.community.entity.User;
import com.meow.community.service.MessageService;
import com.meow.community.service.UserService;
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

import java.util.*;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/conversation/list", method = RequestMethod.GET)
    public String findConversations(Model model, Page page){
        Integer.parseInt("abc");
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

        //查询未读消息总数量
        int allLetterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("allLetterUnreadCount", allLetterUnreadCount);
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
}
