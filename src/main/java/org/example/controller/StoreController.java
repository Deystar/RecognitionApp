package org.example.controller;

import org.example.PointsService;
import org.example.Purchase;
import org.example.PurchaseRepository;
import org.example.StoreItem;
import org.example.StoreItemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final StoreItemRepository storeItems;
    private final PurchaseRepository  purchases;
    private final PointsService       pointsService;

    public StoreController(StoreItemRepository storeItems,
                           PurchaseRepository purchases,
                           PointsService pointsService) {
        this.storeItems   = storeItems;
        this.purchases    = purchases;
        this.pointsService = pointsService;
    }

    @GetMapping("/items")
    public List<StoreItem> getItems() throws SQLException {
        return storeItems.getAll();
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItem(@RequestBody Map<String, Object> body) throws SQLException {
        String  name      = (String)  body.get("name");
        String  desc      = (String)  body.get("description");
        Integer cost      = (Integer) body.get("pointsCost");
        Integer qty       = (Integer) body.get("quantityAvailable");
        if (name == null || cost == null) {
            return ResponseEntity.badRequest().body("name and pointsCost are required");
        }
        try {
            int id = storeItems.insert(name, desc, cost, qty);
            return ResponseEntity.ok(storeItems.findById(id).orElseThrow());
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(@RequestBody Map<String, Object> body) throws SQLException {
        Integer userId      = (Integer) body.get("userId");
        Integer storeItemId = (Integer) body.get("storeItemId");
        if (userId == null || storeItemId == null) {
            return ResponseEntity.badRequest().body("userId and storeItemId are required");
        }
        try {
            Purchase purchase = pointsService.purchaseItem(userId, storeItemId);
            return ResponseEntity.ok(purchase);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/purchases/{userId}")
    public List<Purchase> getPurchases(@PathVariable int userId) throws SQLException {
        return purchases.getPurchasesByUser(userId);
    }
}
