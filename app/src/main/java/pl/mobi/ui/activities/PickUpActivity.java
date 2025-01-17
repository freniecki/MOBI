package pl.mobi.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import pl.mobi.R;

public class PickUpActivity extends AppCompatActivity {
    private BottomNavigationView parentNav;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_up);

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
    }
}
