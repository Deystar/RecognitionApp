package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TeamRepository {

    private static final String TEAM_SELECT =
        "SELECT t.id, t.name, t.description, t.created_at, " +
        "       COALESCE((SELECT SUM(ta.points) FROM TeamAwards ta WHERE ta.team_id = t.id), 0) AS total_points, " +
        "       (SELECT COUNT(*) FROM TeamMemberships tm WHERE tm.team_id = t.id) AS member_count " +
        "FROM Teams t ";

    public List<Team> getAll() throws SQLException {
        String sql = TEAM_SELECT + "ORDER BY t.name";
        List<Team> list = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Team> findById(int id) throws SQLException {
        String sql = TEAM_SELECT + "WHERE t.id = ?";
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

    public void delete(int teamId) throws SQLException {
        try (Connection conn = DatabaseInitializer.connect()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM TeamAwards WHERE team_id = ?")) {
                    ps.setInt(1, teamId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM TeamMemberships WHERE team_id = ?")) {
                    ps.setInt(1, teamId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM Teams WHERE id = ?")) {
                    ps.setInt(1, teamId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Team> getTeamsForUser(int userId) throws SQLException {
        String sql = TEAM_SELECT +
            "WHERE EXISTS (SELECT 1 FROM TeamMemberships tm WHERE tm.team_id = t.id AND tm.user_id = ?) " +
            "ORDER BY t.name";
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

    public List<Map<String, Object>> getAllMemberships() throws SQLException {
        String sql =
            "SELECT tm.user_id, t.id AS team_id, t.name AS team_name " +
            "FROM TeamMemberships tm " +
            "JOIN Teams t ON t.id = tm.team_id " +
            "ORDER BY tm.user_id, t.name";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("userId",    rs.getInt("user_id"));
                row.put("teamId",    rs.getInt("team_id"));
                row.put("teamName",  rs.getString("team_name"));
                list.add(row);
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
