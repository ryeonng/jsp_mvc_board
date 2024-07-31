package com.tenco.tboard.repository.interfaces;

import java.util.List;

import com.tenco.tboard.model.Comment;

public interface CommentRepository {

	void addComment(Comment comment);
	void deleteComment(int id);
	Comment getCommentById(int id);
	List<Comment> getCommentsByBoardId(int boardId);
}
