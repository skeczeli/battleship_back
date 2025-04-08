package com.example.demo;

import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller // This means that this class is a Controller
@RequestMapping(path="") // This means URL's start with /demo (after Application path)
public class UserController {
    @Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> login(@RequestBody UserDTO credentials) {
        Optional<User> userOpt = userRepository.findByUsername(credentials.getUsername());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(credentials.getPassword())) {
                String token = jwtUtil.generateToken(user.getUsername());
                // armar userDTO para devolver
                UserDTO dto = new UserDTO(
                        user.getUsername(),
                        user.getName(),
                        user.getPassword(), // o null si no querés mandarlo
                        user.getEmail(),
                        user.getWins(),
                        user.getLosses()
                );
                return ResponseEntity.ok()
                        .header("Authorization", "Bearer " + token)
                        .body(dto);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
    }


    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(path="/add") // Map ONLY POST Requests
    public @ResponseBody String addNewUser (@RequestBody UserDTO body) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request

        User n = new User();
        n.setName(body.getName());
        n.setEmail(body.getEmail());
        n.setUsername(body.getUsername());
        n.setPassword(body.getPassword());
        userRepository.save(n);
        return "Saved";
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping("/api/users/update")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO updatedData) {
        Optional<User> userOpt = userRepository.findByUsername(updatedData.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        User user = userOpt.get();

        // Solo actualizamos campos si vienen con valores
        if (updatedData.getName() != null) user.setName(updatedData.getName());
        if (updatedData.getEmail() != null) user.setEmail(updatedData.getEmail());
        if (updatedData.getPassword() != null) user.setPassword(updatedData.getPassword());

        userRepository.save(user);

        UserDTO dto = new UserDTO(
                user.getUsername(),
                user.getName(),
                null,
                user.getEmail(),
                user.getWins(),
                user.getLosses()
        );

        return ResponseEntity.ok(dto);
    }

    @GetMapping(path="/all")
    public @ResponseBody Iterable<User> getAllUsers() {
        // This returns a JSON or XML with the users
        return userRepository.findAll();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/api/users/{username}")
    public ResponseEntity<?> getUserByUsername(@org.springframework.web.bind.annotation.PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserDTO dto = new UserDTO(
                user.getUsername(),
                user.getName(),
                null, // no enviamos la contraseña
                user.getEmail(),
                user.getWins(),
                user.getLosses()
            );
            return ResponseEntity.ok(dto);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
    }

    
}