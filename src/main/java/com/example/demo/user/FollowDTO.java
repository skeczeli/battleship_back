package com.example.demo.user;

public class FollowDTO {
    private String usernameToFollow;

    public FollowDTO() {}

    public FollowDTO(String usernameToFollow) {
        this.usernameToFollow = usernameToFollow;
    }

    public String getUsernameToFollow() {
        return usernameToFollow;
    }

    public void setUsernameToFollow(String usernameToFollow) {
        this.usernameToFollow = usernameToFollow;
    }
}