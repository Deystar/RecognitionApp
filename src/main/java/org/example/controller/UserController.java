package org.example.controller;

import org.example.User;
import org.example.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping
    public List<User> getAll() throws SQLException {
        return users.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable int id) throws SQLException {
        return users.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) throws SQLException {
        String name  = body.get("name");
        String email = body.get("email");
        if (name == null || email == null) {
            return ResponseEntity.badRequest().body("name and email are required");
        }
        try {
            int id = users.insertUser(name, email);
            return ResponseEntity.ok(users.findById(id).orElseThrow());
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Map<String, String> body) throws SQLException {
        String name  = body.get("name");
        String email = body.get("email");
        if (name == null || email == null) {
            return ResponseEntity.badRequest().body("name and email are required");
        }
        boolean updated = users.updateUser(id, name, email);
        if (!updated) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(users.findById(id).orElseThrow());
    }
}
