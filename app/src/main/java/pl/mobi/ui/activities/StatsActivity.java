package pl.mobi.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import pl.mobi.R;
import pl.mobi.ui.adapters.StatsAdapter;

public class StatsActivity extends AppCompatActivity {
    private BottomNavigationView parentNav;
    private FirebaseFirestore db;
    private Button startDateButton, endDateButton, filterByQuantityButton, filterByProfitButton;
    private RecyclerView productsRecyclerView;

    private Date startDate = null, endDate = null;
    private StatsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        db = FirebaseFirestore.getInstance();
        parentNav = findViewById(R.id.bottomNavigationView);
        parentNav.setSelectedItemId(R.id.nav_stats);
        startDateButton = findViewById(R.id.startDateButton);
        endDateButton = findViewById(R.id.endDateButton);
        filterByQuantityButton = findViewById(R.id.filterByQuantityButton);
        filterByProfitButton = findViewById(R.id.filterByProfitButton);
        productsRecyclerView = findViewById(R.id.productsRecyclerView);

        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StatsAdapter(new ArrayList<>());
        productsRecyclerView.setAdapter(adapter);

        filterStatistics("quantity");

        startDateButton.setOnClickListener(v -> showDatePickerDialog(true));
        endDateButton.setOnClickListener(v -> showDatePickerDialog(false));

        filterByQuantityButton.setOnClickListener(v -> filterStatistics("quantity"));
        filterByProfitButton.setOnClickListener(v -> filterStatistics("profit"));

        parentNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_conf) {
                startActivity(new Intent(StatsActivity.this, ConfirmActivity.class));
                return true;
            } else if (itemId == R.id.nav_check) {
                startActivity(new Intent(StatsActivity.this, PickUpActivity.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(StatsActivity.this, MyOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_stats) {
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(StatsActivity.this, MyAccountActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showDatePickerDialog(boolean isStartDate) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    Date selectedDate = calendar.getTime();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    if (isStartDate) {
                        startDate = selectedDate;
                        startDateButton.setText("Data początkowa: " + sdf.format(startDate));
                    } else {
                        endDate = selectedDate;
                        endDateButton.setText("Data końcowa: " + sdf.format(endDate));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void filterStatistics(String filterType) {
        db.collection("orders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, StatsAdapter.ProductStat> statsMap = new TreeMap<>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    Timestamp pickupDate = document.getTimestamp("pickupDate");
                    if (pickupDate != null && isWithinDateRange(pickupDate.toDate())) {
                        for (Map<String, Object> item : (Iterable<Map<String, Object>>) document.get("items")) {
                            String productName = (String) item.get("productName");
                            int quantity = ((Long) item.get("quantity")).intValue();
                            double price = ((Double) item.get("productPrice"));
                            double totalPrice = price * quantity;

                            if (statsMap.containsKey(productName)) {
                                StatsAdapter.ProductStat stat = statsMap.get(productName);
                                stat = new StatsAdapter.ProductStat(productName,
                                        stat.getQuantity() + quantity,
                                        stat.getTotalPrice() + totalPrice);
                                statsMap.put(productName, stat);
                            } else {
                                statsMap.put(productName, new StatsAdapter.ProductStat(productName, quantity, totalPrice));
                            }
                        }
                    }
                }

                List<StatsAdapter.ProductStat> sortedStats = new ArrayList<>(statsMap.values());
                if (filterType.equals("quantity")) {
                    sortedStats.sort((a, b) -> Integer.compare(b.getQuantity(), a.getQuantity()));
                } else {
                    sortedStats.sort((a, b) -> Double.compare(b.getTotalPrice(), a.getTotalPrice()));
                }

                adapter.updateData(sortedStats);
            }
        });
    }

    private boolean isWithinDateRange(Date date) {
        if (startDate != null && date.before(startDate)) {
            return false;
        }
        if (endDate != null && date.after(endDate)) {
            return false;
        }
        return true;
    }
}
