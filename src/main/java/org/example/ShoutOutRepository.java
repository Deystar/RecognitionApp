package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ShoutOutRepository {

    private static final String QUARTERLY_WHERE =
        "  AND given_at >= strftime('%Y-%m-%dT%H:%M:%S', " +
        "        CASE " +
        "          WHEN strftime('%m', 'now') IN ('01','02','03') THEN strftime('%Y', 'now') || '-01-01T00:00:00' " +
        "          WHEN strftime('%m', 'now') IN ('04','05','06') THEN strftime('%Y', 'now') || '-04-01T00:00:00' " +
        "          WHEN strftime('%m', 'now') IN ('07','08','09') THEN strftime('%Y', 'now') || '-07-01T00:00:00' " +
        "          ELSE strftime('%Y', 'now') || '-10-01T00:00:00' " +
        "        END" +
        "      )";

    /**
     * Insert a shout-out event + its recipient rows atomically.
     * For an individual shout-out, recipients = [recipientUserId].
     * For a team shout-out, recipients = qualifying member IDs (giver already excluded by caller).
     */
    public ShoutOut insert(int giverId, Integer recipientUserId, Integer recipientTeamId,
                           int points, String message, List<Integer> recipientUserIds) throws SQLException {
        try (Connection conn = DatabaseInitializer.connect()) {
            conn.setAutoCommit(false);
            try {
                int shoutOutId;
                String insertSo =
                    "INSERT INTO ShoutOuts (giver_id, recipient_user_id, recipient_team_id, points, message) " +
                    "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSo, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, giverId);
                    if (recipientUserId != null) ps.setInt(2, recipientUserId); else ps.setNull(2, Types.INTEGER);
                    if (recipientTeamId != null) ps.setInt(3, recipientTeamId); else ps.setNull(3, Types.INTEGER);
                    ps.setInt(4, points);
                    ps.setString(5, message);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No generated key for ShoutOut.");
                        shoutOutId = keys.getInt(1);
                    }
                }

                String insertRecipient =
                    "INSERT INTO ShoutOutRecipients (shout_out_id, user_id, points) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertRecipient)) {
                    for (int uid : recipientUserIds) {
                        ps.setInt(1, shoutOutId);
                        ps.setInt(2, uid);
                        ps.setInt(3, points);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
                return new ShoutOut(shoutOutId, giverId, recipientUserId, recipientTeamId, points, message, null);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Count of shout-outs given by this user on or after the given ISO-8601 timestamp. */
    public int countGivenSince(int giverId, String since) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ShoutOuts WHERE giver_id = ? AND given_at >= ?";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, giverId);
            ps.setString(2, since);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Points spent from the giver's quarterly allowance (one per shout-out event, not per recipient). */
    public int pointsGivenThisQuarter(int giverId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(points), 0) FROM ShoutOuts WHERE giver_id = ?" + QUARTERLY_WHERE;
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, giverId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Total points this user has received across all shout-outs (individual + team). */
    public int totalPointsReceived(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(points), 0) FROM ShoutOutRecipients WHERE user_id = ?";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Recent shout-out events for the activity feed (one row per event, not per recipient). */
    public List<ShoutOutFeedItem> getFeed(int limit) throws SQLException {
        String sql =
            "SELECT so.id, gu.name AS giver_name, " +
            "       CASE WHEN so.recipient_user_id IS NOT NULL THEN ru.name ELSE t.name END AS recipient_name, " +
            "       CASE WHEN so.recipient_team_id IS NOT NULL THEN 1 ELSE 0 END AS is_team, " +
            "       so.points, so.message, so.given_at " +
            "FROM ShoutOuts so " +
            "JOIN Users gu ON gu.id = so.giver_id " +
            "LEFT JOIN Users ru ON ru.id = so.recipient_user_id " +
            "LEFT JOIN Teams t  ON t.id  = so.recipient_team_id " +
            "ORDER BY so.given_at DESC LIMIT ?";
        List<ShoutOutFeedItem> list = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ShoutOutFeedItem(
                        rs.getInt("id"),
                        rs.getString("giver_name"),
                        rs.getString("recipient_name"),
                        rs.getInt("is_team") == 1,
                        rs.getInt("points"),
                        rs.getString("message"),
                        rs.getString("given_at")
                    ));
                }
            }
        }
        return list;
    }

    /** All shout-outs given by a user. */
    public List<ShoutOut> getGivenByUser(int giverId) throws SQLException {
        String sql =
            "SELECT id, giver_id, recipient_user_id, recipient_team_id, points, message, given_at " +
            "FROM ShoutOuts WHERE giver_id = ? ORDER BY given_at DESC";
        return queryList(sql, giverId);
    }

    /** All shout-outs received by a user (via ShoutOutRecipients). */
    public List<ShoutOut> getReceivedByUser(int userId) throws SQLException {
        String sql =
            "SELECT so.id, so.giver_id, so.recipient_user_id, so.recipient_team_id, " +
            "       so.points, so.message, so.given_at " +
            "FROM ShoutOuts so " +
            "JOIN ShoutOutRecipients sor ON sor.shout_out_id = so.id " +
            "WHERE sor.user_id = ? ORDER BY so.given_at DESC";
        return queryList(sql, userId);
    }

    private List<ShoutOut> queryList(String sql, int userId) throws SQLException {
        List<ShoutOut> list = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ShoutOut(
                        rs.getInt("id"),
                        rs.getInt("giver_id"),
                        rs.getObject("recipient_user_id") != null ? rs.getInt("recipient_user_id") : null,
                        rs.getObject("recipient_team_id") != null ? rs.getInt("recipient_team_id") : null,
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
