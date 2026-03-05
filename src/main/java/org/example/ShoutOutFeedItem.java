package org.example;

public class ShoutOutFeedItem {
    private int     id;
    private String  giverName;
    private String  recipientName;  // user name or team name
    private boolean isTeam;
    private int     points;
    private String  message;
    private String  givenAt;

    public ShoutOutFeedItem(int id, String giverName, String recipientName,
                            boolean isTeam, int points, String message, String givenAt) {
        this.id            = id;
        this.giverName     = giverName;
        this.recipientName = recipientName;
        this.isTeam        = isTeam;
        this.points        = points;
        this.message       = message;
        this.givenAt       = givenAt;
    }

    public int     getId()            { return id; }
    public String  getGiverName()     { return giverName; }
    public String  getRecipientName() { return recipientName; }
    public boolean isTeam()           { return isTeam; }
    public int     getPoints()        { return points; }
    public String  getMessage()       { return message; }
    public String  getGivenAt()       { return givenAt; }
}
