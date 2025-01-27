package pl.mobi.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.mobi.R;
import pl.mobi.ui.adapters.CartAdapter;
import pl.mobi.ui.models.CartItem;
import pl.mobi.ui.models.Child;
import pl.mobi.ui.utils.CartManager;

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private BottomNavigationView parentNav;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Child> childrenList = new ArrayList<>();
    private Spinner childSpinner;
    private TextView pickupDateTextView;
    private Button dateButtom, confirmButton;
    private Timestamp selectedPickupDateTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        childSpinner = findViewById(R.id.childSpinner);

        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        cartAdapter = new CartAdapter(this, cartItems);
        recyclerView.setAdapter(cartAdapter);

        TextView totalPrice = findViewById(R.id.totalPrice);
        totalPrice.setText(String.format("Suma: %.2f zł", calculateTotal(cartItems)));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        pickupDateTextView = findViewById(R.id.pickupDateTextView);

        dateButtom = findViewById(R.id.pickupDateButton);
        dateButtom.setOnClickListener(v -> showDatePicker());

        confirmButton = findViewById(R.id.checkoutButton);
        confirmButton.setOnClickListener(v -> confirmOrder());

        parentNav = findViewById(R.id.bottomNavigationView);
        parentNav.setSelectedItemId(R.id.nav_cart);

        parentNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_products) {
                startActivity(new Intent(CartActivity.this, ProductListActivity.class));
                return true;
            } else if (itemId == R.id.nav_cart) {
                return true;
            }
            else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(CartActivity.this, MyOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(CartActivity.this, MyAccountActivity.class));
                return true;
            }
            return false;
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        loadChildrenList(currentUser.getUid());
    }

    public static double calculateTotal(List<CartItem> cartItems) {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getProductPrice() * item.getQuantity();
        }
        return total;
    }

    private void loadChildrenList(String parentId) {
        db.collection("users").document(parentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot parentDocument = task.getResult();
                        List<String> childrenIds = (List<String>) parentDocument.get("children");

                        if (childrenIds != null) {
                            for (String childId : childrenIds) {
                                db.collection("users").document(childId)
                                        .get()
                                        .addOnCompleteListener(childTask -> {
                                            if (childTask.isSuccessful() && childTask.getResult() != null) {
                                                DocumentSnapshot childDocument = childTask.getResult();
                                                String childEmail = childDocument.getString("email");
                                                if (childEmail != null) {
                                                    childrenList.add(new Child(childDocument.getId(), childEmail));
                                                }
                                            }
                                            ArrayAdapter<Child> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, childrenList);
                                            childSpinner.setAdapter(adapter);
                                        });
                            }
                        }
                    }
                });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(selectedYear, selectedMonth, selectedDay);

            Calendar currentDate = Calendar.getInstance();
            if (selectedDate.before(currentDate)) {
                Toast.makeText(CartActivity.this, "Wybierz datę nie wcześniejszą niż dzisiaj!", Toast.LENGTH_SHORT).show();
            } else {
                String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                pickupDateTextView.setText(date);

                selectedPickupDateTimestamp = new Timestamp(selectedDate.getTime());
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void confirmOrder() {
        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Koszyk jest pusty. Dodaj produkty do koszyka!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPickupDateTimestamp == null) {
            Toast.makeText(this, "Wybierz datę odbioru!", Toast.LENGTH_SHORT).show();
            return;
        }

        String parentId = mAuth.getCurrentUser().getUid();
        Child selectedChild = (Child) childSpinner.getSelectedItem();
        if (selectedChild == null) {
            Toast.makeText(this, "Wybierz dziecko!", Toast.LENGTH_SHORT).show();
            return;
        }
        String childId = selectedChild.getId();

        Map<String, Object> order = new HashMap<>();
        order.put("childId", childId);
        order.put("parentId", parentId);
        order.put("pickupDate", selectedPickupDateTimestamp);
        order.put("status", "Złożone");
        order.put("items", cartItems);

        FirebaseFirestore.getInstance().collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Zamówienie zostało złożone!", Toast.LENGTH_SHORT).show();
                    CartManager.getInstance().clearCart();
                    Intent intent = new Intent(CartActivity.this, MyOrdersActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Błąd przy składaniu zamówienia: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
