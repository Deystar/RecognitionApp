package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TeamRepository {

    private static final String TEAM_SELECT =
        "SELECT t.id, t.name, t.description, t.created_at, " +
        "       COALESCE(SUM(ta.points), 0) AS total_points, " +
        "       COUNT(DISTINCT tm.id) AS member_count " +
        "FROM Teams t " +
        "LEFT JOIN TeamAwards ta ON ta.team_id = t.id " +
        "LEFT JOIN TeamMemberships tm ON tm.team_id = t.id ";

    public List<Team> getAll() throws SQLException {
        String sql = TEAM_SELECT + "GROUP BY t.id ORDER BY t.name";
        List<Team> list = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Team> findById(int id) throws SQLException {
        String sql = TEAM_SELECT + "WHERE t.id = ? GROUP BY t.id";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public int insert(String name, String description) throws SQLException {
        String sql = "INSERT INTO Teams (name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No generated key returned.");
            }
        }
    }

    public void addMember(int teamId, int userId) throws SQLException {
        String sql = "INSERT OR IGNORE INTO TeamMemberships (team_id, user_id) VALUES (?, ?)";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void removeMember(int teamId, int userId) throws SQLException {
        String sql = "DELETE FROM TeamMemberships WHERE team_id = ? AND user_id = ?";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public List<User> getMembers(int teamId) throws SQLException {
        String sql =
            "SELECT u.id, u.name, u.email, u.created_at " +
            "FROM Users u " +
            "JOIN TeamMemberships tm ON tm.user_id = u.id " +
            "WHERE tm.team_id = ? ORDER BY u.name";
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("created_at")
                    ));
                }
            }
        }
        return list;
    }

    public List<Team> getTeamsForUser(int userId) throws SQLException {
        String sql = TEAM_SELECT +
            "JOIN TeamMemberships tm2 ON tm2.team_id = t.id AND tm2.user_id = ? " +
            "GROUP BY t.id ORDER BY t.name";
        List<Team> list = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean isMember(int teamId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM TeamMemberships WHERE team_id = ? AND user_id = ? LIMIT 1";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Team mapRow(ResultSet rs) throws SQLException {
        return new Team(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("created_at"),
            rs.getInt("total_points"),
            rs.getInt("member_count")
        );
    }
}
