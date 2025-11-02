package fpt.edu.vn.assigment_travelapp.data.model;

public class CommentWithUser {
    private Comment comment;
    private User user;

    public CommentWithUser() {
    }

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
}

