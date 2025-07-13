package com.example.demo.user;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "user_follows")
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User follower; // Usuario que sigue

    @ManyToOne
    @JoinColumn(name = "following_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User following; // Usuario que es seguido

    // Constructor vacío requerido por JPA
    public Follow() {}

    public Follow(User follower, User following) {
        this.follower = follower;
        this.following = following;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getFollower() {
        return follower;
    }

    public void setFollower(User follower) {
        this.follower = follower;
    }

    public User getFollowing() {
        return following;
    }

    public void setFollowing(User following) {
        this.following = following;
    }
}