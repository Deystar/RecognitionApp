package org.example.controller;

import org.example.ExternalStoreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ExternalStoreService externalStoreService;

    public ConfigController(ExternalStoreService externalStoreService) {
        this.externalStoreService = externalStoreService;
    }

    @GetMapping
    public Map<String, Object> getConfig() {
        return Map.of(
            "externalStoreEnabled", externalStoreService.isEnabled()
        );
    }
}
