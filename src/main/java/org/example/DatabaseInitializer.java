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

    /**
     * Teams — groups of users that can receive recognition points.
     */
    private static void createTeamsTable(Statement stmt) throws SQLException {
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS Teams (" +
            "    id          INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    name        TEXT    NOT NULL UNIQUE," +
            "    description TEXT," +
            "    created_at  TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now'))" +
            ");"
        );
    }

    /**
     * TeamMemberships — many-to-many join between Users and Teams.
     *   UNIQUE(user_id, team_id) prevents duplicate memberships.
     */
    private static void createTeamMembershipsTable(Statement stmt) throws SQLException {
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS TeamMemberships (" +
            "    id         INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    user_id    INTEGER NOT NULL REFERENCES Users(id)," +
            "    team_id    INTEGER NOT NULL REFERENCES Teams(id)," +
            "    created_at TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now'))," +
            "    UNIQUE(user_id, team_id)" +
            ");"
        );
    }

    /**
     * TeamAwards — records of users awarding points to a team from their team giving pool.
     *   Points accumulate on the team (not distributed to members).
     */
    private static void createTeamAwardsTable(Statement stmt) throws SQLException {
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS TeamAwards (" +
            "    id        INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    giver_id  INTEGER NOT NULL REFERENCES Users(id)," +
            "    team_id   INTEGER NOT NULL REFERENCES Teams(id)," +
            "    points    INTEGER NOT NULL," +
            "    message   TEXT," +
            "    given_at  TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now'))" +
            ");"
        );
    }

    /**
     * ShoutOuts — one row per shout-out event (individual or team).
     *   recipient_user_id: set for individual shout-outs, null for team
     *   recipient_team_id: set for team shout-outs, null for individual
     *   points: cost deducted from the giver's giving balance this quarter
     */
    private static void createShoutOutsTable(Statement stmt) throws SQLException {
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS ShoutOuts (" +
            "    id                INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    giver_id          INTEGER NOT NULL REFERENCES Users(id)," +
            "    recipient_user_id INTEGER REFERENCES Users(id)," +
            "    recipient_team_id INTEGER REFERENCES Teams(id)," +
            "    points            INTEGER NOT NULL," +
            "    message           TEXT," +
            "    given_at          TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now'))" +
            ");"
        );
    }

    /**
     * ShoutOutRecipients — one row per user who actually received points.
     *   Individual shout-out: one row (the direct recipient).
     *   Team shout-out: one row per qualifying member (giver excluded if they are a member).
     */
    private static void createShoutOutRecipientsTable(Statement stmt) throws SQLException {
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS ShoutOutRecipients (" +
            "    id           INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    shout_out_id INTEGER NOT NULL REFERENCES ShoutOuts(id)," +
            "    user_id      INTEGER NOT NULL REFERENCES Users(id)," +
            "    points       INTEGER NOT NULL" +
            ");"
        );
    }

    /**
     * AppConfig — key/value store for admin-configurable runtime settings.
     *   shout_out_value:        how many points each individual shout-out awards (default 10)
     *   team_shout_out_value:   how many points each team shout-out awards per member (default 5)
     *   shout_out_allowance:    how many shout-outs a user may give per reset period (default 10)
     *   reset_interval_quantity: numeric portion of the reset period (default 2)
     *   reset_interval_unit:    time unit for the reset period: DAY/WEEK/MONTH/QUARTER/YEAR (default WEEK)
     */
    private static void createAppConfigTable(Statement stmt) throws SQLException {
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS AppConfig (" +
            "    key   TEXT PRIMARY KEY," +
            "    value TEXT NOT NULL" +
            ");"
        );
        stmt.execute(
            "INSERT OR IGNORE INTO AppConfig (key, value) VALUES ('shout_out_value', '10');"
        );
        stmt.execute(
            "INSERT OR IGNORE INTO AppConfig (key, value) VALUES ('team_shout_out_value', '5');"
        );
        stmt.execute(
            "INSERT OR IGNORE INTO AppConfig (key, value) VALUES ('shout_out_allowance', '10');"
        );
        stmt.execute(
            "INSERT OR IGNORE INTO AppConfig (key, value) VALUES ('reset_interval_quantity', '2');"
        );
        stmt.execute(
            "INSERT OR IGNORE INTO AppConfig (key, value) VALUES ('reset_interval_unit', 'WEEK');"
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
            createTeamsTable(stmt);
            createTeamMembershipsTable(stmt);
            createTeamAwardsTable(stmt);
            createShoutOutsTable(stmt);
            createShoutOutRecipientsTable(stmt);
            createAppConfigTable(stmt);

            System.out.println("Database initialization complete.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
