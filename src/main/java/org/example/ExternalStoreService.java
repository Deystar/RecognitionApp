package org.example;

import org.example.config.ExternalStoreProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExternalStoreService {

    private final ExternalStoreProperties props;
    private final RestClient restClient;

    public ExternalStoreService(ExternalStoreProperties props) {
        this.props = props;
        this.restClient = RestClient.create();
    }

    public boolean isEnabled() {
        return props.isEnabled() && props.getUrl() != null && !props.getUrl().isBlank();
    }

    /**
     * Fetches items from the external store API.
     * Returns an empty list if disabled, URL is blank, or the call fails
     * (fail-open so internal items always load).
     *
     * Expects the external API to return a JSON array of objects with fields:
     * id, name, description, pointsCost, quantityAvailable, createdAt
     */
    public List<StoreItem> fetchItems() {
        if (!isEnabled()) {
            return List.of();
        }
        try {
            List<Map<String, Object>> raw = restClient.get()
                    .uri(props.getUrl())
                    .header("X-Api-Key", props.getApiKey() != null ? props.getApiKey() : "")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (raw == null) return List.of();

            List<StoreItem> items = new ArrayList<>();
            for (Map<String, Object> m : raw) {
                items.add(mapToStoreItem(m));
            }
            return items;

        } catch (RestClientException e) {
            System.err.println("[ExternalStoreService] Failed to fetch external items: " + e.getMessage());
            return List.of();
        }
    }

    private StoreItem mapToStoreItem(Map<String, Object> m) {
        int id             = toInt(m.get("id"), 0);
        String name        = toStr(m.get("name"));
        String description = toStr(m.get("description"));
        int pointsCost     = toInt(m.get("pointsCost"), 0);
        Integer qty        = m.get("quantityAvailable") != null ? toInt(m.get("quantityAvailable"), 0) : null;
        String createdAt   = toStr(m.get("createdAt"));
        return new StoreItem(id, name, description, pointsCost, qty, createdAt, "external");
    }

    private static int toInt(Object val, int fallback) {
        if (val instanceof Number n) return n.intValue();
        if (val instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }

    private static String toStr(Object val) {
        return val != null ? val.toString() : null;
    }
}
