package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AwardGivenRepository {

    public int insert(int giverId, int recipientId, int awardTypeId, int points, String message) throws SQLException {
        String sql = "INSERT INTO AwardsGiven (giver_id, recipient_id, award_type_id, points, message) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, giverId);
            ps.setInt(2, recipientId);
            ps.setInt(3, awardTypeId);
            ps.setInt(4, points);
            ps.setString(5, message);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No generated key returned.");
            }
        }
    }

    /** Recent awards with giver/recipient names and award type name joined in — for the feed. */
    public List<AwardFeedItem> getRecentFeed(int limit) throws SQLException {
        String sql =
            "SELECT ag.id, ag.giver_id, g.name AS giver_name, ag.recipient_id, r.name AS recipient_name, " +
            "       ag.award_type_id, at.name AS award_type_name, ag.points, ag.message, ag.given_at " +
            "FROM AwardsGiven ag " +
            "JOIN Users g  ON g.id  = ag.giver_id " +
            "JOIN Users r  ON r.id  = ag.recipient_id " +
            "JOIN AwardTypes at ON at.id = ag.award_type_id " +
            "ORDER BY ag.given_at DESC LIMIT ?";
        List<AwardFeedItem> list = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AwardFeedItem(
                        rs.getInt("id"),
                        rs.getInt("giver_id"),    rs.getString("giver_name"),
                        rs.getInt("recipient_id"), rs.getString("recipient_name"),
                        rs.getInt("award_type_id"), rs.getString("award_type_name"),
                        rs.getInt("points"),
                        rs.getString("message"),
                        rs.getString("given_at")
                    ));
                }
            }
        }
        return list;
    }

    public List<AwardGiven> getAwardsReceivedByUser(int userId) throws SQLException {
        String sql = "SELECT id, giver_id, recipient_id, award_type_id, points, message, given_at " +
                     "FROM AwardsGiven WHERE recipient_id = ? ORDER BY given_at DESC";
        return queryList(sql, userId);
    }

    public List<AwardGiven> getAwardsGivenByUser(int userId) throws SQLException {
        String sql = "SELECT id, giver_id, recipient_id, award_type_id, points, message, given_at " +
                     "FROM AwardsGiven WHERE giver_id = ? ORDER BY given_at DESC";
        return queryList(sql, userId);
    }

    public int pointsGivenThisQuarter(int giverId) throws SQLException {
        String sql =
            "SELECT COALESCE(SUM(points), 0) " +
            "FROM AwardsGiven " +
            "WHERE giver_id = ? " +
            "  AND given_at >= strftime('%Y-%m-%dT%H:%M:%S', " +
            "        CASE " +
            "          WHEN strftime('%m', 'now') IN ('01','02','03') THEN strftime('%Y', 'now') || '-01-01T00:00:00' " +
            "          WHEN strftime('%m', 'now') IN ('04','05','06') THEN strftime('%Y', 'now') || '-04-01T00:00:00' " +
            "          WHEN strftime('%m', 'now') IN ('07','08','09') THEN strftime('%Y', 'now') || '-07-01T00:00:00' " +
            "          ELSE strftime('%Y', 'now') || '-10-01T00:00:00' " +
            "        END" +
            "      )";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, giverId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int totalPointsEarned(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(points), 0) FROM AwardsGiven WHERE recipient_id = ?";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<AwardGiven> queryList(String sql, int userId) throws SQLException {
        List<AwardGiven> list = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private AwardGiven mapRow(ResultSet rs) throws SQLException {
        return new AwardGiven(
            rs.getInt("id"),
            rs.getInt("giver_id"),
            rs.getInt("recipient_id"),
            rs.getInt("award_type_id"),
            rs.getInt("points"),
            rs.getString("message"),
            rs.getString("given_at")
        );
    }
}
