package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PurchaseRepository {

    /**
     * Records a purchase. Does NOT validate point balance or stock —
     * call PointsService.purchaseItem() instead, which does both checks first.
     */
    public int insert(int userId, int storeItemId, int pointsSpent) throws SQLException {
        String sql = "INSERT INTO Purchases (user_id, store_item_id, points_spent) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, storeItemId);
            ps.setInt(3, pointsSpent);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No generated key returned.");
            }
        }
    }

    /** All purchases made by a user, most recent first. */
    public List<Purchase> getPurchasesByUser(int userId) throws SQLException {
        String sql = "SELECT id, user_id, store_item_id, points_spent, purchased_at " +
                     "FROM Purchases WHERE user_id = ? ORDER BY purchased_at DESC";
        List<Purchase> list = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Total points a user has spent in the store (all time). */
    public int totalPointsSpent(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(points_spent), 0) FROM Purchases WHERE user_id = ?";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Purchase mapRow(ResultSet rs) throws SQLException {
        return new Purchase(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getInt("store_item_id"),
            rs.getInt("points_spent"),
            rs.getString("purchased_at")
        );
    }
}
