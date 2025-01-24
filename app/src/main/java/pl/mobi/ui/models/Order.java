package pl.mobi.ui.models;

import com.google.firebase.Timestamp;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Order {

    private String orderId;
    private String parentId;
    private String childId;
    private String status;
    private Timestamp pickupDate;
    private List<CartItem> items;

    public Order() {
    }

    public Order(String parentId, String childId, Timestamp pickupDate, List<CartItem> items, String status) {
        this.parentId = parentId;
        this.childId = childId;
        this.pickupDate = pickupDate;
        this.items = items;
        this.status = status;
    }
}
