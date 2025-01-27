package pl.mobi.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import pl.mobi.R;
import pl.mobi.ui.adapters.OrderAdapter;
import pl.mobi.ui.models.Order;

public class MyOrdersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private TextView title;
    private List<Order> orderList;
    private BottomNavigationView parentNav;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String userRole;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        recyclerView = findViewById(R.id.recyclerViewOrders);
        title = findViewById(R.id.title);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);
        recyclerView.setAdapter(orderAdapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        parentNav = findViewById(R.id.bottomNavigationView);

        fetchUserRole();
    }

    private void setupBottomNavigationView(String role) {
        parentNav.getMenu().clear();

        // Set menu based on role
        if ("Rodzic".equals(role)) {
            parentNav.inflateMenu(R.menu.parent_menu);

        } else if ("Dziecko".equals(role)) {
            parentNav.inflateMenu(R.menu.child_menu);
        } else if ("Owner".equals(role)) {
            parentNav.inflateMenu(R.menu.owner_menu);
            title.setText("Wszystkie zamówienia");
        }

        // Handle navigation clicks
        parentNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_products) {
                startActivity(new Intent(MyOrdersActivity.this, ProductListActivity.class));
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(MyOrdersActivity.this, CartActivity.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(MyOrdersActivity.this, MyAccountActivity.class));
                return true;
            } else if (itemId == R.id.nav_conf) {
                startActivity(new Intent(MyOrdersActivity.this, ConfirmActivity.class));
                return true;
            } else if (itemId == R.id.nav_check) {
                startActivity(new Intent(MyOrdersActivity.this, PickUpActivity.class));
                return true;
            } else if (itemId == R.id.nav_stats) {
                startActivity(new Intent(MyOrdersActivity.this, StatsActivity.class));
                return true;
            }
            return false;
        });

        parentNav.setSelectedItemId(R.id.nav_orders);
    }


    private void fetchUserRole() {
        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userRole = documentSnapshot.getString("role");
                        if (userRole != null) {
                            fetchOrders();
                            setupBottomNavigationView(userRole);
                        } else {
                            Toast.makeText(this, "Nie można pobrać roli użytkownika.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Błąd przy pobieraniu roli użytkownika: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("MyOrdersActivity", "Error fetching user role", e);
                });
    }

    private void fetchOrders() {
        Query query;
        String userId = mAuth.getCurrentUser().getUid();

        if ("Rodzic".equalsIgnoreCase(userRole)) {
            query = db.collection("orders").whereEqualTo("parentId", userId);
        } else if ("Dziecko".equalsIgnoreCase(userRole)) {
            query = db.collection("orders").whereEqualTo("childId", userId);
        } else if ("Owner".equalsIgnoreCase(userRole)) {
            query = db.collection("orders");
        } else {
            Toast.makeText(this, "Nieznana rola użytkownika.", Toast.LENGTH_SHORT).show();
            return;
        }

        query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(this, "Błąd podczas pobierania zamówień: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("FetchOrdersError", "Error fetching orders", e);
                return;
            }

            if (queryDocumentSnapshots != null) {
                orderList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Order order = document.toObject(Order.class);
                    order.setOrderId(document.getId());
                    orderList.add(order);
                }
                orderAdapter.notifyDataSetChanged();
            }
        });
    }
}
