package fpt.edu.vn.assigment_travelapp.data.model;

public class User {
    private String userId;
    private String name;
    private String email;
    private String photoUrl;
    private String role;
    private String location;
    private String background;
    private String bio;

    // Firebase needs a public no-argument constructor
    public User() {
    }

    public User(String userId, String name, String email, String photoUrl) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.role = "user";
    }

    public User(String userId, String name, String email, String photoUrl, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
