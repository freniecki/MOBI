package pl.mobi.ui.models;

import java.util.List;

public class Order {
    private String parentId;
    private String childId;
    private String status;
    private String pickupDate;
    private List<CartItem> items;

    public Order() {
    }

    public Order(String parentId, String childId, String pickupDate, List<CartItem> items, String status) {
        this.parentId = parentId;
        this.childId = childId;
        this.pickupDate = pickupDate;
        this.items = items;
        this.status = status;
    }

    // Gettery i settery
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public String getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(String pickupDate) {
        this.pickupDate = pickupDate;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
