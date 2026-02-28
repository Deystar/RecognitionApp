package org.example;

public class StoreItem {
    private int id;
    private String name;
    private String description;
    private int pointsCost;
    private Integer quantityAvailable; // null = unlimited
    private String createdAt;

    public StoreItem(int id, String name, String description, int pointsCost, Integer quantityAvailable, String createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pointsCost = pointsCost;
        this.quantityAvailable = quantityAvailable;
        this.createdAt = createdAt;
    }

    public int getId()                      { return id; }
    public String getName()                 { return name; }
    public String getDescription()          { return description; }
    public int getPointsCost()              { return pointsCost; }
    public Integer getQuantityAvailable()   { return quantityAvailable; }
    public String getCreatedAt()            { return createdAt; }

    @Override
    public String toString() {
        String stock = (quantityAvailable == null) ? "unlimited" : String.valueOf(quantityAvailable);
        return "StoreItem{id=" + id + ", name='" + name + "', pointsCost=" + pointsCost + ", stock=" + stock + "}";
    }
}
