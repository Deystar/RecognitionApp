package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class TeamAwardRepository {

    private static final String QUARTERLY_CASE =
        "  AND given_at >= strftime('%Y-%m-%dT%H:%M:%S', " +
        "        CASE " +
        "          WHEN strftime('%m', 'now') IN ('01','02','03') THEN strftime('%Y', 'now') || '-01-01T00:00:00' " +
        "          WHEN strftime('%m', 'now') IN ('04','05','06') THEN strftime('%Y', 'now') || '-04-01T00:00:00' " +
        "          WHEN strftime('%m', 'now') IN ('07','08','09') THEN strftime('%Y', 'now') || '-07-01T00:00:00' " +
        "          ELSE strftime('%Y', 'now') || '-10-01T00:00:00' " +
        "        END" +
        "      )";

    public int insert(int giverId, int teamId, int points, String message) throws SQLException {
        String sql = "INSERT INTO TeamAwards (giver_id, team_id, points, message) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, giverId);
            ps.setInt(2, teamId);
            ps.setInt(3, points);
            ps.setString(4, message);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No generated key returned.");
            }
        }
    }

    /** Points the user has given to teams this quarter (from their team giving pool). */
    public int teamPointsGivenThisQuarter(int giverId) throws SQLException {
        String sql =
            "SELECT COALESCE(SUM(points), 0) FROM TeamAwards " +
            "WHERE giver_id = ? " +
            QUARTERLY_CASE;
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, giverId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<TeamAward> getAwardsByTeam(int teamId) throws SQLException {
        String sql =
            "SELECT id, giver_id, team_id, points, message, given_at " +
            "FROM TeamAwards WHERE team_id = ? ORDER BY given_at DESC";
        List<TeamAward> list = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TeamAward(
                        rs.getInt("id"),
                        rs.getInt("giver_id"),
                        rs.getInt("team_id"),
                        rs.getInt("points"),
                        rs.getString("message"),
                        rs.getString("given_at")
                    ));
                }
            }
        }
        return list;
    }
}
