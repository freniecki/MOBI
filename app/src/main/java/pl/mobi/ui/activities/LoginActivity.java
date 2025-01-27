package pl.mobi.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import pl.mobi.R;
import pl.mobi.ui.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.editTextTextEmailAddress2);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        Button loginButton = findViewById(R.id.loginButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            String userRole = sessionManager.getUserRole();
            navigateToRoleBasedActivity(userRole);
        }

        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Adres E-mail jest wymagany");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Hasło jest wymagane");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                        db.collection("users").document(userId)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        String userRole = task1.getResult().getString("role");
                                        
                                        if (userRole != null) {
                                            sessionManager.createSession(userRole);
                                            Toast.makeText(LoginActivity.this, "Otwieram sesję", Toast.LENGTH_SHORT).show();
                                            navigateToRoleBasedActivity(userRole);
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Rola użytkownika nie została przypisana.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Nie udało się pobrać danych użytkownika: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(LoginActivity.this, "Logowanie nie powiodło się: "
                                + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToRoleBasedActivity(String userRole) {
        switch (userRole) {
            case "Rodzic":
                Intent paerntIntent = new Intent(LoginActivity.this, ProductListActivity.class);
                startActivity(paerntIntent);
                break;
            case "Dziecko":
                Intent childIntent = new Intent(LoginActivity.this, MyOrdersActivity.class);
                startActivity(childIntent);
                break;
            case "Owner":
                Intent ownerIntent = new Intent(LoginActivity.this, PickUpActivity.class);
                startActivity(ownerIntent);
                break;
        }
    }
}
