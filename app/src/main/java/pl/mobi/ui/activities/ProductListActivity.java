package pl.mobi.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
                            productList.add(product);
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                });
    }
}
