package com.example.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletedUsernameRepository extends JpaRepository<DeletedUsername, String> {
    boolean existsByUsername(String username);
}
