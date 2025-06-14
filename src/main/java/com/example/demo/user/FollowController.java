package com.example.demo.user;

import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/follow")
@CrossOrigin(origins = "http://localhost:3000")
public class FollowController {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Seguir a un usuario
    @PostMapping("/follow")
    public ResponseEntity<?> followUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody FollowDTO followDTO
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Falta el token de autenticación");
        }

        String token = authHeader.replace("Bearer ", "");
        String followerUsername = jwtUtil.validateTokenAndGetUsername(token);

        if (followerUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        // No permitir seguirse a sí mismo
        if (followerUsername.equals(followDTO.getUsernameToFollow())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No puedes seguirte a ti mismo");
        }

        Optional<User> followerOpt = userRepository.findByUsername(followerUsername);
        Optional<User> followingOpt = userRepository.findByUsername(followDTO.getUsernameToFollow());

        if (followerOpt.isEmpty() || followingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        User follower = followerOpt.get();
        User following = followingOpt.get();

        // Verificar si ya lo sigue
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya sigues a este usuario");
        }

        // Crear la relación de seguimiento
        Follow follow = new Follow(follower, following);
        followRepository.save(follow);

        return ResponseEntity.ok("Ahora sigues a " + following.getUsername());
    }

    // Dejar de seguir a un usuario
    @PostMapping("/unfollow")
    public ResponseEntity<?> unfollowUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody FollowDTO followDTO
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Falta el token de autenticación");
        }

        String token = authHeader.replace("Bearer ", "");
        String followerUsername = jwtUtil.validateTokenAndGetUsername(token);

        if (followerUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        Optional<User> followerOpt = userRepository.findByUsername(followerUsername);
        Optional<User> followingOpt = userRepository.findByUsername(followDTO.getUsernameToFollow());

        if (followerOpt.isEmpty() || followingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        User follower = followerOpt.get();
        User following = followingOpt.get();

        Optional<Follow> followOpt = followRepository.findByFollowerAndFollowing(follower, following);

        if (followOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No sigues a este usuario");
        }

        followRepository.delete(followOpt.get());

        return ResponseEntity.ok("Dejaste de seguir a " + following.getUsername());
    }

    // Verificar si un usuario sigue a otro
    @GetMapping("/is-following/{username}")
    public ResponseEntity<?> isFollowing(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String username
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Falta el token de autenticación");
        }

        String token = authHeader.replace("Bearer ", "");
        String followerUsername = jwtUtil.validateTokenAndGetUsername(token);

        if (followerUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        Optional<User> followerOpt = userRepository.findByUsername(followerUsername);
        Optional<User> followingOpt = userRepository.findByUsername(username);

        if (followerOpt.isEmpty() || followingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        boolean isFollowing = followRepository.existsByFollowerAndFollowing(followerOpt.get(), followingOpt.get());
        return ResponseEntity.ok(isFollowing);
    }

    // Obtener lista de usuarios que sigue
    @GetMapping("/following")
    public ResponseEntity<?> getFollowing(
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Falta el token de autenticación");
        }

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.validateTokenAndGetUsername(token);

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        List<User> following = followRepository.findFollowingByFollower(userOpt.get());
        List<UserDTO> followingDTOs = following.stream()
                .map(user -> new UserDTO(user.getUsername(), user.getName(), null, 
                                       user.getEmail(), user.getWins(), user.getLosses()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(followingDTOs);
    }

    // Obtener lista de seguidores
    @GetMapping("/followers")
    public ResponseEntity<?> getFollowers(
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Falta el token de autenticación");
        }

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.validateTokenAndGetUsername(token);

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        List<User> followers = followRepository.findFollowersByFollowing(userOpt.get());
        List<UserDTO> followerDTOs = followers.stream()
                .map(user -> new UserDTO(user.getUsername(), user.getName(), null, 
                                       user.getEmail(), user.getWins(), user.getLosses()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(followerDTOs);
    }

    // Obtener estadísticas de seguimiento de un usuario
    @GetMapping("/stats/{username}")
    public ResponseEntity<?> getFollowStats(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        User user = userOpt.get();
        long followingCount = followRepository.countByFollower(user);
        long followersCount = followRepository.countByFollowing(user);

        FollowStatsDTO stats = new FollowStatsDTO(followingCount, followersCount);
        return ResponseEntity.ok(stats);
    }

    // DTO para estadísticas
    public static class FollowStatsDTO {
        private long following;
        private long followers;

        public FollowStatsDTO(long following, long followers) {
            this.following = following;
            this.followers = followers;
        }

        public long getFollowing() { return following; }
        public void setFollowing(long following) { this.following = following; }
        public long getFollowers() { return followers; }
        public void setFollowers(long followers) { this.followers = followers; }
    }

    @GetMapping("/friends-ranking")
    public ResponseEntity<?> getFriendsRanking(
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Falta el token de autenticación");
        }

        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.validateTokenAndGetUsername(token);

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        User currentUser = userOpt.get();
        
        // Obtener lista de usuarios que sigue
        List<User> following = followRepository.findFollowingByFollower(currentUser);
        
        // Agregar al usuario actual a la lista para incluirlo en el ranking
        following.add(currentUser);
        
        // Crear DTOs con ranking basado en puntuación
        List<FriendsRankingDTO> friendsRanking = following.stream()
            .map(user -> {
                // Calcular puntuación: (victorias - derrotas) * 10
                int score = (user.getWins() - user.getLosses()) * 10;
                return new FriendsRankingDTO(
                    user.getUsername(),
                    user.getName(),
                    score,
                    user.getWins(),
                    user.getLosses()
                );
            })
            .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore())) // Ordenar por puntuación descendente
            .collect(Collectors.toList());
        
        // Asignar posiciones
        for (int i = 0; i < friendsRanking.size(); i++) {
            friendsRanking.get(i).setRank(i + 1);
        }

        return ResponseEntity.ok(friendsRanking);
    }

    // DTO para el ranking de amigos
    public static class FriendsRankingDTO {
        private int rank;
        private String username;
        private String name;
        private int score;
        private int wins;
        private int losses;

        public FriendsRankingDTO(String username, String name, int score, int wins, int losses) {
            this.username = username;
            this.name = name;
            this.score = score;
            this.wins = wins;
            this.losses = losses;
        }

        // Getters y Setters
        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public int getWins() { return wins; }
        public void setWins(int wins) { this.wins = wins; }
        public int getLosses() { return losses; }
        public void setLosses(int losses) { this.losses = losses; }
    }
}