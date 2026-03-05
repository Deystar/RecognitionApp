package org.example.controller;

import org.example.AppConfigRepository;
import org.example.ExternalStoreService;
import org.example.config.AppPointsProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ExternalStoreService externalStoreService;
    private final AppPointsProperties  pointsProps;
    private final AppConfigRepository  appConfigRepo;

    public ConfigController(ExternalStoreService externalStoreService,
                            AppPointsProperties pointsProps,
                            AppConfigRepository appConfigRepo) {
        this.externalStoreService = externalStoreService;
        this.pointsProps          = pointsProps;
        this.appConfigRepo        = appConfigRepo;
    }

    @GetMapping
    public Map<String, Object> getConfig() throws SQLException {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("externalStoreEnabled", externalStoreService.isEnabled());
        cfg.put("allowSelfTeamAward",   pointsProps.isAllowSelfTeamAward());
        cfg.put("shoutOutValue",           appConfigRepo.getShoutOutValue());
        cfg.put("teamShoutOutValue",       appConfigRepo.getTeamShoutOutValue());
        cfg.put("shoutOutAllowance",       appConfigRepo.getShoutOutAllowance());
        cfg.put("resetIntervalQuantity",   appConfigRepo.getResetIntervalQuantity());
        cfg.put("resetIntervalUnit",       appConfigRepo.getResetIntervalUnit());
        cfg.put("givingAllowance",         pointsProps.getPeerGivingAllowance());
        return cfg;
    }

    @PutMapping("/shout-out-value")
    public ResponseEntity<?> setShoutOutValue(@RequestBody Map<String, Object> body) throws SQLException {
        Object raw = body.get("value");
        if (raw == null) return ResponseEntity.badRequest().body("value is required");
        int value = ((Number) raw).intValue();
        if (value < 1) return ResponseEntity.badRequest().body("value must be at least 1");
        appConfigRepo.setShoutOutValue(value);
        return ResponseEntity.ok(Map.of("shoutOutValue", value));
    }

    @PutMapping("/team-shout-out-value")
    public ResponseEntity<?> setTeamShoutOutValue(@RequestBody Map<String, Object> body) throws SQLException {
        Object raw = body.get("value");
        if (raw == null) return ResponseEntity.badRequest().body("value is required");
        int value = ((Number) raw).intValue();
        if (value < 1) return ResponseEntity.badRequest().body("value must be at least 1");
        appConfigRepo.setTeamShoutOutValue(value);
        return ResponseEntity.ok(Map.of("teamShoutOutValue", value));
    }

    @PutMapping("/shout-out-allowance")
    public ResponseEntity<?> setShoutOutAllowance(@RequestBody Map<String, Object> body) throws SQLException {
        Object raw = body.get("value");
        if (raw == null) return ResponseEntity.badRequest().body("value is required");
        int value = ((Number) raw).intValue();
        if (value < 1) return ResponseEntity.badRequest().body("value must be at least 1");
        appConfigRepo.setShoutOutAllowance(value);
        return ResponseEntity.ok(Map.of("shoutOutAllowance", value));
    }

    @PutMapping("/reset-interval")
    public ResponseEntity<?> setResetInterval(@RequestBody Map<String, Object> body) throws SQLException {
        Object rawQty  = body.get("quantity");
        Object rawUnit = body.get("unit");
        if (rawQty == null || rawUnit == null) return ResponseEntity.badRequest().body("quantity and unit are required");
        int quantity = ((Number) rawQty).intValue();
        if (quantity < 1 || quantity > 100) return ResponseEntity.badRequest().body("quantity must be between 1 and 100");
        String unit = rawUnit.toString().toUpperCase();
        List<String> validUnits = List.of("DAY", "WEEK", "MONTH", "QUARTER", "YEAR");
        if (!validUnits.contains(unit)) return ResponseEntity.badRequest().body("unit must be one of: " + validUnits);
        appConfigRepo.setResetIntervalQuantity(quantity);
        appConfigRepo.setResetIntervalUnit(unit);
        return ResponseEntity.ok(Map.of("resetIntervalQuantity", quantity, "resetIntervalUnit", unit));
    }
}
