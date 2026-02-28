package org.example.controller;

import org.example.AwardType;
import org.example.AwardTypeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/award-types")
public class AwardTypeController {

    private final AwardTypeRepository awardTypes;

    public AwardTypeController(AwardTypeRepository awardTypes) {
        this.awardTypes = awardTypes;
    }

    @GetMapping
    public List<AwardType> getAll() throws SQLException {
        return awardTypes.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AwardType> getById(@PathVariable int id) throws SQLException {
        return awardTypes.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) throws SQLException {
        String name        = (String) body.get("name");
        String description = (String) body.get("description");
        Integer pointsCost = (Integer) body.get("pointsCost");
        if (name == null || pointsCost == null) {
            return ResponseEntity.badRequest().body("name and pointsCost are required");
        }
        try {
            int id = awardTypes.insert(name, description, pointsCost);
            return ResponseEntity.ok(awardTypes.findById(id).orElseThrow());
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
