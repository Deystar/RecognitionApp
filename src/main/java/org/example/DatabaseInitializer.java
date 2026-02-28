package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the SQLite database and creates all tables if they do not exist.
 *
 * Table overview:
 *   Users        - employees in the system
 *   AwardTypes   - catalogue of award types with a default point cost
 *   AwardsGiven  - records of one user giving an award to another (draws from giver's quarterly bank)
 *   StoreItems   - items available in the company store
 *   Purchases    - records of users spending earned points on store items
 */
public class DatabaseInitializer {

    private static final String DB_URL = "jdbc:sqlite:recognition.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // -------------------------------------------------------------------------
    // Table creation
    // -------------------------------------------------------------------------

    private static void createUsersTable(Statement stmt) throws SQLException {
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS Users (" +
            "    id         INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    name       TEXT    NOT NULL," +
            "    email      TEXT    NOT NULL UNIQUE," +
            "    created_at TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now'))" +
            ");"
        );
    }

    /**
     * AwardTypes — the catalogue of awards managers/peers can give.
     *   points_cost: how many points are deducted from the GIVER's quarterly bank
     */
    private static void createAwardTypesTable(Statement stmt) throws SQLException {
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS AwardTypes (" +
            "    id          INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    name        TEXT    NOT NULL UNIQUE," +
            "    description TEXT," +
            "    points_cost INTEGER NOT NULL," +
            "    created_at  TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now'))" +
            ");"
        );
    }

    /**
     * AwardsGiven — each row is one recognition event.
     *   giver_id:    the user spending points from their quarterly bank
     *   recipient_id: the user earning points toward store purchases
     *   points:      actual points transferred (may differ from AwardType default if overridden)
     */
    private static void createAwardsGivenTable(Statement stmt) throws SQLException {
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS AwardsGiven (" +
            "    id             INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    giver_id       INTEGER NOT NULL REFERENCES Users(id)," +
            "    recipient_id   INTEGER NOT NULL REFERENCES Users(id)," +
            "    award_type_id  INTEGER NOT NULL REFERENCES AwardTypes(id)," +
            "    points         INTEGER NOT NULL," +
            "    message        TEXT," +
            "    given_at       TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now'))" +
            ");"
        );
    }

    /**
     * StoreItems — rewards available in the company store.
     *   points_cost:        how many earned points the item costs
     *   quantity_available: NULL means unlimited stock
     */
    private static void createStoreItemsTable(Statement stmt) throws SQLException {
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS StoreItems (" +
            "    id                 INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    name               TEXT    NOT NULL," +
            "    description        TEXT," +
            "    points_cost        INTEGER NOT NULL," +
            "    quantity_available INTEGER," +
            "    created_at         TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now'))" +
            ");"
        );
    }

    /**
     * Purchases — each row is one store redemption by a user.
     */
    private static void createPurchasesTable(Statement stmt) throws SQLException {
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS Purchases (" +
            "    id            INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    user_id       INTEGER NOT NULL REFERENCES Users(id)," +
            "    store_item_id INTEGER NOT NULL REFERENCES StoreItems(id)," +
            "    points_spent  INTEGER NOT NULL," +
            "    purchased_at  TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now'))" +
            ");"
        );
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void initialize() {
        System.out.println("Initializing database...");
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // Foreign key enforcement must be enabled per connection in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            createUsersTable(stmt);
            createAwardTypesTable(stmt);
            createAwardsGivenTable(stmt);
            createStoreItemsTable(stmt);
            createPurchasesTable(stmt);

            System.out.println("Database initialization complete.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
