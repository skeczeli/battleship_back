package com.example.demo.user;

public class UserDTO {
    private String name;
    private String username;
    private String password;
    private String email;
    private int wins;
    private int losses;

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
    
}
