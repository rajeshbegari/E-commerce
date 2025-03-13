package com.example.e_commerce;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.e_commerce.adapters.OrderAdapter;
import com.example.e_commerce.models.Order;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {
    private RecyclerView ordersRecyclerView;
    private OrderAdapter adapter;
    private List<Order> ordersList;
    private TextView noOrdersText;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        setupToolbar();
        initializeViews();
        loadOrders();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Orders");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeViews() {
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        noOrdersText = findViewById(R.id.noOrdersText);
        ordersList = new ArrayList<>();
        adapter = new OrderAdapter(ordersList);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void loadOrders() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to view orders", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        db.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Toast.makeText(this, "Error loading orders", Toast.LENGTH_SHORT).show();
                    return;
                }

                ordersList.clear();
                if (value != null && !value.isEmpty()) {
                    for (var doc : value) {
                        Order order = doc.toObject(Order.class);
                        order.setOrderId(doc.getId());
                        ordersList.add(order);
                    }
                    adapter.notifyDataSetChanged();
                    updateUI(false);
                } else {
                    updateUI(true);
                }
            });
    }

    private void updateUI(boolean isEmpty) {
        if (isEmpty) {
            ordersRecyclerView.setVisibility(View.GONE);
            noOrdersText.setVisibility(View.VISIBLE);
        } else {
            ordersRecyclerView.setVisibility(View.VISIBLE);
            noOrdersText.setVisibility(View.GONE);
        }
    }
}
