package org.example;

import java.sql.*;
import org.springframework.stereotype.Repository;

@Repository
public class AppConfigRepository {

    public int getInt(String key, int defaultValue) throws SQLException {
        String sql = "SELECT value FROM AppConfig WHERE key = ?";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try { return Integer.parseInt(rs.getString("value")); }
                    catch (NumberFormatException e) { return defaultValue; }
                }
            }
        }
        return defaultValue;
    }

    public void setInt(String key, int value) throws SQLException {
        String sql = "INSERT INTO AppConfig (key, value) VALUES (?, ?) " +
                     "ON CONFLICT(key) DO UPDATE SET value = excluded.value";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, String.valueOf(value));
            ps.executeUpdate();
        }
    }

    public int getShoutOutValue() throws SQLException {
        return getInt("shout_out_value", 10);
    }

    public void setShoutOutValue(int value) throws SQLException {
        setInt("shout_out_value", value);
    }

    public int getTeamShoutOutValue() throws SQLException {
        return getInt("team_shout_out_value", 5);
    }

    public void setTeamShoutOutValue(int value) throws SQLException {
        setInt("team_shout_out_value", value);
    }

    public String getString(String key, String defaultValue) throws SQLException {
        String sql = "SELECT value FROM AppConfig WHERE key = ?";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("value");
            }
        }
        return defaultValue;
    }

    public void setString(String key, String value) throws SQLException {
        String sql = "INSERT INTO AppConfig (key, value) VALUES (?, ?) " +
                     "ON CONFLICT(key) DO UPDATE SET value = excluded.value";
        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    public int getShoutOutAllowance() throws SQLException {
        return getInt("shout_out_allowance", 10);
    }

    public void setShoutOutAllowance(int value) throws SQLException {
        setInt("shout_out_allowance", value);
    }

    public int getResetIntervalQuantity() throws SQLException {
        return getInt("reset_interval_quantity", 2);
    }

    public void setResetIntervalQuantity(int value) throws SQLException {
        setInt("reset_interval_quantity", value);
    }

    public String getResetIntervalUnit() throws SQLException {
        return getString("reset_interval_unit", "WEEK");
    }

    public void setResetIntervalUnit(String unit) throws SQLException {
        setString("reset_interval_unit", unit.toUpperCase());
    }

    public String getLastResetAt() throws SQLException {
        return getString("last_reset_at", "");
    }

    public void setLastResetAt(String timestamp) throws SQLException {
        setString("last_reset_at", timestamp);
    }
}
