package org.example;

public class Purchase {
    private int id;
    private int userId;
    private int storeItemId;
    private int pointsSpent;
    private String purchasedAt;

    public Purchase(int id, int userId, int storeItemId, int pointsSpent, String purchasedAt) {
        this.id = id;
        this.userId = userId;
        this.storeItemId = storeItemId;
        this.pointsSpent = pointsSpent;
        this.purchasedAt = purchasedAt;
    }

    public int getId()           { return id; }
    public int getUserId()       { return userId; }
    public int getStoreItemId()  { return storeItemId; }
    public int getPointsSpent()  { return pointsSpent; }
    public String getPurchasedAt(){ return purchasedAt; }

    @Override
    public String toString() {
        return "Purchase{id=" + id + ", userId=" + userId + ", storeItemId=" + storeItemId +
               ", pointsSpent=" + pointsSpent + ", purchasedAt='" + purchasedAt + "'}";
    }
}
