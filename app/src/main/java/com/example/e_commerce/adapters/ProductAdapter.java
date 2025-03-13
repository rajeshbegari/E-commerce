package com.example.e_commerce.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.e_commerce.MainActivity;
import com.example.e_commerce.R;
import com.example.e_commerce.models.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> products;
    private final OnProductClickListener addToCartListener;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener addToCartListener, Context context) {
        this.products = products;
        this.addToCartListener = addToCartListener;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateList(List<Product> newList) {
        products = newList;
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productPrice;
        private final TextView productDescription;
        private final MaterialButton addToCartButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productDescription = itemView.findViewById(R.id.productDescription);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
        }

        void bind(Product product) {
            productName.setText(product.getName());
            productPrice.setText(String.format("$%.2f", product.getPrice()));
            productDescription.setText(product.getDescription());

            Glide.with(itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(productImage);

            itemView.setOnClickListener(v -> showProductDetails(product));
            addToCartButton.setOnClickListener(v -> showProductDetails(product));
        }

        private void showProductDetails(Product product) {
            Context context = itemView.getContext();
            View dialogView = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_product_details, null);

            setupDialogViews(dialogView, product, context);

            new MaterialAlertDialogBuilder(context)
                    .setView(dialogView)
                    .show();
        }

        private void setupDialogViews(View dialogView, Product product, Context context) {
            try {
                ImageView productImage = dialogView.findViewById(R.id.productImage);
                TextView productName = dialogView.findViewById(R.id.productName);
                TextView productPrice = dialogView.findViewById(R.id.productPrice);
                TextView productDescription = dialogView.findViewById(R.id.productDescription);
                AutoCompleteTextView sizeDropdown = dialogView.findViewById(R.id.sizeDropdown);
                TextView quantityText = dialogView.findViewById(R.id.quantityText);
                MaterialButton addToCartButton = dialogView.findViewById(R.id.addToCartButton);

                if (product != null) {
                    // Load product details
                    if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                        Glide.with(context)
                            .load(product.getImageUrl())
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .into(productImage);
                    }

                    productName.setText(product.getName() != null ? product.getName() : "");
                    productPrice.setText(String.format("$%.2f", product.getPrice()));
                    productDescription.setText(product.getDescription() != null ? product.getDescription() : "");

                    // Setup size dropdown
                    String[] sizes = {"S", "M", "L", "XL"};
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            context, R.layout.list_item, sizes);
                    sizeDropdown.setAdapter(adapter);

                    // Initialize quantity text
                    quantityText.setText("1");

                    // Setup quantity controls
                    final int[] quantity = {1};
                    View decreaseBtn = dialogView.findViewById(R.id.decreaseQuantity);
                    View increaseBtn = dialogView.findViewById(R.id.increaseQuantity);

                    if (decreaseBtn != null) {
                        decreaseBtn.setOnClickListener(v -> {
                            if (quantity[0] > 1) {
                                quantity[0]--;
                                quantityText.setText(String.valueOf(quantity[0]));
                            }
                        });
                    }

                    if (increaseBtn != null) {
                        increaseBtn.setOnClickListener(v -> {
                            quantity[0]++;
                            quantityText.setText(String.valueOf(quantity[0]));
                        });
                    }

                    // Setup add to cart button
                    if (addToCartButton != null) {
                        addToCartButton.setOnClickListener(v -> {
                            String selectedSize = sizeDropdown.getText().toString();
                            if (selectedSize.isEmpty()) {
                                sizeDropdown.setError("Please select a size");
                                return;
                            }

                            addToCart(product, selectedSize, quantity[0]);
                            
                            // Safely dismiss dialog
                            View parent = dialogView.getParent() != null ? (View) dialogView.getParent() : null;
                            if (parent != null && parent.getParent() instanceof AlertDialog) {
                                AlertDialog dialog = (AlertDialog) parent.getParent();
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("ProductAdapter", "Error in setupDialogViews: " + e.getMessage());
                Toast.makeText(context, "Error setting up product details", Toast.LENGTH_SHORT).show();
            }
        }

        private void addToCart(Product product, String size, int quantity) {
            try {
                if (auth == null || auth.getCurrentUser() == null) {
                    Toast.makeText(context, "Please login to add items to cart", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (context == null) {
                    Log.e("ProductAdapter", "Context is null");
                    return;
                }

                String userId = auth.getCurrentUser().getUid();
                if (userId == null || userId.isEmpty()) {
                    Toast.makeText(context, "User ID not found. Please login again", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate product data
                if (product == null || product.getId() == null || product.getName() == null) {
                    Toast.makeText(context, "Invalid product data", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> cartItem = new HashMap<>();
                cartItem.put("productId", product.getId());
                cartItem.put("name", product.getName());
                cartItem.put("price", product.getPrice());
                cartItem.put("size", size);
                cartItem.put("quantity", quantity);
                cartItem.put("imageUrl", product.getImageUrl() != null ? product.getImageUrl() : "");

                // Check if Firestore instance is available
                if (db == null) {
                    db = FirebaseFirestore.getInstance();
                }

                db.collection("users").document(userId)
                        .collection("cart")
                        .add(cartItem)
                        .addOnSuccessListener(documentReference -> {
                            if (context != null) {
                                Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();
                                
                                // Safely navigate to Home tab if we're in MainActivity
                                if (context instanceof MainActivity) {
                                    Activity activity = (Activity) context;
                                    if (!activity.isFinishing() && !activity.isDestroyed()) {
                                        ((MainActivity) context).runOnUiThread(() -> {
                                            try {
                                                ((MainActivity) context).navigateToHome();
                                            } catch (Exception e) {
                                                Log.e("ProductAdapter", "Error navigating to home: " + e.getMessage());
                                            }
                                        });
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (context != null) {
                                Toast.makeText(context, "Failed to add to cart. Please try again", 
                                    Toast.LENGTH_SHORT).show();
                                Log.e("ProductAdapter", "Error adding to cart: " + e.getMessage());
                            }
                        });
            } catch (Exception e) {
                Log.e("ProductAdapter", "Error in addToCart: " + e.getMessage());
                if (context != null) {
                    Toast.makeText(context, "Something went wrong. Please try again", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
} 