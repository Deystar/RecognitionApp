package org.example;

public class ShoutOut {
    private int     id;
    private int     giverId;
    private Integer recipientUserId;  // null for team shout-outs
    private Integer recipientTeamId;  // null for individual shout-outs
    private int     points;
    private String  message;
    private String  givenAt;

    public ShoutOut(int id, int giverId, Integer recipientUserId, Integer recipientTeamId,
                    int points, String message, String givenAt) {
        this.id               = id;
        this.giverId          = giverId;
        this.recipientUserId  = recipientUserId;
        this.recipientTeamId  = recipientTeamId;
        this.points           = points;
        this.message          = message;
        this.givenAt          = givenAt;
    }

    public int     getId()               { return id; }
    public int     getGiverId()          { return giverId; }
    public Integer getRecipientUserId()  { return recipientUserId; }
    public Integer getRecipientTeamId()  { return recipientTeamId; }
    public int     getPoints()           { return points; }
    public String  getMessage()          { return message; }
    public String  getGivenAt()          { return givenAt; }
}
