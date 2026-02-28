package org.example.controller;

import org.example.PointsService;
import org.example.dto.PointsSummary;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    private final PointsService pointsService;

    public PointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @GetMapping("/{userId}")
    public PointsSummary getSummary(@PathVariable int userId) throws SQLException {
        return new PointsSummary(
            PointsService.QUARTERLY_GIVING_ALLOWANCE,
            pointsService.givingBalanceThisQuarter(userId),
            pointsService.totalPointsEarned(userId),
            pointsService.totalPointsSpent(userId),
            pointsService.spendableBalance(userId)
        );
    }
}
