package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AwardTypeRepository {

    public int insert(String name, String description, int pointsCost) throws SQLException {
        String sql = "INSERT INTO AwardTypes (name, description, points_cost) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, pointsCost);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No generated key returned.");
            }
        }
    }

    public List<AwardType> getAll() throws SQLException {
        List<AwardType> list = new ArrayList<>();
        String sql = "SELECT id, name, description, points_cost, created_at FROM AwardTypes ORDER BY points_cost";
        try (Connection conn = DatabaseInitializer.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<AwardType> findById(int id) throws SQLException {
        String sql = "SELECT id, name, description, points_cost, created_at FROM AwardTypes WHERE id = ?";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    private AwardType mapRow(ResultSet rs) throws SQLException {
        return new AwardType(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getInt("points_cost"),
            rs.getString("created_at")
        );
    }
}
