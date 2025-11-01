package fpt.edu.vn.assigment_travelapp.data.model;

import java.util.ArrayList;
import java.util.List;

public class CommentWithUser {
    private Comment comment;
    private User user;
    private List<CommentWithUser> replies = new ArrayList<>();

    public CommentWithUser(Comment comment, User user) {
        this.comment = comment;
        this.user = user;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<CommentWithUser> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentWithUser> replies) {
        this.replies = replies;
    }
}
