package pl.mobi.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import pl.mobi.R;
import pl.mobi.ui.adapters.CartAdapter;
import pl.mobi.ui.models.CartItem;
import pl.mobi.ui.utils.CartManager;

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;

    private BottomNavigationView parentNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        cartAdapter = new CartAdapter(this, cartItems);
        recyclerView.setAdapter(cartAdapter);

        TextView totalPrice = findViewById(R.id.totalPrice);
        totalPrice.setText(String.format("Suma: %.2f zł", calculateTotal(cartItems)));

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
                // Navigate to orders
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(CartActivity.this, MyAccountActivity.class));
                return true;
            }
            return false;
        });
    }

    private double calculateTotal(List<CartItem> cartItems) {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getProductPrice() * item.getQuantity();
        }
        return total;
    }
}
