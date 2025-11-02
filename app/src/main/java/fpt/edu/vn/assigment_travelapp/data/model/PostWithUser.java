package fpt.edu.vn.assigment_travelapp.data.model;

public class PostWithUser {
    private Post post;
    private User user;

    public PostWithUser() {
    }

    public PostWithUser(Post post, User user) {
        this.post = post;
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

