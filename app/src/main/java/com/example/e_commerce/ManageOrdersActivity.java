package com.example.e_commerce;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.e_commerce.adapters.AdminOrderAdapter;
import com.example.e_commerce.models.Order;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ManageOrdersActivity extends AppCompatActivity implements AdminOrderAdapter.OrderActionListener {
    private static final String TAG = "ManageOrdersActivity";
    private RecyclerView ordersRecyclerView;
    private AdminOrderAdapter adapter;
    private List<Order> ordersList;
    private TextView noOrdersText;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "Starting ManageOrdersActivity");
            setContentView(R.layout.activity_manage_orders);

            // Initialize Firebase instances
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Please login to access orders", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            setupToolbar();
            initializeViews();
            loadOrders();
            
            Log.d(TAG, "ManageOrdersActivity initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing orders management", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        try {
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Manage Orders");
                }
                toolbar.setNavigationOnClickListener(v -> onBackPressed());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar: " + e.getMessage());
        }
    }

    private void initializeViews() {
        try {
            ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
            noOrdersText = findViewById(R.id.noOrdersText);

            if (ordersRecyclerView == null || noOrdersText == null) {
                throw new IllegalStateException("Required views not found");
            }

            ordersList = new ArrayList<>();
            adapter = new AdminOrderAdapter(this, ordersList, this);
            ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            ordersRecyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            Toast.makeText(this, "Error setting up the view", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadOrders() {
        try {
            if (db == null) {
                db = FirebaseFirestore.getInstance();
            }

            db.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading orders", error);
                        Toast.makeText(this, "Error loading orders", Toast.LENGTH_SHORT).show();
                        updateUI(true);
                        return;
                    }

                    try {
                        ordersList.clear();
                        if (value != null && !value.isEmpty()) {
                            for (var doc : value) {
                                try {
                                    Order order = doc.toObject(Order.class);
                                    if (order != null) {
                                        order.setOrderId(doc.getId());
                                        ordersList.add(order);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing order: " + e.getMessage());
                                }
                            }
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                            updateUI(ordersList.isEmpty());
                        } else {
                            updateUI(true);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing orders: " + e.getMessage());
                        updateUI(true);
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadOrders: " + e.getMessage());
            Toast.makeText(this, "Error loading orders", Toast.LENGTH_SHORT).show();
            updateUI(true);
        }
    }

    private void updateUI(boolean isEmpty) {
        try {
            if (ordersRecyclerView != null && noOrdersText != null) {
                runOnUiThread(() -> {
                    ordersRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                    noOrdersText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateStatus(Order order, String newStatus) {
        try {
            if (order == null || order.getOrderId() == null) {
                Toast.makeText(this, "Invalid order data", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("orders").document(order.getOrderId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Order status updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating order status", e);
                    Toast.makeText(this, "Failed to update order status", Toast.LENGTH_SHORT).show();
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in onUpdateStatus: " + e.getMessage());
            Toast.makeText(this, "Error updating order status", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewDetails(Order order) {
        try {
            if (order == null) {
                Toast.makeText(this, "Invalid order data", Toast.LENGTH_SHORT).show();
                return;
            }

            // First get the customer details
            db.collection("users").document(order.getUserId())
                .get()
                .addOnSuccessListener(userDoc -> {
                    StringBuilder details = new StringBuilder();
                    details.append("Order Details:\n\n");
                    details.append("Order ID: ").append(order.getOrderId()).append("\n");
                    
                    // Add customer details
                    String customerName = userDoc.getString("name");
                    String customerEmail = userDoc.getString("email");
                    details.append("Customer Name: ").append(customerName != null ? customerName : "N/A").append("\n");
                    details.append("Customer Email: ").append(customerEmail != null ? customerEmail : "N/A").append("\n");
                    
                    details.append("Status: ").append(order.getStatus()).append("\n");
                    details.append("Total Amount: $").append(String.format("%.2f", order.getTotalAmount())).append("\n\n");
                    details.append("Items:\n");
                    
                    if (order.getItems() != null) {
                        for (var item : order.getItems()) {
                            if (item != null) {
                                details.append("- ").append(item.getName() != null ? item.getName() : "N/A")
                                       .append(" (Size: ").append(item.getSize() != null ? item.getSize() : "N/A").append(")")
                                       .append(" x").append(item.getQuantity())
                                       .append(" = $").append(String.format("%.2f", item.getPrice() * item.getQuantity()))
                                       .append("\n");
                            }
                        }
                    }

                    new MaterialAlertDialogBuilder(this)
                        .setTitle("Order Details")
                        .setMessage(details.toString())
                        .setPositiveButton("Close", null)
                        .show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching customer details: " + e.getMessage());
                    Toast.makeText(this, "Error loading customer details", Toast.LENGTH_SHORT).show();
                });

        } catch (Exception e) {
            Log.e(TAG, "Error showing order details: " + e.getMessage());
            Toast.makeText(this, "Error displaying order details", Toast.LENGTH_SHORT).show();
        }
    }
} 