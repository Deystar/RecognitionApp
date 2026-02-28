package org.example.controller;

import org.example.AwardFeedItem;
import org.example.AwardGiven;
import org.example.AwardGivenRepository;
import org.example.PointsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/awards")
public class AwardController {

    private final AwardGivenRepository awardGivenRepo;
    private final PointsService        pointsService;

    public AwardController(AwardGivenRepository awardGivenRepo, PointsService pointsService) {
        this.awardGivenRepo = awardGivenRepo;
        this.pointsService  = pointsService;
    }

    @GetMapping("/feed")
    public List<AwardFeedItem> getFeed(
            @RequestParam(defaultValue = "50") int limit) throws SQLException {
        return awardGivenRepo.getRecentFeed(limit);
    }

    @GetMapping("/received/{userId}")
    public List<AwardGiven> getReceived(@PathVariable int userId) throws SQLException {
        return awardGivenRepo.getAwardsReceivedByUser(userId);
    }

    @GetMapping("/given/{userId}")
    public List<AwardGiven> getGiven(@PathVariable int userId) throws SQLException {
        return awardGivenRepo.getAwardsGivenByUser(userId);
    }

    @PostMapping
    public ResponseEntity<?> giveAward(@RequestBody Map<String, Object> body) throws SQLException {
        Integer giverId      = (Integer) body.get("giverId");
        Integer recipientId  = (Integer) body.get("recipientId");
        Integer awardTypeId  = (Integer) body.get("awardTypeId");
        String  message      = (String)  body.get("message");
        if (giverId == null || recipientId == null || awardTypeId == null) {
            return ResponseEntity.badRequest().body("giverId, recipientId, and awardTypeId are required");
        }
        try {
            AwardGiven award = pointsService.giveAward(giverId, recipientId, awardTypeId, message);
            return ResponseEntity.ok(award);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
