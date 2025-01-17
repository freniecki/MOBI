package pl.mobi.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import pl.mobi.R;

public class MyAccountActivity extends AppCompatActivity {

    private TextView emailTextView, childrenTitleText;
    private LinearLayout childrenListLayout;
    private EditText addChildEmailEditText;
    private Button addChildButton, logoutButton;
    private BottomNavigationView parentNav;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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

        parentNav = findViewById(R.id.bottomNavigationView);
        parentNav.setSelectedItemId(R.id.nav_account);

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
                // Navigate to orders
                return true;
            } else if (itemId == R.id.nav_account) {
                // Navigate to account
                return true;
            }
            return false;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            emailTextView.setText(currentUser.getEmail());

            // Check user's role and load children if "Rodzic"
            loadUserRoleAndChildren(currentUser.getUid());

            logoutButton.setOnClickListener(v -> logoutUser());
            addChildButton.setOnClickListener(v -> addChild());
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MyAccountActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void loadUserRoleAndChildren(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userRole = task.getResult().getString("role");

                        if ("Rodzic".equals(userRole)) {
                            childrenTitleText.setText("Dzieci: ");
                            loadChildrenList(userId);
                        } else {
                            childrenTitleText.setVisibility(TextView.GONE);
                            childrenListLayout.setVisibility(LinearLayout.GONE);
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
                                                    // Inflate the child item template
                                                    LinearLayout childLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.child_item, null);

                                                    // Set the child's email dynamically
                                                    TextView childTextView = childLayout.findViewById(R.id.childEmailTextView);
                                                    childTextView.setText(childEmail);

                                                    // Set the click listener for the delete icon
                                                    ImageView deleteIcon = childLayout.findViewById(R.id.childDeleteIcon);
                                                    deleteIcon.setOnClickListener(v -> deleteChildFromParent(parentId, childId));

                                                    // Add the child layout to the children list
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
                    // Refresh the children list after deletion
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
                            Toast.makeText(MyAccountActivity.this, "The user is not a child", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MyAccountActivity.this, "Child not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(MyAccountActivity.this, "Wylogowano cię", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MyAccountActivity.this, MainActivity.class));
        finish();
    }
}
