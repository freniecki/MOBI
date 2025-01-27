package pl.mobi.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import pl.mobi.R;
import pl.mobi.ui.utils.SessionManager;

public class MyAccountActivity extends AppCompatActivity {

    private TextView emailTextView, childrenTitleText;
    private LinearLayout childrenListLayout, addChildSection;
    private EditText addChildEmailEditText;
    private Button addChildButton, logoutButton, themeButton;
    private BottomNavigationView parentNav;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        emailTextView = findViewById(R.id.emailTextView);
        childrenTitleText = findViewById(R.id.childrenTitleText);
        childrenListLayout = findViewById(R.id.childrenListLayout);
        addChildEmailEditText = findViewById(R.id.addChildEmailEditText);
        addChildButton = findViewById(R.id.addChildButton);
        logoutButton = findViewById(R.id.logoutButton);
        addChildSection = findViewById(R.id.addChildSection);

        parentNav = findViewById(R.id.bottomNavigationView);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(this);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            emailTextView.setText(currentUser.getEmail());

            loadUserRoleAndChildren(currentUser.getUid());

            logoutButton.setOnClickListener(v -> logoutUser());
            addChildButton.setOnClickListener(v -> addChild());
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MyAccountActivity.this, LoginActivity.class));
            finish();
        }

        themeButton = findViewById(R.id.themeButton);

        themeButton.setOnClickListener(click -> {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            recreate();
        });
    }

    private void setupBottomNavigationView(String role) {
        parentNav.getMenu().clear();

        if ("Rodzic".equals(role)) {
            parentNav.inflateMenu(R.menu.parent_menu);
        } else if ("Dziecko".equals(role)) {
            parentNav.inflateMenu(R.menu.child_menu);
        } else if ("Owner".equals(role)) {
            parentNav.inflateMenu(R.menu.owner_menu);
        }

        parentNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_products) {
                startActivity(new Intent(MyAccountActivity.this, ProductListActivity.class));
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(MyAccountActivity.this, CartActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(MyAccountActivity.this, MyOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                return true;
            } else if (itemId == R.id.nav_conf) {
                 startActivity(new Intent(MyAccountActivity.this, ConfirmActivity.class));
                return true;
            } else if (itemId == R.id.nav_check) {
                 startActivity(new Intent(MyAccountActivity.this, PickUpActivity.class));
                return true;
            } else if (itemId == R.id.nav_stats) {
                 startActivity(new Intent(MyAccountActivity.this, StatsActivity.class));
                return true;
            }
            return false;
        });

        parentNav.setSelectedItemId(R.id.nav_account);
    }

    private void loadUserRoleAndChildren(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userRole = task.getResult().getString("role");

                        setupBottomNavigationView(userRole);

                        if ("Rodzic".equals(userRole)) {
                            childrenTitleText.setText("Dzieci: ");
                            loadChildrenList(userId);
                        } else {
                            childrenTitleText.setVisibility(TextView.GONE);
                            childrenListLayout.setVisibility(LinearLayout.GONE);
                            addChildSection.setVisibility(LinearLayout.GONE);
                        }
                    } else {
                        Toast.makeText(MyAccountActivity.this, "Failed to get user role", Toast.LENGTH_SHORT).show();
                    }
                });
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
                                                    LinearLayout childLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.child_item, null);
                                                    TextView childTextView = childLayout.findViewById(R.id.childEmailTextView);
                                                    childTextView.setText(childEmail);
                                                    ImageView deleteIcon = childLayout.findViewById(R.id.childDeleteIcon);
                                                    deleteIcon.setOnClickListener(v -> deleteChildFromParent(parentId, childId));

                                                    childrenListLayout.addView(childLayout);
                                                }

                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void deleteChildFromParent(String parentId, String childId) {
        DocumentReference parentRef = db.collection("users").document(parentId);
        parentRef.update("children", FieldValue.arrayRemove(childId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MyAccountActivity.this, "Dziecko zostało usunięte", Toast.LENGTH_SHORT).show();
                    childrenListLayout.removeAllViews();
                    loadChildrenList(parentId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MyAccountActivity.this, "Failed to remove child: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void addChild() {
        String childEmail = addChildEmailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(childEmail)) {
            addChildEmailEditText.setError("Podaj adres email dziecka.");
            return;
        }

        db.collection("users").whereEqualTo("email", childEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot childDoc = task.getResult().getDocuments().get(0);
                        String childRole = childDoc.getString("role");

                        if ("Dziecko".equals(childRole)) {
                            String childId = childDoc.getId();
                            String parentId = mAuth.getCurrentUser().getUid();

                            DocumentReference parentRef = db.collection("users").document(parentId);
                            parentRef.update("children", FieldValue.arrayUnion(childId))
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(MyAccountActivity.this, "Dziecko zostało dodane pomyślnie", Toast.LENGTH_SHORT).show();
                                        childrenListLayout.removeAllViews();
                                        loadChildrenList(parentId);
                                        addChildEmailEditText.setText("");
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(MyAccountActivity.this, "Failed to add child: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(MyAccountActivity.this, "Ten użytkownik nie jest dzieckiem", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MyAccountActivity.this, "Nie znaleziono dziecka", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logoutUser() {
        sessionManager.clearSession();
        mAuth.signOut();
        Toast.makeText(MyAccountActivity.this, "Wylogowano cię", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MyAccountActivity.this, MainActivity.class));
        finish();
    }
}
