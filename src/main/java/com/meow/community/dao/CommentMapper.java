package com.meow.community.dao;


import com.meow.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectComments(int entityType, int entityId, int offset, int limit);

    int selectCommentRows(int entityType, int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);
}
