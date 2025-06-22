package com.example.metro_app.Model;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CommentModel {
    private String userId;
    private String commentId;
    private String comment;
    private Timestamp createAt;
    private String postId;

    public CommentModel() {
    }

    public CommentModel(String userId, String commentId, String comment, Timestamp createAt, String postId) {
        this.userId = userId;
        this.commentId = commentId;
        this.comment = comment;
        this.createAt = createAt;
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }
    public String getCreateAtInString(){
            if (createAt == null) return "";
            Date date = createAt.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(date);

    }
    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
