package fpt.edu.vn.assigment_travelapp.data.model;

public class PostWithUser {
    private final Post post;
    private final User user;

    public PostWithUser(Post post, User user) {
        this.post = post;
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public User getUser() {
        return user;
    }
}
