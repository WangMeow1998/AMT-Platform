package com.meow.community.service;


import com.meow.community.dao.CommentMapper;
import com.meow.community.dao.DiscussPostMapper;
import com.meow.community.entity.Comment;
import com.meow.community.util.CommunityConstant;
import com.meow.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<Comment> findComments(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectComments(entityType,entityId,offset,limit);
    }

    public int findCommentRows(int entityType, int entityId){
        return commentMapper.selectCommentRows(entityType, entityId);
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }

    //先更新帖子，在增加评论数量（这里涉及到了事务）
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //添加评论
        //转移HTML标记
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        //过滤评论的敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        //更新帖子的评论数量
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            int commentCount = commentMapper.selectCommentRows(comment.getEntityType(), comment.getEntityId());
            discussPostMapper.updateDiscussPostCommentCount(comment.getEntityId(), commentCount);
        }
        return rows;
    }
}
