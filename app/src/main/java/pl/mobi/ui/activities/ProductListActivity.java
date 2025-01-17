package pl.mobi.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import pl.mobi.R;
import pl.mobi.ui.adapters.ProductAdapter;
import pl.mobi.ui.models.Product;

public class ProductListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private BottomNavigationView parentNav;
    private List<Product> productList;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(productAdapter);
        parentNav = findViewById(R.id.bottomNavigationView);

        parentNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_products) {
                // Navigate to product list
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(ProductListActivity.this, CartActivity.class));
                return true;
            }
             else if (itemId == R.id.nav_orders) {
                // Navigate to orders
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(ProductListActivity.this, MyAccountActivity.class));
                return true;
            }
            return false;
        });

        db = FirebaseFirestore.getInstance();

        fetchProducts();
    }

    private void fetchProducts() {
        db.collection("products")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(getApplicationContext(), "Błąd podczas pobierania produktów: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("FetchProductsError", "Error fetching products", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        productList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());
                            productList.add(product);
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                });
    }
}
