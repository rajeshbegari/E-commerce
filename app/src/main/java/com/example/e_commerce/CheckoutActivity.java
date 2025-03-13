package com.example.e_commerce;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.e_commerce.adapters.CheckoutItemAdapter;
import com.example.e_commerce.models.CartItem;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private String PublishableKey = "pk_test_51QycXpPFig18ZUMzhwJmZJPMw7ONj9nxCxbr4zwbIzGo9psQgLM9CQZVSuLNNupCPB6lCNLg0NRNz5Q0mwQ7Fqtw005Mf77ZUV";
    private String SecretKey = "sk_test_51QycXpPFig18ZUMzerj6NO759gulhbh13B9c4DOrsACNwPzABh4psVJudzEIjrPTn7wURMQufxdFrnMcBoZuLwVy00vY2lQpy3";
    private String CustomersURL = "https://api.stripe.com/v1/customers";
    private String EphericalKeyURL = "https://api.stripe.com/v1/ephemeral_keys";
    private String ClientSecretURL = "https://api.stripe.com/v1/payment_intents";

    private String CustomerId;
    private String EphericalKey;
    private String ClientSecret;
    private PaymentSheet paymentSheet;
    private String Currency = "usd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        Log.d(TAG, "CheckoutActivity started");

        if (getIntent().hasExtra("total_amount")) {
            totalAmount = getIntent().getDoubleExtra("total_amount", 0.0);
            Log.d(TAG, "Received total amount: " + totalAmount);
        }

        initViews();
        setupStripePayment();
        loadCartItems();
    }

    private void setupStripePayment() {
        PaymentConfiguration.init(this, PublishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);
        createCustomer();
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

            // Set up payment button click listener
            paymentButton.setOnClickListener(v -> {
                if (CustomerId != null && !CustomerId.isEmpty()) {
                    paymentFlow();
                } else {
                    Toast.makeText(this, "Preparing payment gateway...", Toast.LENGTH_SHORT).show();
                }
            });

            updatePriceDetails();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            Toast.makeText(this, "Error initializing checkout", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void createCustomer() {
        StringRequest request = new StringRequest(Request.Method.POST, CustomersURL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        CustomerId = object.getString("id");
                        Log.d(TAG, "Customer created: " + CustomerId);
                        getEphericalKey();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e(TAG, "Error creating customer: " + error.getMessage())) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getEphericalKey() {
        StringRequest request = new StringRequest(Request.Method.POST, EphericalKeyURL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        EphericalKey = object.getString("id");
                        Log.d(TAG, "Ephemeral Key created: " + EphericalKey);
                        getClientSecret(CustomerId, EphericalKey);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e(TAG, "Error getting ephemeral key: " + error.getMessage())) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                headers.put("Stripe-Version", "2022-11-15");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getClientSecret(String customerId, String ephemeralKey) {
        // Convert total amount to cents
        long amountInCents = (long) (totalAmount * 100);
        
        StringRequest request = new StringRequest(Request.Method.POST, ClientSecretURL,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        ClientSecret = object.getString("client_secret");
                        Log.d(TAG, "Client Secret created: " + ClientSecret);
                        // Enable payment button once everything is ready
                        paymentButton.setEnabled(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e(TAG, "Error getting client secret: " + error.getMessage())) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerId);
                params.put("amount", String.valueOf(amountInCents));
                params.put("currency", Currency);
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void paymentFlow() {
        if (ClientSecret == null || ClientSecret.isEmpty()) {
            Toast.makeText(this, "Payment processing is not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }

        paymentSheet.presentWithPaymentIntent(
                ClientSecret,
                new PaymentSheet.Configuration(
                        "E-commerce Store",
                        new PaymentSheet.CustomerConfiguration(
                                CustomerId,
                                EphericalKey
                        )
                )
        );
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Success!", Toast.LENGTH_SHORT).show();
            // Handle successful payment (clear cart, save order, etc.)
            handleSuccessfulPayment();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSuccessfulPayment() {
        // Clear the cart and save the order
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            // Save order details
            saveOrder(userId);
            // Clear cart
            clearCart(userId);
        }
    }

    private void saveOrder(String userId) {
        Map<String, Object> order = new HashMap<>();
        order.put("userId", userId);
        order.put("items", cartItems);
        order.put("totalAmount", totalAmount);
        order.put("timestamp", System.currentTimeMillis());
        order.put("status", "PAID");

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                    // Navigate to order confirmation or home
                    navigateToOrders();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Failed to save order", Toast.LENGTH_SHORT).show());
    }

    private void clearCart(String userId) {
        db.collection("users").document(userId)
                .collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                });
    }

    private void navigateToOrders() {
        Intent intent = new Intent(this, OrdersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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
} 