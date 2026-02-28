package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Data-access methods for the Users table.
 */
@Repository
public class UserRepository {

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    /**
     * Inserts a new user and returns the generated id.
     *
     * @param name  user's full name
     * @param email user's email address (must be unique)
     * @return the auto-generated id of the new row
     * @throws SQLException if the insert fails (e.g. duplicate email)
     */
    public int insertUser(String name, String email) throws SQLException {
        String sql = "INSERT INTO Users (name, email) VALUES (?, ?)";

        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert succeeded but no generated key was returned.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    /**
     * Retrieves all users ordered by id.
     */
    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT id, name, email, created_at FROM Users ORDER BY id";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseInitializer.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }
        return users;
    }

    /**
     * Finds a user by their id.
     */
    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT id, name, email, created_at FROM Users WHERE id = ?";

        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a user by their email address.
     */
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, name, email, created_at FROM Users WHERE email = ?";

        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /**
     * Updates the name and/or email of an existing user.
     *
     * @return true if a row was updated, false if the id was not found
     */
    public boolean updateUser(int id, String newName, String newEmail) throws SQLException {
        String sql = "UPDATE Users SET name = ?, email = ? WHERE id = ?";

        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newName);
            ps.setString(2, newEmail);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    /**
     * Deletes a user by id.
     *
     * @return true if a row was deleted, false if the id was not found
     */
    public boolean deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM Users WHERE id = ?";

        try (Connection conn = DatabaseInitializer.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("created_at")
        );
    }
}
