package com.example.e_commerce;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {
    private TextInputEditText nameInput, emailInput, phoneInput;
    private TextInputEditText addressLine1Input, addressLine2Input, cityInput, stateInput, zipInput;
    private TextInputEditText cardNumberInput, cardExpiryInput, cardCvvInput;
    private MaterialButton updateProfileButton, updateAddressButton, updatePaymentButton, logoutButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        setupToolbar();
        initializeViews();
        setupFirebase();
        loadUserData();
        setupButtons();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeViews() {
        // Profile inputs
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);

        // Address inputs
        addressLine1Input = findViewById(R.id.addressLine1Input);
        addressLine2Input = findViewById(R.id.addressLine2Input);
        cityInput = findViewById(R.id.cityInput);
        stateInput = findViewById(R.id.stateInput);
        zipInput = findViewById(R.id.zipInput);

        // Payment inputs
        cardNumberInput = findViewById(R.id.cardNumberInput);
        cardExpiryInput = findViewById(R.id.cardExpiryInput);
        cardCvvInput = findViewById(R.id.cardCvvInput);

        // Buttons
        updateProfileButton = findViewById(R.id.updateProfileButton);
        updateAddressButton = findViewById(R.id.updateAddressButton);
        updatePaymentButton = findViewById(R.id.updatePaymentButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void setupFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = auth.getCurrentUser().getUid();
    }

    private void loadUserData() {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                // Load profile data
                nameInput.setText(document.getString("name"));
                emailInput.setText(document.getString("email"));
                phoneInput.setText(document.getString("phone"));

                // Load address data
                Map<String, Object> address = (Map<String, Object>) document.get("address");
                if (address != null) {
                    addressLine1Input.setText((String) address.get("line1"));
                    addressLine2Input.setText((String) address.get("line2"));
                    cityInput.setText((String) address.get("city"));
                    stateInput.setText((String) address.get("state"));
                    zipInput.setText((String) address.get("zip"));
                }

                // Load payment data (only last 4 digits for security)
                Map<String, Object> payment = (Map<String, Object>) document.get("payment");
                if (payment != null) {
                    String lastFourDigits = (String) payment.get("lastFourDigits");
                    cardNumberInput.setHint("**** **** **** " + lastFourDigits);
                    cardExpiryInput.setText((String) payment.get("expiry"));
                }
            }
        }).addOnFailureListener(e -> 
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show());
    }

    private void setupButtons() {
        updateProfileButton.setOnClickListener(v -> updateProfile());
        updateAddressButton.setOnClickListener(v -> updateAddress());
        updatePaymentButton.setOnClickListener(v -> updatePayment());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void updateProfile() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> 
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show());
    }

    private void updateAddress() {
        String line1 = addressLine1Input.getText().toString().trim();
        String line2 = addressLine2Input.getText().toString().trim();
        String city = cityInput.getText().toString().trim();
        String state = stateInput.getText().toString().trim();
        String zip = zipInput.getText().toString().trim();

        if (line1.isEmpty() || city.isEmpty() || state.isEmpty() || zip.isEmpty()) {
            Toast.makeText(this, "Please fill required address fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> address = new HashMap<>();
        address.put("line1", line1);
        address.put("line2", line2);
        address.put("city", city);
        address.put("state", state);
        address.put("zip", zip);

        db.collection("users").document(userId)
                .update("address", address)
                .addOnSuccessListener(aVoid -> 
                    Toast.makeText(this, "Address updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Failed to update address", Toast.LENGTH_SHORT).show());
    }

    private void updatePayment() {
        String cardNumber = cardNumberInput.getText().toString().trim();
        String expiry = cardExpiryInput.getText().toString().trim();
        String cvv = cardCvvInput.getText().toString().trim();

        if (cardNumber.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
            Toast.makeText(this, "Please fill all payment fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Basic validation
        if (cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
            Toast.makeText(this, "Invalid card number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!expiry.matches("(?:0[1-9]|1[0-2])/[0-9]{2}")) {
            Toast.makeText(this, "Invalid expiry date (MM/YY)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cvv.length() != 3 || !cvv.matches("\\d+")) {
            Toast.makeText(this, "Invalid CVV", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> payment = new HashMap<>();
        payment.put("lastFourDigits", cardNumber.substring(12));
        payment.put("expiry", expiry);
        // Don't store full card number or CVV for security

        db.collection("users").document(userId)
                .update("payment", payment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Payment info updated successfully", Toast.LENGTH_SHORT).show();
                    cardNumberInput.setText("");
                    cardCvvInput.setText("");
                    loadUserData(); // Reload to show masked card number
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Failed to update payment info", Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        auth.signOut();
        finish();
        // Navigate to login activity
        startActivity(new Intent(this, AuthActivity.class));
    }
}
