package pl.mobi.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import pl.mobi.R;

public class StatsActivity extends AppCompatActivity {
    private BottomNavigationView parentNav;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        parentNav = findViewById(R.id.bottomNavigationView);
        parentNav.setSelectedItemId(R.id.nav_stats);

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

        db = FirebaseFirestore.getInstance();
    }
}
