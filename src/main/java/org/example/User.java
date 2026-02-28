package org.example;

/**
 * Represents a row in the Users table.
 */
public class User {

    private int id;
    private String name;
    private String email;
    private String createdAt;

    public User(int id, String name, String email, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    public int getId()          { return id; }
    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public String getCreatedAt(){ return createdAt; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "', createdAt='" + createdAt + "'}";
    }
}
