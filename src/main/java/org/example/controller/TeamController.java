package org.example.controller;

import org.example.PointsService;
import org.example.Team;
import org.example.TeamAward;
import org.example.TeamAwardRepository;
import org.example.TeamRepository;
import org.example.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository     teamRepo;
    private final TeamAwardRepository teamAwardRepo;
    private final PointsService      pointsService;

    public TeamController(TeamRepository teamRepo,
                          TeamAwardRepository teamAwardRepo,
                          PointsService pointsService) {
        this.teamRepo      = teamRepo;
        this.teamAwardRepo = teamAwardRepo;
        this.pointsService = pointsService;
    }

    @GetMapping
    public List<Team> getAll() throws SQLException {
        return teamRepo.getAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) throws SQLException {
        String name = (String) body.get("name");
        String desc = (String) body.get("description");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body("name is required");
        }
        try {
            int id = teamRepo.insert(name, desc);
            return ResponseEntity.ok(teamRepo.findById(id).orElseThrow());
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTeam(@PathVariable int id) throws SQLException {
        return teamRepo.findById(id)
            .map(team -> {
                try {
                    List<User> members = teamRepo.getMembers(id);
                    Map<String, Object> result = new HashMap<>();
                    result.put("id",          team.getId());
                    result.put("name",        team.getName());
                    result.put("description", team.getDescription());
                    result.put("createdAt",   team.getCreatedAt());
                    result.put("totalPoints", team.getTotalPoints());
                    result.put("memberCount", team.getMemberCount());
                    result.put("members",     members);
                    return ResponseEntity.ok((Object) result);
                } catch (SQLException e) {
                    return ResponseEntity.internalServerError().body((Object) e.getMessage());
                }
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<Team> getTeamsForUser(@PathVariable int userId) throws SQLException {
        return teamRepo.getTeamsForUser(userId);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(@PathVariable int id,
                                       @RequestBody Map<String, Object> body) throws SQLException {
        Object raw = body.get("userId");
        if (raw == null) return ResponseEntity.badRequest().body("userId is required");
        int userId = ((Number) raw).intValue();
        try {
            teamRepo.addMember(id, userId);
            return ResponseEntity.ok(Map.of("teamId", id, "userId", userId));
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable int id,
                                          @PathVariable int userId) throws SQLException {
        teamRepo.removeMember(id, userId);
        return ResponseEntity.ok(Map.of("teamId", id, "userId", userId));
    }

    @PostMapping("/award")
    public ResponseEntity<?> awardTeam(@RequestBody Map<String, Object> body) throws SQLException {
        Object giverRaw  = body.get("giverId");
        Object teamRaw   = body.get("teamId");
        Object ptsRaw    = body.get("points");
        String message   = (String) body.get("message");
        if (giverRaw == null || teamRaw == null || ptsRaw == null) {
            return ResponseEntity.badRequest().body("giverId, teamId, and points are required");
        }
        int giverId = ((Number) giverRaw).intValue();
        int teamId  = ((Number) teamRaw).intValue();
        int points  = ((Number) ptsRaw).intValue();
        try {
            TeamAward award = pointsService.awardTeam(giverId, teamId, points, message);
            return ResponseEntity.ok(award);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/awards")
    public List<TeamAward> getTeamAwards(@PathVariable int id) throws SQLException {
        return teamAwardRepo.getAwardsByTeam(id);
    }
}
