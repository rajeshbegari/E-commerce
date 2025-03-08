package com.example.e_commerce;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.e_commerce.adapters.CheckoutItemAdapter;
import com.example.e_commerce.models.CartItem;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {
    private static final String TAG = "CheckoutActivity";
    private RecyclerView checkoutItemsRecyclerView;
    private CheckoutItemAdapter adapter;
    private TextView subtotalText;
    private TextView totalText;
    private MaterialButton paymentButton;
    private List<CartItem> cartItems;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private double totalAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        Log.d(TAG, "CheckoutActivity started");

        // Get total amount from intent
        if (getIntent().hasExtra("total_amount")) {
            totalAmount = getIntent().getDoubleExtra("total_amount", 0.0);
            Log.d(TAG, "Received total amount: " + totalAmount);
        }

        initViews();
        loadCartItems();
        setupPaymentButton();
    }

    private void initViews() {
        try {
            checkoutItemsRecyclerView = findViewById(R.id.checkoutItemsRecyclerView);
            subtotalText = findViewById(R.id.subtotalText);
            totalText = findViewById(R.id.totalText);
            paymentButton = findViewById(R.id.paymentButton);

            cartItems = new ArrayList<>();
            adapter = new CheckoutItemAdapter(cartItems);
            checkoutItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            checkoutItemsRecyclerView.setAdapter(adapter);

            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();

            // Update initial price details
            updatePriceDetails();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            Toast.makeText(this, "Error initializing checkout", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadCartItems() {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "No user logged in");
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("cart")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                cartItems.clear();
                totalAmount = 0.0;

                for (var document : queryDocumentSnapshots) {
                    CartItem item = document.toObject(CartItem.class);
                    cartItems.add(item);
                    totalAmount += item.getPrice() * item.getQuantity();
                }

                Log.d(TAG, "Loaded " + cartItems.size() + " items");
                adapter.notifyDataSetChanged();
                updatePriceDetails();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading cart items: " + e.getMessage());
                Toast.makeText(this, "Error loading cart items", Toast.LENGTH_SHORT).show();
            });
    }

    private void updatePriceDetails() {
        subtotalText.setText(String.format("$%.2f", totalAmount));
        totalText.setText(String.format("$%.2f", totalAmount));
        paymentButton.setText(String.format("Pay $%.2f", totalAmount));
    }

    private void setupPaymentButton() {
        paymentButton.setOnClickListener(v -> {
            try {
                // Start payment process
                Intent intent = new Intent(this, PaymentActivity.class);
                intent.putExtra("total_amount", totalAmount);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting payment: " + e.getMessage());
                Toast.makeText(this, "Error processing payment", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 