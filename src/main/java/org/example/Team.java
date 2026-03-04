package org.example;

public class Team {
    private int id;
    private String name;
    private String description;
    private String createdAt;
    private int totalPoints;   // computed: SUM of TeamAwards.points
    private int memberCount;   // computed: COUNT of TeamMemberships

    public Team(int id, String name, String description, String createdAt,
                int totalPoints, int memberCount) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.createdAt   = createdAt;
        this.totalPoints = totalPoints;
        this.memberCount = memberCount;
    }

    public int    getId()          { return id; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public String getCreatedAt()   { return createdAt; }
    public int    getTotalPoints() { return totalPoints; }
    public int    getMemberCount() { return memberCount; }

    @Override
    public String toString() {
        return "Team{id=" + id + ", name='" + name + "', members=" + memberCount + ", pts=" + totalPoints + "}";
    }
}
