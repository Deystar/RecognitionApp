package org.example.controller;

import org.example.PointsService;
import org.example.config.AppPointsProperties;
import org.example.dto.PointsSummary;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    private final PointsService       pointsService;
    private final AppPointsProperties pointsProps;

    public PointsController(PointsService pointsService, AppPointsProperties pointsProps) {
        this.pointsService = pointsService;
        this.pointsProps   = pointsProps;
    }

    @GetMapping("/{userId}")
    public PointsSummary getSummary(@PathVariable int userId) throws SQLException {
        return new PointsSummary(
            pointsService.shoutOutAllowance(),
            pointsService.shoutOutGivingBalance(userId),
            pointsService.shoutOutValue(),
            pointsService.teamShoutOutValue(),
            pointsService.totalPointsEarned(userId),
            pointsService.totalPointsSpent(userId),
            pointsService.spendableBalance(userId)
        );
    }
}
