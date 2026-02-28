package org.example;

public class AwardFeedItem {
    private int id;
    private int giverId;
    private String giverName;
    private int recipientId;
    private String recipientName;
    private int awardTypeId;
    private String awardTypeName;
    private int points;
    private String message;
    private String givenAt;

    public AwardFeedItem(int id, int giverId, String giverName,
                         int recipientId, String recipientName,
                         int awardTypeId, String awardTypeName,
                         int points, String message, String givenAt) {
        this.id            = id;
        this.giverId       = giverId;
        this.giverName     = giverName;
        this.recipientId   = recipientId;
        this.recipientName = recipientName;
        this.awardTypeId   = awardTypeId;
        this.awardTypeName = awardTypeName;
        this.points        = points;
        this.message       = message;
        this.givenAt       = givenAt;
    }

    public int getId()             { return id; }
    public int getGiverId()        { return giverId; }
    public String getGiverName()   { return giverName; }
    public int getRecipientId()    { return recipientId; }
    public String getRecipientName(){ return recipientName; }
    public int getAwardTypeId()    { return awardTypeId; }
    public String getAwardTypeName(){ return awardTypeName; }
    public int getPoints()         { return points; }
    public String getMessage()     { return message; }
    public String getGivenAt()     { return givenAt; }
}
