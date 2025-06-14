package com.example.demo.user;

public class UserDTO {
    private String name;
    private String username;
    private String password;
    private String email;
    private int wins;
    private int losses;
    private Long followersCount;
    private Long followingCount;
    private Boolean isFollowing; // Si el usuario autenticado sigue a este usuario

    public UserDTO() {
    }

    public UserDTO(String username, String name, String password, String email, int wins, int losses) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.email = email;
        this.wins = wins;
        this.losses = losses;
    }

    // Constructor completo
    public UserDTO(String username, String name, String password, String email, int wins, int losses, 
                   Long followersCount, Long followingCount, Boolean isFollowing) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.email = email;
        this.wins = wins;
        this.losses = losses;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.isFollowing = isFollowing;
    }

    // Getters y Setters existentes
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getWins() { 
        return wins; 
    }

    public void setWins(int wins) { 
        this.wins = wins; 
    }

    public int getLosses() { 
        return losses; 
    }

    public void setLosses(int losses) { 
        this.losses = losses; 
    }

    public int getTotalGames() {
        return wins + losses;
    }

    // Nuevos getters y setters para seguimiento
    public Long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Long followersCount) {
        this.followersCount = followersCount;
    }

    public Long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Long followingCount) {
        this.followingCount = followingCount;
    }

    public Boolean getIsFollowing() {
        return isFollowing;
    }

    public void setIsFollowing(Boolean isFollowing) {
        this.isFollowing = isFollowing;
    }
}