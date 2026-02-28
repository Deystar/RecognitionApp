package org.example;

public class AwardGiven {
    private int id;
    private int giverId;
    private int recipientId;
    private int awardTypeId;
    private int points;
    private String message;
    private String givenAt;

    public AwardGiven(int id, int giverId, int recipientId, int awardTypeId, int points, String message, String givenAt) {
        this.id = id;
        this.giverId = giverId;
        this.recipientId = recipientId;
        this.awardTypeId = awardTypeId;
        this.points = points;
        this.message = message;
        this.givenAt = givenAt;
    }

    public int getId()           { return id; }
    public int getGiverId()      { return giverId; }
    public int getRecipientId()  { return recipientId; }
    public int getAwardTypeId()  { return awardTypeId; }
    public int getPoints()       { return points; }
    public String getMessage()   { return message; }
    public String getGivenAt()   { return givenAt; }

    @Override
    public String toString() {
        return "AwardGiven{id=" + id + ", giverId=" + giverId + ", recipientId=" + recipientId +
               ", points=" + points + ", givenAt='" + givenAt + "'}";
    }
}
