package org.example;

public class TeamAward {
    private int id;
    private int giverId;
    private int teamId;
    private int points;
    private String message;
    private String givenAt;

    public TeamAward(int id, int giverId, int teamId, int points, String message, String givenAt) {
        this.id      = id;
        this.giverId = giverId;
        this.teamId  = teamId;
        this.points  = points;
        this.message = message;
        this.givenAt = givenAt;
    }

    public int    getId()      { return id; }
    public int    getGiverId() { return giverId; }
    public int    getTeamId()  { return teamId; }
    public int    getPoints()  { return points; }
    public String getMessage() { return message; }
    public String getGivenAt() { return givenAt; }

    @Override
    public String toString() {
        return "TeamAward{id=" + id + ", giverId=" + giverId + ", teamId=" + teamId + ", points=" + points + "}";
    }
}
