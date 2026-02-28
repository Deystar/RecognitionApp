package org.example;

public class AwardType {
    private int id;
    private String name;
    private String description;
    private int pointsCost;
    private String createdAt;

    public AwardType(int id, String name, String description, int pointsCost, String createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pointsCost = pointsCost;
        this.createdAt = createdAt;
    }

    public int getId()           { return id; }
    public String getName()      { return name; }
    public String getDescription(){ return description; }
    public int getPointsCost()   { return pointsCost; }
    public String getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "AwardType{id=" + id + ", name='" + name + "', pointsCost=" + pointsCost + "}";
    }
}
