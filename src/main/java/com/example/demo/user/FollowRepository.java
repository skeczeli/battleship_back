package com.example.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Integer> {
    
    // Verificar si un usuario sigue a otro
    boolean existsByFollowerAndFollowing(User follower, User following);
    
    // Encontrar la relación de seguimiento específica
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    
    // Obtener todos los usuarios que sigue un usuario específico
    @Query("SELECT f.following FROM Follow f WHERE f.follower = :follower")
    List<User> findFollowingByFollower(@Param("follower") User follower);
    
    // Obtener todos los seguidores de un usuario específico
    @Query("SELECT f.follower FROM Follow f WHERE f.following = :following")
    List<User> findFollowersByFollowing(@Param("following") User following);
    
    // Contar cuántos usuarios sigue un usuario
    long countByFollower(User follower);
    
    // Contar cuántos seguidores tiene un usuario
    long countByFollowing(User following);
}