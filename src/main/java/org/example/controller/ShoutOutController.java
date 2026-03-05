package org.example.controller;

import org.example.PointsService;
import org.example.ShoutOut;
import org.example.ShoutOutFeedItem;
import org.example.ShoutOutRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shout-outs")
public class ShoutOutController {

    private final ShoutOutRepository shoutOutRepo;
    private final PointsService      pointsService;

    public ShoutOutController(ShoutOutRepository shoutOutRepo, PointsService pointsService) {
        this.shoutOutRepo = shoutOutRepo;
        this.pointsService = pointsService;
    }

    /**
     * Give a shout-out.
     * Body must contain exactly one of: recipientUserId or teamId.
     * Optional: message.
     * Required: giverId.
     */
    @PostMapping
    public ResponseEntity<?> give(@RequestBody Map<String, Object> body) throws SQLException {
        Object giverRaw = body.get("giverId");
        if (giverRaw == null) return ResponseEntity.badRequest().body("giverId is required");
        int giverId = ((Number) giverRaw).intValue();

        String  message       = (String) body.get("message");
        Object  userRaw       = body.get("recipientUserId");
        Object  teamRaw       = body.get("teamId");

        if (userRaw == null && teamRaw == null) {
            return ResponseEntity.badRequest().body("Either recipientUserId or teamId is required");
        }
        if (userRaw != null && teamRaw != null) {
            return ResponseEntity.badRequest().body("Provide recipientUserId or teamId, not both");
        }

        try {
            ShoutOut result;
            if (userRaw != null) {
                int recipientId = ((Number) userRaw).intValue();
                result = pointsService.giveShoutOut(giverId, recipientId, message);
            } else {
                int teamId = ((Number) teamRaw).intValue();
                result = pointsService.giveTeamShoutOut(giverId, teamId, message);
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/feed")
    public List<ShoutOutFeedItem> getFeed(
            @RequestParam(defaultValue = "50") int limit) throws SQLException {
        return shoutOutRepo.getFeed(limit);
    }

    @GetMapping("/received/{userId}")
    public List<ShoutOut> getReceived(@PathVariable int userId) throws SQLException {
        return shoutOutRepo.getReceivedByUser(userId);
    }

    @GetMapping("/given/{userId}")
    public List<ShoutOut> getGiven(@PathVariable int userId) throws SQLException {
        return shoutOutRepo.getGivenByUser(userId);
    }
}
