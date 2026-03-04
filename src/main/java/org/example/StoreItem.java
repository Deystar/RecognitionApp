package org.example;

public class StoreItem {
    private int id;
    private String name;
    private String description;
    private int pointsCost;
    private Integer quantityAvailable; // null = unlimited
    private String createdAt;
    private String source; // "internal" or "external" — not persisted to DB

    // Used by StoreItemRepository — defaults source to "internal"
    public StoreItem(int id, String name, String description, int pointsCost, Integer quantityAvailable, String createdAt) {
        this(id, name, description, pointsCost, quantityAvailable, createdAt, "internal");
    }

    // Used by ExternalStoreService to tag items with their origin
    public StoreItem(int id, String name, String description, int pointsCost, Integer quantityAvailable, String createdAt, String source) {
        this.id                = id;
        this.name              = name;
        this.description       = description;
        this.pointsCost        = pointsCost;
        this.quantityAvailable = quantityAvailable;
        this.createdAt         = createdAt;
        this.source            = source;
    }

    public int getId()                      { return id; }
    public String getName()                 { return name; }
    public String getDescription()          { return description; }
    public int getPointsCost()              { return pointsCost; }
    public Integer getQuantityAvailable()   { return quantityAvailable; }
    public String getCreatedAt()            { return createdAt; }
    public String getSource()               { return source; }

    @Override
    public String toString() {
        String stock = (quantityAvailable == null) ? "unlimited" : String.valueOf(quantityAvailable);
        return "StoreItem{id=" + id + ", name='" + name + "', pointsCost=" + pointsCost + ", stock=" + stock + ", source='" + source + "'}";
    }
}
