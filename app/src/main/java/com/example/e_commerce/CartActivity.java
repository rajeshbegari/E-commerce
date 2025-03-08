package com.example.e_commerce;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_commerce.adapters.CartAdapter;
import com.example.e_commerce.models.CartItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
//import com.google.android.material.toolbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private RecyclerView cartRecyclerView;
    private TextView totalPriceText;
    private MaterialButton checkoutButton;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private double totalPrice = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        setupToolbar();
        initializeViews();
        setupRecyclerView();
        loadCartItems();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeViews() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalPriceText = findViewById(R.id.totalPriceText);
        checkoutButton = findViewById(R.id.checkoutButton);
        cartItems = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        checkoutButton.setOnClickListener(v -> processCheckout());
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(cartItems, this::updateCartItem, this::removeFromCart);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(adapter);
    }

    private void loadCartItems() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("cart")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading cart", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    cartItems.clear();
                    totalPrice = 0.0;

                    for (QueryDocumentSnapshot doc : value) {
                        CartItem item = new CartItem();
                        item.setId(doc.getId());
                        item.setProductId(doc.getString("productId"));
                        item.setName(doc.getString("name"));
                        item.setPrice(doc.getDouble("price"));
                        item.setSize(doc.getString("size"));
                        item.setQuantity(doc.getLong("quantity").intValue());
                        item.setImageUrl(doc.getString("imageUrl"));

                        cartItems.add(item);
                        totalPrice += item.getPrice() * item.getQuantity();
                    }
                    
                    updateUI();
                });
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();
        totalPriceText.setText(String.format("Total: $%.2f", totalPrice));
        checkoutButton.setVisibility(cartItems.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateCartItem(CartItem item, int newQuantity) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("cart").document(item.getId())
                .update("quantity", newQuantity)
                .addOnSuccessListener(aVoid -> {
                    item.setQuantity(newQuantity);
                    adapter.notifyDataSetChanged();
                    calculateTotal();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show());
    }

    private void removeFromCart(CartItem item) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("cart").document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    cartItems.remove(item);
                    adapter.notifyDataSetChanged();
                    calculateTotal();
                    Toast.makeText(this, "Item removed from cart", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Failed to remove item", Toast.LENGTH_SHORT).show());
    }

    private void calculateTotal() {
        totalPrice = 0.0;
        for (CartItem item : cartItems) {
            totalPrice += item.getPrice() * item.getQuantity();
        }
        totalPriceText.setText(String.format("Total: $%.2f", totalPrice));
        checkoutButton.setVisibility(cartItems.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void processCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Start CheckoutActivity
            Intent intent = new Intent(this, CheckoutActivity.class);
            // Pass the total amount to checkout
            intent.putExtra("total_amount", totalPrice);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error starting checkout: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
