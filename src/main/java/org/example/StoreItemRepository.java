package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class StoreItemRepository {

    public int insert(String name, String description, int pointsCost, Integer quantityAvailable) throws SQLException {
        String sql = "INSERT INTO StoreItems (name, description, points_cost, quantity_available) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, pointsCost);
            if (quantityAvailable != null) ps.setInt(4, quantityAvailable);
            else ps.setNull(4, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No generated key returned.");
            }
        }
    }

    public List<StoreItem> getAll() throws SQLException {
        List<StoreItem> list = new ArrayList<>();
        String sql = "SELECT id, name, description, points_cost, quantity_available, created_at " +
                     "FROM StoreItems ORDER BY points_cost";
        try (Connection conn = DatabaseInitializer.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<StoreItem> findById(int id) throws SQLException {
        String sql = "SELECT id, name, description, points_cost, quantity_available, created_at " +
                     "FROM StoreItems WHERE id = ?";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /** Decrements stock by 1. Should be called inside a purchase transaction. */
    public void decrementStock(int id) throws SQLException {
        String sql = "UPDATE StoreItems SET quantity_available = quantity_available - 1 " +
                     "WHERE id = ? AND quantity_available IS NOT NULL AND quantity_available > 0";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private StoreItem mapRow(ResultSet rs) throws SQLException {
        int qty = rs.getInt("quantity_available");
        Integer quantityAvailable = rs.wasNull() ? null : qty;
        return new StoreItem(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getInt("points_cost"),
            quantityAvailable,
            rs.getString("created_at")
        );
    }
}
