package pl.mobi.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import pl.mobi.R;
import pl.mobi.ui.adapters.PickupAdapter;
import pl.mobi.ui.models.Order;

public class PickUpActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PickupAdapter pickupAdapter;
    private List<Order> orderList;
    private BottomNavigationView parentNav;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup);

        recyclerView = findViewById(R.id.recyclerViewOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        pickupAdapter = new PickupAdapter(this, orderList);
        recyclerView.setAdapter(pickupAdapter);

        parentNav = findViewById(R.id.bottomNavigationView);
        parentNav.setSelectedItemId(R.id.nav_check);

        parentNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_conf) {
                startActivity(new Intent(PickUpActivity.this, ConfirmActivity.class));
                return true;
            } else if (itemId == R.id.nav_check) {
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(PickUpActivity.this, MyOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_stats) {
                startActivity(new Intent(PickUpActivity.this, StatsActivity.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(PickUpActivity.this, MyAccountActivity.class));
                return true;
            }
            return false;
        });

        db = FirebaseFirestore.getInstance();

        fetchPendingOrders();
    }

    private void fetchPendingOrders() {
        db.collection("orders")
                .whereEqualTo("status", "Gotowe do odbioru")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Błąd podczas pobierania zamówień: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("FetchOrdersError", "Error fetching orders", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        orderList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Order order = document.toObject(Order.class);
                            order.setOrderId(document.getId()); // Store the document ID
                            orderList.add(order);
                        }
                        pickupAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        db.collection("orders").document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status zamówienia zaktualizowany na: " + newStatus, Toast.LENGTH_SHORT).show();
                    fetchPendingOrders();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Błąd przy aktualizacji statusu zamówienia: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
