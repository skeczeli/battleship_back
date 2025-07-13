package com.example.demo.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class DeletedUsername {
    @Id
    private String username;

    public DeletedUsername() {}

    public DeletedUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
