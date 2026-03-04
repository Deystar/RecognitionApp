package org.example.controller;

import org.example.ExternalStoreService;
import org.example.config.AppPointsProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ExternalStoreService externalStoreService;
    private final AppPointsProperties  pointsProps;

    public ConfigController(ExternalStoreService externalStoreService,
                            AppPointsProperties pointsProps) {
        this.externalStoreService = externalStoreService;
        this.pointsProps          = pointsProps;
    }

    @GetMapping
    public Map<String, Object> getConfig() {
        return Map.of(
            "externalStoreEnabled", externalStoreService.isEnabled(),
            "allowSelfTeamAward",   pointsProps.isAllowSelfTeamAward()
        );
    }
}
